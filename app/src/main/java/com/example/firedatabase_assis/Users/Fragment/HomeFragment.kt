package com.example.firedatabase_assis.Users.Fragment

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.firedatabase_assis.R
import com.example.firedatabase_assis.Users.EditLaporan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class HomeFragment : Fragment() {
    private lateinit var inflaterPublic: LayoutInflater
    private lateinit var laporanBody: ViewGroup
    private var laporanId: String? = null

    private lateinit var view: View
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.fragment_home, container, false)
        inflaterPublic = inflater
        laporanBody = view.findViewById(R.id.itemBody)

        getAndSetDataLaporan()
        getAndSetUser()

        return view
    }
    private fun getAndSetUser() {
        auth = FirebaseAuth.getInstance()
        firestore = Firebase.firestore

        // Mendapatkan UID pengguna yang sedang aktif
        val userId = auth.currentUser?.uid

        val usernameTextView = view?.findViewById<TextView>(R.id.usernameTextView)

        if (userId != null) {

            firestore.collection("users")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val name = document.getString("name")

                        // Access TextView directly using the public view variable
                        val usernameTextView = view.findViewById<TextView>(R.id.usernameTextView)
                        usernameTextView.text = name // Set the name to the TextView
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle error jika ada
                    Log.e(ContentValues.TAG, "Error retrieving user data: $exception")
                }
        }


    }
    private fun getAndSetDataLaporan() {
        val db = FirebaseFirestore.getInstance()
        val laporanCollection = db.collection("laporan")

        laporanCollection
            .whereEqualTo("userId", FirebaseAuth.getInstance().currentUser?.uid)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    // Tangani kesalahan
                    Toast.makeText(
                        requireContext(),
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                laporanBody.removeAllViews()

                for (document in snapshots!!) {
                    val imageurl = document.getString("image_url") ?: continue

                    laporanId = document.getString("laporanId")
                    val loc = document.getString("lokasi")
                    val name = document.getString("nama_pelapor")
                    val status = document.getString("status_penanganan")
                    val date = document.getString("tanggal_laporan")

                    val linearLayout =
                        inflaterPublic.inflate(R.layout.layout_item_user, laporanBody, false) as LinearLayout
                    val imageView = linearLayout.findViewById<ImageView>(R.id.imageView)
                    val tvLocation = linearLayout.findViewById<TextView>(R.id.locationTextView)
                    val tvName = linearLayout.findViewById<TextView>(R.id.usernameTextView)
                    val tvStatus = linearLayout.findViewById<TextView>(R.id.badgeStatus)
                    val tvDate = linearLayout.findViewById<TextView>(R.id.dateTextView)

                    linearLayout.setOnClickListener {
                        Intent(requireContext(), EditLaporan::class.java).also {
                            it.putExtra("LAPORAN_ID", laporanId)
                            startActivity(it)
                        }
                    }

                    tvLocation.text = loc
                    tvName.text = name
                    tvStatus.text = status
                    tvDate.text = date

                    Picasso.get()
                        .load(imageurl)
                        .into(imageView)

                    laporanBody.addView(linearLayout)
                }
            }
    }
}
