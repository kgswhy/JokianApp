package com.example.firedatabase_assis

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.firedatabase_assis.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.btnrgs.setOnClickListener {
            val name = binding.ed1.text.toString().trim()
            val email = binding.ed2.text.toString().trim()
            val password = binding.ed3.text.toString().trim()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                // Mendaftarkan pengguna menggunakan Firebase Authentication
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                // Simpan informasi pengguna ke dalam Firebase Realtime Database
                                val userRef = database.reference.child("users").child(userId)
                                val userData = mapOf(
                                    "userId" to userId,
                                    "username" to name,
                                    "email" to email,
                                    "password" to password
                                )
                                userRef.setValue(userData)

                                // Tampilkan pesan berhasil jika pendaftaran berhasil
                                Toast.makeText(this, "Akun Berhasil Terdaftar", Toast.LENGTH_SHORT).show()
                                binding.ed1.text.clear()
                                binding.ed2.text.clear()
                                binding.ed3.text.clear()
                            }
                        } else {
                            // Tampilkan pesan error jika pendaftaran gagal
                            Toast.makeText(this, "Gagal Mendaftar: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                // Tampilkan pesan jika ada kolom yang kosong
                Toast.makeText(this, "Harap mengisi semua kolom", Toast.LENGTH_SHORT).show()
            }
        }

        binding.loginLink.setOnClickListener {
            val intent = Intent(this, login_form::class.java)
            startActivity(intent)
        }
    }
}
