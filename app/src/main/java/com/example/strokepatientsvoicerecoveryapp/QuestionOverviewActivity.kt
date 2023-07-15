package com.example.strokepatientsvoicerecoveryapp

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.strokepatientsvoicerecoveryapp.databinding.QuestionOverviewBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.InputStream
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit


class QuestionOverviewActivity : AppCompatActivity() {

    private lateinit var binding: QuestionOverviewBinding
    private lateinit var username: String
    private lateinit var sp1Selection: String
    private lateinit var selectedTitle: String

    private lateinit var QuizSheet : String
    private var currentHintIndex: Int = 0
    private val hints: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = QuestionOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        QuizSheet = "1Cjet_xxbV9EqxhUpAAl40YLharHsV_wT5JFd8OIXCdA"
        username = intent.getStringExtra("username") ?: ""
        sp1Selection = intent.getStringExtra("sp1Selection") ?: ""
        selectedTitle = intent.getStringExtra("selectedTitle") ?: ""

        initView()
        binding.test.text = selectedTitle
        getTheQuizFromSheet()

        binding.hint.setOnClickListener{
            showHint()
        }

//      ======================timer=====================================
        val textView = binding.countdownTimer

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
                textView.text = "時間到" // 補切換畫面 結算成績
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

    // 開資料庫
    fun readQuestionContent(questionNumber: String, callback: (DataSnapshot) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference(QuizSheet)
            .child("流暢")// 換成傳來的selectedTitle
            .child(questionNumber)

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                callback(dataSnapshot)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    // 打開正確的難度、類型的題目View
    private fun getTheQuizFromSheet() {
        val questionNumber = "17" // 改 隨機題目

        readQuestionContent(questionNumber) { dataSnapshot ->
            val currQuestion = dataSnapshot.value as? Map<*, *> ?: return@readQuestionContent

            val type = currQuestion["題型"].toString()

            when (type) {
                "複誦句子" -> binding.q1Evdashimg.visibility = View.VISIBLE
                "A" -> binding.q2Evdashimg.visibility = View.VISIBLE
                "B" -> binding.q3Evdashimg.visibility = View.VISIBLE
                "C" -> binding.q4Evdashimg.visibility = View.VISIBLE
                "口語描述" -> binding.q5Evdashimg.visibility = View.VISIBLE
                "E" -> binding.q6Evdashimg.visibility = View.VISIBLE
            }

            when (type) {
                "複誦句子" -> {
                    val tvImage1 = binding.qSpeech.tvImage1
                    tvImage1.text = currQuestion["題目"].toString()
                }
                "A" -> {
                    // Set the question content to the appropriate TextView
                }
                "B" -> {
                    // Set the question content to the appropriate TextView
                }
                "C" -> {
                    // Set the question content to the appropriate TextView
                }
                "口語描述" -> {
                    val imageUrl = currQuestion["圖片1"].toString()
                    LoadImage(imageUrl) { drawable ->
                        binding.qDescribeImage.tvImage3.setImageDrawable(drawable)
                    }
                    val tvText3 = binding.qDescribeImage.tvText3
                    tvText3.text = currQuestion["題目"].toString()
                }
                "E" -> {
                    // Set the question content to the appropriate TextView
                }
            }

            // Get hints from the current question
            val hint1 = currQuestion["提示1"].toString()
            val hint2 = currQuestion["提示2"].toString()
            val hint3 = currQuestion["提示3"].toString()

            // Update the hints list
            hints.clear()
            if (hint1.isNotEmpty()) hints.add(hint1)
            if (hint2.isNotEmpty()) hints.add(hint2)
            if (hint3.isNotEmpty()) hints.add(hint3)
        }
    }

    fun LoadImage(url: String?, callback: (Drawable?) -> Unit) {
        Thread {
            try {
                val `is`: InputStream = URL(url).content as InputStream
                val drawable = Drawable.createFromStream(`is`, "src name")
                runOnUiThread {
                    callback(drawable)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    callback(null)
                }
            }
        }.start()
    }


    private fun showHint(){
        if (currentHintIndex < hints.size) {
            val hintToShow = hints[currentHintIndex]
            Toast.makeText(this@QuestionOverviewActivity, hintToShow, Toast.LENGTH_SHORT).show()
            currentHintIndex++
        }
    }


}


