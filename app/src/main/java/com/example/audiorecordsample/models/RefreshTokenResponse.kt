package com.example.audiorecordsample.models

data class RefreshTokenResponse(
    val access_token: String,
    val expires_in: Int,
    val id_token: String,
    val scope: String,
    val token_type: String
)