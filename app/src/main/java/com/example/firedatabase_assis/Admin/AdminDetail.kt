package com.example.firedatabase_assis.Admin

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firedatabase_assis.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class AdminDetail : AppCompatActivity() {

    lateinit var submitBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_detail)

        val db = FirebaseFirestore.getInstance()
        val laporanId = intent.getStringExtra("LAPORAN_ID")
        val laporanCollection = db.collection("laporan")

        val imageView = findViewById<ImageView>(R.id.imageView)
        val tvLocation = findViewById<TextView>(R.id.locationTextView)
        val tvName = findViewById<TextView>(R.id.usernameTextView)
        val tvDesc = findViewById<TextView>(R.id.descTextView)
        val tvDamage = findViewById<TextView>(R.id.kerusakanTextView)

        var statusPenanganan: String = ""

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
                    statusPenanganan = document.getString("status_penanganan")!!

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

        val items = arrayOf("Menunggu", "Sedang Pengerjaan", "Selesai")

        val autoCompleteTextView: AutoCompleteTextView = findViewById(R.id.autoCompleteTextView)

        val adapterItems: ArrayAdapter<String> = ArrayAdapter<String>(this, R.layout.dropdown_item, items)

        autoCompleteTextView.setAdapter(adapterItems)

        submitBtn = findViewById(R.id.submitBtn)

        var selectedItem : String = statusPenanganan

        autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener {
                adapterView, view, i, l ->

            selectedItem = adapterView.getItemAtPosition(i) as String
            //Toast.makeText(this, "Item: $selectedItem", Toast.LENGTH_SHORT).show()
        }

        submitBtn.setOnClickListener() {

            laporanCollection
                .whereEqualTo("laporanId", laporanId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        laporanCollection.document(document.id)
                            .update("status_penanganan", selectedItem)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Berhasil Update Status Laporan!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                setResult(Activity.RESULT_OK) // Set the result
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
