package com.example.firedatabase_assis.Users

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.firedatabase_assis.R
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class EditLaporan : AppCompatActivity() {

    lateinit var submitBtn: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_laporan)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val db = FirebaseFirestore.getInstance()
        val laporanId = intent.getStringExtra("LAPORAN_ID")
        val laporanCollection = db.collection("laporan")

        val imageView = findViewById<ImageView>(R.id.imageView)
        val tvLocation = findViewById<TextView>(R.id.locationET)
        val tvName = findViewById<TextView>(R.id.usernameTextView)
        val tvDesc = findViewById<TextView>(R.id.descET)
        val tvDamage = findViewById<TextView>(R.id.kerusakanTextView)


        laporanCollection
            .whereEqualTo("laporanId", laporanId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val imageurl = document.getString("image_url") ?: continue
                    val loc = document.getString("lokasi")
                    val name = document.getString("nama_pelapor")
                    val desc = document.getString("keterangan")
                    val tingkatKerusakan = document.getString("tingkat_kerusakan")

                    tvDesc.text = desc
                    tvDamage.text = tingkatKerusakan
                    tvLocation.text = loc
                    tvName.text = name

                    Picasso.get()
                        .load(imageurl)
                        .into(imageView)
                }
            }.addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

        submitBtn = findViewById(R.id.submitBtn)

        submitBtn.setOnClickListener() {

            val description = tvDesc.text.toString().trim()
            val location = tvLocation.text.toString().trim()

            laporanCollection
                .whereEqualTo("laporanId", laporanId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        laporanCollection.document(document.id)
                            .update(mapOf(
                                "keterangan" to description,
                                "lokasi" to location
                            ))
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Berhasil Update Laporan!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Error: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}