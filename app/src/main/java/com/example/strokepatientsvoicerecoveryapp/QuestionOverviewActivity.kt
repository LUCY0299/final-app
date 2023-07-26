package com.example.strokepatientsvoicerecoveryapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.strokepatientsvoicerecoveryapp.databinding.QuestionOverviewBinding
import com.google.firebase.database.*
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class QuestionOverviewActivity : AppCompatActivity() {

    // for another thread
    private val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var binding: QuestionOverviewBinding
    private lateinit var username: String
    private lateinit var sp1Selection: String
    private lateinit var selectedTitle: String



    private lateinit var QuizSheet : String
    private var QuizTotalSum : Int = 0
    private var Ans : String =""
    private var type: String = ""

    private val hints: MutableList<String> = mutableListOf()
    private var score: Int = 10
    private var TotalScore: Int = 0
    private var TotalAnsSum: Int = 10

    private lateinit var timer: CountDownTimer
    private val randomQnum : Int = 0
    private val recordList: MutableList<RecordData> = mutableListOf()
    private val DateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

    @Suppress("NAME_SHADOWING")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = QuestionOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainHandler.post {
            startTimer()
        }

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
            // 將操作放在非UI線程中
            mainHandler.post {
                isAnsCorrect()
                TotalScore += score
                Log.d("isAnsCorrect", "score: $score, Total: $TotalScore, Ans: $TotalAnsSum")
                TotalAnsSum += 10
                score = 10
                SaveRecData(randomQnum, Ans, score)
            }

            initView()
            val randomQnum = (1..QuizTotalSum).random()
            getTheQuizFromSheet(randomQnum)
        }

    }
    // =========================function=======================================
    private fun startTimer() {
        val textView = binding.countdownTimer

        val timeValue = intent.getIntExtra("timeValue", 0) // 從Intent中檢索時間值
        val timeDuration = TimeUnit.MINUTES.toMillis(timeValue.toLong()) // 設定倒數時間
        val tickInterval: Long = 10 // 接收回調的間隔

        timer = object : CountDownTimer(timeDuration, tickInterval) {
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
                // 將計時的顯示更新放在主UI線程中
                mainHandler.post {
                    textView.text = timerText
                }
            }

            // 倒數完畢時
            override fun onFinish() {
                textView.text = "時間到" // 補切換畫面
                // 結算成績
                val result = (TotalScore.toDouble() / TotalAnsSum.toDouble() * 100).toInt()
                val intent =
                    Intent(this@QuestionOverviewActivity, TimesupOverviewActivity::class.java)
                intent.putExtra("username", username)
                intent.putExtra("sp1Selection", sp1Selection)
                intent.putExtra("Score", result)
                intent.putExtra("timeValue", timeValue)
                startActivity(intent)
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 確保在Activity被銷毀時停止計時
        timer.cancel()
    }

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
    private fun readQuestionContent(questionNumber: String, callback: (DataSnapshot) -> Unit) {
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
    @SuppressLint("ClickableViewAccessibility")
    private fun getTheQuizFromSheet(questionNumber : Int) {
        val questionNumber = questionNumber.toString()

        readQuestionContent(questionNumber) { dataSnapshot ->
            val currQuestion = dataSnapshot.value as? Map<*, *> ?: return@readQuestionContent

            type = currQuestion["題型"].toString()

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
                    binding.qSpeech.tvImage1.text = currQuestion["題目"].toString()
                }
                "簡單應答" -> {
                    LoadImage(currQuestion["圖片1"].toString()) { drawable ->
                        binding.qSpeechImage.tvImage2.setImageDrawable(drawable)
                    }
                }
                "聽覺理解" -> {
                    binding.qChooseImage.tvText2.text = currQuestion["題目"].toString()
                    LoadImage(currQuestion["圖片1"].toString()) { drawable ->
                        binding.qChooseImage.tvImage4.setImageDrawable(drawable)
                    }
                    LoadImage(currQuestion["圖片2"].toString()) { drawable ->
                        binding.qChooseImage.tvImage5.setImageDrawable(drawable)
                    }
                    LoadImage(currQuestion["圖片3"].toString()) { drawable ->
                        binding.qChooseImage.tvImage6.setImageDrawable(drawable)
                    }
                    LoadImage(currQuestion["圖片4"].toString()) { drawable ->
                        binding.qChooseImage.tvImage7.setImageDrawable(drawable)
                    }

                }
                "圖物配對" -> {
                    binding.qDragText.tvOption4.text = currQuestion["答案1"].toString()
                    binding.qDragText.tvOption5.text = currQuestion["答案2"].toString()
                    binding.qDragText.tvOption6.text = currQuestion["答案3"].toString()
                    LoadImage(currQuestion["圖片1"].toString()) { drawable ->
                        binding.qDragText.tvImage8.setImageDrawable(drawable)
                    }
                    LoadImage(currQuestion["圖片2"].toString()) { drawable ->
                        binding.qDragText.tvImage9.setImageDrawable(drawable)
                    }
                    LoadImage(currQuestion["圖片3"].toString()) { drawable ->
                        binding.qDragText.tvImage10.setImageDrawable(drawable)
                    }
                    binding.qDragText.tvOption4.setOnTouchListener(DragTouchListener())
                    binding.qDragText.tvOption5.setOnTouchListener(DragTouchListener())
                    binding.qDragText.tvOption6.setOnTouchListener(DragTouchListener())

                }

                "口語描述" -> {
                    LoadImage(currQuestion["圖片1"].toString()) { drawable ->
                        binding.qDescribeImage.tvImage3.setImageDrawable(drawable)
                    }
                    binding.qDescribeImage.tvText3.text = currQuestion["題目"].toString()
                }
                "詞語表達" -> {
                    binding.qChooseSentence.tvOptionOne.text = currQuestion["選項1"].toString()
                    binding.qChooseSentence.tvOptionTwo.text = currQuestion["選項2"].toString()
                    binding.qChooseSentence.tvOptionThree.text = currQuestion["選項3"].toString()
                    LoadImage(currQuestion["圖片1"].toString()) { drawable ->
                        binding.qChooseSentence.tvImage.setImageDrawable(drawable)
                    }
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

            Ans = currQuestion["答案1"].toString()
        }
    }

    private inner class DragTouchListener : View.OnTouchListener {
        private var offsetX = 0
        private var offsetY = 0
        private var isMoved = false
        private var originalX = 0f
        private var originalY = 0f

        override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Record the touch point's offset for maintaining position during drag
                    offsetX = motionEvent.rawX.toInt() - view.x.toInt()
                    offsetY = motionEvent.rawY.toInt() - view.y.toInt()
                    isMoved = false
                    originalX = view.x
                    originalY = view.y
                }
                MotionEvent.ACTION_MOVE -> {
                    // Update the position of the view being dragged
                    val newX = motionEvent.rawX.toInt() - offsetX
                    val newY = motionEvent.rawY.toInt() - offsetY
                    view.x = newX.toFloat()
                    view.y = newY.toFloat()
                    isMoved = true

                    // Check if the dragged view is close to other views, and change border color accordingly
                    val tvOptions = arrayOf(
                        binding.qDragText.tvOption1,
                        binding.qDragText.tvOption2,
                        binding.qDragText.tvOption3,
                        binding.qDragText.tvOption4,
                        binding.qDragText.tvOption5,
                        binding.qDragText.tvOption6
                    )
                    val margin = 50 // You can adjust this margin value to fit your needs
                    var isAnyOverlapping = true
                    tvOptions.forEach { tvOption ->
                        if (tvOption != view && isViewCloseTo(view, tvOption, margin)) {
                            view.setBackgroundResource(R.drawable.red_border)
                            tvOption.setBackgroundResource(R.drawable.red_border)
                            isAnyOverlapping = true
                        } else {
                            tvOption.setBackgroundResource(R.drawable.text_drag_bg)
                        }
                    }
                    if (!isAnyOverlapping) {
                        view.setBackgroundResource(R.drawable.text_drag_bg)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    // On lifting the finger, check if there is an overlapping view and snap the dragged view to its position
                    val tvOptions = arrayOf(
                        binding.qDragText.tvOption4,
                        binding.qDragText.tvOption5,
                        binding.qDragText.tvOption6
                    )
                    var isOverlapping =true
                    tvOptions.forEach { tvOption ->
                        if (isViewOverlapping(view, tvOption)) {
                            view.x = tvOption.x
                            view.y = tvOption.y
                            view.setBackgroundResource(R.drawable.blue_border)
                            tvOption.setBackgroundResource(R.drawable.green_border)
                            isOverlapping = true
                        }
                    }
                    if (!isOverlapping && isMoved) {
                        view.x = originalX
                        view.y = originalY
                    }
                }

            }
            return true
        }


        // 檢查兩個View是否重疊
        private fun isViewOverlapping(view1: View, view2: View): Boolean {
            val rect1 = Rect(view1.left, view1.top, view1.right, view1.bottom)
            val rect2 = Rect(view2.left, view2.top, view2.right, view2.bottom)
            return rect1.intersect(rect2)
        }

        // 檢查兩個View是否接近，可以根據自己的需求調整邊界值
        private fun isViewCloseTo(view1: View, view2: View, margin: Int): Boolean {
            val centerX1 = view1.x + view1.width / 2
            val centerY1 = view1.y + view1.height / 2
            val centerX2 = view2.x + view2.width / 2
            val centerY2 = view2.y + view2.height / 2

            val distanceX = Math.abs(centerX1 - centerX2)
            val distanceY = Math.abs(centerY1 - centerY2)

            return distanceX < margin && distanceY < margin
        }
    }





    private fun LoadImage(url: String?, callback: (Drawable?) -> Unit) {
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

    private fun isAnsCorrect(){
        when (type) {
            "複誦句子" -> {
                val userAnswer = binding.qSpeech.editWord3.toString().trim()
                if(Ans != userAnswer){ score-- }
                recordList.add(RecordData(getCurrentDateTime(), randomQnum, Ans))
            }
            "簡單應答" -> {
                val userAnswer = binding.qSpeechImage.tvText4.toString().trim()
                if(Ans != userAnswer){ score-- }
                recordList.add(RecordData(getCurrentDateTime(), randomQnum, Ans))
            }
            "聽覺理解" -> {
                recordList.add(RecordData(getCurrentDateTime(), randomQnum, Ans))
                binding.qChooseImage.tvImage4.setOnClickListener { binding.next.performClick() }
                binding.qChooseImage.tvImage5.setOnClickListener { binding.next.performClick() }
                binding.qChooseImage.tvImage6.setOnClickListener { binding.next.performClick() }
                binding.qChooseImage.tvImage7.setOnClickListener { binding.next.performClick() }
            }
            "圖物配對" -> {
                recordList.add(RecordData(getCurrentDateTime(), randomQnum, Ans))
            }
            "口語描述" -> {
                val userAnswer = binding.qDescribeImage.editWord2.toString().trim()
                if(Ans != userAnswer){ score-- }
                recordList.add(RecordData(getCurrentDateTime(), randomQnum, Ans))
            }
            "詞語表達" -> {
                val selectedOption = when {
                    binding.qChooseSentence.tvOptionOne.isSelected -> binding.qChooseSentence.tvOptionOne.text.toString()
                    binding.qChooseSentence.tvOptionTwo.isSelected -> binding.qChooseSentence.tvOptionTwo.text.toString()
                    binding.qChooseSentence.tvOptionThree.isSelected -> binding.qChooseSentence.tvOptionThree.text.toString()
                    else -> ""
                }
                if (selectedOption != Ans) { score-- }
                recordList.add(RecordData(getCurrentDateTime(), randomQnum, Ans))
                binding.qChooseSentence.tvOptionOne.setOnClickListener { binding.next.performClick() }
                binding.qChooseSentence.tvOptionTwo.setOnClickListener { binding.next.performClick() }
                binding.qChooseSentence.tvOptionThree.setOnClickListener { binding.next.performClick() }
            }
        }

    }

    private fun SaveRecData(randomQnum: Int, ans: String, score: Int) {
        val currentTime = getCurrentDateTime()

        val recordData = RecordData(currentTime, randomQnum, ans, score)

        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val recordRef: DatabaseReference = database.getReference("紀錄")
        val userRef: DatabaseReference = recordRef.child(username)
        val dateTimeRef: DatabaseReference = userRef.child(DateTime)
        val timeRef: DatabaseReference = dateTimeRef.child(currentTime)

        timeRef.setValue(recordData)
            .addOnSuccessListener {
                Log.d("SaveRecData", "資料儲存成功")
            }
            .addOnFailureListener {
                Log.e("SaveRecData", "資料儲存失敗")
            }
    }

    private fun getCurrentDateTime(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }
}

data class RecordData(
    val time: String = "",
    val questionNumber: Int = 0,
    val answer: String = "",
    val score: Int = 0
)


