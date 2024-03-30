package com.example.firedatabase_assis.Users.Fragment

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.firedatabase_assis.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = Firebase.firestore

        // Mendapatkan UID pengguna yang sedang aktif
        val userId = auth.currentUser?.uid

        if (userId != null) {
            // Mendapatkan data pengguna dari Cloud Firestore berdasarkan userId
            firestore.collection("users")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val username = document.getString("name")
                        val email = document.getString("email")

                        // Menampilkan data pengguna ke UI
                        binding.usernameTextView.text = "Username: $username"
                        binding.emailTextView.text = "Email: $email"

                        // Log jika data berhasil diambil
                        Log.d(TAG, "Data berhasil diambil: Username: $username, Email: $email")
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle error jika ada
                    // Misalnya, mencetak pesan kesalahan untuk debugging
                    Log.e(TAG, "Error retrieving user data: $exception")
                }
        }

        // Menambahkan listener untuk logout saat tombol logout ditekan
        binding.logoutButton.setOnClickListener {
            auth.signOut()
            // Kembali ke halaman login
            activity?.finish() // Tutup activity saat ini
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
