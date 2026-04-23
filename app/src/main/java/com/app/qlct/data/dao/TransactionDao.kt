package com.app.qlct.data.dao

import androidx.room.*
import com.app.qlct.data.entity.Transaction
import kotlinx.coroutines.flow.Flow

// Anh: Data Access Object (DAO) chứa các câu lệnh SQL để thao tác với bảng transactions
@Dao
interface TransactionDao {
    // Anh: Lấy tất cả giao dịch sắp xếp theo ngày mới nhất
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    // Anh: Thêm mới một giao dịch
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    // Anh: Sửa một giao dịch đã có
    @Update
    suspend fun updateTransaction(transaction: Transaction)

    // Anh: Xóa một giao dịch
    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date >= :start AND date <= :end ORDER BY date DESC")
    fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE type = :type AND date >= :start AND date <= :end ORDER BY date DESC")
    fun getTransactionsByTypeAndDateRange(type: String, start: Long, end: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transaction?

    @Query("SELECT COUNT(*) FROM transactions WHERE walletName = :walletName")
    suspend fun countByWalletName(walletName: String): Int

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactionsOnce(): List<Transaction>
}
