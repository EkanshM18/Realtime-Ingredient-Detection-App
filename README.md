# Blind Bite: Food Ingredients Identification and Assistance App

**Blind Bite** is a mobile application designed to assist visually impaired users by identifying food ingredients through real-time image capture and providing results via audio output. The app supports the identification of vegetables and spices, making cooking and grocery shopping more accessible and convenient.

---

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [System Architecture](#system-architecture)
- [Technologies Used](#technologies-used)
- [Model Details](#model-details)
- [Installation](#installation)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

---

## Overview
Blind Bite leverages computer vision to identify food ingredients and offers auditory feedback to ensure seamless interaction for visually impaired users. The app integrates a lightweight image classification model optimized for mobile devices, ensuring efficiency and real-time performance.

---

## Features
- **Real-Time Image Capture**: Capture images directly within the app.
- **Ingredient Identification**: Recognizes a variety of vegetables and spices using an advanced classification model.
- **Audio Feedback**: Provides text-to-speech output for the identified ingredient.
- **User-Friendly Interface**: Simple and accessible design tailored for visually impaired users.

---

## System Architecture
The system comprises the following components:
1. **Image Preprocessing**: Enhances images for accurate recognition.
2. **Model Inference**: EfficientNet_Lite2 model predicts the food ingredient.
3. **Text-to-Speech Conversion**: Converts recognized text into audible feedback.
4. **Mobile Application**: A Kotlin-based app integrates all components seamlessly.

---

## Technologies Used
- **Mobile Development**: Kotlin
- **Model Development**: TensorFlow, EfficientNet_Lite2
- **Model Optimization**: TensorFlow Lite (TFLite)
- **Dataset**: Kaggle's Vegetable Image Dataset + custom dataset for spices
- **Tools**: Android Studio, TensorFlow Model Maker

---

## Model Details
- **Architecture**: EfficientNet_Lite2
- **Training Details**:
  - Epochs: 5
  - Accuracy: 93.87% (train), 94.16% (test)
  - Loss: 0.9032 (train), 0.8865 (test)
- **Quantization**: Optimized for mobile deployment using TensorFlow Lite.

---

## Installation

1. Clone the repository:
   ```bash
   https://github.com/iishaan30/Realtime-Ingredient-Detection-App
   ```
2. Open the project in Android Studio.
3. Build and run the app on an Android device or emulator.
4. Ensure your device camera and microphone permissions are enabled.

---

## Usage
1. Launch the app on your Android device.
2. Point the camera at a food ingredient and capture the image.
3. Wait for the app to process the image and listen to the audio output for the identified ingredient.

---

## Contributing
We welcome contributions! To contribute:
1. Fork the repository.
2. Create a new branch for your feature or bug fix.
3. Commit your changes and push to your fork.
4. Submit a pull request.

---

## Acknowledgments
- Kaggle for the **Vegetable Image Dataset**.
- TensorFlow for providing tools to build and optimize the model.

For more details, visit the [GitHub Repository](https://github.com/iishaan30/Realtime-Ingredient-Detection-App).
