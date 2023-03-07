package com.example.audiorecordsample.repository

import com.example.audiorecordsample.api.ChatRetrofit
import com.example.audiorecordsample.api.SpeechToTextRetrofit
import com.example.audiorecordsample.models.ChatRequest
import com.example.audiorecordsample.models.speechToText.SpeechToTextRequest

class Repository {

    suspend fun getCompletion(request:ChatRequest) =
            ChatRetrofit.chatAPI.sendRequest(request)

    suspend fun getTextFromAudio(request:SpeechToTextRequest, token:String) = SpeechToTextRetrofit.sttAPI.sendAudio(request,
        "Bearer $token"
    )
}