package com.example.strokepatientsvoicerecoveryapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.strokepatientsvoicerecoveryapp.databinding.BasicInformationBinding
import com.google.firebase.database.*


class BasicinformationActivity : AppCompatActivity() {

    private lateinit var binding : BasicInformationBinding
    private lateinit var dbRef : DatabaseReference
    private lateinit var checkUserDatabase: Query

    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BasicInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getPassedUser()

        //當按下送出鍵就會跳轉到主題選擇畫面
        binding.send.setOnClickListener {
            saveUserData()

            val intent = Intent(this,MainoptionActivity::class.java);
            startActivity(intent)
        }
    }

    // ========================function======================================
    // 上一個頁面傳過來的帳號
    // 現在登入的帳號
    private fun getPassedUser() {
        val intent = getIntent()
        username = intent.getStringExtra("username") ?: ""

        dbRef = FirebaseDatabase.getInstance().getReference("Users")
        checkUserDatabase = dbRef.orderByChild("username").equalTo(username)

        checkUserDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val nameFromDB = snapshot.child("username").child("name").getValue(String::class.java)
                    // 在這裡處理從數據庫中獲取的名稱 nameFromDB
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // 如果需要，處理 onCancelled 事件
            }
        })
        // 測試用 把名字改成username
        // binding.editAge.setText(username)
    }

    // 儲存帳號資料到FireBase
    private fun saveUserData() {
        val realName = binding.textUsername.editText?.text.toString()
        val age = binding.textAge.editText?.text.toString()
        val address = binding.textaddr.editText?.text.toString()
        val member = binding.textmember.editText?.text.toString()
        val memberName = binding.textmembername.editText?.text.toString()

        val spSelection = binding.sp.selectedItem.toString()
        val sp1Selection = binding.sp1.selectedItem.toString()

        if(realName.isEmpty()) {
            binding.textUsername.error = "記得填寫你的真實名稱"
            Toast.makeText(this, "記得填寫你的真實名稱", Toast.LENGTH_SHORT).show()
        }
        else if(age.isEmpty()) {
            binding.textAge.error = "記得填寫你的年齡"
            Toast.makeText(this, "記得填寫你的年齡", Toast.LENGTH_SHORT).show()
        }
        else if(address.isEmpty()) {
            binding.textaddr.error = "記得填寫你的地址"
            Toast.makeText(this, "記得填寫你的地址", Toast.LENGTH_SHORT).show()
        }
        else if(member.isEmpty()) {
            binding.textmember.error = "記得填寫你的家庭成員稱謂"
            Toast.makeText(this, "記得填寫你的家庭成員稱謂", Toast.LENGTH_SHORT).show()
        }
        else if(memberName.isEmpty()) {
            binding.textmembername.error = "記得填寫你的家庭成員姓名"
            Toast.makeText(this, "記得填寫你的家庭成員姓名", Toast.LENGTH_SHORT).show()
        }
        else if(spSelection.isEmpty()) {
            binding.sp.requestFocus()
            Toast.makeText(this, "請選擇一個選項", Toast.LENGTH_SHORT).show()
        }
        else if(sp1Selection.isEmpty()) {
            binding.sp1.requestFocus()
            Toast.makeText(this, "請選擇一個選項", Toast.LENGTH_SHORT).show()
        }
        else{
            val userData = HashMap<String, Any>()
            userData["realName"] = realName
            userData["age"] = age
            userData["address"] = address
            userData["member"] = member
            userData["memberName"] = memberName
            userData["spSelection"] = spSelection
            userData["sp1Selection"] = sp1Selection

            val dbRef = FirebaseDatabase.getInstance().getReference("Users")
            val userRef = dbRef.child("user") // 這裡使用特定的用戶名
            userRef.updateChildren(userData)
                .addOnSuccessListener {
                    // 更新成功
                    Toast.makeText(this@BasicinformationActivity, "資料保存成功", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    // 更新失败
                    Toast.makeText(this@BasicinformationActivity, "資料保存失敗，請再試一次", Toast.LENGTH_SHORT).show()
                }
            passUserData(username)
        }
    }


    // 帳號資料傳到下一頁
    private fun passUserData(username: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Users")
        val query = dbRef.orderByChild("username").equalTo(username)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val nameFromDB = snapshot.child(username).child("name").getValue(String::class.java)
                    val intent = Intent(this@BasicinformationActivity, itemPracriceActivity::class.java)
                    intent.putExtra("name", nameFromDB)
                    intent.putExtra("username", username)
                    startActivity(intent)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // 如果需要，處理 onCancelled 事件
            }
        })
    }
}
