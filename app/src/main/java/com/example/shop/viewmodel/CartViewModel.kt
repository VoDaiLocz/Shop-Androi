package com.example.shop.viewmodel

import androidx.lifecycle.ViewModel
import com.example.shop.data.model.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CartViewModel : ViewModel() {

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart

    val cartCount: StateFlow<Int>
        get() = MutableStateFlow(_cart.value.size)

    fun addToCart(item: CartItem) {
        _cart.value = _cart.value + item
    }

    fun removeItem(item: CartItem) {
        _cart.value = _cart.value - item
    }
}
