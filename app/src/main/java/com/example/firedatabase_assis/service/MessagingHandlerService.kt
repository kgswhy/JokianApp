package com.example.firedatabase_assis.service

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
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


    fun sendNotification(token: String?, title: String, body: String) {
        Log.d("kepanggil", "gasih")
        val url = "https://fcm.googleapis.com/v1/projects/panturaapp/messages:send"
        val serverKey = "ya29.c.c0AY_VpZjDBBR7RDbK-EbKCuqTx9lyP27N8y9_sQYZf11SXqXTawscKHzPE35rcKfW8d8LNYwbOrhFK_j020_CcqebGLtZuIsAo71xBk4zzprMFCgGIP7K7h7a5TypC7xegmybfzZia5eRqJlregbQgDrVNrXqMxpkGoixoZYd6YT5aB8zRZvqR5kgHh3mTQA7O9wLsSKQ1WMAliIrECs7OMJ1eMIM4dnYOIupniOfD9PjUfyzwJd7a4Vrw8M1MN8BSzTPi1bjxtm2szonK7PKdcG-wVYESGH-i48PADxa8R_4s_9Bqr7thBmr9FbIhAQtVJIuM9yjV7Gu6NAG1xVi2H5_DdQVVSSTZWSypq1PP7RitmmKeNEmMwPXT385KOJUatm6Bi5n580IF1r-wgvgcUe7h5cy0c9WeIs39-nZSfoStv_zsZ723nbXUf6z065V9enmVghUoIFdX7My12F0Wnj3JQWZhOB3-rUJ_F5-SQFWJ5MugoIerthOXMVFoXhvs1Wn7ppoqBkIukOY5_0syOr-bXBq993BXVr4k2kInZ5lInqztZIv88Vd8ulhps34rfvfpJj2cuZcr3JdjdlVY3dqnYWYsOatbje_8krghgcj_wZk3zc__2xVortMf5_q7-VaQx8lZa62WJaogI_YoQ9M69ihur5bVjvJc_35UVpO4t5Vt42Stpd6h9sUibMtUiBJveuM2fJzJd8bJcn_r5iY34R0xg-t82mm2W_s1gyqYUxQvopYoB4osw-24eah198OWMSI4XnB5cQ4esrRqurFIvzhQ3sqvrFRiZt4ly7zmuYXYhWOUhqr0FMh7vkJ9UMo0V0Iqq6umrWyOce4pMWF-86mFRyRhy7q8232FR92n0tIMsrejwirXk1hQotJZwmb_SofhXWFmvVMsligv-I_ssYxhbmlXIbqIQ8Onp-fI9mgv122d40w572jahZ-YZhMr5ipzjSb824vpijvkXeas-5r8lrphstqVet99BlvmcgZJgx-_mo"

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

}
