package com.example.strokepatientsvoicerecoveryapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.strokepatientsvoicerecoveryapp.databinding.HistoryRecordDetailBinding


class HistoryrecordActivity : AppCompatActivity() {

    private lateinit var binding: HistoryRecordDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =HistoryRecordDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //當按下查看鍵就會跳轉到題目紀錄畫面
        binding.btnSee.setOnClickListener {
            val intent = Intent(this,QuestiondetailActivity::class.java);
            startActivity(intent)
        }
        //當按下查看鍵就會跳轉到題目紀錄畫面
        binding.btnSee1.setOnClickListener {
            val intent = Intent(this,QuestiondetailActivity::class.java);
            startActivity(intent)
        }
    }
}
