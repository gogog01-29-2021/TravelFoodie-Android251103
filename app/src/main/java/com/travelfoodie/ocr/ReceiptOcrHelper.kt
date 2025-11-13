package com.travelfoodie.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class ReceiptOcrHelper(private val context: Context) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractTextFromImage(imageUri: Uri): String {
        return try {
            val image = InputImage.fromFilePath(context, imageUri)
            val result = recognizer.process(image).await()

            // Extract text from the result
            val extractedText = result.text

            if (extractedText.isBlank()) {
                "텍스트를 인식할 수 없습니다."
            } else {
                extractedText
            }
        } catch (e: Exception) {
            throw Exception("OCR 처리 실패: ${e.message}")
        }
    }

    fun close() {
        recognizer.close()
    }
}
