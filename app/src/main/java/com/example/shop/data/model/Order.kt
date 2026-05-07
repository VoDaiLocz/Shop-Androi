package com.example.shop.data.model

data class Order(
    val orderId: Int = 0,
    val userId: Int,
    val orderDate: Long,
    val totalPrice: Double,
    val status: String = "Pending",
    val address: String,
    val phoneNumber: String
)
