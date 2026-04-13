package com.app.qlct.data.local

import androidx.room.*
import com.app.qlct.model.Wallet
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) cho bảng wallets.
 * Cung cấp tất cả các thao tác CRUD với Room Database.
 */
@Dao
interface WalletDao {

    @Query("SELECT * FROM wallets ORDER BY createdAt DESC")
    fun getAllWallets(): Flow<List<Wallet>>

    @Query("SELECT * FROM wallets WHERE id = :id")
    suspend fun getWalletById(id: Int): Wallet?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: Wallet)

    @Update
    suspend fun updateWallet(wallet: Wallet)

    @Delete
    suspend fun deleteWallet(wallet: Wallet)

    @Query("SELECT SUM(balance) FROM wallets")
    fun getTotalBalance(): Flow<Double?>
}
