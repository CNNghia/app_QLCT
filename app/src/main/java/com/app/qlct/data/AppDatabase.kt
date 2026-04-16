package com.app.qlct.data

import android.content.Context
import androidx.room.*
import com.app.qlct.data.dao.TransactionDao
import com.app.qlct.data.local.WalletDao
import com.app.qlct.data.entity.Transaction
import com.app.qlct.model.Wallet

@Database(entities = [Transaction::class, Wallet::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun walletDao(): WalletDao

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
