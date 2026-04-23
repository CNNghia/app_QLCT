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
import android.app.AlertDialog
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

        // Móc Nút Xuất vào Toolbar
        toolbar.inflateMenu(R.menu.menu_summary)
        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_export) {
                showExportDialog()
                true
            } else {
                false
            }
        }

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

    private fun showExportDialog() {
        val options = arrayOf("Báo cáo Excel (.xlsx)", "Báo cáo PDF (.pdf)", "Dữ liệu thô (.csv)")
        
        AlertDialog.Builder(this)
            .setTitle("Chọn định dạng Xuất")
            .setItems(options) { dialog, which ->
                val type = when (which) {
                    0 -> TransactionViewModel.ExportType.XLSX
                    1 -> TransactionViewModel.ExportType.PDF
                    else -> TransactionViewModel.ExportType.CSV
                }
                processExport(type)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun processExport(type: TransactionViewModel.ExportType) {
        Toast.makeText(this, "Đang xử lý dữ liệu...", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            val uri = withContext(Dispatchers.IO) {
                viewModel.exportData(this@SummaryReportActivity, type)
            }
            if (uri != null) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    val mime = when (type) {
                        TransactionViewModel.ExportType.XLSX -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        TransactionViewModel.ExportType.PDF -> "application/pdf"
                        TransactionViewModel.ExportType.CSV -> "text/csv"
                    }
                    this.type = mime
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(intent, "Chia sẻ Báo cáo"))
            } else {
                Toast.makeText(this@SummaryReportActivity, "Lỗi quá trình xuất dữ liệu!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
