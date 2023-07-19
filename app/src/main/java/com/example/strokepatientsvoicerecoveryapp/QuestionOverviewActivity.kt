package com.example.strokepatientsvoicerecoveryapp

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
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
    private var QuizTotalSum : Int = 0
    private var currentHintIndex: Int = 0
    private val hints: MutableList<String> = mutableListOf()
    private var score: Int = 10
    private var TotalScore: Int = 0
    private var TotalAnsSum: Int = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = QuestionOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        QuizSheet = "1Cjet_xxbV9EqxhUpAAl40YLharHsV_wT5JFd8OIXCdA"
        username = intent.getStringExtra("username") ?: ""
        sp1Selection = intent.getStringExtra("sp1Selection") ?: ""
        selectedTitle = intent.getStringExtra("selectedTitle") ?: ""

        initView()

        // 選題目
        when (selectedTitle) {
            "食物" -> QuizTotalSum = 148
            "生活" -> QuizTotalSum = 107
            "流暢" -> QuizTotalSum = 141
            "理解" -> QuizTotalSum = 114
            "重述-簡單" -> QuizTotalSum = 27
            "重述-困難" -> QuizTotalSum = 22
        }
        val randomQnum = (1..QuizTotalSum).random()
        getTheQuizFromSheet(randomQnum)

        binding.hint.setOnClickListener{
             showHint()
        }
        binding.next.setOnClickListener{
            initView()
            TotalScore += score
            TotalAnsSum += 10
            score = 10
            val randomQnum = (1..QuizTotalSum).random()
            getTheQuizFromSheet(randomQnum)
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
            .child(selectedTitle)
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
    private fun getTheQuizFromSheet(questionNumber : Int) {
        val questionNumber = questionNumber.toString()

        readQuestionContent(questionNumber) { dataSnapshot ->
            val currQuestion = dataSnapshot.value as? Map<*, *> ?: return@readQuestionContent

            val type = currQuestion["題型"].toString()

            when (type) {
                "複誦句子" -> binding.q1Evdashimg.visibility = View.VISIBLE
                "簡單應答" -> binding.q2Evdashimg.visibility = View.VISIBLE
                "聽覺理解" -> binding.q3Evdashimg.visibility = View.VISIBLE
                "圖物配對" -> binding.q4Evdashimg.visibility = View.VISIBLE
                "口語描述" -> binding.q5Evdashimg.visibility = View.VISIBLE
                "詞語表達" -> binding.q6Evdashimg.visibility = View.VISIBLE
            }

            when (type) {
                "複誦句子" -> {
                    val tvImage1 = binding.qSpeech.tvImage1
                    tvImage1.text = currQuestion["題目"].toString()
                }
                "簡單應答" -> {
                    val imageUrl = currQuestion["圖片1"].toString()
                    LoadImage(imageUrl) { drawable ->
                        binding.qSpeechImage.tvImage2.setImageDrawable(drawable)
                    }
                }
                "聽覺理解" -> {
                    // Set the question content to the appropriate TextView
                }
                "圖物配對" -> {
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
                "詞語表達" -> {
                    val imageUrl = currQuestion["圖片1"].toString()
                    LoadImage(imageUrl) { drawable ->
                        binding.qChooseSentence.tvImage.setImageDrawable(drawable)
                    }
                    val tvOptionOne = binding.qChooseSentence.tvOptionOne
                    tvOptionOne.text = currQuestion["選項1"].toString()
                    val tvOptionTwo = binding.qChooseSentence.tvOptionTwo
                    tvOptionTwo.text = currQuestion["選項2"].toString()
                    val tvOptionThree = binding.qChooseSentence.tvOptionThree
                    tvOptionThree.text = currQuestion["選項3"].toString()
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


    private fun showHint() {
        if (hints.isEmpty()) {
            Toast.makeText(this@QuestionOverviewActivity, "沒有提示囉~", Toast.LENGTH_SHORT).show()
        } else {
            val hintToShow = hints.removeAt(0)
            score--
            runOnUiThread {
                Toast.makeText(this@QuestionOverviewActivity, hintToShow, Toast.LENGTH_SHORT).show()
            }
        }

        if (hints.isEmpty()) {
            score = 6
        }
    }

}


