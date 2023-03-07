package com.example.audiorecordsample

import android.app.Application
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.example.audiorecordsample.BuildConfig.OAUTH_CLIENT_ID
import com.example.audiorecordsample.BuildConfig.OAUTH_CLIENT_SECRET
import com.example.audiorecordsample.models.*
import com.example.audiorecordsample.models.RefreshTokenRequest
import com.example.audiorecordsample.models.RefreshTokenResponse
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.Result


class MainViewModel(application: Application) :AndroidViewModel(application) {

    val status: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    // Dynamic content to show on the Wear OS display
    val result: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    val resultAccessToken = MutableLiveData<String>()

    private fun showStatus(statusString: Int, resultString: String = "") {
        status.postValue(statusString)
        result.postValue(resultString)
    }

    fun authenticationSetup(){
        val refreshToken = getApplication<Application>().getSharedPreferences("my_app", Context.MODE_PRIVATE).getString("refresh_token", null)
        if(refreshToken!= null){
            viewModelScope.launch{
                val accessToken = renewAccessToken(refreshToken).getOrElse{
                    showStatus(R.string.status_failed)
                    return@launch
                }
                val userName = retrieveUserProfile(accessToken).getOrElse {
                    showStatus(R.string.status_failed)
                    return@launch
                }

                showStatus(R.string.status_retrieved, userName)

            }
        }else{
            viewModelScope.launch{
                val verificationInfo = retrieveVerificationInfo().getOrElse{
                    showStatus(R.string.status_failed)
                    return@launch
                }

                // Step 2: Show the pairing code & open the verification URI on the paired device
                showStatus(R.string.status_code, verificationInfo.userCode)
                fireRemoteIntent(verificationInfo.verificationUri)

                // Step 3: Poll the Auth server for the token
                val token = retrieveToken(verificationInfo.deviceCode, verificationInfo.interval)
                // Step 4: Use the token to make an authorized request
                val userName = retrieveUserProfile(token).getOrElse {
                    showStatus(R.string.status_failed)
                    return@launch
                }

                showStatus(R.string.status_retrieved, userName)

            }
        }
    }



    data class VerificationRequest(
        val client_id:String,
        val scope:String
    )

    // The response data when retrieving the verification
    data class VerificationInfo(
        val verificationUri: String,
        val userCode: String,
        val deviceCode: String,
        val interval: Int
    )

