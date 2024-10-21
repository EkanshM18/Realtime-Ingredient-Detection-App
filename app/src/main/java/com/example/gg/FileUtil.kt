package com.example.gg

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

object FileUtil {
    fun loadLabels(context: Context, filename: String): List<String> {
        val labels = mutableListOf<String>()
        val inputStream = context.assets.open(filename)
        val reader = BufferedReader(InputStreamReader(inputStream))

        reader.use { br ->
            var line: String?
            while (br.readLine().also { line = it } != null) {
                labels.add(line!!)
            }
        }

        return labels
    }
}
