package com.example.shop.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shop.data.model.Category
import com.example.shop.data.model.Product
import com.example.shop.data.repository.CategoryRepository
import com.example.shop.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository // Cần thiết để chọn Category khi thêm Product
) : ViewModel() {

    //Lấy danh sách toàn bộ sản phẩm để Admin quản lý
    val allProducts: StateFlow<List<Product>> = productRepository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    //Lấy danh mục để Admin chọn khi thêm/sửa sản phẩm
    val allCategories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    //Thêm Sản phẩm
    fun addProduct(
        name: String,
        price: Double,
        description: String,
        imageUrl: String,
        quantity: Int,
        categoryId: Int
    ) {
        viewModelScope.launch {
            val newProduct = Product(
                name = name,
                price = price,
                description = description,
                imageUrl = imageUrl,
                quantity = quantity,
                categoryId = categoryId
            )
            productRepository.insertProduct(newProduct)
        }
    }

    //Cập nhật Sản phẩm
    fun updateProduct(product: Product) {
        viewModelScope.launch {
            productRepository.updateProduct(product)
        }
    }

    //Xóa Sản phẩm
    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            productRepository.deleteProduct(product)
        }
    }
}