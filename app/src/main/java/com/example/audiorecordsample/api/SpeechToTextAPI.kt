package com.example.audiorecordsample.api

import com.example.audiorecordsample.models.speechToText.SpeechToTextRequest
import com.example.audiorecordsample.models.speechToText.SpeechToTextResponse
import com.example.audiorecordsample.util.Constants
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface SpeechToTextAPI {
    @Headers("Content-Type: application/json",
        "Scope: https://www.googleapis.com/auth/cloud-platform"
    )
    @POST("v1/speech:recognize/")
    fun sendAudio(@Body speechToTextRequest: SpeechToTextRequest, @Header("Authorization") token:String ): Call<SpeechToTextResponse>
}