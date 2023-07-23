package com.example.strokepatientsvoicerecoveryapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import com.example.strokepatientsvoicerecoveryapp.databinding.ActivityProfileBinding
import com.example.strokepatientsvoicerecoveryapp.databinding.EditPasswordBinding

class Profile : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //當按下變更密碼鍵就會跳轉到變更密碼畫面
        binding.editPassword.setOnClickListener {
            val intent = Intent(this@Profile,Editpassword::class.java)
            startActivity(intent)
        }
        //當按下編輯資料鍵就會跳轉到編輯資料畫面
        binding.editInfor.setOnClickListener {
            val intent = Intent(this@Profile,Modify::class.java)
            startActivity(intent)
        }
        //當按下歷史紀錄鍵就會跳轉到歷史紀錄畫面
        binding.history.setOnClickListener {
            val intent = Intent(this@Profile,HistoryrecordActivity::class.java)
            startActivity(intent)
        }
    }
}
