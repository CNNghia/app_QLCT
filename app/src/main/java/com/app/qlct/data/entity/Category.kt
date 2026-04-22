package com.app.qlct.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Anh: Model dữ liệu (Entity) đại diện cho một danh mục thu/chi trong Database
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "INCOME" or "EXPENSE"
    val icon: String? = null
)
