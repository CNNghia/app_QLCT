package com.app.qlct

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.app.qlct.data.AppDatabase
import com.app.qlct.data.AppPrefs
import com.app.qlct.data.entity.Transaction
import com.app.qlct.data.TransactionRepository
import com.app.qlct.data.WalletRepository
import com.app.qlct.data.CategoryRepository
import com.app.qlct.presentation.viewmodel.TransactionViewModel
import com.app.qlct.presentation.viewmodel.TransactionViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    // Anh: Khởi tạo database, repository và viewModel theo mô hình MVVM
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { TransactionRepository(database.transactionDao()) }
    private val walletRepository by lazy { WalletRepository(database.walletDao()) }
    private val categoryRepository by lazy { CategoryRepository(database.categoryDao()) }
    
    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(repository, walletRepository, categoryRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        var currentMonthlyExpenses = 0.0
        var dbCategories: List<com.app.qlct.data.entity.Category> = emptyList()
        var dbWallets: List<com.app.qlct.model.Wallet> = emptyList()
        
        viewModel.allTransactions.observe(this) { transactions ->
            if (transactions != null) {
                val cal = Calendar.getInstance()
                val currentMonth = cal.get(Calendar.MONTH)
                val currentYear = cal.get(Calendar.YEAR)
                currentMonthlyExpenses = transactions.filter {
                    val tc = Calendar.getInstance()
                    tc.timeInMillis = it.date
                    it.type == "EXPENSE" && tc.get(Calendar.MONTH) == currentMonth && tc.get(Calendar.YEAR) == currentYear
                }.sumOf { it.amount }
            }
        }
        viewModel.allCategories.observe(this) { cats ->
            if (cats != null) dbCategories = cats
        }
        viewModel.allWallets.observe(this) { wals ->
            if (wals != null) dbWallets = wals
        }

        val btnClose = findViewById<ImageView>(R.id.btnClose)
        val btnSave = findViewById<MaterialButton>(R.id.btnSave)
        val etAmount = findViewById<EditText>(R.id.etInputAmount)
        val etNote = findViewById<EditText>(R.id.etNote)
        val tvCategoryName = findViewById<TextView>(R.id.tvCategoryName)
        val tvWalletName = findViewById<TextView>(R.id.tvWalletName)
        val ivCategoryIcon = findViewById<ImageView>(R.id.ivCategoryIcon)
        val toggleTransactionType = findViewById<MaterialButtonToggleGroup>(R.id.toggleTransactionType)
        val btnSelectCategory = findViewById<LinearLayout>(R.id.btnSelectCategory)
        val btnSelectWallet = findViewById<LinearLayout>(R.id.btnSelectWallet)

        btnClose.setOnClickListener { finish() }

        // Anh: Xử lý chọn Danh mục thu/chi đồng bộ từ Database
        btnSelectCategory.setOnClickListener {
            val isIncome = toggleTransactionType.checkedButtonId == R.id.btnTypeIncome
            
            // Lọc danh sjánh sách từ Database thật thay vì mảng Fake
            val filteredCats = dbCategories.filter { it.type == (if (isIncome) "INCOME" else "EXPENSE") }.map { it.name }
            val categoriesArray = if (filteredCats.isNotEmpty()) {
                filteredCats.toTypedArray()
            } else {
                if (isIncome) arrayOf("Lương", "Gia đình trợ cấp", "Bán hàng online", "Lộc")
                else arrayOf("Ăn uống", "Shopping", "Giải trí", "Xe cộ", "Hóa đơn điện nước", "Nhà cửa", "Chi phí phát sinh")
            }
            
            AlertDialog.Builder(this)
                .setTitle(if (isIncome) "Chọn danh mục thu" else "Chọn danh mục chi")
                .setItems(categoriesArray) { _, which ->
                    tvCategoryName.text = categoriesArray[which]
                    val colorHex = if (isIncome) "#4CAF50" else "#F44336"
                    ivCategoryIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(colorHex))
                }
                .show()
        }

        // Xử lý chọn Ví đồng bộ từ Database
        btnSelectWallet.setOnClickListener {
            val walletNames = dbWallets.map { it.name }
            val walletsArray = if (walletNames.isNotEmpty()) {
                walletNames.toTypedArray()
            } else {
                arrayOf("Ví Tiền Mặt", "Thẻ Tín Dụng VISA", "Tài Khoản Sacombank", "Ví MoMo", "ShopeePay")
            }
            
            AlertDialog.Builder(this)
                .setTitle("Chọn ví nguồn/đích")
                .setItems(walletsArray) { _, which ->
                    tvWalletName.text = walletsArray[which]
                }
                .show()
        }

        val btnSelectDate = findViewById<LinearLayout>(R.id.btnSelectDate)
        val tvSelectedDate = findViewById<TextView>(R.id.tvSelectedDateText)
        val calendar = Calendar.getInstance()
        var selectedDateTimestamp = calendar.timeInMillis

        btnSelectDate.setOnClickListener {
            val datePickerDialog = android.app.DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedDateTimestamp = calendar.timeInMillis
                    
                    // Cập nhật text hiển thị (Ví dụ: 13/04/2026)
                    val dateFormatted = "${dayOfMonth}/${month + 1}/${year}"
                    tvSelectedDate?.text = dateFormatted
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // Kiểm tra xem có phải đang ở chế độ SỬA không
        val transactionId = intent.getLongExtra("TRANSACTION_ID", -1L)
        val isEditMode = transactionId != -1L

        if (isEditMode) {
            val amount = intent.getDoubleExtra("AMOUNT", 0.0)
            val note = intent.getStringExtra("NOTE") ?: ""
            val category = intent.getStringExtra("CATEGORY") ?: ""
            val wallet = intent.getStringExtra("WALLET") ?: ""
            val date = intent.getLongExtra("DATE", System.currentTimeMillis())
            val type = intent.getStringExtra("TYPE") ?: "EXPENSE"

            etAmount.setText(if (amount % 1.0 == 0.0) amount.toLong().toString() else amount.toString())
            etNote.setText(note)
            tvCategoryName.text = category
            tvWalletName.text = wallet
            selectedDateTimestamp = date
            
            val calendarEdit = Calendar.getInstance()
            calendarEdit.timeInMillis = date
            tvSelectedDate?.text = "${calendarEdit.get(Calendar.DAY_OF_MONTH)}/${calendarEdit.get(Calendar.MONTH) + 1}/${calendarEdit.get(Calendar.YEAR)}"

            if (type == "INCOME") toggleTransactionType.check(R.id.btnTypeIncome) else toggleTransactionType.check(R.id.btnTypeExpense)
            
            btnSave.text = "CẬP NHẬT"
            findViewById<TextView>(R.id.tvTitle).text = "Sửa giao dịch"
        }

        // Anh: Xử lý nút LƯU với Validation và cập nhật vào Database (bao gồm cả cập nhật số dư ví)
        btnSave.setOnClickListener {
            val amountStr = etAmount.text.toString().trim()
            val note = etNote.text.toString().trim()
            val category = tvCategoryName.text.toString()
            val wallet = tvWalletName.text.toString()
            val type = if (toggleTransactionType.checkedButtonId == R.id.btnTypeIncome) "INCOME" else "EXPENSE"

            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số tiền!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val amount = amountStr.toDoubleOrNull() ?: 0.0
            if (amount <= 0) {
                Toast.makeText(this, "Số tiền phải lớn hơn 0!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (category == "Chọn danh mục") {
                Toast.makeText(this, "Vui lòng chọn danh mục!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val transaction = Transaction(
                id = if (isEditMode) transactionId else 0L,
                amount = amount,
                note = note,
                categoryName = category,
                walletName = wallet,
                date = selectedDateTimestamp,
                type = type
            )

            // Hàm lưu thật sự
            fun saveRealDataAndFinish() {
                if (isEditMode) {
                    viewModel.update(transaction)
                    Toast.makeText(this@AddTransactionActivity, "Đã cập nhật giao dịch!", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.insert(transaction)
                    Toast.makeText(this@AddTransactionActivity, "Đã lưu giao dịch thành công!", Toast.LENGTH_SHORT).show()
                }
                finish()
            }

            // Gọi ngân sách đã thiết lập (nếu có)
            val prefs = AppPrefs.get(this)
            val budgetLimit = prefs.getLong(AppPrefs.KEY_BUDGET_LIMIT, 0L).toDouble()

            // Kiểm tra Logic lố tiền nếu là Chi phí
            if (type == "EXPENSE" && budgetLimit > 0) {
                // Nếu sửa, ta phải trừ số tiền cũ ra trước khi cộng số tiền mới vào để tính chính xác
                val oldAmount = if (isEditMode && intent.getStringExtra("TYPE") == "EXPENSE") intent.getDoubleExtra("AMOUNT", 0.0) else 0.0
                
                // Mức chi phí trong tháng nếu bill này được duyệt
                val futureTotalExpense = currentMonthlyExpenses - oldAmount + amount

                if (futureTotalExpense > budgetLimit) {
                    val overAmount = futureTotalExpense - budgetLimit
                    val df = java.text.DecimalFormat("#,###")
                    
                    AlertDialog.Builder(this@AddTransactionActivity)
                        .setTitle("Cảnh báo vượt ngân sách \u26A0\uFE0F")
                        .setMessage("Bạn đã thiết lập ngân sách tháng này, khoản chi tiêu này sẽ khiến bạn vượt lố mất ${df.format(overAmount).replace(",", ".")} đ. \n\nBạn vẫn có muốn chấp nhận chi tiêu khoản tiền này hay không?")
                        .setPositiveButton("Vẫn chi") { _, _ -> saveRealDataAndFinish() }
                        .setNegativeButton("Hủy bỏ", null)
                        .setCancelable(false)
                        .show()
                    return@setOnClickListener
                }
            }

            // Nếu an toàn không lố ngân sách => Lưu luôn
            saveRealDataAndFinish()
        }
    }
}
