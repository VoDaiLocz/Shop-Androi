package com.example.shop.data.model

data class OrderWithItems(
    val order: Order,
    val items: List<OrderItem>
)
