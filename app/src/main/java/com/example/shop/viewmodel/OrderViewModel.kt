package com.example.shop.viewmodel

import androidx.lifecycle.ViewModel
import com.example.shop.data.model.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class OrderViewModel : ViewModel() {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    fun placeOrder(order: Order) {
        _orders.value = _orders.value + order
    }
}
