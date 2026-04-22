package com.app.qlct.data.dao

import androidx.room.*
import com.app.qlct.data.entity.Category
import kotlinx.coroutines.flow.Flow

// Anh: DAO chứa các câu lệnh SQL để thao tác với bảng danh mục (categories)
@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories")
    suspend fun getCategoriesOnce(): List<Category>

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name ASC")
    fun getCategoriesByType(type: String): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)
}
