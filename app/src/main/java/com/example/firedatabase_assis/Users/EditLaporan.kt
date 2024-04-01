package com.example.firedatabase_assis.Users

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.firedatabase_assis.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class EditLaporan : AppCompatActivity() {

    lateinit var submitBtn: Button
    lateinit var db: FirebaseFirestore
    lateinit var laporanId: String
    lateinit var laporanCollection: CollectionReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_laporan)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        laporanId = intent.getStringExtra("LAPORAN_ID")!!
        laporanCollection = db.collection("laporan")

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

        val btnDelete : MaterialButton = findViewById(R.id.deleteBtn)

        btnDelete.setOnClickListener{
            val message = "Are you sure you want to delete this report?"
            showCustomDialog(message)
        }

    }

    private fun showCustomDialog(message: String){
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.layout_custom_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvMessage: TextView = dialog.findViewById(R.id.tvMessage)
        val btnYes: MaterialButton = dialog.findViewById(R.id.btnYes)
        val btnNo: MaterialButton = dialog.findViewById(R.id.btnNo)

        tvMessage.text = message

        btnYes.setOnClickListener{
            laporanCollection
                .whereEqualTo("laporanId", laporanId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        laporanCollection.document(document.id).delete()
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Berhasil Delete Laporan!",
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
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        btnNo.setOnClickListener{
            dialog.dismiss()
        }

        dialog.show()
    }
}