package com.example.data.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class TtsManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    private var currentSpeed: Float = 1.0f

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TtsManager", "Language US is not supported or missing data")
                _isReady.value = false
            } else {
                tts?.setSpeechRate(currentSpeed)
                _isReady.value = true
            }
        } else {
            Log.e("TtsManager", "TextToSpeech Initialization Failed!")
            _isReady.value = false
        }
    }

    fun setSpeechRate(rate: Float) {
        currentSpeed = rate
        tts?.setSpeechRate(rate)
    }

    fun speak(text: String) {
        if (_isReady.value && text.isNotBlank()) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ENGLISH_MASTERY_TTS_${System.currentTimeMillis()}")
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
