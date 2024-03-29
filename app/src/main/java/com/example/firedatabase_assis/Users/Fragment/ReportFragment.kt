package com.example.firedatabase_assis.Users.Fragment
import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.firedatabase_assis.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReportFragment : Fragment() {
    private lateinit var editTextName: EditText
    private lateinit var editTextLocation: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var editTextDate: EditText
    private lateinit var buttonSubmit: Button
    private lateinit var buttonAddPhoto: Button
    private lateinit var firestore: FirebaseFirestore
    private lateinit var progressDialog: ProgressDialog
    private lateinit var storageReference: StorageReference
    private lateinit var imageViewPhoto: ImageView

    private val calendar = Calendar.getInstance()
    private var selectedImageUri: Uri? = null

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
        buttonAddPhoto = view.findViewById(R.id.buttonAddPhoto)
        imageViewPhoto = view.findViewById(R.id.imageViewPhoto)
        firestore = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        // Set OnClickListener untuk editTextDate
        editTextDate.setOnClickListener {
            showDatePicker()
        }

        // Set OnClickListener untuk buttonAddPhoto
        buttonAddPhoto.setOnClickListener {
            // Memulai intent untuk memilih foto dari galeri
            selectImage()
        }

        buttonSubmit.setOnClickListener {
            processReport()
        }

        return view
    }

    // Menampilkan date picker untuk memilih tanggal
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

    // Memperbarui EditText untuk menampilkan tanggal yang dipilih
    private fun updateDateEditText() {
        val dateFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
        editTextDate.setText(sdf.format(calendar.time))
    }

    // Proses laporan saat tombol submit ditekan
    private fun processReport() {
        val name = editTextName.text.toString().trim()
        val location = editTextLocation.text.toString().trim()
        val description = editTextDescription.text.toString().trim()
        val date = editTextDate.text.toString().trim()
        val status = "Menunggu" // Anda dapat mengambil status dari spinner jika diperlukan

        // Mendapatkan UID pengguna yang sedang aktif
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Memeriksa apakah ada foto yang dipilih
        if (selectedImageUri == null) {
            showAlert("Error", "Pilih foto terlebih dahulu.")
            return
        }

        // Menampilkan ProgressDialog saat data sedang disimpan
        progressDialog = ProgressDialog.show(requireContext(), "", "Menyimpan...", true)

        // Menyimpan foto ke Firebase Storage
        val storagePath = "images/${System.currentTimeMillis()}_${selectedImageUri!!.lastPathSegment}"
        val imageRef = storageReference.child(storagePath)
        imageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                // Foto berhasil diunggah, mendapatkan URL gambar
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    // Membuat objek HashMap untuk menyimpan data laporan
                    val report = hashMapOf(
                        "laporanId" to userId,
                        "nama_pelapor" to name,
                        "lokasi" to location,
                        "keterangan" to description,
                        "tanggal_laporan" to date,
                        "status_penanganan" to status,
                        "userId" to userId, // Menambahkan ID pengguna yang melaporkan laporan
                        "image_url" to uri.toString() // Menambahkan URL gambar ke dalam data laporan
                    )

                    // Menyimpan data laporan ke Firestore
                    firestore.collection("laporan")
                        .add(report)
                        .addOnCompleteListener(requireActivity(), OnCompleteListener {
                            if (it.isSuccessful) {
                                // Data berhasil disimpan
                                progressDialog.dismiss()
                                showAlert("Sukses", "Data berhasil disimpan.")
                            } else {
                                // Gagal menyimpan data
                                progressDialog.dismiss()
                                showAlert("Error", "Gagal menyimpan data: ${it.exception?.message}")
                            }
                        })
                }
            }
            .addOnFailureListener { exception ->
                // Gagal mengunggah foto
                progressDialog.dismiss()
                showAlert("Error", "Gagal mengunggah foto: ${exception.message}")
            }
    }

    // Menampilkan dialog dengan pesan tertentu
    private fun showAlert(title: String, message: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    // Membuka galeri untuk memilih foto
    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    // Handle hasil pemilihan gambar dari galeri
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            data?.data?.let { uri ->
                selectedImageUri = uri
                // Tampilkan gambar yang dipilih ke dalam ImageView
                showImagePreview(uri)
                // Tampilkan pesan bahwa gambar telah dipilih
                Toast.makeText(requireContext(), "Gambar dipilih: $selectedImageUri", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fungsi untuk menampilkan preview gambar ke dalam ImageView
    private fun showImagePreview(uri: Uri) {
        Glide.with(requireContext())
            .load(uri)
            .into(imageViewPhoto)
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }
}
