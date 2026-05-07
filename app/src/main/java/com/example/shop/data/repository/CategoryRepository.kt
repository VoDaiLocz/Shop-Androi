package com.example.shop.data.repository

import com.example.shop.data.model.Category
import com.example.shop.data.remote.api.CategoryApi
import com.example.shop.data.remote.dto.CreateCategoryRequest
import com.example.shop.data.remote.dto.UpdateCategoryRequest
import com.example.shop.data.remote.dto.toCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryApi: CategoryApi,
    private val authRepository: AuthRepository
) {
    private val _categories = MutableStateFlow<List<Category>>(emptyList())

    fun getAllCategories(): Flow<List<Category>> = flow {
        refreshCategories()
        emitAll(_categories.asStateFlow())
    }

    fun getCategoryById(id: Int): Flow<Category?> = flow {
        refreshCategories()
        emit(_categories.value.firstOrNull { category -> category.id == id })
    }

    suspend fun addCategory(category: Category) {
        val authorization = authRepository.getAuthorizationHeader() ?: return

        runCatching {
            categoryApi.createCategory(
                authorization,
                CreateCategoryRequest(
                    name = category.name,
                    imageUrl = category.imageUrl
                )
            )
        }.onSuccess {
            refreshCategories()
        }
    }

    suspend fun deleteCategory(category: Category) {
        val authorization = authRepository.getAuthorizationHeader() ?: return

        runCatching {
            categoryApi.deleteCategory(authorization, category.id)
        }.onSuccess {
            refreshCategories()
        }
    }

    suspend fun updateCategory(category: Category) {
        val authorization = authRepository.getAuthorizationHeader() ?: return

        runCatching {
            categoryApi.updateCategory(
                authorization,
                category.id,
                UpdateCategoryRequest(
                    name = category.name,
                    imageUrl = category.imageUrl
                )
            )
        }.onSuccess {
            refreshCategories()
        }
    }

    private suspend fun refreshCategories() {
        runCatching {
            categoryApi.getCategories()
        }.onSuccess { categories ->
            _categories.value = categories.map { it.toCategory() }
        }
    }
}
