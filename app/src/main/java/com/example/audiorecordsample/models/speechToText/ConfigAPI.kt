package com.example.audiorecordsample.models.speechToText

data class ConfigAPI (
    val languageCode: String,
    val encoding: String,
    val sampleRateHertz: Int
)