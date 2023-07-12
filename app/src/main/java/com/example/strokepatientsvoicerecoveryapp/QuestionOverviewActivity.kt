package com.example.strokepatientsvoicerecoveryapp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.strokepatientsvoicerecoveryapp.databinding.QuestionOverviewBinding
import java.util.*
import java.util.concurrent.TimeUnit

class QuestionOverviewActivity : AppCompatActivity() {

    private lateinit var binding: QuestionOverviewBinding
    private lateinit var username: String
    private lateinit var sp1Selection: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = QuestionOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        username = intent.getStringExtra("username") ?: ""
        sp1Selection = intent.getStringExtra("sp1Selection") ?: ""


        initView()


//      ======================timer=====================================
        val textView = findViewById<TextView>(R.id.countdown_timer)

        val timeValue = intent.getIntExtra("timeValue", 0) // 從Intent中檢索時間值
        val timeDuration = TimeUnit.MINUTES.toMillis(timeValue.toLong()) // 設定倒數時間
        val tickInterval: Long = 10 //接收回調的間隔

        object : CountDownTimer(timeDuration, tickInterval) {
            var millis: Long = 1000 //1000=1秒
            override fun onTick(millisUntilFinished: Long) {
                millis -= tickInterval
                if (millis == 0L) millis = 1000
                val timerText = String.format(
                    Locale.getDefault(), "%2d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                            TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(
                                    millisUntilFinished
                                )//公式
                            ),
                    millis
                )
                textView.text = timerText
            }
            // 倒數完畢時
            override fun onFinish() {
                textView.text = "時間到"
                val intent = Intent(this@QuestionOverviewActivity, TimesupOverviewActivity::class.java)
                startActivity(intent)
            }
        }.start()
//      ======================timer end=====================================
    }


    // =========================function=======================================
    // 初始畫面，全部隱藏
    private fun initView(){
        binding.q1Evdashimg.visibility = View.GONE
        binding.q2Evdashimg.visibility = View.GONE
        binding.q3Evdashimg.visibility = View.GONE
        binding.q4Evdashimg.visibility = View.GONE
        binding.q5Evdashimg.visibility = View.GONE
        binding.q6Evdashimg.visibility = View.GONE
    }

    // 打開正確的難度、類型的題目集合
    private fun getTheQuizFromSheet(){

    }
    // 挑選題目，放上畫面
    private fun pickQuiz(){

    }

    private fun showHint(){

    }


}