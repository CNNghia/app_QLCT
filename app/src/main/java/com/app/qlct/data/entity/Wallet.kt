package com.app.qlct.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity đại diện cho một ví trong hệ thống.
 * Mỗi ví lưu trữ tên, số dư hiện tại và đơn vị tiền tệ.
 */
@Entity(
    tableName = "wallets",
    indices = [Index(value = ["name"])]  // Tăng tốc getWalletByName()
)
data class Wallet(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val balance: Double,
    val currency: String = "VND",
    val createdAt: Long = System.currentTimeMillis()
)

