package com.example.shop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shop.data.model.CartItem
import com.example.shop.data.model.Order
import com.example.shop.data.repository.AuthRepository
import com.example.shop.data.repository.CartRepository
import com.example.shop.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository,
    private val authRepository: AuthRepository // 1. Inject thêm AuthRepository
) : ViewModel() {

    // 2. Sử dụng flatMapLatest để tự động chuyển đổi giỏ hàng khi User thay đổi
    @OptIn(ExperimentalCoroutinesApi::class)
    val cartItems: StateFlow<List<CartItem>> = authRepository.currentUser
        .flatMapLatest { user ->
            if (user != null) {
                flow {
                    cartRepository.refreshCart()
                    emitAll(cartRepository.getItemsForUser(user.id))
                }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Hàm xử lý đặt hàng
    fun placeOrder(userId: Int, address: String, phoneNumber: String, totalPrice: Double) {
        viewModelScope.launch {
            val items = cartItems.value
            if (items.isNotEmpty()) {
                val newOrder = Order(
                    userId = userId,
                    orderDate = System.currentTimeMillis(),
                    totalPrice = totalPrice,
                    address = address,
                    phoneNumber = phoneNumber,
                    status = "Pending"
                )
                orderRepository.placeOrder(newOrder, items)
            }
        }
    }

    // Lấy lịch sử đơn hàng của người dùng hiện tại
    fun getOrderHistory(userId: Int) = orderRepository.getOrdersByUserId(userId)
}
