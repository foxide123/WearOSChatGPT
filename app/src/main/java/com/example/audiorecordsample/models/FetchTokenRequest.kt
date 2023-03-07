package com.example.audiorecordsample.models

data class FetchTokenRequest (
    val client_id:String,
    val client_secret: String,
    val device_code: String,
    val grant_type: String
)