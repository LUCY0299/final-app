package com.example.strokepatientsvoicerecoveryapp


import android.content.Intent
import android.widget.Button
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.strokepatientsvoicerecoveryapp.databinding.QuestionDetailBinding


class QuestiondetailActivity : AppCompatActivity() {

    private lateinit var binding: QuestionDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = QuestionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //當按查看測驗表現鍵就會跳轉到測驗表現畫面
        binding.btnSeequiz.setOnClickListener {
            val intent = Intent(this@QuestiondetailActivity,Feedback::class.java);
            startActivity(intent)
        }
    }
}