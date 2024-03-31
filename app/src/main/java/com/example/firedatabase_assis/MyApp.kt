package com.example.firedatabase_assis

import android.app.Application
import com.google.firebase.FirebaseApp

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        // Any other initialization tasks you may have
    }
}
