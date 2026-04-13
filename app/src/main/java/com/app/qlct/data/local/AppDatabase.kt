package com.app.qlct.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.app.qlct.model.Wallet

/**
 * Room Database chính của ứng dụng.
 * Sử dụng Singleton pattern để đảm bảo chỉ có một instance duy nhất.
 *
 * Các thành viên khác sẽ thêm Entity (Transaction, Category) vào đây khi cần.
 */
@Database(
    entities = [Wallet::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun walletDao(): WalletDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_manager.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
