package com.app.qlct.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity đại diện cho một ví trong hệ thống.
 * Mỗi ví lưu trữ tên, số dư hiện tại và đơn vị tiền tệ.
 */
@Entity(tableName = "wallets")
data class Wallet(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val balance: Double,
    val currency: String = "VND",
    val createdAt: Long = System.currentTimeMillis()
)
