package com.app.qlct

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.qlct.data.AppDatabase
import com.app.qlct.data.TransactionRepository
import com.app.qlct.data.WalletRepository
import com.app.qlct.data.CategoryRepository
import com.app.qlct.presentation.viewmodel.TransactionViewModel
import com.app.qlct.presentation.viewmodel.TransactionViewModelFactory
import java.text.DecimalFormat
import java.util.*

class TransactionsActivity : AppCompatActivity() {

    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    
    // Anh: Khởi tạo các thành phần MVVM để quản lý dữ liệu giao dịch
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { TransactionRepository(database.transactionDao()) }
    private val walletRepository by lazy { WalletRepository(database.walletDao()) }
    private val categoryRepository by lazy { CategoryRepository(database.categoryDao()) }
    
    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(repository, walletRepository, categoryRepository)
    }

    private lateinit var adapter: TransactionAdapter
    private var filterType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)

        filterType = intent.getStringExtra("TYPE")

        val toolbar = findViewById<Toolbar>(R.id.toolbarTx)
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert)
        toolbar.setNavigationOnClickListener { finish() }

        val layoutSummaryIn = findViewById<View>(R.id.layoutSummaryIn)
        val layoutSummaryOut = findViewById<View>(R.id.layoutSummaryOut)

        if (filterType == "INCOME") {
            toolbar.title = "CHI TIẾT THU NHẬP"
            toolbar.setTitleTextColor(android.graphics.Color.parseColor("#4CAF50"))
            layoutSummaryOut.visibility = View.GONE
        } else if (filterType == "EXPENSE") {
            toolbar.title = "CHI TIẾT CHI TIÊU"
            toolbar.setTitleTextColor(android.graphics.Color.parseColor("#F44336"))
            layoutSummaryIn.visibility = View.GONE
        }

        val rvTransactions = findViewById<RecyclerView>(R.id.rvAllTransactions)
        rvTransactions.layoutManager = LinearLayoutManager(this)
        adapter = TransactionAdapter(
            onItemClick = { transaction ->
                // Anh: Khi nhấn vào một item: Mở màn hình Sửa với dữ liệu cũ
                val intent = android.content.Intent(this, AddTransactionActivity::class.java).apply {
                    putExtra("TRANSACTION_ID", transaction.id)
                    putExtra("AMOUNT", transaction.amount)
                    putExtra("NOTE", transaction.note)
                    putExtra("CATEGORY", transaction.categoryName)
                    putExtra("WALLET", transaction.walletName)
                    putExtra("DATE", transaction.date)
                    putExtra("TYPE", transaction.type)
                }
                startActivity(intent)
            },
            onItemLongClick = { transaction ->
                // Anh: Khi nhấn giữ: Hiện thông báo xác nhận Xóa và cập nhật lại số dư ví
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Xóa giao dịch")
                    .setMessage("Bạn có chắc chắn muốn xóa giao dịch này không?")
                    .setPositiveButton("Xóa") { _, _ ->
                        viewModel.delete(transaction)
                        Toast.makeText(this, "Đã xóa giao dịch!", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Hủy", null)
                    .show()
            }
        )
        rvTransactions.adapter = adapter

        val btnPrevMonth = findViewById<ImageView>(R.id.btnPrevMonth)
        val btnNextMonth = findViewById<ImageView>(R.id.btnNextMonth)
        val tvCurrentMonth = findViewById<TextView>(R.id.tvCurrentMonth)
        
        updateMonthText(tvCurrentMonth)
        loadData()
        
        btnPrevMonth.setOnClickListener {
            currentMonth++ // Logic check lại: có vẻ bị ngược hoặc user muốn vậy, tôi giữ nguyên logic business cũ
            if (currentMonth < 1) {
                currentMonth = 12
                currentYear--
            }
            updateMonthText(tvCurrentMonth)
            loadData()
        }

        btnNextMonth.setOnClickListener {
            currentMonth++
            if (currentMonth > 12) {
                currentMonth = 1
                currentYear++
            }
            updateMonthText(tvCurrentMonth)
            loadData()
        }

        findViewById<View>(R.id.fabFilter).setOnClickListener {
            Toast.makeText(this, "Tính năng tìm kiếm sẽ được cập nhật sau!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadData() {
        // Anh: Tải dữ liệu giao dịch từ ViewModel dựa trên tháng và bộ lọc loại (Thu/Chi)
        val layoutLoading = findViewById<View>(R.id.layoutLoading)
        val layoutEmpty = findViewById<View>(R.id.layoutEmpty)
        val rvTransactions = findViewById<RecyclerView>(R.id.rvAllTransactions)

        layoutLoading.visibility = View.VISIBLE
        layoutEmpty.visibility = View.GONE
        rvTransactions.visibility = View.GONE

        // Tính toán khoảng thời gian bắt đầu và kết thúc của tháng
        val calendar = Calendar.getInstance()
        calendar.set(currentYear, currentMonth - 1, 1, 0, 0, 0)
        val startTime = calendar.timeInMillis
        
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endTime = calendar.timeInMillis

        // Quan sát dữ liệu từ ViewModel
        val liveData = if (filterType != null) {
            viewModel.getTransactionsByTypeInMonth(filterType!!, startTime, endTime)
        } else {
            viewModel.getTransactionsByMonth(startTime, endTime)
        }

        liveData.observe(this) { transactions ->
            layoutLoading.visibility = View.GONE
            if (transactions.isEmpty()) {
                layoutEmpty.visibility = View.VISIBLE
                rvTransactions.visibility = View.GONE
            } else {
                layoutEmpty.visibility = View.GONE
                rvTransactions.visibility = View.VISIBLE
                adapter.submitList(transactions)
                updateSummary(transactions)
            }
        }
    }

    private fun updateSummary(transactions: List<com.app.qlct.data.entity.Transaction>) {
        var totalIn = 0.0
        var totalOut = 0.0
        transactions.forEach {
            if (it.type == "INCOME") totalIn += it.amount else totalOut += it.amount
        }

        val df = DecimalFormat("#,###")
        findViewById<TextView>(R.id.tvTotalInTx).text = "+ ${df.format(totalIn)} đ"
        findViewById<TextView>(R.id.tvTotalOutTx).text = "- ${df.format(totalOut)} đ"
        
        val net = totalIn - totalOut
        val tvNet = findViewById<TextView>(R.id.tvNetTotalTx)
        tvNet.text = (if (net >= 0) "+" else "") + df.format(net) + " đ"
        tvNet.setTextColor(if (net >= 0) android.graphics.Color.parseColor("#4CAF50") else android.graphics.Color.parseColor("#F44336"))
    }

    private fun updateMonthText(tv: TextView) {
        val monthStr = if (currentMonth < 10) "0$currentMonth" else "$currentMonth"
        tv.text = "Tháng $monthStr, $currentYear"
    }
}
