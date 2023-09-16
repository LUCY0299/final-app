package com.example.strokepatientsvoicerecoveryapp

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.strokepatientsvoicerecoveryapp.databinding.QuestionDetailBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.InputStream
import java.net.URL

class QuestiondetailActivity : AppCompatActivity() {

    private lateinit var binding: QuestionDetailBinding
    private lateinit var username: String
    private lateinit var dateTime: String
    private lateinit var practiceTime: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = QuestionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        username = intent.getStringExtra("username") ?: ""
        dateTime = intent.getStringExtra("dateTime") ?: ""
        practiceTime = intent.getStringExtra("practiceTime") ?: ""



        // 设置RecyclerView
        val recyclerView = binding.recyclerView
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        val recordItemList = mutableListOf<RecordItemClass>()

        val adapter = DetailRecordAdapter(recordItemList) { recordItem ->
        }

        recyclerView.adapter = adapter

        val database = FirebaseDatabase.getInstance()

        val reference = database.getReference("紀錄").child(username).child(dateTime).child(practiceTime)

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                recordItemList.clear()

                for (dataSnapshot in snapshot.children) {
                    val questionNum = dataSnapshot.key // 子節點的名稱就是questionNum
                    val correctAnswer = dataSnapshot.child("correctAnswer").getValue(String::class.java) ?: ""
                    val imageUrl = dataSnapshot.child("imageUrl").getValue(String::class.java) ?: ""
                    val question = dataSnapshot.child("question").getValue(String::class.java) ?: ""
                    val type = dataSnapshot.child("type").getValue(String::class.java) ?: ""
                    val userAnswer = dataSnapshot.child("userAnswer").getValue(String::class.java) ?: ""

                    val recordItem = RecordItemClass(questionNum ?: "", correctAnswer, imageUrl, question, type, userAnswer)
                    recordItemList.add(recordItem)
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}

data class RecordItemClass(
    val questionNum: String,
    val correctAnswer: String,
    val imageUrl: String,
    val question: String,
    val type: String,
    val userAnswer: String
)

class DetailRecordAdapter(
    private val DetailrecordList: List<RecordItemClass>,
    private val onItemClickListener: (RecordItemClass) -> Unit
    ) : RecyclerView.Adapter<DetailRecordAdapter.RecordViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.question_detail_rec, parent, false)
        return RecordViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val currentItem = DetailrecordList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount() = DetailrecordList.size

    inner class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questionNumTextView: TextView = itemView.findViewById(R.id.question_num)
        private val correctAnswerTextView: TextView = itemView.findViewById(R.id.correctAnswer)
        private val imageUrlImageView: ImageView = itemView.findViewById(R.id.imageUrl)
        private val questionTextView: TextView = itemView.findViewById(R.id.question)
        private val typeTextView: TextView = itemView.findViewById(R.id.type)
        private val userAnswerTextView: TextView = itemView.findViewById(R.id.userAnswer)

        fun bind(recordItem: RecordItemClass) {
            // Set the question number
            questionNumTextView.text = (adapterPosition + 1).toString()

            // Set other data from recordItem
            correctAnswerTextView.text = recordItem.correctAnswer
            questionTextView.text = recordItem.question
            typeTextView.text = recordItem.type

            // Load image into the imageView using Glide or Picasso library
            if (recordItem.imageUrl.isNotEmpty()) {
                LoadImage(recordItem.imageUrl) { drawable ->
                    imageUrlImageView.post {
                        imageUrlImageView.setImageDrawable(drawable)
                    }
                }
            }
            if(recordItem.userAnswer.endsWith(".jpg")){
                LoadImage(recordItem.userAnswer) { drawable ->
                    imageUrlImageView.post {
                        imageUrlImageView.setImageDrawable(drawable)
                    }
                }
            }else{
                userAnswerTextView.text = recordItem.userAnswer
            }


            itemView.setOnClickListener {
                onItemClickListener(recordItem)
            }
        }
    }

    private fun LoadImage(url: String?, callback: (Drawable?) -> Unit) {
        Thread {
            try {
                val `is`: InputStream = URL(url).content as InputStream
                val drawable = Drawable.createFromStream(`is`, "src name")
                callback(drawable)
            } catch (e: Exception) {
                callback(null)
            }
        }.start()
    }
}
