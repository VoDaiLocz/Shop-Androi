package com.example.shop.data.repository

import com.example.shop.data.model.CartItem

interface CartRepository {
    suspend fun getCartItems(userId: String): List<CartItem>
    suspend fun addItem(item: CartItem)
}
