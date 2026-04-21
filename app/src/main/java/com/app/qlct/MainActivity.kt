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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.app.qlct.data.AppDatabase
import com.app.qlct.data.TransactionRepository
import com.app.qlct.data.WalletRepository
import com.app.qlct.data.CategoryRepository
import com.app.qlct.presentation.viewmodel.TransactionViewModel
import com.app.qlct.presentation.viewmodel.TransactionViewModelFactory

import com.app.qlct.data.entity.Transaction
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import android.widget.ImageView
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private var currentTransactions: List<Transaction> = emptyList()

    private fun getCurrency(): String {
        return "đ"
    }

    override fun onResume() {
        super.onResume()
        // Ép Reload lại Layout khi quay lại từ màn hình Settings để nhúng Tiền Tệ Mới
        if (currentTransactions.isNotEmpty()) {
            redrawCurrentChart()
            val totalIncome = currentTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
            val totalExpense = currentTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            val formatter = java.text.DecimalFormat("#,###")
            findViewById<View>(R.id.cardIncome).findViewById<TextView>(R.id.tvStatAmount).text = if (totalIncome > 0) "+ ${formatter.format(totalIncome).replace(",", ".")} ${getCurrency()}" else "0 ${getCurrency()}"
            findViewById<View>(R.id.cardExpense).findViewById<TextView>(R.id.tvStatAmount).text = if (totalExpense > 0) "- ${formatter.format(totalExpense).replace(",", ".")} ${getCurrency()}" else "0 ${getCurrency()}"
            
            // Xử lý nốt cái Tổng Số Balance
            val totalBal = totalIncome - totalExpense
            findViewById<TextView>(R.id.tvTotalBalance).text = "${formatter.format(totalBal).replace(",", ".")} ${getCurrency()}"
        }
    }

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { TransactionRepository(database.transactionDao()) }
    private val walletRepository by lazy { WalletRepository(database.walletDao()) }
    private val categoryRepository by lazy { CategoryRepository(database.categoryDao()) }
    
    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(repository, walletRepository, categoryRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContentView(R.layout.fragment_dashboard)

        // ======= SETUP SIDEBAR (NAVIGATION DRAWER) =======
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val btnMenu = findViewById<ImageView>(R.id.btnMenu)
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            drawerLayout.closeDrawer(GravityCompat.START)
            when (menuItem.itemId) {
                R.id.nav_overview -> { /* Đang ở Tổng Quan, bỏ qua */ }
                R.id.nav_summary -> startActivity(android.content.Intent(this, SummaryReportActivity::class.java))
                R.id.nav_transactions -> startActivity(android.content.Intent(this, TransactionsActivity::class.java))
                R.id.nav_accounts -> startActivity(android.content.Intent(this, WalletActivity::class.java))
                R.id.nav_calendar -> startActivity(android.content.Intent(this, CalendarActivity::class.java))
                R.id.nav_faq -> startActivity(android.content.Intent(this, FaqActivity::class.java))
                R.id.nav_options -> startActivity(android.content.Intent(this, OptionsActivity::class.java))
                R.id.nav_settings -> startActivity(android.content.Intent(this, SettingsActivity::class.java))
            }
            true
        }
        // ===============================================

        // Khởi tạo thẻ UI (giá trị sẽ được observer cập nhật)
        val incomeCard = findViewById<View>(R.id.cardIncome)
        val titleIncome = incomeCard.findViewById<TextView>(R.id.tvStatTitle)
        val amountIncome = incomeCard.findViewById<TextView>(R.id.tvStatAmount)
        titleIncome.text = "Tổng Thu"
        // Bắt đầu lắng nghe tổng số dư từ Wallet Repository
        val tvTotalBalance = findViewById<TextView>(R.id.tvTotalBalance)
        
        lifecycleScope.launch {
            walletRepository.totalBalance.collect { balance ->
                val actualBalance = balance ?: 0.0
                val formatter = java.text.DecimalFormat("#,###")
                val formattedBalance = formatter.format(actualBalance).replace(",", ".")
                tvTotalBalance.text = "$formattedBalance ${getCurrency()}"
            }
        }

        // Bắt sự kiện chữ "XEM TẤT CẢ" -> Mở Màn hình Danh sách Giao Dịch
        val tvViewAll = findViewById<TextView>(R.id.tvViewAll)
        tvViewAll.setOnClickListener {
            val intent = android.content.Intent(this, TransactionsActivity::class.java)
            startActivity(intent)
        }

        // Khởi tạo thẻ Tổng Chi
        val expenseCard = findViewById<View>(R.id.cardExpense)
        val titleExpense = expenseCard.findViewById<TextView>(R.id.tvStatTitle)
        val amountExpense = expenseCard.findViewById<TextView>(R.id.tvStatAmount)
        titleExpense.text = "Tổng Chi"
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
                redrawCurrentChart()
            }
        }

        // Sét up Adapter cho Danh sách cuộn Giao dịch (RecyclerView)
        val rvTransactions = findViewById<RecyclerView>(R.id.rvTransactions)
        rvTransactions.layoutManager = LinearLayoutManager(this)
        val adapter = TransactionAdapter(onItemClick = {}, onItemLongClick = {})
        rvTransactions.adapter = adapter
        
        // Quan sát dữ liệu thật từ ViewModel và Tự động tính toán
        viewModel.allTransactions.observe(this) { transactions ->
            if (transactions != null) {
                currentTransactions = transactions
                adapter.submitList(transactions.take(10))
                
                val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
                val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                
                val formatter = java.text.DecimalFormat("#,###")
                amountIncome.text = if (totalIncome > 0) "+ ${formatter.format(totalIncome).replace(",", ".")} ${getCurrency()}" else "0 ${getCurrency()}"
                amountExpense.text = if (totalExpense > 0) "- ${formatter.format(totalExpense).replace(",", ".")} ${getCurrency()}" else "0 ${getCurrency()}"

                redrawCurrentChart()
            }
        }

        // Bắt sự kiện bấm Nút FAB Nổi góc phải -> Mở Màn hình nhập
        val fab = findViewById<View>(R.id.fabAddTransaction)
        fab.setOnClickListener {
            val intent = android.content.Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }
    }

    private fun redrawCurrentChart() {
        val toggleChart = findViewById<MaterialButtonToggleGroup>(R.id.toggleChartType)
        val checkedId = toggleChart.checkedButtonId
        
        when (checkedId) {
            R.id.btnChartTotal -> setupPieChartTotal()
            R.id.btnChartIncome -> setupPieChartIncome()
            R.id.btnChartExpense -> setupPieChartExpense()
            else -> setupPieChartTotal()
        }
    }

    private fun setupPieChartExpense() {
        val pieChart = findViewById<PieChart>(R.id.pieChart)
        val expenses = currentTransactions.filter { it.type == "EXPENSE" }
        val totalExpense = expenses.sumOf { it.amount }
        val expensesByCategory = expenses.groupBy { it.categoryName }.mapValues { it.value.sumOf { t -> t.amount } }
        
        val entries = ArrayList<PieEntry>()
        for ((category, amount) in expensesByCategory) {
            if (amount > 0) entries.add(PieEntry(amount.toFloat(), category))
        }

        val dataSet = PieDataSet(entries, "")
        val colorsStr = listOf("#FF9800", "#E91E63", "#2196F3", "#9C27B0", "#F44336", "#00BCD4", "#8BC34A")
        val colors = entries.indices.map { Color.parseColor(colorsStr[it % colorsStr.size]) }
        dataSet.colors = colors
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.WHITE

        pieChart.data = PieData(dataSet)
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.holeRadius = 50f
        pieChart.setTransparentCircleAlpha(0)
        val formatter = java.text.DecimalFormat("#,###")
        val formattedTot = formatter.format(totalExpense).replace(",", ".")
        pieChart.centerText = "Chi Tiêu\n- $formattedTot ${getCurrency()}"
        pieChart.setCenterTextSize(14f)
        pieChart.setCenterTextColor(Color.parseColor("#F44336"))
        pieChart.legend.isEnabled = false // Tắt Legend rởm của thư viện
        
        pieChart.animateY(500)

        updateLegend(entries, colors, "Tổng Chi", "- $formattedTot ${getCurrency()}", Color.parseColor("#F44336"))
    }

    private fun setupPieChartIncome() {
        val pieChart = findViewById<PieChart>(R.id.pieChart)
        val incomes = currentTransactions.filter { it.type == "INCOME" }
        val totalIncome = incomes.sumOf { it.amount }
        val incomesByCategory = incomes.groupBy { it.categoryName }.mapValues { it.value.sumOf { t -> t.amount } }

        val entries = ArrayList<PieEntry>()
        for ((category, amount) in incomesByCategory) {
            if (amount > 0) entries.add(PieEntry(amount.toFloat(), category))
        }

        val dataSet = PieDataSet(entries, "")
        val colorsStr = listOf("#4CAF50", "#8BC34A", "#CDDC39", "#009688", "#00BCD4", "#3F51B5", "#9C27B0")
        val colors = entries.indices.map { Color.parseColor(colorsStr[it % colorsStr.size]) }
        dataSet.colors = colors
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.WHITE

        pieChart.data = PieData(dataSet)
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.holeRadius = 50f
        pieChart.setTransparentCircleAlpha(0)
        val formatter = java.text.DecimalFormat("#,###")
        val formattedTot = formatter.format(totalIncome).replace(",", ".")
        pieChart.centerText = "Tổng Thu\n+ $formattedTot ${getCurrency()}"
        pieChart.setCenterTextSize(14f)
        pieChart.setCenterTextColor(Color.parseColor("#4CAF50"))
        pieChart.legend.isEnabled = false
        
        pieChart.animateY(500)

        updateLegend(entries, colors, "Tổng Thu", "+ $formattedTot ${getCurrency()}", Color.parseColor("#4CAF50"))
    }

    private fun setupPieChartTotal() {
        val pieChart = findViewById<PieChart>(R.id.pieChart)
        val totalIncome = currentTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
        val totalExpense = currentTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }

        val entries = ArrayList<PieEntry>()
        if (totalIncome > 0) entries.add(PieEntry(totalIncome.toFloat(), "Thu nhập"))
        if (totalExpense > 0) entries.add(PieEntry(totalExpense.toFloat(), "Chi tiêu"))

        val dataSet = PieDataSet(entries, "")
        val colors = ArrayList<Int>()
        if (totalIncome > 0) colors.add(Color.parseColor("#4CAF50"))
        if (totalExpense > 0) colors.add(Color.parseColor("#F44336"))
        
        dataSet.colors = colors
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.WHITE

        pieChart.data = PieData(dataSet)
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.holeRadius = 50f
        pieChart.setTransparentCircleAlpha(0)
        
        val formatter = java.text.DecimalFormat("#,###")
        val diff = totalIncome - totalExpense
        val formattedDiff = formatter.format(Math.abs(diff)).replace(",", ".")
        val prefix = if (diff >= 0) "Số dư dương\n+" else "Số dư âm\n-"
        val colorNet = if (diff >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
        
        pieChart.centerText = "$prefix $formattedDiff ${getCurrency()}"
        pieChart.setCenterTextSize(12f)
        pieChart.setCenterTextColor(colorNet)
        pieChart.legend.isEnabled = false
        
        pieChart.animateY(500)

        val prefixShort = if (diff >= 0) "+" else "-"
        updateLegend(entries, colors, "Còn Dư", "$prefixShort $formattedDiff ${getCurrency()}", colorNet)
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
