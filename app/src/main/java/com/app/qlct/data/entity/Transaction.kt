package com.app.qlct.data.entity

import androidx.room.*

// Anh: Model dữ liệu (Entity) đại diện cho một giao dịch trong Database
@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["date"]),          // Tăng tốc filter theo tháng
        Index(value = ["type"]),          // Tăng tốc lọc INCOME/EXPENSE
        Index(value = ["walletName"])     // Tăng tốc countByWalletName & updateBalance
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val note: String,
    val categoryName: String,
    val walletName: String,
    val date: Long, // Timestamp
    val type: String // Dùng Constants.TransactionType.INCOME / EXPENSE
)
