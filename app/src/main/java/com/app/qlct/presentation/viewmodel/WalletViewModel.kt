package com.app.qlct.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.qlct.data.AppDatabase
import com.app.qlct.data.TransactionRepository
import com.app.qlct.data.WalletRepository
import com.app.qlct.data.entity.Wallet
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel cho màn hình Wallet.
 * Expose StateFlow để Compose UI observe.
 */
class WalletViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository: WalletRepository = WalletRepository(db.walletDao())
    private val txRepository: TransactionRepository = TransactionRepository(db.transactionDao())

    /** Danh sách tất cả ví, cập nhật tự động khi DB thay đổi */
    val wallets: StateFlow<List<Wallet>> = repository.allWallets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** Tổng số dư của tất cả ví */
    val totalBalance: StateFlow<Double> = repository.totalBalance
        .map { it ?: 0.0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0.0
        )

    /**
     * Kết quả kiểm tra trước khi xóa ví:
     * null = chưa check, false = an toàn, true = có linked transactions
     */
    var deleteCheckResult: ((hasLinkedTransactions: Boolean, wallet: Wallet) -> Unit)? = null

    fun addWallet(name: String, balance: Double, currency: String) {
        viewModelScope.launch {
            val wallet = Wallet(
                name = name.trim(),
                balance = balance,
                currency = currency.trim().ifEmpty { "VND" }
            )
            repository.insertWallet(wallet)
        }
    }

    fun updateWallet(wallet: Wallet) {
        viewModelScope.launch {
            repository.updateWallet(wallet)
        }
    }

    /**
     * Xóa ví với kiểm tra trước.
     * @param wallet ví cần xóa
     * @param onHasTransactions callback khi ví còn giao dịch liên quan
     * @param onSafe callback khi ví không có giao dịch nào — xóa ngay
     */
    fun deleteWalletSafe(
        wallet: Wallet,
        onHasTransactions: (count: Int) -> Unit,
        onSafe: () -> Unit
    ) {
        viewModelScope.launch {
            val count = txRepository.countByWalletName(wallet.name)
            if (count > 0) {
                onHasTransactions(count)
            } else {
                repository.deleteWallet(wallet)
                onSafe()
            }
        }
    }

    /** Xóa ví ngay không cần kiểm tra (dùng khi người dùng đã xác nhận xóa cả giao dịch) */
    fun deleteWallet(wallet: Wallet) {
        viewModelScope.launch {
            repository.deleteWallet(wallet)
        }
    }
}
