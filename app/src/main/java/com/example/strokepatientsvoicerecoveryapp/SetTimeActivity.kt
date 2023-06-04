package com.example.strokepatientsvoicerecoveryapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SetTimeActivity: AppCompatActivity() {
    private lateinit var sure : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.set_time)

        sure.setOnClickListener {
            val   intent = Intent(this,CountdowntimerActivity::class.java);
            startActivity(intent)
        }//當按下確定鍵就會跳轉到倒數畫面
    }
}