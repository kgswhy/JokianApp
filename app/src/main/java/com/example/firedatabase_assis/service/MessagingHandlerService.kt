package com.example.firedatabase_assis.service

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


class MessagingHandlerService {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun updateFCMToken(token: String) {
        // Get the current user
        val userId = auth.currentUser?.uid

        // Update the FCM token
        db.collection("users")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val userRef = db.collection("users").document(document.id)
                    userRef.update("fcmToken", token)
                        .addOnSuccessListener {
                            Log.d("Update user", "Update token")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Update user", "Failed to update token: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Update user", "Failed to query user document: ${e.message}")
            }

    }


    fun sendNotification(accessToken: String?, token: String?, title: String, body: String) {
        Log.d("kepanggil", "gasih")
        val url = "https://fcm.googleapis.com/v1/projects/panturaapp/messages:send"
        val serverKey = accessToken

        Log.d("Serverkey", serverKey.toString())

        val json = JSONObject()
        val message = JSONObject()
        val notification = JSONObject()

        notification.put("title", title)
        notification.put("body", body)

        message.put("token", token)
        message.put("notification", notification)

        json.put("message", message)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $serverKey")
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Failed to send notification: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.d("Send notif", "gagal kirim ${response.code} notifikasi ke ${token}")

                } else {
                    Log.d("Send notif", "berhasil kirim ${response.code} notifikasi ke ${token}")
                }
            }
        })
    }

    fun generateAccessToken(context: Context): String {
        val inputStream = context.assets.open("service-account.json")
        val credentials = GoogleCredentials.fromStream(inputStream)
            .createScoped(mutableListOf<String>("https://www.googleapis.com/auth/firebase.messaging"))
        credentials.refresh()
        val token = credentials.getAccessToken().tokenValue
        Log.d("Token", token.toString())

        return token.toString();
    }
}
