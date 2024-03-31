package com.example.firedatabase_assis.Users

import com.example.firedatabase_assis.Users.Fragment.ProfileFragment
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.firedatabase_assis.Users.Fragment.HomeFragment
import com.example.firedatabase_assis.R
import com.example.firedatabase_assis.Users.Fragment.ReportFragment
import com.example.firedatabase_assis.service.MessagingHandlerService
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging

class HomePage : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private val messagingHandlerService = MessagingHandlerService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.button_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.button_report -> {
                    replaceFragment(ReportFragment())
                    true
                }
                R.id.button_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
        replaceFragment(HomeFragment())
        sendUpdateToken()
    }

    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()
    }

    private fun sendUpdateToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("Token", "FCM token failed to retrieve", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            messagingHandlerService.updateFCMToken(token)
            Log.w("Token", "FCM token retrieve success", task.exception)

        })
    }
}
