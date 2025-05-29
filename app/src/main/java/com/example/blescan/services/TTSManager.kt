package com.example.blescan.services

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

const val TTS = "TTS"

object TTSManager : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null

    fun initialize(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TTS, "Language is not supported or missing data")
            }
        } else {
            Log.e(TTS, "Initialization failed")
        }
    }

    fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
