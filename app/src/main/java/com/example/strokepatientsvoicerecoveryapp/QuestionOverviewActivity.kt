package com.example.strokepatientsvoicerecoveryapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.media.MediaRecorder
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.strokepatientsvoicerecoveryapp.databinding.QuestionOverviewBinding
import com.google.firebase.database.*
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.Socket
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import android.Manifest
import android.content.ContextWrapper
import android.net.Uri
import android.os.*
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.os.Bundle
import android.os.Environment
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage


class QuestionOverviewActivity : AppCompatActivity() {

    // for another thread
    private val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var binding: QuestionOverviewBinding
    private lateinit var username: String
    private lateinit var sp1Selection: String
    private lateinit var selectedTitle: String
    private var timeValue: Int = 0

    private var currQuestion: Map<*, *>? = null
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


    private var originalXForOption4: Float = 0f
    private var originalYForOption4: Float = 0f

    private var originalXForOption5: Float = 0f
    private var originalYForOption5: Float = 0f

    private var originalXForOption6: Float = 0f
    private var originalYForOption6: Float = 0f

    //speech
    private val SPEECH_REQUEST_CODE = 0
    private val REQUEST_MICROPHONE = 1
    private var recorder: MediaRecorder? = null
    private lateinit var textView: TextView
    private lateinit var storageReference: StorageReference
    private var questionNumberStr: String = ""

    @Suppress("NAME_SHADOWING")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MyApp", "onCreate called")

        binding = QuestionOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化 Firebase
        FirebaseApp.initializeApp(this)

        // 初始化 storageReference
        storageReference = FirebaseStorage.getInstance().reference

        mainHandler.post {
            startTimer()
        }

