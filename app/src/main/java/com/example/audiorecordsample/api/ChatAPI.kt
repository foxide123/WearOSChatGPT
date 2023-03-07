package com.example.audiorecordsample.api

import com.example.audiorecordsample.BuildConfig
import com.example.audiorecordsample.models.ChatRequest
import com.example.audiorecordsample.models.ChatResponse
import retrofit2.Call
import retrofit2.http.*

interface ChatAPI {
    @Headers("Content-Type: application/json",
        "Authorization: Bearer "+ BuildConfig.CHAT_API_KEY
    )
    @POST("v1/chat/completions")
    fun sendRequest(@Body chatRequest: ChatRequest): Call<ChatResponse>
}