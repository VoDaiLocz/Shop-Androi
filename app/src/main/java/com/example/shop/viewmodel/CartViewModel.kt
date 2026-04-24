package com.example.shop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shop.data.model.CartItem
import com.example.shop.data.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repository: CartRepository
) : ViewModel() {

    // Lấy dữ liệu thật từ Database (Flow tự động cập nhật UI)
    val cartItems: StateFlow<List<CartItem>> = repository.allItems
        .stateIn(viewModelScope,
            SharingStarted.WhileSubscribed(5000), emptyList())

    // Tính tổng tiền từ danh sách thật
    val totalPrice: Double
        get() = cartItems.value.sumOf { it.price * it.quantity }

    // --- THÊM HÀM NÀY ĐỂ PRODUCT DETAIL GỌI ---
    fun addToCart(item: CartItem) {
        viewModelScope.launch {
            repository.addToCart(item)
        }
    }

    // Tăng số lượng (Dùng trong CartScreen)
    fun increaseQuantity(item: CartItem) {
        viewModelScope.launch {
            repository.updateQuantity(item.copy(quantity = item.quantity + 1))
        }
    }

    // Giảm số lượng (Dùng trong CartScreen)
    fun decreaseQuantity(item: CartItem) {
        if (item.quantity > 1) {
            viewModelScope.launch {
                repository.updateQuantity(item.copy(quantity = item.quantity - 1))
            }
        }
    }

    // Xóa món hàng (Dùng trong CartScreen)
    fun removeFromCart(item: CartItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }
}