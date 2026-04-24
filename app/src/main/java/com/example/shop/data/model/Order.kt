package com.example.shop.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey
    val id: String = "", //Mã Đơn hàng
    val userId: String = "",
    val total: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(), //Thời gian đặt hàng
    val status: String = "PENDING" // PENDING, SHIPPED, COMPLETED
)
