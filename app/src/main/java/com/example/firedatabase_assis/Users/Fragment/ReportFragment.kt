package com.example.firedatabase_assis.Users.Fragment
import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
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
import com.example.firedatabase_assis.ml.Model
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
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

    private lateinit var labels : List<String>

    private var bitmap: Bitmap? = null

    var imageProcessor = ImageProcessor.Builder()
        .add(NormalizeOp(0.0f, 255.0f))
        .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
        .build()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_report, container, false)
        editTextLocation = view.findViewById(R.id.editTextLocation)
        editTextDescription = view.findViewById(R.id.editTextDescription)
        editTextDate = view.findViewById(R.id.editTextDate)
        buttonSubmit = view.findViewById(R.id.buttonSubmit)
        buttonAddPhoto = view.findViewById(R.id.buttonAddPhoto)
        imageViewPhoto = view.findViewById(R.id.imageViewPhoto)
        firestore = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        labels = requireContext().assets.open("labels.txt").bufferedReader().readLines()

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

    fun generateRandomString(length: Int): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    private fun getUserName(uid: String?) : String? {
        if (uid != null){
            var name: String? = null
            firestore.collection("users")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener { documents ->
                    for (doc in documents) {
                        name = doc.getString("name")
                    }
            }.addOnFailureListener { exception ->
                showAlert("Error", "get failed with  ${exception.message}")
            }
            return name
        }else {
            return null
        }
    }

    // Proses laporan saat tombol submit ditekan
    private fun processReport() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val laporanId = generateRandomString(20)
        val location = editTextLocation.text.toString().trim()
        val description = editTextDescription.text.toString().trim()
        val date = editTextDate.text.toString().trim()
        val tingkatKerusakan = getTingkatKerusakan()
        val status = "Menunggu"

        // Memeriksa apakah ada foto yang dipilih
        if (selectedImageUri == null) {
            showAlert("Error", "Pilih foto terlebih dahulu.")
            return
        }

        var name : String? = ""
        firestore.collection("users")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    name = doc.getString("name")
                }
            }.addOnFailureListener { exception ->
                showAlert("Error", "get failed with  ${exception.message}")
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

                    val report = hashMapOf(
                        "laporanId" to laporanId,
                        "nama_pelapor" to name,
                        "lokasi" to location,
                        "keterangan" to description,
                        "tingkat_kerusakan" to tingkatKerusakan,
                        "tanggal_laporan" to date,
                        "status_penanganan" to status,
                        "userId" to userId,
                        "image_url" to uri.toString()
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
                //Toast.makeText(requireContext(), "Gambar dipilih: $selectedImageUri", Toast.LENGTH_SHORT).show()
                try {
                    selectedImageUri?.let {
                        bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, it)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
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
