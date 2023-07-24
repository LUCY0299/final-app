package com.example.strokepatientsvoicerecoveryapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.strokepatientsvoicerecoveryapp.databinding.FeedbackBinding

class Feedback : AppCompatActivity() {
    private lateinit var binding: FeedbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //當按下一頁鍵就會跳轉到MainoptionActivity畫面
        binding.btnNextpage.setOnClickListener {
            val intent = Intent(this@Feedback,MainoptionActivity::class.java);
            startActivity(intent)
        }
    }
}