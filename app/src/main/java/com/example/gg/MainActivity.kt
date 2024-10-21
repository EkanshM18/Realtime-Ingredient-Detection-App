package com.example.gg

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
// Update this to the correct import for your final model
import com.example.gg.ml.Ingredientmodel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.util.*

data class Category(val label: String, val score: Float)

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var labels: List<String>
    private val paint = Paint()
    private lateinit var imageView: ImageView
    private lateinit var cameraDevice: CameraDevice
    private lateinit var handler: Handler
    private lateinit var cameraManager: CameraManager
    private lateinit var textureView: TextureView

    // TTS object
    private lateinit var tts: TextToSpeech
    private var isTtsInitialized: Boolean = false
    private var lastSpokenLabel: String? = null
    private var lastSpeakTime: Long = 0
    private val speakCooldown = 2000
    private var frameCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the content view to the XML layout file
        setContentView(R.layout.activity_main)

        get_permission()

        // Initialize TextToSpeech
        tts = TextToSpeech(this, this)

        labels = FileUtil.loadLabels(this, "labels.txt") // Update the label file accordingly

        // Initialize other components
        val handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        imageView = findViewById(R.id.imageView)
        textureView = findViewById(R.id.textureView)

        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                open_camera()
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {}

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                val bitmap = textureView.bitmap
                if (bitmap != null) {
                    frameCount++

                    // Process every second frame to reduce lag
                    if (frameCount % 2 == 0) {
                        processFrame(bitmap)
                    }
                }
            }
        }

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isTtsInitialized) {
            tts.stop()
            tts.shutdown()
        }
        handler.removeCallbacksAndMessages(null)
    }

    @SuppressLint("MissingPermission")
    private fun open_camera() {
        val cameraIdList = cameraManager.cameraIdList
        if (cameraIdList.isNotEmpty()) {
            cameraManager.openCamera(cameraIdList[0], object : CameraDevice.StateCallback() {
                override fun onOpened(cameraDevice: CameraDevice) {
                    this@MainActivity.cameraDevice = cameraDevice
                    val surfaceTexture = textureView.surfaceTexture
                    val surface = Surface(surfaceTexture)

                    val captureRequest =
                        cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                    captureRequest.addTarget(surface)

                    cameraDevice.createCaptureSession(
                        listOf(surface),
                        object : CameraCaptureSession.StateCallback() {
                            override fun onConfigured(session: CameraCaptureSession) {
                                session.setRepeatingRequest(captureRequest.build(), null, handler)
                            }

                            override fun onConfigureFailed(session: CameraCaptureSession) {
                                Log.e("CameraError", "Camera configuration failed")
                            }
                        },
                        handler
                    )
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    Log.e("CameraError", "Camera error: $error")
                }
            }, handler)
        } else {
            Log.e("CameraError", "No cameras available")
        }
    }

    private fun get_permission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 101)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
            get_permission()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
            isTtsInitialized = true
        }
    }

    private fun speakDetectedObject(label: String) {
        val message = "Detected $label"
        if (isTtsInitialized) {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    private fun processFrame(bitmap: Bitmap) {
        // Resize the bitmap to [260, 260] to match the model's input size
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 260, 260, true)

        // Load the image as a TensorImage
        val image = TensorImage.fromBitmap(resizedBitmap)

        // Load the new model (ingredientmodel.tflite)
        val model = Ingredientmodel.newInstance(this)

        // Run model inference and get the results
        val outputs = model.process(image)

        // Retrieve the scores, assuming it's a list of 20 categories
        val scoreList = outputs.scoreAsCategoryList

        // Create a mutable bitmap for drawing
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        // Set up the paint object for drawing text
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        paint.textSize = mutableBitmap.width / 30f

        // Loop over the categories (assuming 20 categories now)
        for (category in scoreList) {
            if (category.score > 0.5) {
                // Draw the label for categories with a confidence score greater than 0.5
                canvas.drawText(category.label, 10f, (scoreList.indexOf(category) + 1) * 50f, paint)

                // Speak the detected object if the cooldown has passed
                if (lastSpokenLabel != category.label || System.currentTimeMillis() - lastSpeakTime > speakCooldown) {
                    speakDetectedObject(category.label)
                    lastSpokenLabel = category.label
                    lastSpeakTime = System.currentTimeMillis()
                }
            }
        }

        // Set the modified bitmap to the ImageView
        imageView.setImageBitmap(mutableBitmap)

        // Close the model instance to free resources
        model.close()
    }

}
