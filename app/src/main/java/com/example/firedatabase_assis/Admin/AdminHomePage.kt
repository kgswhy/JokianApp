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
    }
}
