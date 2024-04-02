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
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.firedatabase_assis.R
import com.example.firedatabase_assis.ml.Model
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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

    private lateinit var labels: List<String>
    private lateinit var model: Model

    private var bitmap: Bitmap? = null

    private val imageProcessor = ImageProcessor.Builder()
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
        firestore = Firebase.firestore
        storageReference = FirebaseStorage.getInstance().reference
        model = Model.newInstance(requireContext())

        labels = requireContext().assets.open("labels.txt").bufferedReader().readLines()

        editTextDate.setOnClickListener {
            showDatePicker()
        }

        buttonAddPhoto.setOnClickListener {
            selectImage()
        }

        buttonSubmit.setOnClickListener {
            processReport()
        }

        return view
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateEditText()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun updateDateEditText() {
        val dateFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
        editTextDate.setText(sdf.format(calendar.time))
    }

    private fun processReport() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val laporanId = generateRandomString(20)
        val location = editTextLocation.text.toString().trim()
        val description = editTextDescription.text.toString().trim()
        val date = editTextDate.text.toString().trim()
        val tingkatKerusakan = getTingkatKerusakan()
        val status = "Menunggu"

        if (selectedImageUri == null) {
            showAlert("Error", "Pilih foto terlebih dahulu.")
            return
        }

        val name = getUserName(userId)

        progressDialog = ProgressDialog.show(requireContext(), "", "Menyimpan...", true)

        val storagePath = "images/${System.currentTimeMillis()}_${selectedImageUri!!.lastPathSegment}"
        val imageRef = storageReference.child(storagePath)
        imageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener { taskSnapshot ->
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

                    firestore.collection("laporan")
                        .add(report)
                        .addOnCompleteListener(requireActivity(), OnCompleteListener {
                            progressDialog.dismiss()
                            if (it.isSuccessful) {
                                showAlert("Sukses", "Data berhasil disimpan.")
                            } else {
                                showAlert("Error", "Gagal menyimpan data: ${it.exception?.message}")
                            }
                        })
                }
            }
            .addOnFailureListener { exception ->
                progressDialog.dismiss()
                showAlert("Error", "Gagal mengunggah foto: ${exception.message}")
            }
    }

    private fun getTingkatKerusakan(): String {
        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        val processedImage = imageProcessor.process(tensorImage)

        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(processedImage.buffer)

        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

        var maxIdx = 0
        outputFeature0.forEachIndexed { index, fl ->
            if (outputFeature0[maxIdx] < fl) {
                maxIdx = index
            }
        }

        return labels[maxIdx]
    }

    private fun getUserName(uid: String?): String {
        var name = ""
        uid?.let {
            firestore.collection("users")
                .whereEqualTo("userId", it)
                .get()
                .addOnSuccessListener { documents ->
                    for (doc in documents) {
                        name = doc.getString("name") ?: ""
                    }
                }
                .addOnFailureListener { exception ->
                    showAlert("Error", "get failed with  ${exception.message}")
                }
        }
        return name
    }

    private fun generateRandomString(length: Int): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

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

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            data?.data?.let { uri ->
                selectedImageUri = uri
                showImagePreview(uri)
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun showImagePreview(uri: Uri) {
        Glide.with(requireContext())
            .load(uri)
            .into(imageViewPhoto)
    }

    override fun onDestroy() {
        super.onDestroy()
        model.close()
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }
}
