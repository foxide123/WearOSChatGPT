package com.example.audiorecordsample.util

import android.media.AudioFormat
import android.media.AudioRecord

class Constants {
    companion object{
        const val GOOGLE_BASE_URL = "https://speech.googleapis.com/"
        const val CHAT_BASE_URL = "https://api.openai.com/"

        //AUDIO
        const val SAMPLE_RATE = 44100
        const val CHANNEL_CONFIG_IN = AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        const val RC_AUTH = 100
    }
}