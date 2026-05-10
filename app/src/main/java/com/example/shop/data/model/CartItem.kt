package com.example.shop.data.model

data class CartItem(
    val id: Int = 0,
    val userId: Int = 0,
    val productId: Int = 0,
    val productName: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val imageUrl: String = ""
)
