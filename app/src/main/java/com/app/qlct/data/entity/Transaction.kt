package com.app.qlct.data.entity

import androidx.room.*

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val note: String,
    val categoryName: String,
    val walletName: String,
    val date: Long, // Timestamp
    val type: String // "INCOME" or "EXPENSE"
)
