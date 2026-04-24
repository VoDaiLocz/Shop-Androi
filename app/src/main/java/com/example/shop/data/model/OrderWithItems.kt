package com.example.shop.data.model

import androidx.room.Embedded
import androidx.room.Relation



//Lịch sử đơn hàng

data class OrderWithItems(
    @Embedded val order: Order,
    @Relation(
        parentColumn = "orderId",
        entityColumn = "orderId"
    )
    val items: List<OrderItem>
)