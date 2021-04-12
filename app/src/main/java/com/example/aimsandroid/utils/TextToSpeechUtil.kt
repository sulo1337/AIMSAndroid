package com.example.aimsandroid.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.*


class TextToSpeechUtil(context: Context): TextToSpeech.OnInitListener {
    private var textToSpeech = TextToSpeech(context, this, "com.google.android.tts")

    override fun onInit(status: Int) {
        if(status!=TextToSpeech.ERROR){
            textToSpeech.setLanguage(Locale.US)
        }
    }

    fun speakText(textToSpeak: String, queueMode: Int){
        textToSpeech.speak(textToSpeak, queueMode, null, null)
    }
}