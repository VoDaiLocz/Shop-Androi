package com.example.shop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shop.data.model.CartItem
import com.example.shop.data.repository.CartRepository
import com.example.shop.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repository: CartRepository,
    private val authRepository: AuthRepository // Inject thêm AuthRepository
) : ViewModel() {

    // 1. Lấy dữ liệu giỏ hàng dựa trên ID của người dùng đang đăng nhập
    @OptIn(ExperimentalCoroutinesApi::class)
    val cartItems: StateFlow<List<CartItem>> = authRepository.currentUser
        .flatMapLatest { user ->
            if (user != null) {
                flow {
                    repository.refreshCart()
                    emitAll(repository.getItemsForUser(user.id))
                }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // Tính tổng tiền
    val totalPrice: Double
        get() = cartItems.value.sumOf { it.price * it.quantity }

    // Thêm vào giỏ hàng
    fun addToCart(item: CartItem) {
        viewModelScope.launch {
            val currentUser = authRepository.currentUser.first()
            if (currentUser != null) {
                repository.addToCart(item.copy(userId = currentUser.id))
            }
        }
    }

    // Tăng số lượng
    fun increaseQuantity(item: CartItem) {
        viewModelScope.launch {
            repository.updateQuantity(item.copy(quantity = item.quantity + 1))
        }
    }

    // Giảm số lượng
    fun decreaseQuantity(item: CartItem) {
        viewModelScope.launch {
            if (item.quantity > 1) {
                repository.updateQuantity(item.copy(quantity = item.quantity - 1))
            } else {
                repository.deleteItem(item)
            }
        }
    }

    // Xóa món hàng
    fun removeFromCart(item: CartItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }
}
