package com.app.qlct

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.qlct.data.AppDatabase
import com.app.qlct.data.CategoryRepository
import com.app.qlct.data.TransactionRepository
import com.app.qlct.data.WalletRepository
import com.app.qlct.presentation.viewmodel.TransactionViewModel
import com.app.qlct.presentation.viewmodel.TransactionViewModelFactory
import java.text.DecimalFormat
import java.util.Calendar
import com.app.qlct.data.dto.MonthSummary

class SummaryReportActivity : AppCompatActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { TransactionRepository(database.transactionDao()) }
    private val walletRepository by lazy { WalletRepository(database.walletDao()) }
    private val categoryRepository by lazy { CategoryRepository(database.categoryDao()) }
    
    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(repository, walletRepository, categoryRepository)
    }

    private val summaryList = mutableListOf<MonthSummary>()
    private lateinit var adapter: SummaryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary_report)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val rvSummaryList = findViewById<RecyclerView>(R.id.rvSummaryList)
        rvSummaryList.layoutManager = LinearLayoutManager(this)
        
        adapter = SummaryAdapter(summaryList) { summary ->
            // Bấm vào 1 Cột Tháng thì chuyển qua trang Danh sách Giao Dịch của đúng tháng đó
            val intent = Intent(this, TransactionsActivity::class.java).apply {
                putExtra("TARGET_MONTH", summary.month)
                putExtra("TARGET_YEAR", summary.year)
            }
            startActivity(intent)
        }
        rvSummaryList.adapter = adapter

        viewModel.monthlySummaries.observe(this) { summaries ->
            if (summaries != null) {
                summaryList.clear()
                summaryList.addAll(summaries)
                adapter.notifyDataSetChanged()
            }
        }
    }

    inner class SummaryAdapter(
        private val list: List<MonthSummary>,
        private val onClick: (MonthSummary) -> Unit
    ) : RecyclerView.Adapter<SummaryAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvMonthTitle = view.findViewById<TextView>(R.id.tvMonthTitle)
            val tvIncome = view.findViewById<TextView>(R.id.tvIncome)
            val tvExpense = view.findViewById<TextView>(R.id.tvExpense)
            val tvTotal = view.findViewById<TextView>(R.id.tvTotal)

            fun bind(item: MonthSummary) {
                val monthNames = arrayOf("Một", "Hai", "Ba", "Tư", "Năm", "Sáu", "Bảy", "Tám", "Chín", "Mười", "Mười Một", "Mười Hai")
                val monthName = monthNames.getOrNull(item.month - 1) ?: item.month.toString()
                
                tvMonthTitle.text = "Tháng $monthName ${item.year}"
                
                val df = DecimalFormat("#,###")
                tvIncome.text = df.format(item.totalIncome).replace(",", ".") + " đ"
                tvExpense.text = df.format(item.totalExpense).replace(",", ".") + " đ"
                tvTotal.text = df.format(item.balance).replace(",", ".") + " đ"

                itemView.setOnClickListener { onClick(item) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_summary_month, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(list[position])
        }

        override fun getItemCount() = list.size
    }
}
