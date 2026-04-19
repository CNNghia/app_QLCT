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
import com.app.qlct.data.entity.Transaction
import com.app.qlct.data.TransactionRepository
import com.app.qlct.presentation.viewmodel.TransactionViewModel
import com.app.qlct.presentation.viewmodel.TransactionViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    // Anh: Khởi tạo database, repository và viewModel theo mô hình MVVM
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { TransactionRepository(database.transactionDao()) }
    private val walletRepository by lazy { com.app.qlct.data.WalletRepository(database.walletDao()) }
    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(repository, walletRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

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

        // Anh: Xử lý chọn Danh mục thu/chi dựa trên loại giao dịch hiện tại
        btnSelectCategory.setOnClickListener {
            val isIncome = toggleTransactionType.checkedButtonId == R.id.btnTypeIncome
            val categories = if (isIncome) {
                arrayOf("Lương", "Nhận tiền từ Cty", "Gia đình trợ cấp", "Bán hàng online", "Tiền Lì Xì")
            } else {
                arrayOf("Ăn uống", "Shopping", "Giải trí", "Đổ xăng", "Hóa đơn điện nước")
            }
            
            AlertDialog.Builder(this)
                .setTitle(if (isIncome) "Chọn danh mục thu" else "Chọn danh mục chi")
                .setItems(categories) { _, which ->
                    tvCategoryName.text = categories[which]
                    val colorHex = if (isIncome) "#4CAF50" else "#F44336"
                    ivCategoryIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(colorHex))
                }
                .show()
        }

        // Xử lý chọn Ví
        btnSelectWallet.setOnClickListener {
            val wallets = arrayOf("Ví Tiền Mặt", "Thẻ Tín Dụng VISA", "Tài Khoản Sacombank", "Ví MoMo", "ShopeePay")
            AlertDialog.Builder(this)
                .setTitle("Chọn ví nguồn/đích")
                .setItems(wallets) { _, which ->
                    tvWalletName.text = wallets[which]
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

            if (isEditMode) {
                viewModel.update(transaction)
                Toast.makeText(this, "Đã cập nhật giao dịch!", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.insert(transaction)
                Toast.makeText(this, "Đã lưu giao dịch thành công!", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }
}
