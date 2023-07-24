package com.example.strokepatientsvoicerecoveryapp


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import com.example.strokepatientsvoicerecoveryapp.databinding.TimesupOverviewBinding


class TimesupOverviewActivity : AppCompatActivity() {

    private lateinit var binding: TimesupOverviewBinding
    private lateinit var username: String
    private lateinit var sp1Selection: String
    private var Score: Int = 0
    private var timeValue: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TimesupOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        username = intent.getStringExtra("username") ?: ""
        sp1Selection = intent.getStringExtra("sp1Selection") ?: ""
        Score = intent.getIntExtra("Score", 0)
        timeValue = intent.getIntExtra("timeValue", 0)

        binding.testTime.text = timeValue.toString()
        binding.num.text = Score.toString()

        //當按下返回鍵就會跳轉到領域選擇畫面
        binding.btnReturn.setOnClickListener {
            val intent = Intent(this@TimesupOverviewActivity, MainoptionActivity::class.java)
            intent.putExtra("username", username)
            intent.putExtra("sp1Selection", sp1Selection)
            startActivity(intent)
        }
    }
}