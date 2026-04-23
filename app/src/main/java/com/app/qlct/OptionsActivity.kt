package com.app.qlct

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.app.qlct.data.AppDatabase
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat

class OptionsActivity : AppCompatActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarOptions)
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert)
        toolbar.setNavigationOnClickListener { finish() }

        // ── Điều hướng tới Category ────────────────────────────────────
        findViewById<MaterialCardView>(R.id.cardCategory).setOnClickListener {
            startActivity(Intent(this, CategoryActivity::class.java))
        }

        // ── Điều hướng tới Budget ──────────────────────────────────────
        findViewById<MaterialCardView>(R.id.cardBudget).setOnClickListener {
            startActivity(Intent(this, BudgetActivity::class.java))
        }

        // ── Điều hướng tới Settings ────────────────────────────────────
        findViewById<MaterialCardView>(R.id.cardSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // ── Load thống kê nhanh từ DB ──────────────────────────────────
        loadQuickStats()
    }

    private fun loadQuickStats() {
        lifecycleScope.launch {
            val (txCount, walletCount, catCount, totalExpense, totalIncome) = withContext(Dispatchers.IO) {
                val db = database
                val allTx = db.transactionDao().getAllTransactionsOnce()
                val wallets = db.walletDao().getWalletsOnce()
                val cats = db.categoryDao().getCategoriesOnce()
                val income = allTx.filter { it.type == "INCOME" }.sumOf { it.amount }
                val expense = allTx.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                QuickStats(allTx.size, wallets.size, cats.size, expense, income)
            }

            val df = DecimalFormat("#,###")

            // Hiển thị lên các TextView trong layout
            findViewById<TextView>(R.id.tvStatTxCount)?.text = "$txCount giao dịch"
            findViewById<TextView>(R.id.tvStatWalletCount)?.text = "$walletCount ví"
            findViewById<TextView>(R.id.tvStatCatCount)?.text = "$catCount danh mục"
            findViewById<TextView>(R.id.tvStatTotalIncome)?.text = "+ ${df.format(totalIncome).replace(",", ".")} đ"
            findViewById<TextView>(R.id.tvStatTotalExpense)?.text = "- ${df.format(totalExpense).replace(",", ".")} đ"
            val net = totalIncome - totalExpense
            val tvNet = findViewById<TextView>(R.id.tvStatNet)
            tvNet?.text = (if (net >= 0) "+" else "") + df.format(net).replace(",", ".") + " đ"
            tvNet?.setTextColor(
                android.graphics.Color.parseColor(if (net >= 0) "#4CAF50" else "#F44336")
            )
        }
    }

    private data class QuickStats(
        val txCount: Int,
        val walletCount: Int,
        val catCount: Int,
        val totalExpense: Double,
        val totalIncome: Double
    )
}
