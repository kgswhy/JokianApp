package com.example.firedatabase_assis

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.firedatabase_assis.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var dbhelp=DB_class(applicationContext)
        var db=dbhelp.writableDatabase
        binding.btnrgs.setOnClickListener {
            var name=binding.ed1.text.toString()
            var email=binding.ed2.text.toString()
            var password=binding.ed3.text.toString()
            if(name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                var data = ContentValues()
                data.put("name", binding.ed1.text.toString())
                data.put("email", binding.ed2.text.toString())
                data.put("pswd", binding.ed3.text.toString())
                var rs:Long = db.insert("user", null, data)
                if(!rs.equals(-1)) {
                    var ad = AlertDialog.Builder(this)
                    ad.setTitle("Message")
                    ad.setMessage("Akun Berhasil Terdaftar")
                    ad.setPositiveButton("Ok", null)
                    ad.show()
                    binding.ed1.text.clear()
                    binding.ed2.text.clear()
                    binding.ed3.text.clear()
                }else{
                    var ad = AlertDialog.Builder(this)
                    ad.setTitle("Message")
                    ad.setMessage("Kolom Tidak Terisi")
                    ad.setPositiveButton("Ok", null)
                    ad.show()
                    binding.ed1.text.clear()
                    binding.ed2.text.clear()
                    binding.ed3.text.clear()
                }
            }else{
                Toast.makeText(this,"Harap mengisi semua kolom",Toast.LENGTH_SHORT).show()
            }
        }
        binding.loginLink.setOnClickListener {
            val intent=Intent(this,login_form::class.java)
            startActivity(intent)
        }
    }
}