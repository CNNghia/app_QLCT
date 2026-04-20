package com.app.qlct

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.app.qlct.data.AppDatabase
import com.app.qlct.data.CategoryRepository
import com.app.qlct.data.TransactionRepository
import com.app.qlct.data.WalletRepository
import com.app.qlct.data.entity.Transaction
import com.app.qlct.presentation.viewmodel.TransactionViewModel
import com.app.qlct.presentation.viewmodel.TransactionViewModelFactory
import java.text.DecimalFormat
import java.util.Calendar
import java.util.Locale

class BudgetActivity : AppCompatActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { TransactionRepository(database.transactionDao()) }
    private val walletRepository by lazy { WalletRepository(database.walletDao()) }
    private val categoryRepository by lazy { CategoryRepository(database.categoryDao()) }

    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(repository, walletRepository, categoryRepository)
    }

    private var cachedTransactions: List<Transaction> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarBudget)
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert)
        toolbar.setNavigationOnClickListener { finish() }

        val prefs = getSharedPreferences("AppConfig", MODE_PRIVATE)
        val savedBudget = prefs.getFloat("BUDGET_LIMIT", 0f).toDouble()

        val etBudgetLimit = findViewById<EditText>(R.id.etBudgetLimit)
        val btnSaveBudget = findViewById<Button>(R.id.btnSaveBudget)
        
        if (savedBudget > 0) {
            etBudgetLimit.setText(String.format(Locale.US, "%.0f", savedBudget))
        }

        btnSaveBudget.setOnClickListener {
            val amountStr = etBudgetLimit.text.toString()
            if (amountStr.isNotEmpty()) {
                val amount = amountStr.toDoubleOrNull() ?: 0.0
                prefs.edit().putFloat("BUDGET_LIMIT", amount.toFloat()).apply()
                Toast.makeText(this, "Thiết lập thành công!", Toast.LENGTH_SHORT).show()
                calculateAndRender()
            }
        }

        viewModel.allTransactions.observe(this) { transactions ->
            if (transactions != null) {
                calculateAndRender(transactions)
            }
        }
    }

    private fun calculateAndRender(transactions: List<Transaction> = cachedTransactions) {
        cachedTransactions = transactions // Giữ lại mảng lịch sử khi gọi chạy lại form
        
        val prefs = getSharedPreferences("AppConfig", MODE_PRIVATE)
        val budget = prefs.getFloat("BUDGET_LIMIT", 0f).toDouble()

        // Chỉ tính toán các khoản chi tiêu trong đúng "Tháng hiện tại"
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        val monthlyExpenses = transactions.filter {
            val tc = Calendar.getInstance()
            tc.timeInMillis = it.date
            it.type == "EXPENSE" && tc.get(Calendar.MONTH) == currentMonth && tc.get(Calendar.YEAR) == currentYear
        }.sumOf { it.amount }

        val progressBudget = findViewById<ProgressBar>(R.id.progressBudget)
        val tvSpent = findViewById<TextView>(R.id.tvSpent)
        val tvRemaining = findViewById<TextView>(R.id.tvRemaining)
        val tvWarning = findViewById<TextView>(R.id.tvWarning)
        val tvSafe = findViewById<TextView>(R.id.tvSafe)

        val df = DecimalFormat("#,###")
        tvSpent.text = "Đã chi: ${df.format(monthlyExpenses).replace(",", ".")} đ"

        if (budget > 0) {
            val remaining = budget - monthlyExpenses
            val percent = ((monthlyExpenses / budget) * 100).toInt()
            
            // Ép UI chặn ở 100% nếu lố tiền (để thanh ProgressBar không bị lỗi UI)
            progressBudget.progress = percent.coerceAtMost(100)

            if (remaining >= 0) {
                tvRemaining.text = "Còn lại: ${df.format(remaining).replace(",", ".")} đ"
            } else {
                tvRemaining.text = "Vượt mức hạn: ${df.format(Math.abs(remaining)).replace(",", ".")} đ"
            }

            // Logic trạng thái màu cảnh báo thông minh
            if (percent >= 100) { 
                // Vượt quá ngân sách (Đỏ chót)
                progressBudget.progressTintList = ColorStateList.valueOf(Color.parseColor("#D32F2F"))
                tvWarning.visibility = View.VISIBLE
                tvSafe.visibility = View.GONE
            } else if (percent >= 80) { 
                // Xài tới 80% ngân sách (Cam cảnh báo)
                progressBudget.progressTintList = ColorStateList.valueOf(Color.parseColor("#F57C00"))
                tvWarning.visibility = View.VISIBLE
                tvWarning.text = "Chú ý: Bạn đã sử dụng ${percent}% năng lực tài chính giới hạn!"
                tvSafe.visibility = View.GONE
            } else { 
                // An toàn
                progressBudget.progressTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
                tvWarning.visibility = View.GONE
                tvSafe.visibility = View.VISIBLE
            }
        } else {
            // Chưa điền ngân sách
            progressBudget.progress = 0
            tvRemaining.text = "Chưa kích hoạt giới hạn"
            tvWarning.visibility = View.GONE
            tvSafe.visibility = View.GONE
        }
    }
}
