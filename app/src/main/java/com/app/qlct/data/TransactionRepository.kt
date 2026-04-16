package com.app.qlct.data

import com.app.qlct.data.dao.TransactionDao
import com.app.qlct.data.entity.Transaction
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {
    
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    suspend fun insert(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

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
}
