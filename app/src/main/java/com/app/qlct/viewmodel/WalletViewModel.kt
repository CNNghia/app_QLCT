package com.app.qlct.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.qlct.data.AppDatabase
import com.app.qlct.data.WalletRepository
import com.app.qlct.model.Wallet
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

    private val repository: WalletRepository = WalletRepository(
        AppDatabase.getInstance(application).walletDao()
    )

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

    fun deleteWallet(wallet: Wallet) {
        viewModelScope.launch {
            repository.deleteWallet(wallet)
        }
    }
}
