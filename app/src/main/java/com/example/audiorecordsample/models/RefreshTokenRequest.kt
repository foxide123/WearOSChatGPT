package com.example.audiorecordsample.models

data class RefreshTokenRequest (
    val client_id:String,
    val client_secret:String,
    val refresh_token:String,
    val grant_type:String
)