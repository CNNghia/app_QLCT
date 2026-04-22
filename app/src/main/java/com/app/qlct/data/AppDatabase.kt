package com.app.qlct.data

import android.content.Context
import androidx.room.*
import com.app.qlct.data.dao.TransactionDao
import com.app.qlct.data.local.WalletDao
import com.app.qlct.data.entity.Transaction
import com.app.qlct.model.Wallet

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
                    "expense_manager_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }

        // Alias for compatibility with Wallet module
        fun getInstance(context: Context): AppDatabase = getDatabase(context)
    }
}
