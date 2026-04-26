package com.example.shop.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shop.data.model.OrderWithItems
import com.example.shop.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminOrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    /**
     * Lấy toàn bộ danh sách đơn hàng (kèm sản phẩm chi tiết) từ database.
     * Sử dụng stateIn để biến Flow từ Room thành StateFlow giúp UI dễ dàng quan sát.
     */
    val allOrders: StateFlow<List<OrderWithItems>> = orderRepository.getALLOrders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Hàm cập nhật trạng thái đơn hàng (Ví dụ: PENDING -> SHIPPING -> COMPLETED)
     */
    fun updateOrderStatus(orderId: Int, newStatus: String) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, newStatus)
        }
    }
}