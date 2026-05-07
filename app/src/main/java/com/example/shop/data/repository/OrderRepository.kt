package com.example.shop.data.repository

import com.example.shop.data.model.CartItem
import com.example.shop.data.model.Order
import com.example.shop.data.model.OrderWithItems
import com.example.shop.data.remote.api.OrderApi
import com.example.shop.data.remote.dto.CreateOrderRequest
import com.example.shop.data.remote.dto.UpdateOrderStatusRequest
import com.example.shop.data.remote.dto.toOrderWithItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val orderApi: OrderApi,
    private val authRepository: AuthRepository
) {
    private val _myOrders = MutableStateFlow<List<OrderWithItems>>(emptyList())
    private val _allOrders = MutableStateFlow<List<OrderWithItems>>(emptyList())

    fun getOrdersByUserId(_userId: Int): Flow<List<OrderWithItems>> = flow {
        refreshMyOrders()
        emitAll(_myOrders.asStateFlow())
    }

    suspend fun placeOrder(order: Order, cartItems: List<CartItem>): Boolean {
        val authorization = authRepository.getAuthorizationHeader() ?: return false
        if (cartItems.isEmpty()) return false

        return runCatching {
            val createdOrder = orderApi.createOrder(
                authorization,
                CreateOrderRequest(
                    address = order.address,
                    phoneNumber = order.phoneNumber
                )
            )
            _myOrders.value = listOf(createdOrder.toOrderWithItems()) + _myOrders.value
            true
        }.getOrDefault(false)
    }

    fun getALLOrders(): Flow<List<OrderWithItems>> = flow {
        refreshAllOrders()
        emitAll(_allOrders.asStateFlow())
    }

    suspend fun updateOrderStatus(orderId: Int, status: String) {
        val authorization = authRepository.getAuthorizationHeader() ?: return

        runCatching {
            orderApi.updateStatus(
                authorization,
                orderId,
                UpdateOrderStatusRequest(status = status)
            )
        }.onSuccess {
            refreshAllOrders()
        }
    }

    private suspend fun refreshMyOrders() {
        val authorization = authRepository.getAuthorizationHeader()
        if (authorization == null) {
            _myOrders.value = emptyList()
            return
        }

        runCatching {
            orderApi.getMyOrders(authorization)
        }.onSuccess { orders ->
            _myOrders.value = orders.map { it.toOrderWithItems() }
        }
    }

    private suspend fun refreshAllOrders() {
        val authorization = authRepository.getAuthorizationHeader()
        if (authorization == null) {
            _allOrders.value = emptyList()
            return
        }

        runCatching {
            orderApi.getAllOrders(authorization)
        }.onSuccess { orders ->
            _allOrders.value = orders.map { it.toOrderWithItems() }
        }
    }
}
