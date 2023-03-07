package com.example.audiorecordsample.models.speechToText

data class Result(
    val alternatives: List<Alternative>,
    val languageCode: String,
    val resultEndTime: String
)