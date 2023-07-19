package com.example.strokepatientsvoicerecoveryapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.strokepatientsvoicerecoveryapp.databinding.ItemPracriceBinding

class itemPracriceActivity : AppCompatActivity() {
    private lateinit var binding: ItemPracriceBinding
    private lateinit var username: String
    private lateinit var sp1Selection: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ItemPracriceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        username = intent.getStringExtra("username") ?: ""
        sp1Selection = intent.getStringExtra("sp1Selection") ?: ""

        //當按下流暢鍵就會跳轉到設定時間畫面
        binding.btnFluent.setOnClickListener {
            navigateToNextPage(SetTimeActivity::class.java, "流暢")
        }

        //當按下理解鍵就會跳轉到設定時間畫面
        binding.btnUnderstand.setOnClickListener {
            navigateToNextPage(SetTimeActivity::class.java, "理解")
        }

        //當按下重述鍵就會跳轉到設定時間畫面
        binding.btnRestate.setOnClickListener {
            if(sp1Selection == "簡單"){
                navigateToNextPage(SetTimeActivity::class.java, "重述-簡單")
            }else if(sp1Selection == "困難"){
                navigateToNextPage(SetTimeActivity::class.java, "重述-困難")
            }
        }
    }

    private fun navigateToNextPage(activityClass: Class<*>, selectedTitle: String) {
        val intent = Intent(this, activityClass)
        intent.putExtra("username", username)
        intent.putExtra("sp1Selection", sp1Selection)
        intent.putExtra("selectedTitle", selectedTitle)
        startActivity(intent)

    }
}