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

    }
}