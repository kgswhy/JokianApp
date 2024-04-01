package com.example.firedatabase_assis.Admin

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.firedatabase_assis.R
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class AdminHomePage : AppCompatActivity() {

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val linearLayout: LinearLayout = findViewById(R.id.itemBody)
            linearLayout.removeAllViews()
            reloadData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home_page)
        reloadData()
    }

    private fun reloadData() {
        val db = FirebaseFirestore.getInstance()
        val laporanCollection = db.collection("laporan")

        laporanCollection.get().addOnSuccessListener { documents ->
            for (document in documents) {
                val imageurl = document.getString("image_url") ?: continue

                val laporanId = document.getString("laporanId")
                val loc = document.getString("lokasi")
                val name = document.getString("nama_pelapor")
                val status = document.getString("status_penanganan")
                val date = document.getString("tanggal_laporan")

                val laporanBody = findViewById<LinearLayout>(R.id.itemBody)
                val linearLayout = layoutInflater.inflate(R.layout.layout_item_admin, laporanBody, false) as LinearLayout
                val imageView = linearLayout.findViewById<ImageView>(R.id.imageview)
                val tvLocation = linearLayout.findViewById<TextView>(R.id.locationTextView)
                val tvName = linearLayout.findViewById<TextView>(R.id.usernameTextView)
                val tvStatus = linearLayout.findViewById<TextView>(R.id.badgeStatus)
                val tvDate = linearLayout.findViewById<TextView>(R.id.dateTextView)

                tvLocation.text = loc
                tvName.text = name
                tvStatus.text = status
                tvDate.text = date

                linearLayout.setOnClickListener {
                    Intent(this, AdminDetail::class.java).apply {
                        putExtra("LAPORAN_ID", laporanId)
                        startForResult.launch(this)
                    }
                }

                Picasso.get()
                    .load(imageurl)
                    .into(imageView)

                laporanBody.addView(linearLayout)
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
