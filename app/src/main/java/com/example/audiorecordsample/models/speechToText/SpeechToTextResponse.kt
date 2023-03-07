package com.example.audiorecordsample.models.speechToText

data class SpeechToTextResponse(
    val results: List<Result>,
    val totalBilledTime: String,
    val requestId: String
)