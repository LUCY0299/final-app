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
import android.content.ClipData
import android.content.ClipDescription
import android.content.ContextWrapper
import android.net.Uri
import android.os.*
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.os.Bundle
import android.os.Environment
import android.speech.tts.TextToSpeech
import android.view.DragEvent
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
    private val REQUEST_MICROPHONE = 1
    private var recorder: MediaRecorder? = null
    private lateinit var textView: TextView
    private lateinit var storageReference: StorageReference
    private var questionNumberStr: String = ""
    private var textToSpeech: TextToSpeech? = null

    @Suppress("NAME_SHADOWING")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MyApp", "onCreate called")

        //TTS
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = Locale.getDefault()
                val result = textToSpeech?.setLanguage(locale)
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    Log.e("TTS", "Language is not supported.")
                }
            } else {
                Log.e("TTS", "Initialization failed.")
            }
        }


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
            "食物" -> QuizTotalSum = 153
            "生活" -> QuizTotalSum = 122
            "流暢" -> QuizTotalSum = 147
            "理解" -> QuizTotalSum = 127
            "重述-簡單" -> QuizTotalSum = 26
            "重述-困難" -> QuizTotalSum = 23
        }
        val randomQnum = (1..QuizTotalSum).random()
        getTheQuizFromSheet(randomQnum)

        binding.hint.setOnClickListener{
            showHint()
        }

        binding.next.setOnClickListener{
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
            // 在初始化界面时保存按钮的原始背景
            val originalBackgroundOption1 = binding.qDragText.tvOption1.background
            val originalBackgroundOption2 = binding.qDragText.tvOption2.background
            val originalBackgroundOption3 = binding.qDragText.tvOption3.background
            val randomQnum = (1..QuizTotalSum).random()
            getTheQuizFromSheet(randomQnum)

            binding.qDragText.tvOption1.text = "" // 清除 tvOption1 的文本内容
            binding.qDragText.tvOption2.text = ""
            binding.qDragText.tvOption3.text = ""

            binding.qDragText.tvOption1.background = originalBackgroundOption1
            binding.qDragText.tvOption2.background = originalBackgroundOption2
            binding.qDragText.tvOption3.background = originalBackgroundOption3
            resetViews()
        }

        //speech

        binding.qSpeech.btnSpeech.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_MICROPHONE)
            } else {
                // 啟动SpeechActivity
                startRecording() // 進行錄音
                binding.qSpeech.btnSpeech.setBackgroundResource(R.drawable.btn_background_yellow)
            }
        }
        binding.qSpeech.btnStop.setOnClickListener {
            stopRecording()   //停止錄音
            binding.qSpeech.btnSpeech.setBackgroundResource(R.drawable.btn_background_teal)
        }

        binding.qSpeechImage.btnSpeech1.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_MICROPHONE)
            } else {
                // 啟动SpeechActivity
                startRecording() // 進行錄音
                binding.qSpeechImage.btnSpeech1.setBackgroundResource(R.drawable.btn_background_yellow)
            }
        }
        binding.qSpeechImage.btnStop1.setOnClickListener {
            stopRecording()   //停止錄音
            binding.qSpeechImage.btnSpeech1.setBackgroundResource(R.drawable.btn_background_teal)
        }

        binding.qDescribeImage.btnSpeech2.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_MICROPHONE)
            } else {
                // 啟动SpeechActivity
                startRecording() // 進行錄音
                binding.qDescribeImage.btnSpeech2.setBackgroundResource(R.drawable.btn_background_yellow)
            }
        }
        binding.qDescribeImage.btnStop2.setOnClickListener {
            stopRecording()   //停止錄音
            binding.qDescribeImage.btnSpeech2.setBackgroundResource(R.drawable.btn_background_teal)
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
    private fun speakText(textToSpeak: String) {
        val speechParams = HashMap<String, String>()
        speechParams[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "option"

        // 使用TTS引擎朗讀文本
        textToSpeech?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, speechParams)
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
                    val followText = binding.qSpeech.tvText1.text
                    val followSpeechParams = HashMap<String, String>()
                    followSpeechParams[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "followText"
                    textToSpeech?.speak(followText.toString(), TextToSpeech.QUEUE_FLUSH, followSpeechParams)

                    binding.qSpeech.tvImage1.text= currQuestion?.get("題目")?.toString() ?: ""
                   val question= binding.qSpeech.tvImage1.text
                            val speechParams = HashMap<String, String>()
                            speechParams[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = " question"
                            // 使用 TextToSpeech
                            textToSpeech?.speak(question.toString(), TextToSpeech.QUEUE_FLUSH, speechParams)

                    binding.qSpeech.tvImage1.setOnClickListener {
                        val question= binding.qSpeech.tvImage1.text
                        val speechParams = HashMap<String, String>()
                        speechParams[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = " question"
                        // 使用 TextToSpeech
                        textToSpeech?.speak(question.toString(), TextToSpeech.QUEUE_FLUSH, speechParams)
                            }
                        }

                "簡單應答" -> {
                    LoadImage(currQuestion?.get("圖片1")?.toString()) { drawable ->
                        binding.qSpeechImage.tvImage2.setImageDrawable(drawable)

                        val tvquestion= binding.qSpeechImage.tvText4.text
                        val speechParams = HashMap<String, String>()
                        speechParams[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "tvquestion"
                        // 使用 TextToSpeech
                        textToSpeech?.speak(tvquestion.toString(), TextToSpeech.QUEUE_FLUSH, speechParams)
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
                    val tv_question= binding.qChooseImage.tvText2.text
                    val speechParams = HashMap<String, String>()
                    speechParams[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "tv_question"
                    // 使用 TextToSpeech
                    textToSpeech?.speak(tv_question.toString(), TextToSpeech.QUEUE_FLUSH, speechParams)
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
                    }/*
                    binding.qDragText.tvOption4.setOnTouchListener(DragTouchListener())
                    binding.qDragText.tvOption5.setOnTouchListener(DragTouchListener())
                    binding.qDragText.tvOption6.setOnTouchListener(DragTouchListener())*/

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
                    // 设置拖動源的 OnTouchListener
                    val draggableViews = listOf(
                        binding.qDragText.tvOption4,
                        binding.qDragText.tvOption5,
                        binding.qDragText.tvOption6
                    )

                    for (view in draggableViews) {
                        view.setOnTouchListener(DragTouchListener())
                    }

                    // 设置拖放目標的 OnDragListener
                    val targetViews = listOf(
                        binding.qDragText.tvOption1,
                        binding.qDragText.tvOption2,
                        binding.qDragText.tvOption3
                    )

                    for (view in targetViews) {
                        view.setOnDragListener(DragListener())
                    }
                    binding.qDragText.tvImage8.setOnClickListener {
                        showHint()
                    }
                    binding.qDragText.tvImage9.setOnClickListener {
                        showHint()
                    }
                    binding.qDragText.tvImage10.setOnClickListener {
                        showHint()
                    }
                    binding.qDragText.tvOption1.setOnClickListener {
                        val question11= binding.qDragText.tvOption1.text
                        val speechParams = HashMap<String, String>()
                        speechParams[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "question11"
                        textToSpeech?.speak(question11.toString(), TextToSpeech.QUEUE_FLUSH, speechParams)
                    }
                    binding.qDragText.tvOption2.setOnClickListener {
                        val question22= binding.qDragText.tvOption2.text
                        val speechParams = HashMap<String, String>()
                        speechParams[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "question22"
                        textToSpeech?.speak(question22.toString(), TextToSpeech.QUEUE_FLUSH, speechParams)
                    }
                    binding.qDragText.tvOption3.setOnClickListener {
                        val question33= binding.qDragText.tvOption3.text
                        val speechParams = HashMap<String, String>()
                        speechParams[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "question33"
                        textToSpeech?.speak(question33.toString(), TextToSpeech.QUEUE_FLUSH, speechParams)
                    }
                    binding.qDragText.tvOption4.setOnClickListener {
                        val question1= binding.qDragText.tvOption4.text
                        val speechParams = HashMap<String, String>()
                        speechParams[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "question1"
                        textToSpeech?.speak(question1.toString(), TextToSpeech.QUEUE_FLUSH, speechParams)
                    }
                    binding.qDragText.tvOption5.setOnClickListener {
                        val question2= binding.qDragText.tvOption5.text
                        val speechParams = HashMap<String, String>()
                        speechParams[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "question2"
                        textToSpeech?.speak(question2.toString(), TextToSpeech.QUEUE_FLUSH, speechParams)
                    }
                    binding.qDragText.tvOption6.setOnClickListener {
                        val question3= binding.qDragText.tvOption6.text
                        val speechParams = HashMap<String, String>()
                        speechParams[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "question3"
                        textToSpeech?.speak(question3.toString(), TextToSpeech.QUEUE_FLUSH, speechParams)
                    }
                }
                "口語描述" -> {
                    LoadImage(currQuestion?.get("圖片1")?.toString()) { drawable ->
                        binding.qDescribeImage.tvImage3.setImageDrawable(drawable)
                    }
                    binding.qDescribeImage.tvText3.text = currQuestion?.get("題目")?.toString() ?: ""
                    val questions= binding.qDescribeImage.tvText3.text
                    val speechParams = HashMap<String, String>()
                    speechParams[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "questions"
                    textToSpeech?.speak(questions.toString(), TextToSpeech.QUEUE_FLUSH, speechParams)
                }
                "詞語表達" -> {
                    binding.qChooseSentence.tvOptionOne.text = currQuestion?.get("選項1")?.toString() ?: ""
                    binding.qChooseSentence.tvOptionTwo.text = currQuestion?.get("選項2")?.toString() ?: ""
                    binding.qChooseSentence.tvOptionThree.text = currQuestion?.get("選項3")?.toString() ?: ""

                    val tvquestions= binding.qChooseSentence.tvQuestion.text
                    val speechParams = HashMap<String, String>()
                    speechParams[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "tvquestions"
                    textToSpeech?.speak(tvquestions.toString(), TextToSpeech.QUEUE_FLUSH, speechParams)

                    /*
                    binding.qChooseSentence.tvOptionOne.setOnClickListener {
                        val questionOne=binding.qChooseSentence.tvOptionOne.text
                        val speechParams = HashMap<String, String>()
                        speechParams[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "questionOne"
                        textToSpeech?.speak(questionOne.toString(), TextToSpeech.QUEUE_FLUSH, speechParams)
                    }
                    binding.qChooseSentence.tvOptionTwo.setOnClickListener {
                        val questionTwo=binding.qChooseSentence.tvOptionTwo.text
                        val speechParams = HashMap<String, String>()
                        speechParams[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "questionTwo"
                        textToSpeech?.speak(questionTwo.toString(), TextToSpeech.QUEUE_FLUSH, speechParams)
                    }

                    binding.qChooseSentence.tvOptionThree.setOnClickListener {
                        val questionThree=binding.qChooseSentence.tvOptionThree.text
                        val speechParams = HashMap<String, String>()
                        speechParams[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "questionThree"
                        textToSpeech?.speak(questionThree.toString(), TextToSpeech.QUEUE_FLUSH, speechParams)
                    }*/

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
    //-------------------------------------speech recording--------------------------------------
    private fun startRecording() {
        try {
            recorder = MediaRecorder()
            recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            val audioFile = getRecordingFile(questionNumberStr)
            recorder?.setOutputFile(audioFile.absolutePath)

            recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder?.prepare()
            recorder?.start()

            Toast.makeText(this, "開始錄音嘍~", Toast.LENGTH_LONG).show()
        }
        catch(e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        Log.d("MyApp", "stopRecording called")
        recorder?.stop()
        recorder?.reset()
        recorder?.release()
        recorder = null

        runOnUiThread {
            Toast.makeText(this, "錄音停止嘍~", Toast.LENGTH_LONG).show()
        }

        val username = intent.getStringExtra("username") ?: ""
        val dateTime = DateTime

        val filePath = "recording/$username/$dateTime/${getRecordingFileName(questionNumberStr)}"

        // 停止錄音後，將音檔傳到server
        GlobalScope.launch(Dispatchers.IO) {
            sendAudioToServer(dateTime)
        }

        // 將錄音音檔上傳至 Firebase Storage
        val storageRef = storageReference.child(filePath)
        val audioFile = getRecordingFile(questionNumberStr)
        val fileUri = Uri.fromFile(audioFile)

        Log.d("FileUriDebug", "File Uri: $fileUri")

        storageRef.putFile(fileUri)
            .addOnSuccessListener {
                // 上傳成功
                Log.d("UploadDebug", "File uploaded successfully")
            }
            .addOnFailureListener { exception ->
                // 上傳失敗，處理錯誤
                Log.e("UploadError", "File upload failed: ${exception.message}")
            }
    }

    private fun getRecordingFileName(questionNumber: String): String {
        return "${questionNumber}.mp3"
    }

    private fun getRecordingFile(questionNumber: String): File  {
        val contextWrapper = ContextWrapper(applicationContext)
        val musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val fileName = getRecordingFileName(questionNumber)
        return File(musicDirectory, fileName)
    }

    // 發送錄音檔案到後端server
    private fun sendAudioToServer(dateTime: String) {
        thread {
            val socket = Socket("163.13.201.83", 3000)
            val outputStream = socket.getOutputStream()

            val username = intent.getStringExtra("username") ?: ""
            val filename =getRecordingFileName(questionNumberStr)
            val infoString = "$username/$dateTime/$filename"
            outputStream.write(infoString.toByteArray(Charsets.UTF_8))

            val audioFile = getRecordingFile(questionNumberStr)
            val audioFileStream = FileInputStream(audioFile)
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (audioFileStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            audioFileStream.close()
            outputStream.flush()

            socket.close()
            Thread.sleep(1000)
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

        binding.qDragText.tvOption1.setBackgroundResource(R.drawable.text_drag_bg)
        binding.qDragText.tvOption2.setBackgroundResource(R.drawable.text_drag_bg)
        binding.qDragText.tvOption3.setBackgroundResource(R.drawable.text_drag_bg)

        binding.qDragText.tvOption4.isSelected = false
        binding.qDragText.tvOption5.isSelected = false
        binding.qDragText.tvOption6.isSelected = false

    }


    private inner class DragTouchListener : View.OnTouchListener {
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val clipData = ClipData.newPlainText("", "")
                    val dragShadowBuilder = View.DragShadowBuilder(view)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        view.startDragAndDrop(clipData, dragShadowBuilder, view, 0)
                    } else {
                        view.startDrag(clipData, dragShadowBuilder, view, 0)
                    }
                    view.performClick()
                    return true
                }
                else -> return false
            }
        }
    }
    private var originalBackground: Drawable? = null
    private inner class DragListener : View.OnDragListener {
        override fun onDrag(v: View, event: DragEvent): Boolean {
            when (event.action) {
                //拖动开始时
                DragEvent.ACTION_DRAG_STARTED -> {
                    if (event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                        // 保存原始背景
                        originalBackground = v.background
                        return true
                    }
                    return false
                }
                //拖动的View进入监听的View时
                DragEvent.ACTION_DRAG_ENTERED -> {
                    v.setBackgroundResource(R.drawable.green_border)
                    return true
                }
                //拖动的View离开在监听的View中时
                DragEvent.ACTION_DRAG_EXITED -> {
                    v.background = originalBackground
                    return true
                }
                //拖动放下时
                DragEvent.ACTION_DROP -> {
                    val draggedView = event.localState as TextView
                    val targetView = v as TextView
                    val droppedText = draggedView.text.toString()

                    val isTextAlreadyExist =
                        binding.qDragText.tvOption1.text.toString() == droppedText ||
                                binding.qDragText.tvOption2.text.toString() == droppedText ||
                                binding.qDragText.tvOption3.text.toString() == droppedText

                    if (!isTextAlreadyExist) {
                        targetView.text = droppedText
                        v.background = originalBackground

                        // 使用TTS引擎朗讀文本
                        textToSpeech?.speak(droppedText, TextToSpeech.QUEUE_FLUSH, null, null)

                        /*// 更新 tvOption4, tvOption5, tvOption6 的背景颜色
                        binding.qDragText.tvOption4.setBackgroundResource(R.drawable.text_drag_bgblack)
                        binding.qDragText.tvOption5.setBackgroundResource(R.drawable.text_drag_bgblack)
                        binding.qDragText.tvOption6.setBackgroundResource(R.drawable.text_drag_bgblack)*/
                        return true
                    } else {
                        // 文本重復，不允许拖放
                        return true
                    }
                }
                //拖动结束时
                DragEvent.ACTION_DRAG_ENDED -> {
                    return true
                }
                else -> return false
            }
        }
    }

    private fun resetViews() {
        binding.qDragText.tvOption1.background = originalBackground
        binding.qDragText.tvOption2.background = originalBackground
        binding.qDragText.tvOption3.background = originalBackground
        binding.qDragText.tvOption1.isSelected = false
        binding.qDragText.tvOption2.isSelected = false
        binding.qDragText.tvOption3.isSelected = false
        // 可以添加其他重置邏輯
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
            Toast.makeText(this@QuestionOverviewActivity, "沒有提示囉~", Toast.LENGTH_LONG).show()
            speakText("沒有提示囉~")
        } else {
            val hintToShow = hints.removeAt(0)
            score--
            runOnUiThread {
                Toast.makeText(this@QuestionOverviewActivity, hintToShow, Toast.LENGTH_LONG).show()
                speakText(hintToShow)
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
                //val userAnswer = binding.qSpeechImage.editWord.text.toString().trim()
                // if(Ans != userAnswer){ score-- }
                val recordData = recordList.lastOrNull {it.type == type }
                //if (recordData != null) { recordData.userAnswer = userAnswer }
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
                var userAnswer = ""
                val selectedOption = when {
                    binding.qDragText.tvOption4.isSelected -> binding.qDragText.tvOption4.text.toString()
                    binding.qDragText.tvOption5.isSelected -> binding.qDragText.tvOption5.text.toString()
                    binding.qDragText.tvOption6.isSelected -> binding.qDragText.tvOption6.text.toString()
                    else -> ""
                }

                if (selectedOption.isNotEmpty()) {
                    // 如果有選项被拖放到了 tvOption1、tvOption2、tvOption3 中
                    if (binding.qDragText.tvOption1.text.isEmpty()) {
                        binding.qDragText.tvOption1.text = selectedOption
                        userAnswer = binding.qDragText.tvOption1.text.toString() // 记錄用户答案到 userAnswer
                    } else if (binding.qDragText.tvOption2.text.isEmpty()) {
                        binding.qDragText.tvOption2.text = selectedOption
                        userAnswer = binding.qDragText.tvOption2.text.toString() // 记錄用户答案到 userAnswer
                    } else if (binding.qDragText.tvOption3.text.isEmpty()) {
                        binding.qDragText.tvOption3.text = selectedOption
                        userAnswer = binding.qDragText.tvOption3.text.toString() // 记錄用户答案到 userAnswer
                    }
                }

                Log.d("isAnsCorrect", "selectedOption: $selectedOption")

                if (selectedOption != Ans) { score-- }

                val recordData = recordList.lastOrNull { it.type == type }
                if (recordData != null) {
                    recordData.userAnswer = userAnswer
                }
            }


            "口語描述" -> {
                // val userAnswer = binding.qDescribeImage.editWord2.text.toString().trim()
                //if(Ans != userAnswer){ score-- }
                val recordData = recordList.lastOrNull {it.type == type }
                // if (recordData != null) { recordData.userAnswer = userAnswer }
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
                binding.qChooseSentence.tvOptionOne.setOnClickListener { binding.next.performClick()  }
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
    // val 評語: String = "",
    val audioFileName: String = ""
)
