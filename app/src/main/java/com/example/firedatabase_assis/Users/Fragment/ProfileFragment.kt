package com.example.firedatabase_assis.Users.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.firedatabase_assis.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
        firestore = FirebaseFirestore.getInstance()

        // Mendapatkan UID pengguna yang sedang aktif
        val userId = auth.currentUser?.uid

        if (userId != null) {
            // Mendapatkan data pengguna dari Cloud Firestore
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val username = document.getString("username")
                        val email = document.getString("email")

                        // Menampilkan data pengguna ke UI
                        binding.usernameTextView.text = "Username: $username"
                        binding.emailTextView.text = "Email: $email"
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle error jika ada
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
