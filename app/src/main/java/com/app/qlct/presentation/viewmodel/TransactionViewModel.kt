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

    // Anh: Luồng dữ liệu lấy tất cả giao dịch từ DB
    val allTransactions: LiveData<List<Transaction>> = repository.allTransactions.asLiveData()

    // Anh: Lấy danh sách danh mục và ví từ DB
    val allCategories: LiveData<List<com.app.qlct.data.entity.Category>> = categoryRepository.allCategories.asLiveData()
    val allWallets: LiveData<List<com.app.qlct.model.Wallet>> = walletRepository.allWallets.asLiveData()

    init {
        seedWallets()
        seedCategories()
    }

    private fun seedWallets() = viewModelScope.launch {
        val wallets = walletRepository.getWalletsOnce()
        if (wallets.isEmpty()) {
            val defaults = listOf("Ví Tiền Mặt", "Thẻ Tín Dụng VISA", "Tài Khoản Sacombank", "Ví MoMo", "ShopeePay")
            defaults.forEach { name ->
                walletRepository.insertWallet(com.app.qlct.model.Wallet(name = name, balance = 0.0))
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
     * Anh: Hàm nội bộ dùng để tính toán và cập nhật số dư ví dựa trên giao dịch
     */
    private suspend fun updateWalletBalance(walletName: String, amount: Double, type: String, isAddition: Boolean) {
        val wallet = walletRepository.getWalletByName(walletName) ?: return
        
        val delta = if (type == "INCOME") {
            if (isAddition) amount else -amount
        } else {
            if (isAddition) -amount else amount
        }
        
        val newWallet = wallet.copy(balance = wallet.balance + delta)
        walletRepository.updateWallet(newWallet)
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
}

class TransactionViewModelFactory(
    private val repository: TransactionRepository,
    private val walletRepository: WalletRepository,
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
