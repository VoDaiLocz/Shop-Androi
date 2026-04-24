package com.example.shop.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_items")
data class OrderItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val orderId: String = "",  //PK  Order.id
    val productId: String = "",
    val productName: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0
)
