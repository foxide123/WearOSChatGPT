package com.example.audiorecordsample.models.speechToText

data class SpeechToTextRequest (
    val config: ConfigAPI,
    val audio: AudioAPI
)