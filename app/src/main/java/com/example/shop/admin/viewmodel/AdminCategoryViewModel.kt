package com.example.shop.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shop.data.model.Category
import com.example.shop.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminCategoryViewModel @Inject constructor(
    private val repository: CategoryRepository
) : ViewModel() {

    val categories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getCategoryById(id: Int): Flow<Category?> {
        return repository.getCategoryById(id)
    }


    fun addCategory(name: String, imageUrl: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.addCategory(Category(name = name, imageUrl = imageUrl))
            }
        }
    }

    fun updateCategory(id: Int, name: String, imageUrl: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                val updatedCategory = Category(id = id, name = name, imageUrl = imageUrl)
                repository.updateCategory(updatedCategory)
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }
}
