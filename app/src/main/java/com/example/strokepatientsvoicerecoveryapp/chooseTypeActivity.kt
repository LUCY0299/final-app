package com.example.strokepatientsvoicerecoveryapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class chooseTypeActivity: AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dataList: ArrayList<Dataclass>
    private lateinit var myAdapter: Adapterclass
    lateinit var imageList: Array<Int>
    lateinit var titleList: Array<String>
    private lateinit var username: String
    private lateinit var sp1Selection: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.choose_type)

        username = intent.getStringExtra("username") ?: ""
        sp1Selection = intent.getStringExtra("sp1Selection") ?: ""

        imageList = arrayOf(
            R.drawable.image1,
            R.drawable.image2,
            R.drawable.images3,
            R.drawable.images10,
            R.drawable.images6,
            R.drawable.images7,
            R.drawable.images9,
        ) // Images in order
        titleList = arrayOf(
            "節慶","食物","飲料","衣服","身體部位","心情","住所"
        ) // Titles in order
        recyclerView = findViewById(R.id.rv_Type)
        recyclerView.layoutManager = LinearLayoutManager(this) // Use LayoutManager to specify the arrangement of RecyclerView
        recyclerView.setHasFixedSize(true)

        dataList = arrayListOf<Dataclass>()
        getDate()

        myAdapter = Adapterclass(dataList)

        myAdapter.onItemClick = { data ->
            when (data.dataTitle) {
                "節慶" -> navigateToNextPage(TraditionalActivity::class.java, data.dataTitle)
                "食物" -> navigateToNextPage(FoodActivity::class.java, data.dataTitle)
                "飲料" -> navigateToNextPage(DrinkActivity::class.java, data.dataTitle)
                "衣服" -> navigateToNextPage(ClothesActivity::class.java, data.dataTitle)
                "身體部位" -> navigateToNextPage(BodypartsActivity::class.java, data.dataTitle)
                "心情" -> navigateToNextPage(FeelingActivity::class.java, data.dataTitle)
                "住所" -> navigateToNextPage(ResidenceActivity::class.java, data.dataTitle)
            }
        }
        recyclerView.adapter = myAdapter
    }

    private fun getDate() {
        for (i in imageList.indices) {
            val dataclass = Dataclass(imageList[i], titleList[i])
            dataList.add(dataclass)
        } // Get information of the index, create another Dataclass
    }

    private fun navigateToNextPage(activityClass: Class<*>, selectedTitle: String) {
        val intent = Intent(this, activityClass)
        intent.putExtra("username", username)
        intent.putExtra("sp1Selection", sp1Selection)
        intent.putExtra("selectedTitle", selectedTitle)
        startActivity(intent)
    }
}

