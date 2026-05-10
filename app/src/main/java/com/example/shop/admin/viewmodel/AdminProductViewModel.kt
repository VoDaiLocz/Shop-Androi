package com.example.shop.admin.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shop.data.model.Category
import com.example.shop.data.model.Product
import com.example.shop.data.repository.CategoryRepository
import com.example.shop.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    // Lấy danh sách toàn bộ sản phẩm để Admin quản lý
    val allProducts: StateFlow<List<Product>> = productRepository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Lấy danh mục để Admin chọn khi thêm/sửa sản phẩm
    val allCategories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- SỬA LỖI: Thêm hàm lấy sản phẩm theo ID ---
    fun getProductById(productId: Int): Flow<Product?> {
        return productRepository.getProductById(productId)
    }

    // Thêm Sản phẩm
    fun addProduct(
        name: String,
        price: Double,
        description: String,
        imageUrl: String,
        quantity: Int,
        categoryId: Int,
        imageUri: Uri?,
        onResult: (Boolean) -> Unit
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
            val createdProduct = productRepository.insertProduct(newProduct)
            val success = if (createdProduct != null && imageUri != null) {
                val uploaded = productRepository.uploadProductImage(createdProduct.id, imageUri)
                if (!uploaded) {
                    productRepository.deleteProduct(createdProduct)
                }
                uploaded
            } else {
                createdProduct != null
            }
            onResult(success)
        }
    }

    fun updateProduct(
        id: Int,
        name: String,
        price: Double,
        description: String,
        imageUrl: String,
        quantity: Int,
        categoryId: Int,
        imageUri: Uri?,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val updatedProduct = Product(
                id = id,
                name = name,
                price = price,
                description = description,
                imageUrl = imageUrl,
                quantity = quantity,
                categoryId = categoryId
            )
            val savedProduct = productRepository.updateProduct(updatedProduct)
            val success = if (savedProduct != null && imageUri != null) {
                productRepository.uploadProductImage(savedProduct.id, imageUri)
            } else {
                savedProduct != null
            }
            onResult(success)
        }
    }

    // Xóa Sản phẩm
    fun deleteProduct(product: Product, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            onResult(productRepository.deleteProduct(product))
        }
    }
}
