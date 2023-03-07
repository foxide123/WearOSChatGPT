package com.example.audiorecordsample

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.audiorecordsample.repository.Repository

class ChatViewModelProvider(
    private val app: Application,
    private val repository: Repository
)
    : ViewModelProvider.AndroidViewModelFactory(app) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(app, repository) as T
    }
}
