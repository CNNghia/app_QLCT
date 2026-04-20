package com.app.qlct

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.qlct.data.AppDatabase
import com.app.qlct.data.TransactionRepository
import com.app.qlct.data.WalletRepository
import com.app.qlct.data.CategoryRepository
import com.app.qlct.data.entity.Transaction
import com.app.qlct.presentation.viewmodel.TransactionViewModel
import com.app.qlct.presentation.viewmodel.TransactionViewModelFactory
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.card.MaterialCardView
import java.text.DecimalFormat
import java.util.*

class SummaryReportActivity : AppCompatActivity() {

    private val currentMonthCal: Calendar = Calendar.getInstance()
    private var allTransactions: List<Transaction> = emptyList()

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { TransactionRepository(database.transactionDao()) }
    private val walletRepository by lazy { WalletRepository(database.walletDao()) }
    private val categoryRepository by lazy { CategoryRepository(database.categoryDao()) }
    
    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(repository, walletRepository, categoryRepository)
    }

    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary_report)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val rvReportTransactions = findViewById<RecyclerView>(R.id.rvReportTransactions)
        rvReportTransactions.layoutManager = LinearLayoutManager(this)
        adapter = TransactionAdapter(onItemClick = {}, onItemLongClick = {})
        rvReportTransactions.adapter = adapter

        val btnPrevMonth = findViewById<ImageButton>(R.id.btnPrevMonth)
        val btnNextMonth = findViewById<ImageButton>(R.id.btnNextMonth)

        btnPrevMonth.setOnClickListener {
            currentMonthCal.add(Calendar.MONTH, -1)
            updateMonthDisplay()
            filterAndCalculate()
        }

        btnNextMonth.setOnClickListener {
            currentMonthCal.add(Calendar.MONTH, 1)
            updateMonthDisplay()
            filterAndCalculate()
        }

        viewModel.allTransactions.observe(this) { transactions ->
            if (transactions != null) {
                allTransactions = transactions
                updateMonthDisplay()
                filterAndCalculate()
            }
        }
    }

    private fun updateMonthDisplay() {
        val tvCurrentMonth = findViewById<TextView>(R.id.tvCurrentMonth)
        val month = currentMonthCal.get(Calendar.MONTH) + 1
        val year = currentMonthCal.get(Calendar.YEAR)
        tvCurrentMonth.text = "Tháng $month/$year"
    }

    private fun filterAndCalculate() {
        val filteredList = allTransactions.filter { 
            val transCal = Calendar.getInstance()
            transCal.timeInMillis = it.date
            transCal.get(Calendar.YEAR) == currentMonthCal.get(Calendar.YEAR) &&
            transCal.get(Calendar.MONTH) == currentMonthCal.get(Calendar.MONTH)
        }.sortedByDescending { it.date }

        val tvEmptyReport = findViewById<TextView>(R.id.tvEmptyReport)
        val rvReportTransactions = findViewById<RecyclerView>(R.id.rvReportTransactions)

        if (filteredList.isEmpty()) {
            tvEmptyReport.visibility = View.VISIBLE
            rvReportTransactions.visibility = View.GONE
        } else {
            tvEmptyReport.visibility = View.GONE
            rvReportTransactions.visibility = View.VISIBLE
        }
        
        adapter.submitList(filteredList)

        val totalIncome = filteredList.filter { it.type == "INCOME" }.sumOf { it.amount }
        val totalExpense = filteredList.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val balance = totalIncome - totalExpense

        val formatter = DecimalFormat("#,###")
        val tvReportIncome = findViewById<TextView>(R.id.tvReportIncome)
        val tvReportExpense = findViewById<TextView>(R.id.tvReportExpense)
        val tvReportBalance = findViewById<TextView>(R.id.tvReportBalance)

        tvReportIncome.text = if (totalIncome > 0) "+ ${formatter.format(totalIncome).replace(",", ".")} đ" else "0 đ"
        tvReportExpense.text = if (totalExpense > 0) "- ${formatter.format(totalExpense).replace(",", ".")} đ" else "0 đ"
        
        val absBalanceStr = formatter.format(Math.abs(balance)).replace(",", ".")
        if (balance >= 0) {
            tvReportBalance.text = "+ $absBalanceStr đ"
            tvReportBalance.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            tvReportBalance.text = "- $absBalanceStr đ"
            tvReportBalance.setTextColor(Color.parseColor("#F44336"))
        }

        drawPieChart(totalIncome, totalExpense)
    }

    private fun drawPieChart(totalIncome: Double, totalExpense: Double) {
        val pieChart = findViewById<PieChart>(R.id.reportPieChart)
        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()

        if (totalIncome > 0) {
            entries.add(PieEntry(totalIncome.toFloat(), "Thu nhập"))
            colors.add(Color.parseColor("#4CAF50"))
        }
        if (totalExpense > 0) {
            entries.add(PieEntry(totalExpense.toFloat(), "Chi tiêu"))
            colors.add(Color.parseColor("#F44336"))
        }

        if (entries.isEmpty()) {
            pieChart.clear()
            pieChart.centerText = "Không có dữ liệu gốc"
            pieChart.setCenterTextSize(12f)
            pieChart.setCenterTextColor(Color.GRAY)
            val layoutReportLegend = findViewById<LinearLayout>(R.id.layoutReportLegend)
            layoutReportLegend.removeAllViews()
            pieChart.invalidate()
            return
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colors
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.WHITE

        pieChart.data = PieData(dataSet)
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.holeRadius = 50f
        pieChart.setTransparentCircleAlpha(0)
        
        val diff = totalIncome - totalExpense
        val formatter = DecimalFormat("#,###")
        val formattedDiff = formatter.format(Math.abs(diff)).replace(",", ".")
        val prefix = if (diff >= 0) "Chênh lệch\n+" else "Chênh lệch\n-"
        val colorNet = if (diff >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
        
        pieChart.centerText = "$prefix $formattedDiff đ"
        pieChart.setCenterTextSize(12f)
        pieChart.setCenterTextColor(colorNet)
        pieChart.legend.isEnabled = false
        
        pieChart.animateY(500)
        pieChart.invalidate()

        updateLegend(entries, colors)
    }

    private fun updateLegend(entries: List<PieEntry>, colors: List<Int>) {
        val layoutLegend = findViewById<LinearLayout>(R.id.layoutReportLegend)
        layoutLegend.removeAllViews()

        for (i in entries.indices) {
            val entry = entries[i]
            val color = colors[i]
            val row = layoutInflater.inflate(R.layout.item_chart_legend, layoutLegend, false)
            val cardIndicator = row.findViewById<MaterialCardView>(R.id.cardIndicator)
            val tvLabel = row.findViewById<TextView>(R.id.tvLegendLabel)
            val tvValue = row.findViewById<TextView>(R.id.tvLegendValue)

            cardIndicator.setCardBackgroundColor(color)
            tvLabel.text = entry.label
            tvLabel.setTextColor(color)
            
            val formatValue = if (entry.value >= 1000000) "${String.format(Locale.US, "%.1f", entry.value / 1000000)}M" else "${String.format(Locale.US, "%.0f", entry.value / 1000)}K"
            tvValue.text = ": $formatValue"
            tvValue.setTextColor(color)
            layoutLegend.addView(row)
        }
    }
}
