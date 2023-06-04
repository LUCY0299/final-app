package com.example.strokepatientsvoicerecoveryapp


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ActionBarContextView
import com.example.strokepatientsvoicerecoveryapp.databinding.BasicInformationBinding
import com.example.strokepatientsvoicerecoveryapp.databinding.UserloginBinding
import com.google.firebase.database.DatabaseReference



class BasicinformationActivity : AppCompatActivity() {

    private lateinit var binding : BasicInformationBinding
    private lateinit var dbRef : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BasicInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        getPassedUser()

        //當按下送出鍵就會跳轉到主題選擇畫面
        binding.send.setOnClickListener {

            saveUserData()
            getPassedUser()
//            val intent = Intent(this,MainoptionActivity::class.java);
//            startActivity(intent)
        }
    }

    // ========================function======================================
    // 上一個頁面傳過來的帳號
    // 現在登入的帳號
    private fun getPassedUser(){

        val intent = getIntent();

        // 有資料就放格子
        val nameUser = intent.getStringExtra("name");
        val emailUser = intent.getStringExtra("email");
        val usernameUser = intent.getStringExtra("username");
        val passwordUser = intent.getStringExtra("password");

    }

    // 儲存帳號資料到FireBase
    private fun saveUserData(){

    }
}

//    private lateinit var edusername : EditText
//    private lateinit var edpassword : EditText
//    private lateinit var send : Button
