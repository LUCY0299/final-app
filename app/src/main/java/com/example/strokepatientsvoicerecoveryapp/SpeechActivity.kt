package com.example.strokepatientsvoicerecoveryapp


import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.ContextWrapper
import android.media.MediaRecorder
import android.widget.TextView

import java.io.File

import android.os.Environment
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class SpeechActivity: AppCompatActivity() {
    private lateinit var btn_next1: Button
    private val SPEECH_REQUEST_CODE = 0
    private val REQUEST_MICROPHONE = 1
    private var recorder: MediaRecorder? = null
    private lateinit var textView: TextView
    private var fileNumber = 1  // 記錄檔案數量的變數
    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.speech)

        textView = findViewById(R.id.text_view)
        btn_next1 = findViewById(R.id.btn_speech)
        btn_next1.setOnClickListener {
            // 检查是否已经获得了麦克风权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_MICROPHONE)
            } else {
                displaySpeechRecognizer() // 执行辨識
            }
        }
    }

    private fun displaySpeechRecognizer() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
        }
        startActivityForResult(intent, SPEECH_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val spokenText: String? =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).let { results ->
                    results?.get(0)
                }
            handleSpeechResult(spokenText)
        }
    }

    private fun handleSpeechResult(spokenText: String?) {
        if (spokenText != null && spokenText.isNotEmpty()) {
            textView.text = spokenText
            startRecording()
        } else {
            textView.text = "錄音失敗或無辨識結果"
            // 可以在這裡處理錄音失敗的情況，例如顯示錯誤訊息給使用者
        }
    }

    private fun startRecording() {
        isRecording = true
        // 初始化 MediaRecorder 并开始录音
        recorder = MediaRecorder()
        recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        //recorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        recorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

        recorder?.setOutputFile(getRecordingFilePath())
        recorder?.prepare()
        recorder?.start()


    }

    private fun stopRecording() {
        // 录音结束后释放 MediaRecorder 资源
        recorder?.stop()
        recorder?.reset()
        recorder?.release()
        recorder = null

    }

    private fun getRecordingFilePath(): String {
        val contextWrapper = ContextWrapper(applicationContext)
        val musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val audioFileName = "_$fileNumber.mp3"
        //val audioFileName = "_$fileNumber.3gp"
        fileNumber++  // 檔案數量自增1
        val file = File(musicDirectory, audioFileName)
        return file.path
    }
    override fun onDestroy() {
        super.onDestroy()
        if (isRecording) {
            recorder?.apply {
                stop()
                reset()
                release()
            }
            recorder = null
            isRecording = false
        }
    }
}


////speech recognition

// 語音辨識按鈕
//private lateinit var textView: TextView
//private lateinit var btnNext1: Button
//private val SPEECH_REQUEST_CODE = 0
//private val REQUEST_MICROPHONE = 1
//private var recorder: MediaRecorder? = null
//private var fileNumber = 1

//speech recognition & record
//textView = findViewById(R.id.text_view)
//btnNext1 = findViewById(R.id.btn_speech)
//btnNext1.setOnClickListener {
//    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_MICROPHONE)
//    } else {
//        displaySpeechRecognizer() // 执行辨識
//    }
//}
//    private fun displaySpeechRecognizer() {
//        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
//            putExtra(
//                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
//            )
//        }
//        startActivityForResult(intent, SPEECH_REQUEST_CODE)
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            val spokenText: String? =
//                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).let { results ->
//                    results?.get(0)
//                }
//            if (spokenText != null && spokenText.isNotEmpty()) {
//                textView.text = spokenText
//
//                // 初始化 MediaRecorder 并开始录音
//                recorder = MediaRecorder()
//                recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
//                recorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
//                //recorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4) // 更改此行
//                recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
//
//                // 檔案名稱設置為數字
//                val audioFileName = "_$fileNumber.3gp"
//                fileNumber++  // 檔案數量自增1
//                //val audioFileName = "AUDIO_$timeStamp.mp3"
//                val audioFile = File(getExternalFilesDir(null), audioFileName)
//                recorder?.setOutputFile(audioFile.absolutePath)
//                recorder?.prepare()
//                recorder?.start()
//
//                // 在新线程中执行 Socket 连接和请求发送操作
//                thread {
//                    val socket = Socket("192.168.2.100", 3000)
//                    val outputStream = socket.getOutputStream()
//                    val inputStream = socket.getInputStream()
//
//                    val filenameBytes = audioFileName.toByteArray(Charsets.UTF_8)
//                    outputStream.write(filenameBytes)
//
//                    val fileInputStream = FileInputStream(audioFile)
//                    val buffer = ByteArray(1024)
//                    var bytesRead: Int
//                    while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
//                        outputStream.write(buffer, 0, bytesRead)
//                    }
//                    fileInputStream.close()
//
//                    val response = ByteArray(1024)
//                    val responseBytesRead = inputStream.read(response)
//                    val responseData = String(response, 0, responseBytesRead)
//
//                    socket.close()
//
//                    // 添加延迟等待文件写入完成
//                    Thread.sleep(1000) // 可根据需要调整延迟时间
//
//                    // 录音结束后释放 MediaRecorder 资源
//                    recorder?.stop()
//                    recorder?.reset()
//                    recorder?.release()
//                    recorder = null
//                }
//            }else {
//                textView.text = "錄音失敗或無辨識結果"
//            }
//        }
//    }
