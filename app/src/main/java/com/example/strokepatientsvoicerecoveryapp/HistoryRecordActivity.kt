package com.example.strokepatientsvoicerecoveryapp

import android.os.Bundle
import android.util.Log
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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HistoryRecordActivity : AppCompatActivity() {

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

        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("紀錄").child(username)

        val onItemClickListener = object : RecordAdapter.OnItemClickListener {
            override fun onItemClick(recordItem: RecordItem) {
                // Handle item click
            }
        }

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                recordList.clear()

                // 直接在snapshot中找到指定使用者的資料
                val userSnapshot = snapshot.child(username)
                for (dateSnapshot in userSnapshot.children) {
                    for (dataSnapshot in dateSnapshot.children) {
                        val dateTime = dataSnapshot.child("日期時間").getValue(String::class.java) ?: ""
                        val practiceTime = dataSnapshot.child("練習時間").getValue(String::class.java) ?: ""
                        val questionType = dataSnapshot.child("選擇類型").getValue(String::class.java) ?: ""
                        val questionDegree = dataSnapshot.child("難度").getValue(String::class.java) ?: ""

                        val recordItem = RecordItem(dateTime, practiceTime, questionType, questionDegree)
                        recordList.add(recordItem)
                    }
                }

                val adapter = RecordAdapter(recordList, onItemClickListener)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}

data class RecordItem(
    val date: String,
    val practiceTime: String,
    val questionType: String,
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

        private val dateNumberTextView: TextView = itemView.findViewById(R.id.datenumber)
        private val timeTextView: TextView = itemView.findViewById(R.id.time)
        private val degreeTextView: TextView = itemView.findViewById(R.id.degree)
        private val questionTypeTextView: TextView = itemView.findViewById(R.id.questionType)
        private val btnSee: Button = itemView.findViewById(R.id.btn_see)

        fun bind(recordItem: RecordItem) {
            dateNumberTextView.text = recordItem.date
            timeTextView.text = recordItem.practiceTime
            degreeTextView.text = recordItem.questionDegree
            questionTypeTextView.text = recordItem.questionType

            btnSee.setOnClickListener {
                onItemClickListener.onItemClick(recordItem)
            }

            Log.d("RecyclerViewDebug", "Binding record item: $recordItem")
        }
    }
}