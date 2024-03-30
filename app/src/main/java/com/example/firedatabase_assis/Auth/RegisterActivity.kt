package com.example.firedatabase_assis.Auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.firedatabase_assis.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = Firebase.firestore

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
                                // Simpan informasi pengguna beserta UID ke dalam Firestore
                                val user = hashMapOf(
                                    "userId" to userId,
                                    "name" to name,
                                    "email" to email,
                                    "password" to password,
                                    "roleUser" to "0"
                                )

                                firestore.collection("users")
                                    .add(user)
                                    .addOnSuccessListener { documentReference ->
                                        // Tampilkan pesan berhasil jika pendaftaran berhasil
                                        Toast.makeText(
                                            this,
                                            "Akun Berhasil Terdaftar",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        binding.ed1.text.clear()
                                        binding.ed2.text.clear()
                                        binding.ed3.text.clear()
                                    }
                                    .addOnFailureListener { e ->
                                        // Tampilkan pesan error jika pendaftaran gagal
                                        Toast.makeText(
                                            this,
                                            "Gagal Mendaftar: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        } else {
                            // Tampilkan pesan error jika pendaftaran gagal
                            Toast.makeText(
                                this,
                                "Gagal Mendaftar: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                binding.loginLink.setOnClickListener {
                    val intent = Intent(this, LoginForm::class.java)
                    startActivity(intent)
                }
            }
        }
    }
}