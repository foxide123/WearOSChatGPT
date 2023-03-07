package com.example.audiorecordsample

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.service.controls.ControlsProviderService
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.audiorecordsample.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.*

class ChatActivity: AppCompatActivity(), TextToSpeech.OnInitListener {



    var recordAudioRecord: ImageButton? = null
    var sendAudioRecord: Button? = null
    var textView: TextView? = null

    var fileNameAudio: String? = null

    private lateinit var fileAudio: File


    lateinit var viewModel: ChatViewModel
    private var tts: TextToSpeech? = null
    private var speak: Boolean = false;

    private lateinit var accessToken:String


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val repository = Repository()
        val viewModelProviderFactory =
            ChatViewModelProvider(application, repository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory).get(ChatViewModel::class.java)


        val extras = intent.extras
        if (extras != null) {
            accessToken = extras.getString("access_token")!!
        }


        recordAudioRecord = findViewById(R.id.record_audiorecord);
        sendAudioRecord = findViewById(R.id.send_audiotrack);
        textView = findViewById<EditText>(R.id.textView)
        tts = TextToSpeech(this, this)


        if (ActivityCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) { // get permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 200)
        }

        viewModel.isRecording.postValue(false)

        setListeners()


    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language not supported!")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setListeners() {
        viewModel.jsonBody.observe(this) { newName ->
            // Update the UI, in this case, a TextView.
            textView?.text = newName
            if(speak) {
                speakOut()
                speak=false
            }
        }

        recordAudioRecord!!.setOnClickListener {
            if (viewModel.isRecording.value == false) {


                viewModel.initializeRecording()
                viewModel.startRecording()
            } else {
                if(accessToken == null){
                    Log.d(ControlsProviderService.TAG, "ACCESS_TOKEN_IS_NULL")
                }else{
                    viewModel.stopRecording()
                    lifecycleScope.launch(Dispatchers.IO) {
                        viewModel.doInBackground(accessToken)
                    }
                }
            }
        }

        sendAudioRecord!!.setOnClickListener{
            try{
                val request = textView?.text.toString()
                textView?.text = null
                lifecycleScope.launch(Dispatchers.IO) {
                    speak = true
                    viewModel.sendChatRequest(request)
                }
                speakOut()
            }catch(exception: IOException){
                Log.d(TAG, "problem sending request")
            }
        }


/*
        playAudioTrack!!.setOnClickListener{
            if (!isPlayingAudio) {
                startPlaying();
            } else {
                stopPlaying();
            }
        }
 */
    }

    private fun speakOut() {
        val text = textView!!.text.toString()
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null,"")
    }



    public override fun onDestroy() {
        // Shutdown TTS when
        // activity is destroyed
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }











/*
    @RequiresApi(Build.VERSION_CODES.O)
    private fun downloadFile(file: File) {
        // Get the Download directory on external storage
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        // Create the file in the downloads directory
        val outputFile = File(downloadsDir, file.name)
        // Copy the file to the external storage
        val inputStream = FileInputStream(file)
        val outputStream = FileOutputStream(outputFile)
        inputStream.copyTo(outputStream)
        val file = File(fileNameAudio)
        val bytes = fileAudio.readBytes()
        val base64String = Base64.getEncoder().encodeToString(bytes)
        Log.d("base64StringIn downloadFile method", base64String)
        // Notify the MediaScanner about the new file
        MediaScannerConnection.scanFile(
            applicationContext,
            arrayOf(file.absolutePath),
            null,
            null
        )
        // Show a toast message to confirm that the download is complete
        Toast.makeText(
            applicationContext,
            "File downloaded to ${file.absolutePath}",
            Toast.LENGTH_SHORT
        ).show()
    }
 */




}