package com.example.audiorecordsample.api

import com.example.audiorecordsample.util.Constants.Companion.CHAT_BASE_URL
import com.example.audiorecordsample.util.Constants.Companion.GOOGLE_BASE_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ChatRetrofit {

        companion object{

            private val retrofit by lazy{
                val logging = HttpLoggingInterceptor()
                logging.setLevel(HttpLoggingInterceptor.Level.BODY)
                val client = OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build()
                Retrofit.Builder()
                    .baseUrl(CHAT_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
            }

            //api object
            val chatAPI by lazy{
                retrofit.create(ChatAPI::class.java)
            }
        }
}