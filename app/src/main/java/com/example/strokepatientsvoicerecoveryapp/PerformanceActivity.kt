package com.example.strokepatientsvoicerecoveryapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.strokepatientsvoicerecoveryapp.databinding.ActivityPerformanceBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.firebase.database.*
import java.text.DecimalFormat

class PerformanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerformanceBinding
    private lateinit var username: String
    private lateinit var lineChart: LineChart
    private lateinit var PieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerformanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lineChart = findViewById(R.id.lineChart)
        PieChart = findViewById(R.id.PieChart)

        username = intent.getStringExtra("username") ?: ""


        //當按下變更密碼鍵就會跳轉到變更密碼畫面
        binding.editPassword.setOnClickListener {
            navigateToNextPage(Editpassword::class.java)
        }
        //當按下編輯資料鍵就會跳轉到編輯資料畫面
        binding.editInfor.setOnClickListener {
            navigateToNextPage(Modify::class.java)
        }
        //當按下歷史紀錄鍵就會跳轉到歷史紀錄畫面
        binding.history.setOnClickListener {
            navigateToNextPage(HistoryRecordActivity::class.java)
        }

        //==================================Firebase==================================
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("Users").child(username)


        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val realnameFromDB = snapshot.child("realName").getValue(String::class.java)
                binding.name1.text = "$realnameFromDB\n$username"
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        //========================================Piechart==================================//

        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()
        val dataSet = PieDataSet(entries,"")
        val pieData = PieData(dataSet)

        entries.add(PieEntry(20F,"理解"))
        entries.add(PieEntry(40F,"重述"))
        entries.add(PieEntry(10F,"生活"))
        entries.add(PieEntry(20F,"食物"))
        entries.add(PieEntry(10F,"流暢"))


        colors.add(resources.getColor(R.color.purple_200))
        colors.add(resources.getColor(R.color.teal_200))
        colors.add(resources.getColor(R.color.pink))
        colors.add(resources.getColor(R.color.green))
        colors.add(resources.getColor(R.color.yellow))


        dataSet.setColors(colors)
        pieData.setDrawValues(true)

        PieChart.setData(pieData)
        PieChart.invalidate()
        pieData.setValueTextSize(15f)
        PieChart.setHoleRadius(0f)


        //=========================================LineChart==========================================//

        // Green Line Data
        val values = ArrayList<Entry>().apply {
            add(Entry(1f, 60.65f))
            add(Entry(2f, 70.69f))
            add(Entry(3f, 66.69f))
            add(Entry(4f, 71.69f))
            add(Entry(5f, 80.58f))
            add(Entry(6f, 90.58f))
        }
        // purple Line  Data
        val values1 = ArrayList<Entry>().apply {
            add(Entry(1f, 66.63f))
            add(Entry(2f, 77.65f))
            add(Entry(3f, 88.66f))
            add(Entry(4f, 60.53f))
            add(Entry(5f, 90.53f))
            add(Entry(6f, 70.00f))
        }

        // Pink Line Data
        val values2 = ArrayList<Entry>().apply {
            add(Entry(1f, 63.63f))
            add(Entry(2f, 75.65f))
            add(Entry(3f, 60.66f))
            add(Entry(4f, 80.53f))
            add(Entry(5f, 90.53f))
            add(Entry(6f, 77.00f))
        }

        // Green Line End Data
        val values_end = ArrayList<Entry>().apply {
            add(Entry(6f, 90.58f))
        }

        // purple Line End Data
        val values1_end = ArrayList<Entry>().apply {
            add(Entry(6f, 70.00f))
        }
        // pink Line End Data
        val values2_end = ArrayList<Entry>().apply {
            add(Entry(6f, 77.00f))
        }

        initDataSet(values, values1,values2 ,values_end, values1_end,values2_end)
        initX()
        initY()
        initChartFormat()
    }


    private fun initDataSet(
        values: ArrayList<Entry>,
        values1: ArrayList<Entry>,
        values2: ArrayList<Entry>,
        values_end: ArrayList<Entry>,
        values1_end: ArrayList<Entry>,
        values2_end: ArrayList<Entry>,
    ) {
        val set: LineDataSet
        val set_end: LineDataSet
        val set2: LineDataSet
        val set2_end: LineDataSet
        val set3: LineDataSet
        val set3_end: LineDataSet


        // Green Line
        set = LineDataSet(values, "重述")
        set.mode = LineDataSet.Mode.LINEAR
        set.color = ContextCompat.getColor(this, R.color.green)
        set.lineWidth = 1.5f
        set.setDrawCircles(true)
        set.setDrawValues(true)
        set.circleRadius = 4f // 设置实心圆的半径
        set.circleHoleRadius = 0f // 设置圆心的空心半径
        set.valueTextSize = 10f
        set.valueFormatter = DefaultValueFormatter(1)
        set.setDrawFilled(true)
        set.highLightColor = Color.RED

        /*  // Green Line End Circle
          set_end = LineDataSet(values_end, "")
          set_end.color = ContextCompat.getColor(this, R.color.green)
          set_end.circleRadius = 4f
          set_end.setDrawCircles(true)
          set.setDrawFilled(true)
          set_end.setDrawValues(true)*/

        // purple Line
        set2 = LineDataSet(values1, "流暢")
        set2.mode = LineDataSet.Mode.LINEAR
        set2.color = ContextCompat.getColor(this, R.color.purple_700)
        set2.lineWidth = 1.5f
        set2.setDrawCircles(true)
        set2.setDrawValues(true)
        set2.circleRadius = 4f // 设置实心圆的半径
        set2.circleHoleRadius = 0f // 设置圆心的空心半径
        set2.valueTextSize = 10f
        set2.valueFormatter = DefaultValueFormatter(1)
        set2.setDrawFilled(true)
        set2.highLightColor = Color.RED

        /*  // purple Line End Circle
          set2_end = LineDataSet(values1_end, "")
          set2_end.color = ContextCompat.getColor(this, R.color.purple_700)
          set2_end.circleRadius = 4f
          set2_end.setDrawCircles(true)
          set2.setDrawFilled(true)
          set2_end.setDrawValues(true)**/

        // pink Line
        set3 = LineDataSet(values2, "理解")
        set3.mode = LineDataSet.Mode.LINEAR
        set3.color = ContextCompat.getColor(this, R.color.pink)
        set3.lineWidth = 1.5f
        set3.setDrawCircles(true)
        set3.setDrawValues(true)
        set3.circleRadius = 4f // 设置实心圆的半径
        set3.circleHoleRadius = 0f // 设置圆心的空心半径
        set3.valueTextSize = 10f
        set3.valueFormatter = DefaultValueFormatter(1)
        set3.setDrawFilled(true)
        set3.highLightColor = Color.RED

        // pink Line End Circle
        /* set3_end = LineDataSet(values2_end, "")
         set3_end.color = ContextCompat.getColor(this, R.color.pink)
         set3_end.circleRadius = 4f
         set3_end.setDrawCircles(true)
         set3.setDrawFilled(true)
         set3_end.setDrawValues(true)*/

        // Similar settings for purpleLine and its end circle

        val dataSets: List<ILineDataSet> = listOf(set, set2,set3)
        val data = LineData(dataSets)
        lineChart.data = data
        lineChart.invalidate()

        binding.btnOption1.setOnClickListener {
            // 设置 LineChart 数据
            setupLineChartData(values,"重述")
            updateChartTitle("重述") // 更新标题为"重述"

        }

        binding.btnOption2.setOnClickListener {
            // 设置 LineChart 数据
            setupLineChartData(values1,"流暢")
            updateChartTitle("流暢") // 更新标题为"流暢"
        }

        binding.btnOption3.setOnClickListener {
            // 设置 LineChart 数据
            setupLineChartData(values2,"理解")
            updateChartTitle("理解") // 更新标题为"理解"
        }

        binding.btnOption4.setOnClickListener {
            // 设置 LineChart 数据
            setupLineChartData(values2,"食物")
            updateChartTitle("食物") // 更新标题为"食物"
        }
        binding.btnOption5.setOnClickListener {
            // 设置 LineChart 数据
            setupLineChartData(values2,"生活")
            updateChartTitle("生活") // 更新标题为"生活"
        }

        binding.btnUpdate.setOnClickListener {
            // 更新 LineChart 数据
            updateChartData()
        }
    }
    //更新圖表
    private fun updateChartData() {
        val values = ArrayList<Entry>().apply {
            add(Entry(1f, 60.65f))
            add(Entry(2f, 70.69f))
            add(Entry(3f, 66.69f))
            add(Entry(4f, 71.69f))
            add(Entry(5f, 80.58f))
            add(Entry(6f, 90.58f))
        }

        val values1 = ArrayList<Entry>().apply {
            add(Entry(1f, 66.63f))
            add(Entry(2f, 77.65f))
            add(Entry(3f, 88.66f))
            add(Entry(4f, 60.53f))
            add(Entry(5f, 90.53f))
            add(Entry(6f, 70.00f))
        }

        // Pink Line Data
        val values2 = ArrayList<Entry>().apply {
            add(Entry(1f, 63.63f))
            add(Entry(2f, 75.65f))
            add(Entry(3f, 60.66f))
            add(Entry(4f, 80.53f))
            add(Entry(5f, 90.53f))
            add(Entry(6f, 77.00f))
        }

        val values_end = ArrayList<Entry>().apply {
            add(Entry(6f, 90.58f))
        }

        val values1_end = ArrayList<Entry>().apply {
            add(Entry(6f, 70.00f))
        }

        val values2_end = ArrayList<Entry>().apply {
            add(Entry(6f, 77.00f))
        }

        initDataSet(values, values1,values2 ,values_end, values1_end,values2_end)
        initX()
        initY()
        initChartFormat()
    }


    //設定X軸
    private fun initX() {
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.GRAY
        xAxis.textSize = 15f
        xAxis.labelCount = 6
        xAxis.labelRotationAngle = -35f // 设置标签旋转角度
        xAxis.spaceMin = 0.5f
        xAxis.spaceMax = 0.5f
        xAxis.setDrawGridLines(false)
        xAxis.setDrawLabels(true);

        val xValue = arrayOf("", "8/3", "8/10", "8/17", "8/24", "8/25", "8/31")
        xAxis.valueFormatter = IndexAxisValueFormatter(xValue.toList())
    }

    //設定Y軸
    private fun initY() {
        val rightAxis = lineChart.axisRight
        rightAxis.isEnabled = false
        val leftAxis = lineChart.axisLeft

        leftAxis.labelCount = 4
        leftAxis.textColor = Color.GRAY
        leftAxis.textSize = 15f
        leftAxis.axisMinimum = 60.0f
        leftAxis.axisMaximum = 100.0f
        leftAxis.valueFormatter = MyYAxisValueFormatter()

    }
    // 更新标题文本
    private fun updateChartTitle(newTitle: String) {
        val description = lineChart.description
        description.text = newTitle
        lineChart.invalidate()
    }

    //設定圖表樣式
    private fun initChartFormat() {
        val description = Description()
        description.text = " " // 设置标题文本
        description.textColor = Color.BLACK // 设置标题颜色
        description.textSize = 20f
        description.setPosition(580f, 65f)


        lineChart.description = description


        val legend = lineChart.legend
        legend.isEnabled = true // 启用图例
        legend.textSize = 12f
        legend.textColor = Color.BLACK
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)
        legend.form = Legend.LegendForm.SQUARE // 设置图例项的形状为线条
        legend.formSize = 12f
        legend.xEntrySpace = 12f // 设置图例项之间的间距

        lineChart.setBackgroundColor(Color.WHITE)

        lineChart.setNoDataText("暫時沒有數據")
        lineChart.setNoDataTextColor(Color.BLUE)

    }


    inner class MyYAxisValueFormatter : IAxisValueFormatter {
        private val mFormat: DecimalFormat = DecimalFormat("###,###.0")

        override fun getFormattedValue(value: Float, axis: AxisBase): String {
            return mFormat.format(value)
        }
    }

    private fun setupLineChartData(dataSet: List<Entry>, label: String) {
        val lineDataSet = LineDataSet(dataSet, label)

        // 设置 lineDataSet 的样式和属性
        val initialColor = getInitialColor(label) // 获取初始标签对应的颜色
        lineDataSet.setDrawCircles(true)
        lineDataSet.circleRadius = 0f // 设置实心圆的半径
        lineDataSet.circleHoleRadius = 0f // 设置圆心的空心半径
        lineDataSet.setCircleColor(initialColor)
        lineDataSet.color = initialColor
        lineDataSet.circleColors = listOf(initialColor) // 设置圆圈颜色
        lineDataSet.valueTextSize = 15f // 设置数据标签字体大小

        val lineData = LineData(lineDataSet)
        lineChart.data = lineData
        lineChart.invalidate()
    }

    // 获取初始标签对应的颜色
    private fun getInitialColor(label: String): Int {
        return when (label) {
            "重述" -> ContextCompat.getColor(this, R.color.green)
            "流暢" -> ContextCompat.getColor(this, R.color.purple_700)
            "理解" -> ContextCompat.getColor(this, R.color.pink)
            "食物" -> ContextCompat.getColor(this, R.color.pink)
            "生活" -> ContextCompat.getColor(this, R.color.pink)
            else -> Color.BLACK // 设置一个默认的颜色
        }
    }

    private fun navigateToNextPage(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.putExtra("username", username)
        startActivity(intent)
    }
}
