package com.example.firedatabase_assis.Auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.firedatabase_assis.Admin.AdminHomePage
//import com.example.firedatabase_assis.Admin.Fragment.AdminHomePage
import com.example.firedatabase_assis.Auth.RegisterActivity
import com.example.firedatabase_assis.Users.HomePage
import com.example.firedatabase_assis.databinding.ActivityLoginFormBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginForm : AppCompatActivity() {
    private lateinit var bind: ActivityLoginFormBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityLoginFormBinding.inflate(layoutInflater)
        setContentView(bind.root)

        auth = Firebase.auth
        firestore = Firebase.firestore

        bind.btnlogin.setOnClickListener {
            val email = bind.logtxt.text.toString().trim()
            val password = bind.ed3.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                // Tampilkan pesan kesalahan jika kolom email atau password kosong
                AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Kolom email dan password harus diisi.")
                    .setPositiveButton("OK", null)
                    .show()
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            val user = auth.currentUser
                            val uid = user?.uid

                            // Periksa roleUser dari Firestore
                            firestore.collection("users").document(uid!!)
                                .get()
                                .addOnSuccessListener { document ->
                                    if (document != null) {
                                        val role = document.getString("roleUser")
                                        if (role == "1") {
                                            startActivity(Intent(this, AdminHomePage::class.java))
                                        } else {
                                            // Jika roleUser adalah user biasa, arahkan ke halaman user
                                            startActivity(Intent(this, HomePage::class.java))
                                        }
                                        finish() // Menutup activity saat ini setelah berhasil login
                                    } else {
                                        // Jika dokumen tidak ada, tampilkan pesan kesalahan
                                        AlertDialog.Builder(this)
                                            .setTitle("Error")
                                            .setMessage("Data pengguna tidak ditemukan.")
                                            .setPositiveButton("OK", null)
                                            .show()
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    // Handle error jika terjadi kegagalan saat mengambil data dari Firestore
                                    AlertDialog.Builder(this)
                                        .setTitle("Error")
                                        .setMessage("Gagal memeriksa roleUser: ${exception.message}")
                                        .setPositiveButton("OK", null)
                                        .show()
                                }
                        } else {
                            // If sign in fails, display a message to the user.
                            AlertDialog.Builder(this)
                                .setTitle("Error")
                                .setMessage("Username atau Password Salah")
                                .setPositiveButton("OK", null)
                                .show()
                        }
                    }
            }
        }

        bind.regisLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
