package com.example.firedatabase_assis.Users.Fragment

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.firedatabase_assis.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ReportFragment : Fragment() {
    private lateinit var editTextName: EditText
    private lateinit var editTextLocation: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var editTextDate: EditText
    private lateinit var buttonSubmit: Button
    private lateinit var firestore: FirebaseFirestore
    private lateinit var progressDialog: ProgressDialog

    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_report, container, false)
        editTextName = view.findViewById(R.id.editTextName)
        editTextLocation = view.findViewById(R.id.editTextLocation)
        editTextDescription = view.findViewById(R.id.editTextDescription)
        editTextDate = view.findViewById(R.id.editTextDate)
        buttonSubmit = view.findViewById(R.id.buttonSubmit)
        firestore = FirebaseFirestore.getInstance()

        // Set OnClickListener untuk editTextDate
        editTextDate.setOnClickListener {
            showDatePicker()
        }

        buttonSubmit.setOnClickListener {
            val name = editTextName.text.toString().trim()
            val location = editTextLocation.text.toString().trim()
            val description = editTextDescription.text.toString().trim()
            val date = editTextDate.text.toString().trim()
            val status = "Menunggu" // Anda dapat mengambil status dari spinner jika diperlukan

            // Mendapatkan UID pengguna yang sedang aktif
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            // Membuat objek HashMap untuk menyimpan data laporan
            val report = hashMapOf(
                "nama_pelapor" to name,
                "lokasi" to location,
                "keterangan" to description,
                "tanggal_laporan" to date,
                "status_penanganan" to status,
                "userId" to userId // Menambahkan ID pengguna yang melaporkan laporan
            )

            // Menampilkan ProgressDialog saat data sedang disimpan
            progressDialog = ProgressDialog.show(requireContext(), "", "Menyimpan...", true)

            // Menyimpan data laporan ke Firestore
            firestore.collection("laporan")
                .add(report)
                .addOnCompleteListener(requireActivity(), OnCompleteListener {
                    if (it.isSuccessful) {
                        // Data berhasil disimpan
                        // Tampilkan pesan sukses
                        progressDialog.dismiss()
                        showAlert("Sukses", "Data berhasil disimpan.")
                    } else {
                        // Gagal menyimpan data
                        // Tampilkan pesan error
                        progressDialog.dismiss()
                        showAlert("Error", "Gagal menyimpan data: ${it.exception?.message}")
                    }
                })
        }

        return view
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateEditText()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis() // Batasi tanggal maksimal menjadi hari ini
        datePickerDialog.show()
    }

    private fun updateDateEditText() {
        val dateFormat = "dd/MM/yyyy"
        val sdf = java.text.SimpleDateFormat(dateFormat, Locale.getDefault())
        editTextDate.setText(sdf.format(calendar.time))
    }

    private fun showAlert(title: String, message: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}
