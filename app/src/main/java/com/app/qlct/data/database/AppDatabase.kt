package com.app.qlct.data.database

import android.content.Context
import androidx.room.*
import com.app.qlct.data.dao.TransactionDao
import com.app.qlct.data.entity.Transaction

@Database(entities = [Transaction::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

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
    }
}
