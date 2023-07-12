package com.example.strokepatientsvoicerecoveryapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class DrinkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.set_time)

        // 跳转到 SetTimeActivity
        val intent = Intent(this, SetTimeActivity::class.java)
        startActivity(intent)
    }
}