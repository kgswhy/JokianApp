package com.example.firedatabase_assis

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.firedatabase_assis.databinding.ActivityLoginFormBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class login_form : AppCompatActivity() {
    private lateinit var bind : ActivityLoginFormBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind= ActivityLoginFormBinding.inflate(layoutInflater)
        setContentView(bind.root)

        auth = Firebase.auth

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
                            val name = user?.displayName
                            startActivity(Intent(this, HomePage::class.java).putExtra("name", name))
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
            val intent= Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
    }
}
