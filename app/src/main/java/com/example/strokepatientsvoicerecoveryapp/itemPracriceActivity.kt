package com.example.strokepatientsvoicerecoveryapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class itemPracriceActivity : AppCompatActivity() {
    private lateinit var btn_fluent : Button
    private lateinit var btn_understand : Button
    private lateinit var btn_restate : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_pracrice)

        btn_fluent.setOnClickListener {
            val   intent = Intent(this,SetTimeActivity::class.java);
            startActivity(intent)
        }//當按下流暢鍵就會跳轉到設定時間畫面
        btn_understand.setOnClickListener {
            val   intent = Intent(this,SetTimeActivity::class.java);
            startActivity(intent)
        }//當按下理解鍵就會跳轉到設定時間畫面
        btn_restate.setOnClickListener {
            val   intent = Intent(this,SetTimeActivity::class.java);
            startActivity(intent)
        }//當按下重述鍵就會跳轉到設定時間畫面
    }
}