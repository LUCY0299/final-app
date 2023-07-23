package com.example.strokepatientsvoicerecoveryapp


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import com.example.strokepatientsvoicerecoveryapp.databinding.TimesupOverviewBinding


class TimesupOverviewActivity : AppCompatActivity() {

    private lateinit var binding: TimesupOverviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TimesupOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //當按下返回鍵就會跳轉到領域選擇畫面
        binding.btnReturn.setOnClickListener {
            val intent = Intent(this@TimesupOverviewActivity, MainoptionActivity::class.java)
            startActivity(intent)
        }
        //當按下再次測驗鍵就會跳轉到itemPracriceActivity畫面
        binding.btnTryagain.setOnClickListener {
        val intent = Intent(this@TimesupOverviewActivity, itemPracriceActivity::class.java)
        startActivity(intent)
        }
    }
}