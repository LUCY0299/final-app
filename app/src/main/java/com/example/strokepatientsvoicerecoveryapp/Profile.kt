package com.example.strokepatientsvoicerecoveryapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import com.example.strokepatientsvoicerecoveryapp.databinding.ActivityProfileBinding

class Profile : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        username = intent.getStringExtra("username") ?: ""

        //當按下變更密碼鍵就會跳轉到變更密碼畫面
        binding.editPassword.setOnClickListener {
            navigateToNextPage(Editpassword::class.java)
        }
        //當按下編輯資料鍵就會跳轉到編輯資料畫面
        binding.editInfor.setOnClickListener {
            navigateToNextPage(Modify::class.java)
        }
        //當按下歷史紀錄鍵就會跳轉到歷史紀錄畫面
        binding.history.setOnClickListener {
            navigateToNextPage(HistoryRecordActivity::class.java)
        }
    }

    private fun navigateToNextPage(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.putExtra("username", username)
        startActivity(intent)
    }
}
