package com.example.audiorecordsample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.*
import android.os.Build
import android.os.Bundle
import android.service.controls.ControlsProviderService
import android.service.controls.ControlsProviderService.TAG
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.audiorecordsample.repository.Repository
import com.example.audiorecordsample.util.Constants
import com.example.audiorecordsample.util.Constants.Companion.AUDIO_FORMAT
import com.example.audiorecordsample.util.Constants.Companion.CHANNEL_CONFIG_IN
import com.example.audiorecordsample.util.Constants.Companion.SAMPLE_RATE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.openid.appauth.*
import java.io.*
import java.util.*


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val viewModel by viewModels<MainViewModel>()

        // Start the OAuth flow when the user presses the button
        findViewById<View>(R.id.authenticateButton).setOnClickListener {
            viewModel.authenticationSetup()
        }

        // Show current status on the screen
        viewModel.status.observe(this) { statusText ->
            findViewById<TextView>(R.id.status_text_view).text = resources.getText(statusText)
        }

        // Show dynamic content on the screen
        viewModel.result.observe(this) { resultText ->
            findViewById<TextView>(R.id.result_text_view).text = resultText
        }

        viewModel.resultAccessToken.observe(this){token->
            val intent = Intent(this@MainActivity,ChatActivity::class.java);
            Log.d("TOKEN", token)
            intent.putExtra("access_token", token)
            startActivity(intent);
        }
    }
}