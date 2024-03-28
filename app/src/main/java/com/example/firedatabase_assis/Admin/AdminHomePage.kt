package com.example.firedatabase_assis.Admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.firedatabase_assis.Admin.Fragment.AdminDetailFragment
import com.example.firedatabase_assis.Admin.Fragment.AdminHomeFragment
import com.example.firedatabase_assis.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminHomePage : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home_page)

        bottomNavigationView = findViewById(R.id.bottom_navigation_admin)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.button_home -> {
//                    replaceFragment(AdminHomeFragment())
                    true
                }
                R.id.button_report -> {
//                    replaceFragment(AdminDetailFragment())
                    true
                }
                R.id.button_profile -> {
                    // Mengganti dengan fragment yang sesuai untuk profil admin
//                    replaceFragment(ProfileFragment()) // Ganti dengan AdminProfileFragment jika ada
                    true
                }
                else -> false
            }
        }
        // Mengganti fragment awal dengan AdminHomeFragment
        replaceFragment(AdminHomeFragment())
    }

    private fun replaceFragment(fragment: AdminHomeFragment){
//        supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()
    }
}