     private suspend fun retrieveVerificationInfo(): Result<VerificationInfo> {

     return try{
         withContext(Dispatchers.IO){
             val verificationRequest = VerificationRequest("453045987930-udh7k64ojqh01q276u1jhkaouftlnear.apps.googleusercontent.com", "https://www.googleapis.com/auth/userinfo.profile")
             val client = OkHttpClient()
             val request = Request.Builder()
                 .method(
                     "POST",
                     Gson().toJson(verificationRequest)
                         .toRequestBody("application/json".toMediaType())
                 )
                 .url("https://oauth2.googleapis.com/device/code")
                 .build()

             val response = client.newCall(request).execute()
             val responseInString = response.body?.string()
             print("RETRIEVE_VERIFICATION_INFO RESPONSE: ")
             print(responseInString)


             val gson = Gson()
             val jsonObject = gson.fromJson(responseInString, JsonObject::class.java)

             val verification_url = jsonObject.get("verification_url").asString
             val user_code = jsonObject.get("user_code").asString
             val device_code = jsonObject.get("device_code").asString
             val interval = jsonObject.get("interval").asInt
             val mappedObjectResponse = VerificationInfo(verification_url, user_code , device_code, interval)



             Result.success(
                 mappedObjectResponse
             )
         }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Opens the verification URL on the paired device.
     *
     * When the user has the corresponding app installed on their paired Android device, the Data
     * Layer can be used instead, see https://developer.android.com/training/wearables/data-layer.
     *
     * When the user has the corresponding app installed on their paired iOS device, it should
     * use [Universal Links](https://developer.apple.com/ios/universal-links/) to intercept the
     * intent.
     */
    private fun fireRemoteIntent(verificationUri: String) {
        RemoteActivityHelper(getApplication()).startRemoteActivity(
            Intent(Intent.ACTION_VIEW).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
                data = Uri.parse(verificationUri)
            },
            null
        )
    }

    /**
     * Poll the Auth server for the token. This will only return when the user has finished their
     * authorization flow on the paired device.
     *
     * For this sample the various exceptions aren't handled.
     */
    private tailrec suspend fun retrieveToken(deviceCode: String, interval: Int): String {
        Log.d(TAG, "Polling for token...")

        return fetchToken(deviceCode).getOrElse {
            Log.d(TAG, "No token yet. Waiting...")
            delay(interval * 1000L)
            return retrieveToken(deviceCode, interval)
        }
    }

    private suspend fun fetchToken(deviceCode: String): Result<String> {

        return try{
            withContext(Dispatchers.IO){
                val fetchTokenRequest = FetchTokenRequest(OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, deviceCode, "urn:ietf:params:oauth:grant-type:device_code")
                val client = OkHttpClient()
                val request = Request.Builder()
                    .method(
                        "POST",
                        Gson().toJson(fetchTokenRequest)
                            .toRequestBody("application/json".toMediaType())
                    )
                    .url("https://oauth2.googleapis.com/token")
                    .build()

                val response = client.newCall(request).execute()
                val responseInString = response.body?.string()


                print("FETCH_TOKEN_RESPONSE: ")
                print(responseInString)


                val gson = Gson()
                val jsonObject = gson.fromJson(responseInString, JsonObject::class.java)
                val access_token = jsonObject.get("access_token").asString
                val expires_in = jsonObject.get("expires_in").asInt
                val refresh_token = jsonObject.get("refresh_token").asString
                val scope = jsonObject.get("scope").asString
                val token_type = jsonObject.get("token_type").asString
                val id_token=jsonObject.get("id_token").asString
                val mappedObjectResponse = FetchTokenResponse(access_token, expires_in, refresh_token, scope, token_type, id_token)


                getApplication<Application>().getSharedPreferences("my_app", Context.MODE_PRIVATE)
                    .edit()
                    .putString("refresh_token", mappedObjectResponse.refresh_token).apply()

                Result.success(mappedObjectResponse.access_token)
            }
        }catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun renewAccessToken(refreshToken:String?) : Result<String>{
        val refreshTokenRequest = RefreshTokenRequest(OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, refreshToken!!, "refresh_token")

        return try{
            withContext(Dispatchers.IO) {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .method(
                        "POST",
                        Gson().toJson(refreshTokenRequest)
                            .toRequestBody("application/json".toMediaType())
                    )
                    .url("https://oauth2.googleapis.com/token")
                    .build()

                val response = client.newCall(request).execute()
                val responseInString = response.body?.string()

                val gson = Gson()
                val jsonObject = gson.fromJson(responseInString, JsonObject::class.java)
                val access_token = jsonObject.get("access_token").asString
                val expires_in = jsonObject.get("expires_in").asInt
                val scope = jsonObject.get("scope").asString
                val token_type = jsonObject.get("token_type").asString
                val id_token=jsonObject.get("id_token").asString
                val mappedObjectResponse = RefreshTokenResponse(access_token, expires_in, id_token, scope, token_type)

                if(responseInString !=null){
                    Log.i("RESPONSE OF REFRESH TOKEN", responseInString!!)
                    Log.i("ACCESS_TOKEN",mappedObjectResponse.access_token )
                }
                resultAccessToken.postValue(mappedObjectResponse.access_token)
                Result.success(mappedObjectResponse.access_token)
            }
        }catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }


    }



    /**
     * Using the access token, make an authorized request to the Auth server to retrieve the user's
     * profile.
     *
     *
     */

    data class ProfileRequest(
        val url: String,
        val request_header:String
    )
    private suspend fun retrieveUserProfile(token: String): Result<String> {
        return try{
            withContext(Dispatchers.IO) {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .header("Authorization", "Bearer $token")
                    .url("https://www.googleapis.com/oauth2/v2/userinfo")
                    .build()

                val response = client.newCall(request).execute()
                val responseInString = response.body?.string()

                resultAccessToken.postValue(token)
                Result.success(JSONObject(responseInString).getString("name"))
            }
        }catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}