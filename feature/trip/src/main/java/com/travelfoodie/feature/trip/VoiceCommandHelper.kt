package com.travelfoodie.feature.trip

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.*

class VoiceCommandHelper(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit
) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    init {
        initializeSpeechRecognizer()
        initializeTextToSpeech()
    }

    private fun initializeSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("음성 인식을 사용할 수 없습니다")
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "오디오 에러"
                    SpeechRecognizer.ERROR_CLIENT -> "클라이언트 에러"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "권한이 필요합니다"
                    SpeechRecognizer.ERROR_NETWORK -> "네트워크 에러"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트워크 타임아웃"
                    SpeechRecognizer.ERROR_NO_MATCH -> "인식된 내용이 없습니다"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "음성 인식이 바쁩니다"
                    SpeechRecognizer.ERROR_SERVER -> "서버 에러"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "음성 입력 시간 초과"
                    else -> "알 수 없는 에러"
                }
                onError(errorMessage)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.isNotEmpty()) {
                    onResult(matches[0])
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.KOREAN)
                isTtsInitialized = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED
            }
        }
    }

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "말씀하세요")
        }
        speechRecognizer?.startListening(intent)
    }

    fun speak(text: String) {
        if (isTtsInitialized) {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun destroy() {
        speechRecognizer?.destroy()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }

    companion object {
        // Parse voice commands for trip editing
        fun parseVoiceCommand(command: String): VoiceCommand? {
            val lowerCommand = command.lowercase(Locale.KOREAN)

            // Date change pattern: "3월 15일로 변경", "날짜 3월 20일"
            val datePattern = Regex("(\\d+)월\\s*(\\d+)일")
            val dateMatch = datePattern.find(lowerCommand)
            if (dateMatch != null && (lowerCommand.contains("변경") || lowerCommand.contains("날짜"))) {
                val month = dateMatch.groupValues[1].toInt()
                val day = dateMatch.groupValues[2].toInt()
                return VoiceCommand.ChangeDate(month, day)
            }

            // Region add pattern: "서울 추가", "부산 추가해줘"
            val regionPattern = Regex("([가-힣]+)\\s*(추가|넣|더해)")
            val regionMatch = regionPattern.find(lowerCommand)
            if (regionMatch != null) {
                val region = regionMatch.groupValues[1]
                return VoiceCommand.AddRegion(region)
            }

            // Member add pattern: "팀원 추가 철수", "멤버 영희"
            val memberPattern = Regex("(팀원|멤버)\\s*(추가|넣)?\\s*[:：]?\\s*([가-힣]+)")
            val memberMatch = memberPattern.find(lowerCommand)
            if (memberMatch != null) {
                val member = memberMatch.groupValues[3]
                return VoiceCommand.AddMember(member)
            }

            return null
        }
    }

    sealed class VoiceCommand {
        data class ChangeDate(val month: Int, val day: Int) : VoiceCommand()
        data class AddRegion(val region: String) : VoiceCommand()
        data class AddMember(val member: String) : VoiceCommand()
    }
}
