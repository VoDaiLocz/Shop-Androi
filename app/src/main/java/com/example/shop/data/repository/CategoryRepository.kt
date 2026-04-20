package com.example.shop.data.repository

import com.example.shop.data.local.dao.CategoryDao
import com.example.shop.data.model.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    //User và Admin: Xem danh sách
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    //Admin: Thêm, Xóa, Sửa
    suspend fun addCategory(category: Category) = categoryDao.insertCategory(category)
    suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)
    suspend fun updateCategory(category: Category) = categoryDao.updateCategory(category)
}