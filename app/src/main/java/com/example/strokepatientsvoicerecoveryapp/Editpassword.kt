package com.example.strokepatientsvoicerecoveryapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.strokepatientsvoicerecoveryapp.databinding.EditPasswordBinding



class Editpassword : AppCompatActivity() {

    private lateinit var binding: EditPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //當按下儲存修改就會跳轉到前一個畫面
        binding.saveEdit.setOnClickListener {
            val intent = Intent(this@Editpassword,PerformanceActivity::class.java);
            startActivity(intent)
        }
    }
}