package com.app.qlct.data.repository

import com.app.qlct.data.local.WalletDao
import com.app.qlct.model.Wallet
import kotlinx.coroutines.flow.Flow

/**
 * Repository trung gian giữa ViewModel và DAO.
 * Đây là single source of truth cho data Wallet.
 */
class WalletRepository(private val walletDao: WalletDao) {

    val allWallets: Flow<List<Wallet>> = walletDao.getAllWallets()

    val totalBalance: Flow<Double?> = walletDao.getTotalBalance()

    suspend fun getWalletById(id: Int): Wallet? = walletDao.getWalletById(id)

    suspend fun insertWallet(wallet: Wallet) = walletDao.insertWallet(wallet)

    suspend fun updateWallet(wallet: Wallet) = walletDao.updateWallet(wallet)

    suspend fun deleteWallet(wallet: Wallet) = walletDao.deleteWallet(wallet)
}
