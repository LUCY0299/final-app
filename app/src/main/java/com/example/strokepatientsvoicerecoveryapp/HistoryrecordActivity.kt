package com.example.strokepatientsvoicerecoveryapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.strokepatientsvoicerecoveryapp.databinding.HistoryRecordDetailBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HistoryrecordActivity : AppCompatActivity() {

    private lateinit var binding: HistoryRecordDetailBinding
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HistoryRecordDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        username = intent.getStringExtra("username") ?: ""

        val recyclerView = binding.recyclerView
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        val recordList = mutableListOf<RecordItem>()

        val database = Firebase.database
        val reference = database.getReference("紀錄").child(username)

        val onItemClickListener = object : RecordAdapter.OnItemClickListener {
            override fun onItemClick(recordItem: RecordItem) {
                val intent = Intent(this@HistoryrecordActivity, QuestiondetailActivity::class.java)
                startActivity(intent)
            }
        }

        reference.orderByValue().equalTo(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                recordList.clear() // 清空原有資料

                for (childSnapshot in snapshot.children) {
                    val date = childSnapshot.key.toString() // 日期時間的值就是childSnapshot的key值
                    val practiceTime = childSnapshot.child("10").child("時間").value.toString() // 取得練習時間
                    val questionId = childSnapshot.child("10").child("1").child("題號").value.toString() // 取得題目編號

                    // 注意這裡要修改你的Firebase結構，讓程式能夠正確取得題目難度和結果編號
                    val resultId = childSnapshot.child("10").child("結果編號").value?.toString() ?: "#" // 如果結果編號不存在，就設為#
                    val questionDegree = childSnapshot.child("10").child("題目難度").value?.toString() ?: "#" // 如果題目難度不存在，就設為#

                    val recordItem = RecordItem(questionId, resultId, date, practiceTime, questionDegree)
                    recordList.add(recordItem)
                }

                // 將資料填入RecyclerView的Adapter中
                val adapter = RecordAdapter(recordList, onItemClickListener)
                recyclerView.adapter = adapter

//                // 取得最新的資料
//                val latestRecordItem = recordList.firstOrNull()
//                latestRecordItem?.let {
//                    // 將值設置到對應的 TextView 中
//                    binding.dateNumber.text = it.date
//                    binding.timeTextView.text = it.practiceTime
//                }
            }


            override fun onCancelled(error: DatabaseError) {
                // 處理取消讀取資料的情況
            }
        })


    }
}

data class RecordItem(
    val questionId: String,
    val resultId: String,
    val date: String,
    val practiceTime: String,
    val questionDegree: String
)

class RecordAdapter(private val recordList: List<RecordItem>, private val onItemClickListener: OnItemClickListener) : RecyclerView.Adapter<RecordAdapter.RecordViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(recordItem: RecordItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.history_rec_recycler, parent, false)
        return RecordViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val currentItem = recordList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount() = recordList.size

    inner class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val numberIdTextView: TextView = itemView.findViewById(R.id.number_id)
        private val number1IdTextView: TextView = itemView.findViewById(R.id.number1_id)
        private val dateNumberTextView: TextView = itemView.findViewById(R.id.datenumber)
        private val timeTextView: TextView = itemView.findViewById(R.id.time)
        private val degreeTextView: TextView = itemView.findViewById(R.id.degree)
        private val btnSee: Button = itemView.findViewById(R.id.btn_see)

        fun bind(recordItem: RecordItem) {
            numberIdTextView.text = recordItem.questionId
            number1IdTextView.text = recordItem.resultId

            // 將值設置到對應的 TextView 中
            dateNumberTextView.text = recordItem.date
            timeTextView.text = recordItem.practiceTime

            degreeTextView.text = recordItem.questionDegree

            // 設置 btnSee 的點擊監聽器
            btnSee.setOnClickListener {
                onItemClickListener.onItemClick(recordItem)
            }
        }
    }
}