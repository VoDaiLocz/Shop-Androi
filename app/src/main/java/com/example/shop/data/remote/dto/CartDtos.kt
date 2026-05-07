package com.example.shop.data.remote.dto

import com.example.shop.data.model.CartItem

data class CartResponse(
    val items: List<CartItemResponse>,
    val totalPrice: Double
)

data class CartItemResponse(
    val id: Int,
    val productId: Int,
    val productName: String,
    val price: Double,
    val imageUrl: String,
    val quantity: Int,
    val lineTotal: Double
)

data class AddCartItemRequest(
    val productId: Int,
    val quantity: Int
)

data class UpdateCartItemRequest(
    val quantity: Int
)

fun CartItemResponse.toCartItem(): CartItem {
    return CartItem(
        id = id,
        productId = productId.toString(),
        productName = productName,
        quantity = quantity,
        price = price,
        imageUrl = imageUrl
    )
}
