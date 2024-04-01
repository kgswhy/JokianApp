package com.example.firedatabase_assis.Admin

import android.app.Activity
import android.content.Intent
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.firedatabase_assis.R
import com.example.firedatabase_assis.Users.HomePage
import com.example.firedatabase_assis.service.MessagingHandlerService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminDetail : AppCompatActivity() {

    lateinit var submitBtn: Button
    private val messagingHandlerService = MessagingHandlerService()
    private var accessToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_detail)

        // get access token
        CoroutineScope(Dispatchers.Main).launch {
            // Switch to IO dispatcher for network operation
            accessToken = withContext(Dispatchers.IO) {
                messagingHandlerService.generateAccessToken(this@AdminDetail)
            }

        }

        val db = FirebaseFirestore.getInstance()
        val laporanId = intent.getStringExtra("LAPORAN_ID")
        val laporanCollection = db.collection("laporan")
        val usersCollection = db.collection("users")


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

                        val userId = document.getString("userId") // Retrieve userId from the document
                        if (userId != null) {
                            usersCollection
                                .whereEqualTo("userId", userId)
                                .get()
                                .addOnSuccessListener { querySnapshot ->
                                    if (!querySnapshot.isEmpty) {
                                        val document = querySnapshot.documents[0]
                                        val fcmToken = document.getString("fcmToken");
                                        messagingHandlerService.sendNotification(accessToken, fcmToken, "Update progress", "Status laporan kamu berubah menjadi ${selectedItem}")

                                    } else {
                                        // Jika dokumen tidak ditemukan, tampilkan pesan kesalahan
                                        AlertDialog.Builder(this)
                                            .setTitle("Error")
                                            .setMessage("Gagal kirim notifikasi ke user.")
                                            .setPositiveButton("OK", null)
                                            .show()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    // Handle failure to retrieve user document
                                    AlertDialog.Builder(this)
                                        .setTitle("Error")
                                        .setMessage("Gagal kirim notifikasi kepada user.")
                                        .setPositiveButton("OK", null)
                                        .show()
                                }
                        } else {
                            // Handle case where userId is null in the document
                        }


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
