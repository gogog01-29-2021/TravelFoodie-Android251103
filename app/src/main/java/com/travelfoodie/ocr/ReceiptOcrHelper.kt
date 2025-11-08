package com.travelfoodie.ocr

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class ReceiptOcrHelper(private val context: Context) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun scanReceipt(imageUri: Uri): ReceiptData {
        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        return scanReceipt(bitmap)
    }

    suspend fun scanReceipt(bitmap: Bitmap): ReceiptData {
        val image = InputImage.fromBitmap(bitmap, 0)
        val result = recognizer.process(image).await()

        val fullText = result.text
        val merchantName = extractMerchantName(fullText)
        val total = extractTotal(fullText)

        return ReceiptData(
            merchantName = merchantName,
            total = total,
            fullOcrText = fullText
        )
    }

    private fun extractMerchantName(text: String): String {
        // Try to find merchant name in first few lines
        val lines = text.split("\n").take(5)
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.length > 3 && !trimmed.matches(Regex(".*\\d{4}.*"))) {
                // Likely a merchant name if it doesn't contain year-like numbers
                return trimmed
            }
        }
        return lines.firstOrNull()?.trim() ?: "알 수 없음"
    }

    private fun extractTotal(text: String): Double {
        // Look for common total patterns in Korean receipts
        val patterns = listOf(
            Regex("합계[:\\s]*([\\d,]+)"),
            Regex("총액[:\\s]*([\\d,]+)"),
            Regex("결제금액[:\\s]*([\\d,]+)"),
            Regex("total[:\\s]*([\\d,]+)", RegexOption.IGNORE_CASE),
            Regex("([\\d,]+)원")
        )

        for (pattern in patterns) {
            val match = pattern.findAll(text).lastOrNull()
            if (match != null) {
                val numberStr = match.groupValues[1].replace(",", "")
                try {
                    return numberStr.toDouble()
                } catch (e: NumberFormatException) {
                    continue
                }
            }
        }

        // Fallback: find largest number in text
        val allNumbers = Regex("([\\d,]+)").findAll(text)
            .map { it.groupValues[1].replace(",", "") }
            .mapNotNull { it.toDoubleOrNull() }
            .filter { it > 100 } // Filter out small numbers

        return allNumbers.maxOrNull() ?: 0.0
    }

    fun close() {
        recognizer.close()
    }

    data class ReceiptData(
        val merchantName: String,
        val total: Double,
        val fullOcrText: String
    )
}
