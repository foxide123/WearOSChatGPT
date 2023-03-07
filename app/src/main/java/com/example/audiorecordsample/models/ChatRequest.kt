package com.example.audiorecordsample.models

data class ChatRequest(
    val messages: List<Message>,
    val model: String,
    val max_tokens: Int,
)