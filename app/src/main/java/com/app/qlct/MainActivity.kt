package com.app.qlct

import android.os.Bundle
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import android.widget.LinearLayout

import androidx.activity.viewModels
import com.app.qlct.data.database.AppDatabase
import com.app.qlct.data.repository.TransactionRepository
import com.app.qlct.presentation.viewmodel.TransactionViewModel
import com.app.qlct.presentation.viewmodel.TransactionViewModelFactory

class MainActivity : AppCompatActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { TransactionRepository(database.transactionDao()) }
    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContentView(R.layout.fragment_dashboard)

        // Mock dữ liệu thẻ Tổng Thu
        val incomeCard = findViewById<View>(R.id.cardIncome)
        val titleIncome = incomeCard.findViewById<TextView>(R.id.tvStatTitle)
        val amountIncome = incomeCard.findViewById<TextView>(R.id.tvStatAmount)
        titleIncome.text = "Tổng Thu"
        amountIncome.text = "+ 15.000.000 đ"
        val tvTotalBalance = findViewById<TextView>(R.id.tvTotalBalance)
        tvTotalBalance.text = "14.725.000 đ"

        // Bắt sự kiện icon góc phải trên cùng Dashboard -> Phóng qua màn hình VÍ
        val btnOpenWallet = findViewById<View>(R.id.btnOpenWallet)
        btnOpenWallet.setOnClickListener {
            val intent = android.content.Intent(this, WalletActivity::class.java)
            startActivity(intent)
        }

        // Bắt sự kiện chữ "XEM TẤT CẢ" -> Mở Màn hình Danh sách Giao Dịch
        val tvViewAll = findViewById<TextView>(R.id.tvViewAll)
        tvViewAll.setOnClickListener {
            val intent = android.content.Intent(this, TransactionsActivity::class.java)
            startActivity(intent)
        }

        // Mock dữ liệu thẻ Tổng Chi
        val expenseCard = findViewById<View>(R.id.cardExpense)
        val titleExpense = expenseCard.findViewById<TextView>(R.id.tvStatTitle)
        val amountExpense = expenseCard.findViewById<TextView>(R.id.tvStatAmount)
        titleExpense.text = "Tổng Chi"
        amountExpense.text = "- 275.000 đ"
        amountExpense.setTextColor(Color.parseColor("#F44336")) // Đổi màu đỏ

        // Sét up chức năng Bấm Vào Card chui sang Màn Chi Tiết
        incomeCard.setOnClickListener {
            val intent = android.content.Intent(this, TransactionsActivity::class.java)
            intent.putExtra("TYPE", "INCOME")
            startActivity(intent)
        }
        expenseCard.setOnClickListener {
            val intent = android.content.Intent(this, TransactionsActivity::class.java)
            intent.putExtra("TYPE", "EXPENSE")
            startActivity(intent)
        }

        // Bắt sự kiện Gạt Nút TỔNG / THU / CHI trên Biểu đồ
        val toggleChart = findViewById<MaterialButtonToggleGroup>(R.id.toggleChartType)
        toggleChart.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnChartTotal -> setupPieChartTotal()
                    R.id.btnChartIncome -> setupPieChartIncome()
                    R.id.btnChartExpense -> setupPieChartExpense()
                }
            }
        }

        // Mặc định lúc vừa vào app thì vẽ biểu đồ TỔNG
        setupPieChartTotal()

        // Sét up Adapter cho Danh sách cuộn Giao dịch (RecyclerView)
        val rvTransactions = findViewById<RecyclerView>(R.id.rvTransactions)
        rvTransactions.layoutManager = LinearLayoutManager(this)
        val adapter = TransactionAdapter(onItemClick = {}, onItemLongClick = {})
        rvTransactions.adapter = adapter
        
        // Quan sát dữ liệu thật từ ViewModel (Lấy 10 cái gần nhất chẳng hạn)
        viewModel.allTransactions.observe(this) { transactions ->
            if (transactions != null) {
                adapter.submitList(transactions.take(10))
            }
        }

        // Bắt sự kiện bấm Nút FAB Nổi góc phải -> Mở Màn hình nhập
        val fab = findViewById<View>(R.id.fabAddTransaction)
        fab.setOnClickListener {
            val intent = android.content.Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupPieChartExpense() {
        val pieChart = findViewById<PieChart>(R.id.pieChart)

        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(120000f, "Giải trí"))
        entries.add(PieEntry(50000f, "Ăn uống"))
        entries.add(PieEntry(40000f, "Đi lại"))
        entries.add(PieEntry(65000f, "Cafe"))

        val dataSet = PieDataSet(entries, "")
        val colors = listOf(
            Color.parseColor("#FF9800"), // Cam
            Color.parseColor("#E91E63"), // Hồng
            Color.parseColor("#2196F3"), // Xanh dương
            Color.parseColor("#9C27B0")  // Tím
        )
        dataSet.colors = colors
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.WHITE

        pieChart.data = PieData(dataSet)
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.holeRadius = 50f
        pieChart.setTransparentCircleAlpha(0)
        pieChart.centerText = "Chi Tiêu\n- 275.000 đ"
        pieChart.setCenterTextSize(14f)
        pieChart.setCenterTextColor(Color.parseColor("#F44336"))
        pieChart.legend.isEnabled = false // Tắt Legend rởm của thư viện
        
        pieChart.animateY(800)

        // Tự động Vẽ Bảng Chi Tiết (Legend Xịn) sang Góc Trái
        updateLegend(entries, colors, "Tổng Chi", "- 275.000 đ", Color.parseColor("#F44336"))
    }

    private fun setupPieChartIncome() {
        val pieChart = findViewById<PieChart>(R.id.pieChart)

        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(5000000f, "Lương"))
        entries.add(PieEntry(500000f, "Lì xì"))
        entries.add(PieEntry(1500000f, "Bán đồ"))
        entries.add(PieEntry(8000000f, "Thưởng"))

        val dataSet = PieDataSet(entries, "")
        val colors = listOf(
            Color.parseColor("#4CAF50"), // Xanh lá
            Color.parseColor("#8BC34A"), // Xanh mạ
            Color.parseColor("#CDDC39"), // Vàng chanh
            Color.parseColor("#009688")  // Xanh ngọc
        )
        dataSet.colors = colors
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.WHITE

        pieChart.data = PieData(dataSet)
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.holeRadius = 50f
        pieChart.setTransparentCircleAlpha(0)
        pieChart.centerText = "Tổng Thu\n+ 15.000.000 đ"
        pieChart.setCenterTextSize(14f)
        pieChart.setCenterTextColor(Color.parseColor("#4CAF50"))
        pieChart.legend.isEnabled = false
        
        pieChart.animateY(800)

        // Cập nhật Legend cho Thu
        updateLegend(entries, colors, "Tổng Thu", "+ 15.000.000 đ", Color.parseColor("#4CAF50"))
    }

    private fun setupPieChartTotal() {
        val pieChart = findViewById<PieChart>(R.id.pieChart)

        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(15000000f, "Thu nhập"))
        entries.add(PieEntry(275000f, "Chi tiêu"))

        val dataSet = PieDataSet(entries, "")
        val colors = listOf(
            Color.parseColor("#4CAF50"), // Xanh lá mơn mởn (Thu)
            Color.parseColor("#F44336")  // Đỏ rực rỡ (Chi)
        )
        dataSet.colors = colors
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.WHITE

        pieChart.data = PieData(dataSet)
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.holeRadius = 50f
        pieChart.setTransparentCircleAlpha(0)
        pieChart.centerText = "Số dư dương\n+ 14.725.000 đ"
        pieChart.setCenterTextSize(12f)
        pieChart.setCenterTextColor(Color.parseColor("#4CAF50")) // Ở đây Dư nên nó màu XANH
        pieChart.legend.isEnabled = false
        
        pieChart.animateY(800)

        // Cập nhật Legend cho Tổng
        updateLegend(entries, colors, "Còn Dư", "+ 14.725.000 đ", Color.parseColor("#4CAF50"))
    }

    // Hàm kiến trúc chuyên dựng Layout Cột Bảng Chi Tiết (Legend) bên Trái
    private fun updateLegend(entries: List<PieEntry>, colors: List<Int>, totalPrefix: String, totalAmountStr: String, netColor: Int) {
        val layoutLegend = findViewById<LinearLayout>(R.id.layoutLegend)
        layoutLegend.removeAllViews() // Quét sạch dữ liệu cũ

        // Vẽ từng phần tử với đúng màu sắc
        for (i in entries.indices) {
            val entry = entries[i]
            val color = colors[i]

            val row = layoutInflater.inflate(R.layout.item_chart_legend, layoutLegend, false)
            val cardIndicator = row.findViewById<MaterialCardView>(R.id.cardIndicator)
            val tvLabel = row.findViewById<TextView>(R.id.tvLegendLabel)
            val tvValue = row.findViewById<TextView>(R.id.tvLegendValue)

            cardIndicator.setCardBackgroundColor(color)
            tvLabel.text = entry.label
            tvLabel.setTextColor(color) // Chữ màu y như Chart
            
            // Format gọn giá trị, lược bỏ phần triệu (k cho mượt)
            val formatValue = if (entry.value >= 1000000) "${String.format("%.1f", entry.value / 1000000)}M" else "${String.format("%.0f", entry.value / 1000)}K"
            tvValue.text = ": $formatValue"
            tvValue.setTextColor(color)

            layoutLegend.addView(row)
        }

        // Kẻ 1 đưởng Gạch ngang màu xám nhạt
        val line = View(this)
        line.setBackgroundColor(Color.parseColor("#E0E0E0"))
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
        params.setMargins(0, 16, 0, 16)
        layoutLegend.addView(line, params)

        // Gắn Dòng Tổng (Total) bự lên dưới cùng có màu Xanh/Đỏ tùy dữ kiện
        val totalRow = layoutInflater.inflate(R.layout.item_chart_legend, layoutLegend, false)
        totalRow.findViewById<MaterialCardView>(R.id.cardIndicator).visibility = View.GONE
        
        val tvTotalLabel = totalRow.findViewById<TextView>(R.id.tvLegendLabel)
        tvTotalLabel.text = totalPrefix
        tvTotalLabel.setTextColor(netColor)
        
        val tvTotalValue = totalRow.findViewById<TextView>(R.id.tvLegendValue)
        tvTotalValue.text = totalAmountStr
        tvTotalValue.setTextColor(netColor)

        layoutLegend.addView(totalRow)
    }
}