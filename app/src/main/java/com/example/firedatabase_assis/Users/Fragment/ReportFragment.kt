package com.example.firedatabase_assis.Users.Fragment

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.firedatabase_assis.R
import com.example.firedatabase_assis.ml.Model
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.util.Calendar
import java.util.Locale

class ReportFragment : Fragment() {
    private lateinit var editTextName: EditText
    private lateinit var editTextLocation: EditText
    private lateinit var buttonAddPhoto : Button
    private lateinit var imageViewPhoto: ImageView // tambahan ImageView untuk menampilkan foto yang dipilih
    private lateinit var editTextDescription: EditText
    private lateinit var editTextDate: EditText
    private lateinit var buttonSubmit: Button
    private lateinit var firestore: FirebaseFirestore
    private lateinit var progressDialog: ProgressDialog

    private var bitmap: Bitmap? = null // Bitmap untuk menyimpan foto yang dipilih

    private val calendar = Calendar.getInstance()

    private lateinit var labels : List<String>

    var imageProcessor = ImageProcessor.Builder()
        .add(NormalizeOp(0.0f, 255.0f))
        .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
        .build()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_report, container, false)
        editTextName = view.findViewById(R.id.editTextName)
        editTextLocation = view.findViewById(R.id.editTextLocation)
        buttonAddPhoto = view.findViewById(R.id.buttonAddPhoto)
        imageViewPhoto = view.findViewById(R.id.imageViewPhoto) // inisialisasi ImageView untuk menampilkan foto
        editTextDescription = view.findViewById(R.id.editTextDescription)
        editTextDate = view.findViewById(R.id.editTextDate)
        buttonSubmit = view.findViewById(R.id.buttonSubmit)
        firestore = FirebaseFirestore.getInstance()

        labels = requireContext().assets.open("labels.txt").bufferedReader().readLines()

        editTextDate.setOnClickListener {
            showDatePicker()
        }

        buttonAddPhoto.setOnClickListener {
            // Memilih foto dari galeri menggunakan Intent
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }

        buttonSubmit.setOnClickListener {
            // Memproses laporan saat tombol submit ditekan
            processReport()
        }

        return view
    }

    private fun showDatePicker() {
        // Menampilkan date picker untuk memilih tanggal
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

    private fun getTingkatKerusakan() : String {
        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)

        tensorImage = imageProcessor.process(tensorImage)

        val model = Model.newInstance(requireContext())

        // Creates inputs for reference.
        val inputFeature0 =
            TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(tensorImage.buffer)

        // Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

        var maxIdx = 0
        outputFeature0.forEachIndexed { index, fl ->
            if(outputFeature0[maxIdx] < fl){
                maxIdx = index
            }
        }

        val result = labels[maxIdx].toString()

        model.close()
        return result
    }

    private fun updateDateEditText() {
        // Memperbarui EditText untuk menampilkan tanggal yang dipilih
        val dateFormat = "dd/MM/yyyy"
        val sdf = java.text.SimpleDateFormat(dateFormat, Locale.getDefault())
        editTextDate.setText(sdf.format(calendar.time))
    }

    private fun processReport() {
        // Memeriksa apakah semua field telah diisi
        val name = editTextName.text.toString().trim()
        val location = editTextLocation.text.toString().trim()
        val description = editTextDescription.text.toString().trim()
        val date = editTextDate.text.toString().trim()

        if (name.isEmpty() || location.isEmpty() || description.isEmpty() || date.isEmpty()) {
            // Menampilkan pesan kesalahan jika ada field yang kosong
            showAlert("Error", "Harap mengisi semua kolom.")
            return
        }

        // Menyimpan laporan ke Firestore
        saveReport(name, location, description, date)
    }

    private fun saveReport(name: String, location: String, description: String, date: String) {
        // Menyiapkan data laporan
        val status = "Menunggu" // Anda dapat mengambil status dari spinner jika diperlukan
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val tingkatKerusakan = getTingkatKerusakan()

        val report = hashMapOf(
            "nama_pelapor" to name,
            "lokasi" to location,
            "keterangan" to description,
            "tanggal_laporan" to date,
            "status_penanganan" to status,
            "userId" to userId // Menambahkan ID pengguna yang melaporkan laporan
        )

        progressDialog = ProgressDialog.show(requireContext(), "", "Menyimpan...", true)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // Mendapatkan foto yang dipilih dari galeri
            val selectedImageUri = data?.data
            imageViewPhoto.setImageURI(selectedImageUri) // Menampilkan foto di ImageView

            // Mengonversi URI gambar menjadi bitmap
            try {
                selectedImageUri?.let {
                    bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, it)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun showAlert(title: String, message: String) {
        // Menampilkan dialog dengan pesan tertentu
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1

        fun newInstance(): ReportFragment {
            return ReportFragment()
            }
        }
}