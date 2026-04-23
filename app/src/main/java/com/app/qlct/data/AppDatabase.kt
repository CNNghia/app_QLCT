package com.app.qlct.data

import android.content.Context
import androidx.room.*
import com.app.qlct.data.dao.TransactionDao
import com.app.qlct.data.local.WalletDao
import com.app.qlct.data.entity.Transaction
import com.app.qlct.data.entity.Wallet

// Anh: Nơi cấu hình toàn bộ cơ sở dữ liệu Room, bao gồm các bảng (entities) và phiên bản (version = 2 do có thêm bảng Category)
@Database(entities = [Transaction::class, Wallet::class, com.app.qlct.data.entity.Category::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun walletDao(): WalletDao
    abstract fun categoryDao(): com.app.qlct.data.dao.CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    AppConstants.DB_NAME
                )
                // Nếu nâng version DB mà chưa viết Migration → xóa & tạo lại DB (tránh crash)
                // TODO: Thay bằng addMigrations(...) khi phát triển thêm tính năng có thay đổi schema
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        // Alias cho Wallet module dùng getInstance()
        fun getInstance(context: Context): AppDatabase = getDatabase(context)
    }
}

