package com.example.firedatabase_assis

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.firedatabase_assis.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

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
        database = FirebaseDatabase.getInstance()

        // Mendapatkan referensi ke node "users" di Firebase Realtime Database
        val userId = auth.currentUser?.uid
        val userRef = database.reference.child("users").child(userId ?: "")

        // Mendapatkan data username dari Firebase Realtime Database
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val username = snapshot.child("username").value.toString()
                    binding.usernameTextView.text = username
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error jika ada
            }
        })

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
