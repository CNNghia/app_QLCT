package com.app.qlct.data

import com.app.qlct.data.dao.TransactionDao
import com.app.qlct.data.entity.Transaction
import com.app.qlct.data.dto.MonthSummary
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {
    
    // Anh: Dòng dữ liệu quan sát tất cả giao dịch
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    // Thịnh: Dữ liệu quan sát đã được tính tổng thu chi theo từng tháng từ Backend/Local Database
    fun getMonthlySummary(): Flow<List<MonthSummary>> = transactionDao.getMonthlySummary()

    // Anh: Phương thức trung gian để thực hiện CRUD qua DAO
    suspend fun insert(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun getTransactionById(id: Long) = transactionDao.getTransactionById(id)

    suspend fun update(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun delete(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    fun getTransactionsByType(type: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByType(type)
    }

    fun getTransactionsByMonth(start: Long, end: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByDateRange(start, end)
    }

    fun getTransactionsByTypeInMonth(type: String, start: Long, end: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByTypeAndDateRange(type, start, end)
    }

    // Kiểm tra số giao dịch của ví trước khi xóa
    suspend fun countByWalletName(walletName: String): Int {
        return transactionDao.countByWalletName(walletName)
    }

    // Thịnh: Lấy toàn bộ dữ liệu 1 lần tĩnh để Xuất File
    suspend fun getAllTransactionsOnce(): List<Transaction> {
        return transactionDao.getAllTransactionsOnce()
    }
}
