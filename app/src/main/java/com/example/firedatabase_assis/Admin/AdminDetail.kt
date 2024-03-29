package com.example.firedatabase_assis.Admin

import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firedatabase_assis.R

class AdminDetail : AppCompatActivity() {

    lateinit var submitBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_detail)

        val items = arrayOf("Menunggu", "Sedang Pengerjaan", "Selesai")

        val autoCompleteTextView: AutoCompleteTextView = findViewById(R.id.autoCompleteTextView)

        val adapterItems: ArrayAdapter<String> = ArrayAdapter<String>(this, R.layout.dropdown_item, items)

        autoCompleteTextView.setAdapter(adapterItems)

        submitBtn = findViewById(R.id.submitBtn)

        var selectedItem : String

        autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener {
                adapterView, view, i, l ->

            val selectedItem = adapterView.getItemAtPosition(i) as String
            //Toast.makeText(this, "Item: $selectedItem", Toast.LENGTH_SHORT).show()
        }

        submitBtn.setOnClickListener() {

        }

    }
}
