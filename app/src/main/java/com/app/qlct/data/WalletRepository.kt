package com.app.qlct.data

import com.app.qlct.data.local.WalletDao
import com.app.qlct.model.Wallet
import kotlinx.coroutines.flow.Flow

/**
 * Repository trung gian giữa ViewModel và DAO.
 * Đây là single source of truth cho data Wallet.
 */
class WalletRepository(private val walletDao: WalletDao) {

    val allWallets: Flow<List<Wallet>> = walletDao.getAllWallets()

    // Anh: Hàm phụ trợ lấy danh sách ví tĩnh phục vụ cho tính năng Seed Data
    suspend fun getWalletsOnce(): List<Wallet> = walletDao.getWalletsOnce()

    val totalBalance: Flow<Double?> = walletDao.getTotalBalance()

    suspend fun getWalletById(id: Int): Wallet? = walletDao.getWalletById(id)
    
    suspend fun getWalletByName(name: String): Wallet? = walletDao.getWalletByName(name)

    suspend fun insertWallet(wallet: Wallet) = walletDao.insertWallet(wallet)

    suspend fun updateWallet(wallet: Wallet) = walletDao.updateWallet(wallet)

    suspend fun deleteWallet(wallet: Wallet) = walletDao.deleteWallet(wallet)
}
