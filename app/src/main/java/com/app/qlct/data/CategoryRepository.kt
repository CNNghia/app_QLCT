package com.app.qlct.data

import com.app.qlct.data.dao.CategoryDao
import com.app.qlct.data.entity.Category
import kotlinx.coroutines.flow.Flow

// Anh: Lớp Repository đóng vai trò trung gian, lấy dữ liệu từ CategoryDao rồi cung cấp cho ViewModel. Giúp tách biệt logic truy vấn DB khỏi UI.
class CategoryRepository(private val categoryDao: CategoryDao) {
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun getCategoriesOnce(): List<Category> = categoryDao.getCategoriesOnce()
    
    fun getCategoriesByType(type: String): Flow<List<Category>> = categoryDao.getCategoriesByType(type)
    
    suspend fun insertCategory(category: Category) = categoryDao.insertCategory(category)
    suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)
}
