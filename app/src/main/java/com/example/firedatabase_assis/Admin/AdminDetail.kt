package com.example.firedatabase_assis.Admin

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firedatabase_assis.R

class AdminDetail : AppCompatActivity() {

    val items = arrayOf("Menunggu", "Sedang Pengerjaan", "Selesai")

    lateinit var autoCompleteTextView: AutoCompleteTextView

    lateinit var adapterItems: ArrayAdapter<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_detail)

        // Initialize the AutoCompleteTextView
        autoCompleteTextView = findViewById(R.id.autoCompleteText)

        // Initialize the ArrayAdapter with the array of items
        adapterItems = ArrayAdapter<String>(this, R.layout.dropdown_item)

        // Set the adapter to the AutoCompleteTextView
        autoCompleteTextView.setAdapter(adapterItems)

        autoCompleteTextView.setOnItemClickListener { adapterView, view, position, id ->
            // Your code here
            // For example, to get the selected item:
            val selectedItem = adapterView.getItemAtPosition(position) as String
            Toast.makeText(this, "Item: $selectedItem", Toast.LENGTH_SHORT).show()
            // Use the selectedItem as needed
        }


    }
}
