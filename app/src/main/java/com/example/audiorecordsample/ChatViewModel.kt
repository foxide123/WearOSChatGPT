package com.example.audiorecordsample

import android.app.Application
import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.service.controls.ControlsProviderService
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.audiorecordsample.models.ChatRequest
import com.example.audiorecordsample.models.Message
import com.example.audiorecordsample.models.speechToText.AudioAPI
import com.example.audiorecordsample.models.speechToText.ConfigAPI
import com.example.audiorecordsample.models.speechToText.SpeechToTextRequest
import com.example.audiorecordsample.repository.Repository
import com.example.audiorecordsample.util.Constants
import com.example.audiorecordsample.util.Constants.Companion.RC_AUTH
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.openid.appauth.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class ChatViewModel(application: Application, val repository: Repository) :AndroidViewModel(application) {

    protected var audioRecord: AudioRecord? = null
    private var mAuthService: AuthorizationService? = AuthorizationService(getApplication())
    private var mStateManager: AuthStateManager? =  AuthStateManager.getInstance(getApplication())
    val BUFFER_SIZE_RECORDING =
        AudioRecord.getMinBufferSize(
            Constants.SAMPLE_RATE,
            Constants.CHANNEL_CONFIG_IN,
            Constants.AUDIO_FORMAT
        )
    private lateinit var fileAudio: File
    var fileNameAudio: String? = null
    private var recordingThread: Thread? = null
    var fileInBase64: String?=null
    // val fileInBase64 = MutableLiveData<String>()
    val jsonBody: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val isRecording: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    suspend fun doInBackground(token:String){
        val requestConfig = SpeechToTextRequest(ConfigAPI("en-US","LINEAR16",44100), AudioAPI(fileInBase64!!))
        val speechToTextResponse = repository.getTextFromAudio(requestConfig, token).execute();
        if(speechToTextResponse.isSuccessful){
           jsonBody.postValue(speechToTextResponse.body()?.results?.get(0)?.alternatives?.get(0)?.transcript)
        }
        else{
            Exception("problem with http call")
        }

    }

    suspend fun sendChatRequest(request: String){
        Log.i("VALUE_OF_EDITVIEW", request)
        val requestConfig = ChatRequest(listOf(Message(request, "user")), "gpt-3.5-turbo", 200)

        try{
            val chatResponse = repository.getCompletion(requestConfig).execute();
            jsonBody.postValue(chatResponse!!.body()?.choices?.get(0)?.message?.content?.trimStart())
        }catch(exception:Exception){
            Log.w("LOG_TAG", exception)
        }
    }

    fun initializeRecording(){

        fileNameAudio = "${getApplication<Application>().externalCacheDir?.absolutePath}/audiorecordtest.wav"
        fileAudio = File(fileNameAudio)
        if (!fileAudio.exists()) { // create empty files if needed
            try {
                fileAudio.createNewFile()
            } catch (e: IOException) {
                Log.d(ControlsProviderService.TAG, "could not create file " + e.toString())
                e.printStackTrace()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startRecording() {
        if (audioRecord == null) { // safety check
            if (ActivityCompat.checkSelfPermission(
                    getApplication(),
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(getApplication(), arrayOf(Manifest.permission.RECORD_AUDIO), 200)
            }
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                Constants.SAMPLE_RATE,
                Constants.CHANNEL_CONFIG_IN,
                Constants.AUDIO_FORMAT,
                BUFFER_SIZE_RECORDING
            )
            if (audioRecord!!.state != AudioRecord.STATE_INITIALIZED) { // check for proper initialization
                Log.e(ControlsProviderService.TAG, "error initializing AudioRecord")
                return
            }
            audioRecord!!.startRecording()
            Log.d(ControlsProviderService.TAG, "recording started with AudioRecord")
            isRecording.postValue(true)
            recordingThread = Thread { writeAudioDataToFile() }
            recordingThread!!.start()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun stopRecording() {
        if (audioRecord != null) {
            isRecording.postValue(false) // triggers recordingThread to exit while loop
        }
        // downloadFile(fileAudio)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun writeAudioDataToFile() { // called inside Runnable of recordingThread
        val data =
            ByteArray(BUFFER_SIZE_RECORDING / 2) // assign size so that bytes are read in in chunks inferior to AudioRecord internal buffer size
        var outputStream: FileOutputStream? = null
        try {
            outputStream = FileOutputStream(fileNameAudio)
        } catch (e: FileNotFoundException) {
            // handle error
            Log.e(ControlsProviderService.TAG, "file not found for file name " + fileNameAudio + ", " + e.toString())
            return
        }
        while (isRecording.value!!) {
            val read = audioRecord!!.read(data, 0, data.size)
            try {
                outputStream.write(data, 0, read)
            } catch (e: IOException) {
                Log.d(ControlsProviderService.TAG, "IOException while recording with AudioRecord, $e")
                e.printStackTrace()
            }
        }
        try { // clean up file writing operations
            Log.d("Base64 byte array", Base64.getEncoder().encodeToString(data))
            fileInBase64 =  Base64.getEncoder().encodeToString(data)
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            Log.e(ControlsProviderService.TAG, "exception while closing output stream $e")
            e.printStackTrace()
        }
        audioRecord!!.stop()
        audioRecord!!.release()
        audioRecord = null
        recordingThread = null


        val bytes = fileAudio.readBytes()
        val base64String = Base64.getEncoder().encodeToString(bytes)
        Log.d("base64String", base64String)
        println("base64 println"+base64String)
        fileInBase64 = base64String
    }
}