package com.app.qlct.presentation.viewmodel

import androidx.lifecycle.*
import com.app.qlct.data.entity.Transaction
import com.app.qlct.data.TransactionRepository
import com.app.qlct.data.WalletRepository
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val repository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val categoryRepository: com.app.qlct.data.CategoryRepository
) : ViewModel() {

    companion object {
        // Flag tĩnh — seedData chỉ chạy đúng 1 lần dù Activity recreate bao nhiêu lần
        private var isDataSeeded = false
    }

    // Anh: Luồng dữ liệu lấy tất cả giao dịch từ DB
    val allTransactions: LiveData<List<Transaction>> = repository.allTransactions.asLiveData()

    // Thịnh: Dữ liệu quan sát đã được tính tổng thu/chi nhóm theo từng tháng từ Database
    val monthlySummaries: LiveData<List<com.app.qlct.data.dto.MonthSummary>> = repository.getMonthlySummary().asLiveData()

    // Anh: Lấy danh sách danh mục và ví từ DB
    val allCategories: LiveData<List<com.app.qlct.data.entity.Category>> = categoryRepository.allCategories.asLiveData()
    val allWallets: LiveData<List<com.app.qlct.data.entity.Wallet>> = walletRepository.allWallets.asLiveData()

    init {
        // Chỉ chạy seed data 1 lần duy nhất (tránh lãng phí 2 query DB mỗi lần init)
        if (!isDataSeeded) {
            isDataSeeded = true
            seedWallets()
            seedCategories()
        }
    }

    private fun seedWallets() = viewModelScope.launch {
        val wallets = walletRepository.getWalletsOnce()
        if (wallets.isEmpty()) {
            val defaults = listOf("Ví Tiền Mặt", "Thẻ Tín Dụng VISA", "Tài Khoản Sacombank", "Ví MoMo", "ShopeePay")
            defaults.forEach { name ->
                walletRepository.insertWallet(com.app.qlct.data.entity.Wallet(name = name, balance = 0.0))
            }
        }
    }

    private fun seedCategories() = viewModelScope.launch {
        val categories = categoryRepository.getCategoriesOnce()
        if (categories.isEmpty()) {
            val income = listOf("Lương", "Gia đình trợ cấp", "Bán hàng online", "Lộc")
            val expense = listOf("Ăn uống", "Shopping", "Giải trí", "Xe cộ", "Hóa đơn điện nước", "Nhà cửa", "Chi phí phát sinh")
            
            income.forEach { name ->
                categoryRepository.insertCategory(com.app.qlct.data.entity.Category(name = name, type = "INCOME"))
            }
            expense.forEach { name ->
                categoryRepository.insertCategory(com.app.qlct.data.entity.Category(name = name, type = "EXPENSE"))
            }
        }
    }

    // Anh: Xử lý thêm mới giao dịch và cập nhật số dư ví
    fun insert(transaction: Transaction) = viewModelScope.launch {
        repository.insert(transaction)
        updateWalletBalance(transaction.walletName, transaction.amount, transaction.type, isAddition = true)
    }

    // Anh: Xử lý cập nhật giao dịch (Hoàn tác ví cũ -> Sửa DB -> Áp dụng ví mới)
    fun update(newTransaction: Transaction) = viewModelScope.launch {
        val oldTransaction = repository.getTransactionById(newTransaction.id) ?: return@launch
        
        // 1. Hoàn tác số tiền từ transaction cũ
        updateWalletBalance(oldTransaction.walletName, oldTransaction.amount, oldTransaction.type, isAddition = false)
        
        // 2. Cập nhật transaction mới vào DB
        repository.update(newTransaction)
        
        // 3. Áp dụng số tiền từ transaction mới
        updateWalletBalance(newTransaction.walletName, newTransaction.amount, newTransaction.type, isAddition = true)
    }

    // Anh: Xử lý xóa giao dịch và hoàn lại tiền vào ví
    fun delete(transaction: Transaction) = viewModelScope.launch {
        repository.delete(transaction)
        updateWalletBalance(transaction.walletName, transaction.amount, transaction.type, isAddition = false)
    }

    /**
     * Hàm nội bộ dùng để tính toán và cập nhật số dư ví dựa trên giao dịch.
     * Nếu không tìm thấy ví (ví đã bị xóa) → bỏ qua nhưng ghi log cảnh báo.
     */
    private suspend fun updateWalletBalance(walletName: String, amount: Double, type: String, isAddition: Boolean) {
        val wallet = walletRepository.getWalletByName(walletName)
        if (wallet == null) {
            android.util.Log.w("TransactionViewModel", "updateWalletBalance: không tìm thấy ví '$walletName' — số dư không được cập nhật")
            return
        }

        val delta = if (type == com.app.qlct.data.TransactionType.INCOME) {
            if (isAddition) amount else -amount
        } else {
            if (isAddition) -amount else amount
        }

        walletRepository.updateWallet(wallet.copy(balance = wallet.balance + delta))
    }

    fun getTransactionsByType(type: String): LiveData<List<Transaction>> {
        return repository.getTransactionsByType(type).asLiveData()
    }

    fun getTransactionsByMonth(start: Long, end: Long): LiveData<List<Transaction>> {
        return repository.getTransactionsByMonth(start, end).asLiveData()
    }

    fun getTransactionsByTypeInMonth(type: String, start: Long, end: Long): LiveData<List<Transaction>> {
        return repository.getTransactionsByTypeInMonth(type, start, end).asLiveData()
    }

    // Anh: Vá lỗi Memory Leak bằng cách dùng switchMap
    data class MonthFilter(val type: String?, val start: Long, val end: Long)
    private val _monthFilter = MutableLiveData<MonthFilter>()
    
    val currentMonthTransactions: LiveData<List<Transaction>> = _monthFilter.switchMap { filter ->
        if (filter.type != null) {
            repository.getTransactionsByTypeInMonth(filter.type, filter.start, filter.end).asLiveData()
        } else {
            repository.getTransactionsByMonth(filter.start, filter.end).asLiveData()
        }
    }

    fun setMonthFilter(type: String?, start: Long, end: Long) {
        _monthFilter.value = MonthFilter(type, start, end)
    }

    // Anh: Vá lỗi tính tổng trên UI Thread
    data class SummaryResult(val totalIn: Double, val totalOut: Double, val net: Double)
    
    private val _summaryResult = MutableLiveData<SummaryResult>()
    val summaryResult: LiveData<SummaryResult> = _summaryResult

    fun calculateSummaryAsync(transactions: List<Transaction>) {
        // Đẩy việc tính toán mảng sang luồng nền (Default) thay vì luồng UI (Main)
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            var totalIn = 0.0
            var totalOut = 0.0
            for (t in transactions) {
                if (t.type == com.app.qlct.data.TransactionType.INCOME) totalIn += t.amount else totalOut += t.amount
            }
            _summaryResult.postValue(SummaryResult(totalIn, totalOut, totalIn - totalOut))
        }
    }

    /** Helper class cho logic giám sát ngân sách từ BudgetActivity */
    data class BudgetState(
        val monthlyExpenses: Double,
        val percent: Int,
        val remaining: Double,
        val statusColorHex: String,
        val warningText: String?,
        val isSafe: Boolean
    )

    fun calculateBudgetState(budgetLimit: Double, transactions: List<Transaction>, currentMonth: Int, currentYear: Int): BudgetState {
        val monthlyExpenses = transactions.filter {
            val tc = java.util.Calendar.getInstance()
            tc.timeInMillis = it.date
            it.type == com.app.qlct.data.TransactionType.EXPENSE && tc.get(java.util.Calendar.MONTH) == currentMonth && tc.get(java.util.Calendar.YEAR) == currentYear
        }.sumOf { it.amount }

        if (budgetLimit <= 0) {
            return BudgetState(monthlyExpenses, 0, 0.0, "#4CAF50", null, false)
        }

        val remaining = budgetLimit - monthlyExpenses
        val percent = ((monthlyExpenses / budgetLimit) * 100).toInt()

        val color = when {
            percent >= 100 -> "#D32F2F" // Vượt quá ngân sách (Đỏ chót)
            percent >= 80 -> "#F57C00" // Xài tới 80% ngân sách (Cam cảnh báo)
            else -> "#4CAF50" // An toàn
        }
        
        val warning = if (percent in 80..99) "Chú ý: Bạn đã sử dụng ${percent}% năng lực tài chính giới hạn!" else null
        
        return BudgetState(monthlyExpenses, percent.coerceAtMost(100), remaining, color, warning, percent < 80)
    }

    enum class ExportType { CSV, PDF, XLSX }

    // Thịnh: Tính năng xuất báo cáo các loại định dạng
    suspend fun exportData(context: android.content.Context, type: ExportType): android.net.Uri? {
        val transactions = repository.getAllTransactionsOnce()
        val exportDir = java.io.File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()

        val fileName = "QLCT_Report_${System.currentTimeMillis()}"
        val file: java.io.File

        when (type) {
            ExportType.CSV -> {
                file = java.io.File(exportDir, "$fileName.csv")
                file.writeText("\uFEFFNgày,Loại,Danh mục,Ví,Số tiền,Ghi chú\n") // UTF-8 BOM
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                transactions.forEach { t ->
                    val dateStr = sdf.format(java.util.Date(t.date))
                    val amountStr = "%.0f".format(t.amount)
                    val safeNote = t.note.replace("\"", "\"\"") // Escape quotes in CSV
                    file.appendText("$dateStr,${t.type},${t.categoryName},${t.walletName},$amountStr,\"$safeNote\"\n")
                }
            }
            ExportType.PDF -> {
                file = java.io.File(exportDir, "$fileName.pdf")
                val document = android.graphics.pdf.PdfDocument()
                val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size roughly
                var page = document.startPage(pageInfo)
                var canvas = page.canvas
                val paint = android.graphics.Paint()
                paint.textSize = 12f
                paint.color = android.graphics.Color.BLACK
                
                var top = 50f
                canvas.drawText("BÁO CÁO GIAO DỊCH", 50f, top, paint)
                top += 30f
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                transactions.forEach { t ->
                    if (top > 800f) {
                        document.finishPage(page)
                        page = document.startPage(pageInfo)
                        canvas = page.canvas
                        top = 50f
                    }
                    val text = "${sdf.format(java.util.Date(t.date))} | ${if(t.type=="INCOME") "+" else "-"}${t.amount} | ${t.categoryName} (${t.walletName})"
                    canvas.drawText(text, 50f, top, paint)
                    top += 20f
                }
                document.finishPage(page)
                val out = java.io.FileOutputStream(file)
                document.writeTo(out)
                document.close()
                out.close()
            }
            ExportType.XLSX -> {
                file = java.io.File(exportDir, "$fileName.xlsx")
                try {
                    val workbook = org.apache.poi.xssf.usermodel.XSSFWorkbook()
                    val sheet = workbook.createSheet("Transactions")
                    val headerRow = sheet.createRow(0)
                    headerRow.createCell(0).setCellValue("Ngày")
                    headerRow.createCell(1).setCellValue("Loại")
                    headerRow.createCell(2).setCellValue("Danh mục")
                    headerRow.createCell(3).setCellValue("Ví")
                    headerRow.createCell(4).setCellValue("Số tiền")
                    headerRow.createCell(5).setCellValue("Ghi chú")

                    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                    var rowNum = 1
                    for (t in transactions) {
                        val row = sheet.createRow(rowNum++)
                        row.createCell(0).setCellValue(sdf.format(java.util.Date(t.date)))
                        row.createCell(1).setCellValue(if(t.type=="INCOME") "Thu" else "Chi")
                        row.createCell(2).setCellValue(t.categoryName)
                        row.createCell(3).setCellValue(t.walletName)
                        row.createCell(4).setCellValue(t.amount)
                        row.createCell(5).setCellValue(t.note)
                    }
                    val out = java.io.FileOutputStream(file)
                    workbook.write(out)
                    out.close()
                    workbook.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                    return null
                }
            }
        }

        return androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }
}

class TransactionViewModelFactory(
    private val repository: com.app.qlct.data.TransactionRepository,
    private val walletRepository: com.app.qlct.data.WalletRepository,
    private val categoryRepository: com.app.qlct.data.CategoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(repository, walletRepository, categoryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
