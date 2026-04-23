package com.app.qlct

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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
    // Biến lưu trữ Toàn bộ dữ liệu của tháng hiện tại để sẵn sàng lọc
    private var allLoadedTransactions: List<com.app.qlct.data.entity.Transaction> = emptyList()
    private var dbCategories: List<String> = emptyList()
    private var currentSearchQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)

        filterType = intent.getStringExtra("TYPE")

        // Bắt giá trị nếu click từ Sơ Lược (Thống Kê) sang màn hình này
        val targetMonth = intent.getIntExtra("TARGET_MONTH", -1)
        val targetYear = intent.getIntExtra("TARGET_YEAR", -1)
        if (targetMonth != -1 && targetYear != -1) {
            currentMonth = targetMonth
            currentYear = targetYear
        }

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

        // Anh: Thêm tính năng vuốt phải để xóa (Swipe to Delete)
        val itemTouchHelperCallback = object : androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val transactionToDelete = adapter.currentList[position]
                
                androidx.appcompat.app.AlertDialog.Builder(this@TransactionsActivity)
                    .setTitle("Xóa giao dịch")
                    .setMessage("Bạn có chắc chắn muốn xóa giao dịch này không?")
                    .setPositiveButton("Xóa") { _, _ ->
                        viewModel.delete(transactionToDelete)
                        Toast.makeText(this@TransactionsActivity, "Đã xóa giao dịch!", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Hủy") { _, _ ->
                        adapter.notifyItemChanged(position)
                    }
                    .setOnCancelListener {
                        adapter.notifyItemChanged(position)
                    }
                    .show()
            }
        }
        androidx.recyclerview.widget.ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rvTransactions)

        val btnPrevMonth = findViewById<ImageView>(R.id.btnPrevMonth)
        val btnNextMonth = findViewById<ImageView>(R.id.btnNextMonth)
        val tvCurrentMonth = findViewById<TextView>(R.id.tvCurrentMonth)
        
        updateMonthText(tvCurrentMonth)
        
        // Anh: Vá lỗi Memory Leak - Chỉ Observe duy nhất 1 lần tại onCreate
        viewModel.currentMonthTransactions.observe(this) { transactions ->
            findViewById<View>(R.id.layoutLoading).visibility = View.GONE
            allLoadedTransactions = transactions // Lưu lại nguyên bản gốc của tháng này
            applySearchFilter() // Áp dụng search hiện tại ngay sau khi load tháng mới
        }

        // Anh: Vá lỗi tính tổng trên UI Thread - Chỉ Observe kết quả từ ViewModel
        viewModel.summaryResult.observe(this) { summary ->
            val df = DecimalFormat("#,###")
            findViewById<TextView>(R.id.tvTotalInTx).text = "+ ${df.format(summary.totalIn)} đ"
            findViewById<TextView>(R.id.tvTotalOutTx).text = "- ${df.format(summary.totalOut)} đ"
            
            val tvNet = findViewById<TextView>(R.id.tvNetTotalTx)
            tvNet.text = (if (summary.net >= 0) "+" else "") + df.format(summary.net) + " đ"
            tvNet.setTextColor(if (summary.net >= 0) android.graphics.Color.parseColor("#4CAF50") else android.graphics.Color.parseColor("#F44336"))
        }
        
        loadData()
        
        btnPrevMonth.setOnClickListener {
            currentMonth-- 
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

        // Load danh sách danh mục để chuẩn bị cho hộp lọc
        viewModel.allCategories.observe(this) { cats ->
            if (cats != null) {
                dbCategories = cats.map { it.name }.distinct()
            }
        }

        // Search realtime theo ghi chú và danh mục
        val searchView = findViewById<SearchView>(R.id.searchViewTransaction)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText?.trim() ?: ""
                applySearchFilter()
                return true
            }
        })

        findViewById<View>(R.id.fabFilter).setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_advanced_filter, null)
            val rgType = dialogView.findViewById<android.widget.RadioGroup>(R.id.rgFilterType)
            val btnDate = dialogView.findViewById<android.widget.Button>(R.id.btnFilterDate)
            val spinnerCat = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerFilterCategory)
            val etAmt = dialogView.findViewById<android.widget.EditText>(R.id.etFilterAmount)

            // Setup Spinner Danh mục
            val spinnerList = mutableListOf("Tất cả danh mục")
            spinnerList.addAll(dbCategories)
            val spinnerAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinnerList)
            spinnerCat.adapter = spinnerAdapter
            
            // Setup Chọn ngày
            var selectedDay = -1
            btnDate.setOnClickListener {
                val cal = Calendar.getInstance()
                android.app.DatePickerDialog(this, { _, y, m, d ->
                    selectedDay = d
                    btnDate.text = "Ngày được chọn: $d/${m+1}/$y"
                }, currentYear, currentMonth - 1, cal.get(Calendar.DAY_OF_MONTH)).show()
            }

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Bộ lọc nâng cao")
                .setView(dialogView)
                .setPositiveButton("Áp dụng Lọc") { _, _ ->
                    val isIncome = dialogView.findViewById<android.widget.RadioButton>(R.id.rbTypeIncome).isChecked
                    val isExpense = dialogView.findViewById<android.widget.RadioButton>(R.id.rbTypeExpense).isChecked
                    val catChosen = spinnerCat.selectedItem.toString()
                    val exactAmt = etAmt.text.toString().trim()
                    
                    val filteredList = allLoadedTransactions.filter { t ->
                        var match = true
                        
                        // 1. Lọc Loại
                        if (isIncome && t.type != "INCOME") match = false
                        if (isExpense && t.type != "EXPENSE") match = false
                        
                        // 2. Lọc Danh mục
                        if (catChosen != "Tất cả danh mục" && t.categoryName != catChosen) match = false
                        
                        // 3. Lọc Số tiền chính xác
                        if (exactAmt.isNotEmpty()) {
                            // So sánh kiểu ép kiểu để tránh lỗi 50000.0 khác 50000
                            val amtDouble = exactAmt.toDoubleOrNull()
                            if (amtDouble != null && t.amount != amtDouble) match = false
                        }
                        
                        // 4. Lọc Ngày
                        if (selectedDay != -1) {
                            val c = Calendar.getInstance()
                            c.timeInMillis = t.date
                            if (c.get(Calendar.DAY_OF_MONTH) != selectedDay) match = false
                        }
                        
                        match
                    }

                    adapter.submitList(filteredList)
                    updateSummary(filteredList)
                    
                    if (filteredList.isEmpty()) {
                        findViewById<View>(R.id.layoutEmpty).visibility = View.VISIBLE
                        findViewById<RecyclerView>(R.id.rvAllTransactions).visibility = View.GONE
                    } else {
                        findViewById<View>(R.id.layoutEmpty).visibility = View.GONE
                        findViewById<RecyclerView>(R.id.rvAllTransactions).visibility = View.VISIBLE
                    }
                }
                .setNegativeButton("Hủy / Đặt lại") { _, _ ->
                    adapter.submitList(allLoadedTransactions)
                    updateSummary(allLoadedTransactions)
                    if (allLoadedTransactions.isNotEmpty()) {
                         findViewById<View>(R.id.layoutEmpty).visibility = View.GONE
                         findViewById<RecyclerView>(R.id.rvAllTransactions).visibility = View.VISIBLE
                    }
                }
                .show()
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

        // Anh: Vá lỗi Memory Leak - Thay vì observe cái mới liên tục, ta chỉ truyền params cho ViewModel
        viewModel.setMonthFilter(filterType, startTime, endTime)
    }

    /**
     * Áp dụng bộ lọc tìm kiếm hiện tại lên allLoadedTransactions.
     * Gọi mỗi khi: (1) load tháng mới, (2) người dùng gõ đa text vào SearchView.
     */
    private fun applySearchFilter() {
        val layoutEmpty = findViewById<View>(R.id.layoutEmpty)
        val rvTransactions = findViewById<RecyclerView>(R.id.rvAllTransactions)

        val filtered = if (currentSearchQuery.isEmpty()) {
            allLoadedTransactions
        } else {
            val query = currentSearchQuery.lowercase()
            allLoadedTransactions.filter { t ->
                t.note.lowercase().contains(query) ||
                t.categoryName.lowercase().contains(query) ||
                t.walletName.lowercase().contains(query)
            }
        }

        if (filtered.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            rvTransactions.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            rvTransactions.visibility = View.VISIBLE
        }
        adapter.submitList(filtered)
        updateSummary(filtered)
    }

    private fun updateSummary(transactions: List<com.app.qlct.data.entity.Transaction>) {
        // Anh: Dời logic chạy vòng lặp cực nặng ra khỏi UI Thread, đẩy sang ViewModel xử lý
        viewModel.calculateSummaryAsync(transactions)
    }

    private fun updateMonthText(tv: TextView) {
        val monthStr = if (currentMonth < 10) "0$currentMonth" else "$currentMonth"
        tv.text = "Tháng $monthStr, $currentYear"
    }
}
