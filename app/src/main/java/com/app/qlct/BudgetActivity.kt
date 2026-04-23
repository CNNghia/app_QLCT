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
import com.app.qlct.data.AppPrefs
import com.app.qlct.data.TransactionType
import com.app.qlct.utils.formatVND
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

        val prefs = AppPrefs.get(this)
        val savedBudget = prefs.getLong(AppPrefs.KEY_BUDGET_LIMIT, 0L).toDouble()

        val etBudgetLimit = findViewById<EditText>(R.id.etBudgetLimit)
        val btnSaveBudget = findViewById<Button>(R.id.btnSaveBudget)

        if (savedBudget > 0) {
            etBudgetLimit.setText(String.format(Locale.US, "%.0f", savedBudget))
        }

        btnSaveBudget.setOnClickListener {
            val amountStr = etBudgetLimit.text.toString()
            if (amountStr.isNotEmpty()) {
                val amount = amountStr.toLongOrNull() ?: 0L
                prefs.edit().putLong(AppPrefs.KEY_BUDGET_LIMIT, amount).apply()
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

        val prefs = AppPrefs.get(this)
        val budget = prefs.getLong(AppPrefs.KEY_BUDGET_LIMIT, 0L).toDouble()

        // Chỉ tính toán các khoản chi tiêu trong đúng "Tháng hiện tại"
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        val budgetState = viewModel.calculateBudgetState(budget, transactions, currentMonth, currentYear)

        val progressBudget = findViewById<ProgressBar>(R.id.progressBudget)
        val tvSpent = findViewById<TextView>(R.id.tvSpent)
        val tvRemaining = findViewById<TextView>(R.id.tvRemaining)
        val tvWarning = findViewById<TextView>(R.id.tvWarning)
        val tvSafe = findViewById<TextView>(R.id.tvSafe)

        tvSpent.text = "Đã chi: ${budgetState.monthlyExpenses.formatVND()}"

        if (budget > 0) {
            progressBudget.progress = budgetState.percent

            if (budgetState.remaining >= 0) {
                tvRemaining.text = "Còn lại: ${budgetState.remaining.formatVND()}"
            } else {
                tvRemaining.text = "Vượt mức hạn: ${Math.abs(budgetState.remaining).formatVND()}"
            }

            // Logic trạng thái màu cảnh báo thông minh được lấy từ ViewModel
            progressBudget.progressTintList = ColorStateList.valueOf(Color.parseColor(budgetState.statusColorHex))
            
            if (budgetState.percent >= 100) { 
                tvWarning.visibility = View.VISIBLE
                tvWarning.text = "Cảnh báo: Bạn đã vượt quá ngân sách!"
                tvSafe.visibility = View.GONE
            } else if (!budgetState.isSafe) { 
                tvWarning.visibility = View.VISIBLE
                tvWarning.text = budgetState.warningText
                tvSafe.visibility = View.GONE
            } else { 
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
