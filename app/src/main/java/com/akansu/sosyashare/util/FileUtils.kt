package com.akansu.sosyashare.util


import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File


object FileUtils {

    fun createFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File(context.cacheDir, "temp_image_file_${System.currentTimeMillis()}.jpg")
            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            Log.e("FileUtils", "Failed to create file from URI: $e")
            null
        }
      }
    }