        QuizSheet = "1Cjet_xxbV9EqxhUpAAl40YLharHsV_wT5JFd8OIXCdA"
        username = intent.getStringExtra("username") ?: ""
        sp1Selection = intent.getStringExtra("sp1Selection") ?: ""
        selectedTitle = intent.getStringExtra("selectedTitle") ?: ""
        timeValue = (intent.getIntExtra("timeValue", 0) ?: "") as Int

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
                SaveRecData()
                resetOptionsPosition()
            }

            initView()
            val randomQnum = (1..QuizTotalSum).random()
            getTheQuizFromSheet(randomQnum)
        }

        //speech

        binding.qSpeech.btnSpeech.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_MICROPHONE)
            } else {
                // 启动SpeechActivity
                startRecording() // 進行錄音
            }
        }
        binding.qSpeech.btnStop.setOnClickListener {
            stopRecording()   //停止錄音
        }

        binding.qSpeechImage.btnSpeech1.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_MICROPHONE)
            } else {
                // 启动SpeechActivity
                startRecording() // 進行錄音
            }
        }
        binding.qSpeechImage.btnStop1.setOnClickListener {
            stopRecording()   //停止錄音
        }

        binding.qDescribeImage.btnSpeech2.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_MICROPHONE)
            } else {
                // 启动SpeechActivity
                startRecording() // 進行錄音
            }
        }
        binding.qDescribeImage.btnStop2.setOnClickListener {
            stopRecording()   //停止錄音
        }

    }

    private fun startRecording() {
        try {
            // 初始化 MediaRecorder 并开始录音
            recorder = MediaRecorder()
            recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
//            recorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            val audioFile = getRecordingFile(questionNumberStr) // 获取包含檔名+檔案 的 File 对象
            recorder?.setOutputFile(audioFile.absolutePath)

//            recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            recorder?.prepare()
            recorder?.start()

            Toast.makeText(this, "Recording is started", Toast.LENGTH_LONG).show()
        }
        catch(e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        Log.d("MyApp", "stopRecording called")
        // 录音结束后释放 MediaRecorder 资源
        recorder?.stop()
        recorder?.reset()
        recorder?.release()
        recorder = null

        // 获取用户名
        val username = intent.getStringExtra("username") ?: ""
        val dateTime = DateTime

        // 创建文件夹路径
        val filePath = "recording/$username/$dateTime/${getRecordingFileName(questionNumberStr)}"

        // 停止录音后，将录音文件发送到服务器进行转文字
        GlobalScope.launch(Dispatchers.IO) {
            sendAudioToServer()
        }

        // 将录音音檔上傳至 Firebase Storage
        val storageRef = storageReference.child(filePath)
        val audioFile = getRecordingFile(questionNumberStr)
        val fileUri = Uri.fromFile(audioFile)

        // 輸出 fileUri 以檢查是否正確
        Log.d("FileUriDebug", "File Uri: $fileUri")

        storageRef.putFile(fileUri)
            .addOnSuccessListener {
                // 上傳成功，您可以處理相關邏輯，例如更新介面或資料庫等
                Log.d("UploadDebug", "File uploaded successfully")
            }
            .addOnFailureListener { exception ->
                // 上傳失敗，處理錯誤
                Log.e("UploadError", "File upload failed: ${exception.message}")
            }
    }

    private fun getRecordingFileName(questionNumber: String): String {
        return "${questionNumber}.3gp"
    }

    private fun getRecordingFile(questionNumber: String): File  {
        val contextWrapper = ContextWrapper(applicationContext)
        val musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val fileName = getRecordingFileName(questionNumberStr)
        return File(musicDirectory, fileName)
    }
    private fun sendAudioToServer() {
        thread {
            val socket = Socket("163.13.201.83", 3000)
            val outputStream = socket.getOutputStream()
            val inputStream = socket.getInputStream()

//            // 发送文件名给服务器
            val filename =getRecordingFileName(questionNumberStr)
            outputStream.write(filename.toByteArray(Charsets.UTF_8))

            // 发送录音文件到服务器
            val audioFile = getRecordingFile(questionNumberStr)
            val audioFileStream = FileInputStream(audioFile)
            val buffer = ByteArray(1024)
            var bytesRead: Int  //用于存储每次从输入流 audioFileStream 中读取的字节数
            while (audioFileStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            audioFileStream.close()
            outputStream.flush()

//            // 等待服务器返回识别结果
//            val responseBytes = ByteArray(1024)
//            val responseBytesRead = inputStream.read(responseBytes)
//            val responseData = String(responseBytes, 0, responseBytesRead)

            socket.close()
            // 添加延迟等待文件写入完成

            Thread.sleep(1000)
        }
    }


    // =========================function=======================================
    private fun startTimer() {
        val textView = binding.countdownTimer

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
    private fun getTheQuizFromSheet(questionNumber: Int) {
        questionNumberStr = questionNumber.toString()

        readQuestionContent(questionNumberStr) { dataSnapshot ->
            currQuestion = dataSnapshot.value as? Map<*, *> ?: return@readQuestionContent

            type = currQuestion?.get("題型")?.toString() ?: ""

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
                    binding.qSpeech.tvImage1.text = currQuestion?.get("題目")?.toString() ?: ""
                }
                "簡單應答" -> {
                    LoadImage(currQuestion?.get("圖片1")?.toString()) { drawable ->
                        binding.qSpeechImage.tvImage2.setImageDrawable(drawable)
                    }
                }
                "聽覺理解" -> {
                    binding.qChooseImage.tvText2.text = currQuestion?.get("題目")?.toString() ?: ""
                    LoadImage(currQuestion?.get("圖片1")?.toString()) { drawable ->
                        binding.qChooseImage.tvImage4.setImageDrawable(drawable)
                    }
                    LoadImage(currQuestion?.get("圖片2")?.toString()) { drawable ->
                        binding.qChooseImage.tvImage5.setImageDrawable(drawable)
                    }
                    LoadImage(currQuestion?.get("圖片3")?.toString()) { drawable ->
                        binding.qChooseImage.tvImage6.setImageDrawable(drawable)
                    }
                    LoadImage(currQuestion?.get("圖片4")?.toString()) { drawable ->
                        binding.qChooseImage.tvImage7.setImageDrawable(drawable)
                    }
                }
                "圖物配對" -> {
                    val answerList = mutableListOf(
                        currQuestion?.get("答案1").toString(),
                        currQuestion?.get("答案2").toString(),
                        currQuestion?.get("答案3").toString()
                    )
                    // 對答案列表進行隨機洗牌
                    answerList.shuffle(Random(System.currentTimeMillis()))

                    // 設置洗牌後的答案到相應的 TextView 中
                    binding.qDragText.tvOption4.text = answerList[0]
                    binding.qDragText.tvOption5.text = answerList[1]
                    binding.qDragText.tvOption6.text = answerList[2]

                    LoadImage(currQuestion?.get("圖片1")?.toString()) { drawable ->
                        binding.qDragText.tvImage8.setImageDrawable(drawable)
                    }
                    LoadImage(currQuestion?.get("圖片2")?.toString()) { drawable ->
                        binding.qDragText.tvImage9.setImageDrawable(drawable)
                    }
                    LoadImage(currQuestion?.get("圖片3")?.toString()) { drawable ->
                        binding.qDragText.tvImage10.setImageDrawable(drawable)
                    }
                    binding.qDragText.tvOption4.setOnTouchListener(DragTouchListener())
                    binding.qDragText.tvOption5.setOnTouchListener(DragTouchListener())
                    binding.qDragText.tvOption6.setOnTouchListener(DragTouchListener())

                    binding.qDragText.tvOption4.post {
                        originalXForOption4 = binding.qDragText.tvOption4.x
                        originalYForOption4 = binding.qDragText.tvOption4.y
                    }

                    binding.qDragText.tvOption5.post {
                        originalXForOption5 = binding.qDragText.tvOption5.x
                        originalYForOption5 = binding.qDragText.tvOption5.y
                    }

                    binding.qDragText.tvOption6.post {
                        originalXForOption6 = binding.qDragText.tvOption6.x
                        originalYForOption6 = binding.qDragText.tvOption6.y
                    }
                }
                "口語描述" -> {
                    LoadImage(currQuestion?.get("圖片1")?.toString()) { drawable ->
                        binding.qDescribeImage.tvImage3.setImageDrawable(drawable)
                    }
                    binding.qDescribeImage.tvText3.text = currQuestion?.get("題目")?.toString() ?: ""
                }
                "詞語表達" -> {
                    binding.qChooseSentence.tvOptionOne.text = currQuestion?.get("選項1")?.toString() ?: ""
                    binding.qChooseSentence.tvOptionTwo.text = currQuestion?.get("選項2")?.toString() ?: ""
                    binding.qChooseSentence.tvOptionThree.text = currQuestion?.get("選項3")?.toString() ?: ""
                    LoadImage(currQuestion?.get("圖片1")?.toString()) { drawable ->
                        binding.qChooseSentence.tvImage.setImageDrawable(drawable)
                    }
                }
            }

            // Get hints from the current question
            val hint1 = currQuestion?.get("提示1")?.toString() ?: ""
            val hint2 = currQuestion?.get("提示2")?.toString() ?: ""
            val hint3 = currQuestion?.get("提示3")?.toString() ?: ""

            // Update the hints list
            hints.clear()
            if (hint1.isNotEmpty()) hints.add(hint1)
            if (hint2.isNotEmpty()) hints.add(hint2)
            if (hint3.isNotEmpty()) hints.add(hint3)

            Ans = currQuestion?.get("答案1")?.toString() ?: ""
            recordList.add(
                RecordData(
                    questionNumberStr,
                    type,
                    currQuestion?.get("題目")?.toString() ?: "",
                    currQuestion?.get("圖片1")?.toString() ?: "",
                    Ans,
                    "" // 答案欄位暫時留空，等待用戶回答後填入
                )
            )
        }
    }
    private fun resetOptionsPosition() {
        // 將選項移回它們的原始位置
        binding.qDragText.tvOption4.x = originalXForOption4
        binding.qDragText.tvOption4.y = originalYForOption4

        binding.qDragText.tvOption5.x = originalXForOption5
        binding.qDragText.tvOption5.y = originalYForOption5

        binding.qDragText.tvOption6.x = originalXForOption6
        binding.qDragText.tvOption6.y = originalYForOption6

        // 重置選項的背景顏色
        binding.qDragText.tvOption4.setBackgroundResource(R.drawable.text_drag_bg)
        binding.qDragText.tvOption5.setBackgroundResource(R.drawable.text_drag_bg)
        binding.qDragText.tvOption6.setBackgroundResource(R.drawable.text_drag_bg)

        binding.qDragText.tvOption4.isSelected = false
        binding.qDragText.tvOption5.isSelected = false
        binding.qDragText.tvOption6.isSelected = false

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

                    // Check if the dragged view is close to valid target options, and change border color accordingly
                    val validTargetOptions = arrayOf(
                        binding.qDragText.tvOption4,
                        binding.qDragText.tvOption5,
                        binding.qDragText.tvOption6
                    )
                    val margin = 50
                    var isAnyOverlapping = false
                    validTargetOptions.forEach { tvOption ->
                        if (tvOption != view && isViewCloseTo(view, tvOption, margin)) {
                            val selectedOption = tvOption.text.toString()
                            if (selectedOption == Ans) {
                                view.setBackgroundResource(R.drawable.green_border)
                                tvOption.setBackgroundResource(R.drawable.green_border)
                            } else {
                                view.setBackgroundResource(R.drawable.red_border)
                                tvOption.setBackgroundResource(R.drawable.red_border)
                            }
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
                    val validTargetOptions = arrayOf(
                        binding.qDragText.tvOption4,
                        binding.qDragText.tvOption5,
                        binding.qDragText.tvOption6
                    )
                    var isOverlapping = false
                    validTargetOptions.forEach { tvOption ->
                        if (isViewOverlapping(view, tvOption)) {
                            view.x = tvOption.x
                            view.y = tvOption.y

                            // Check if the dragged option is correct
                            val selectedOption = tvOption.text.toString()
                            if (selectedOption == Ans) {
                                view.setBackgroundResource(R.drawable.green_border)
                                tvOption.setBackgroundResource(R.drawable.green_border)
                            } else {
                                view.setBackgroundResource(R.drawable.red_border)
                                tvOption.setBackgroundResource(R.drawable.red_border)
                            }

                            isOverlapping = true
                        }
                    }
                    if (!isOverlapping && isMoved) {
                        // If the view is not overlapping any options and it was moved, reset it to the original position
                        view.x = originalX
                        view.y = originalY

                        // Check if the dragged option is tvOption1, tvOption2, or tvOption3
                        val nonTargetOptions = arrayOf(
                            binding.qDragText.tvOption1,
                            binding.qDragText.tvOption2,
                            binding.qDragText.tvOption3
                        )
                        if (nonTargetOptions.contains(view)) {
                            view.setBackgroundResource(R.drawable.red_border)
                        }
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
                //val userAnswer = binding.qSpeech.editWord3.text.toString().trim()
                //if(Ans != userAnswer){ score-- }
                val recordData = recordList.lastOrNull {it.type == type }
                //if (recordData != null) { recordData.userAnswer = userAnswer }
            }
            "簡單應答" -> {
                val userAnswer = binding.qSpeechImage.editWord.text.toString().trim()
                if(Ans != userAnswer){ score-- }
                val recordData = recordList.lastOrNull {it.type == type }
                if (recordData != null) { recordData.userAnswer = userAnswer }
            }
            "聽覺理解" -> {
                binding.qChooseImage.tvImage4.setOnClickListener {
                    val selectedImageUrl = currQuestion?.get("圖片1").toString()
                    val recordData = recordList.lastOrNull { it.type == type }
                    if (recordData != null) {
                        recordData.userAnswer = selectedImageUrl
                    }
                    binding.next.performClick()
                }
                binding.qChooseImage.tvImage5.setOnClickListener {
                    val selectedImageUrl = currQuestion?.get("圖片2").toString()
                    val recordData = recordList.lastOrNull { it.type == type }
                    if (recordData != null) {
                        recordData.userAnswer = selectedImageUrl
                    }
                    binding.next.performClick()
                }
                binding.qChooseImage.tvImage6.setOnClickListener {
                    val selectedImageUrl = currQuestion?.get("圖片3").toString()
                    val recordData = recordList.lastOrNull { it.type == type }
                    if (recordData != null) {
                        recordData.userAnswer = selectedImageUrl
                    }
                    binding.next.performClick()
                }
                binding.qChooseImage.tvImage7.setOnClickListener {
                    val selectedImageUrl = currQuestion?.get("圖片4").toString()
                    val recordData = recordList.lastOrNull { it.type == type }
                    if (recordData != null) {
                        recordData.userAnswer = selectedImageUrl
                    }
                    binding.next.performClick()
                }
            }
            "圖物配對" -> {
                val selectedOption = when {
                    binding.qDragText.tvOption4.isSelected -> binding.qDragText.tvOption4.text.toString()
                    binding.qDragText.tvOption5.isSelected -> binding.qDragText.tvOption5.text.toString()
                    binding.qDragText.tvOption6.isSelected -> binding.qDragText.tvOption6.text.toString()
                    else -> ""
                }

                Log.d("isAnsCorrect", "selectedOption: $selectedOption")

                if (selectedOption == Ans) {
                    binding.qDragText.tvOption4.setBackgroundResource(R.drawable.green_border)
                    binding.qDragText.tvOption5.setBackgroundResource(R.drawable.green_border)
                    binding.qDragText.tvOption6.setBackgroundResource(R.drawable.green_border)
                } else {
                    binding.qDragText.tvOption4.setBackgroundResource(R.drawable.red_border)
                    binding.qDragText.tvOption5.setBackgroundResource(R.drawable.red_border)
                    binding.qDragText.tvOption6.setBackgroundResource(R.drawable.red_border)
                    score--
                }
                val recordData = recordList.lastOrNull { it.type == type }
                if (recordData != null) {
                    recordData.userAnswer = selectedOption
                }
            }

            "口語描述" -> {
                val userAnswer = binding.qDescribeImage.editWord2.text.toString().trim()
                if(Ans != userAnswer){ score-- }
                val recordData = recordList.lastOrNull {it.type == type }
                if (recordData != null) { recordData.userAnswer = userAnswer }
            }
            "詞語表達" -> {
                val selectedOption = when {
                    binding.qChooseSentence.tvOptionOne.isSelected -> binding.qChooseSentence.tvOptionOne.text.toString()
                    binding.qChooseSentence.tvOptionTwo.isSelected -> binding.qChooseSentence.tvOptionTwo.text.toString()
                    binding.qChooseSentence.tvOptionThree.isSelected -> binding.qChooseSentence.tvOptionThree.text.toString()
                    else -> ""
                }
                if (selectedOption != Ans) { score-- }
                val recordData = recordList.lastOrNull { it.type == type }
                if (recordData != null) {
                    recordData.userAnswer = selectedOption
                }
                binding.qChooseSentence.tvOptionOne.setOnClickListener { binding.next.performClick() }
                binding.qChooseSentence.tvOptionTwo.setOnClickListener { binding.next.performClick() }
                binding.qChooseSentence.tvOptionThree.setOnClickListener { binding.next.performClick() }
            }

        }

    }

    private fun SaveRecData() {
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val recordRef: DatabaseReference = database.getReference("紀錄")
        val userRef: DatabaseReference = recordRef.child(username)
        val dateTimeRef: DatabaseReference = userRef.child(DateTime)

        val timeValueRef: DatabaseReference = dateTimeRef.child(timeValue.toString())

        val dateRef: DatabaseReference = dateTimeRef.child("日期時間")
        val PracTimeRef: DatabaseReference = dateTimeRef.child("練習時間")
        val degreeRef: DatabaseReference = dateTimeRef.child("難度")
        val typeRef: DatabaseReference = dateTimeRef.child("選擇類型")
        val commentRef: DatabaseReference = dateTimeRef.child("評語")
        dateRef.setValue(DateTime)
        PracTimeRef.setValue(timeValue.toString())
        degreeRef.setValue(sp1Selection)
        typeRef.setValue(selectedTitle)
        commentRef.setValue("")


        recordList.forEachIndexed { index, recordData ->
            val adjustedIndex = index + 1
            timeValueRef.child(adjustedIndex.toString()).setValue(recordData)
                .addOnSuccessListener {
                    Log.d("SaveRecData", "資料儲存成功")
                }
                .addOnFailureListener {
                    Log.e("SaveRecData", "資料儲存失敗")
                }
        }
    }

}

data class RecordData(
    val questionNumber: String = "",
    val type: String = "",
    val question: String = "",
    val imageUrl: String = "",
    val correctAnswer: String = "",
    var userAnswer: String = "",
    val 評語: String = "",
    val audioFileName: String = ""
)

