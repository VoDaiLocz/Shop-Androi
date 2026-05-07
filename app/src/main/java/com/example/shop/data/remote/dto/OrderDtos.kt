package com.example.shop.data.remote.dto

import com.example.shop.data.model.Order
import com.example.shop.data.model.OrderItem
import com.example.shop.data.model.OrderWithItems
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class OrderResponse(
    val id: Int,
    val userId: Int,
    val username: String,
    val orderDate: String,
    val totalPrice: Double,
    val status: String,
    val address: String,
    val phoneNumber: String,
    val paymentMethod: String,
    val items: List<OrderItemResponse>
)

data class OrderItemResponse(
    val id: Int,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val price: Double,
    val imageUrl: String,
    val lineTotal: Double
)

data class CreateOrderRequest(
    val address: String,
    val phoneNumber: String,
    val paymentMethod: String = "COD"
)

data class UpdateOrderStatusRequest(
    val status: String
)

fun OrderResponse.toOrderWithItems(): OrderWithItems {
    return OrderWithItems(
        order = Order(
            orderId = id,
            userId = userId,
            orderDate = parseApiDate(orderDate),
            totalPrice = totalPrice,
            status = status,
            address = address,
            phoneNumber = phoneNumber
        ),
        items = items.map { item ->
            OrderItem(
                orderItemId = item.id,
                orderId = id,
                productId = item.productId,
                productName = item.productName,
                quantity = item.quantity,
                price = item.price
            )
        }
    )
}

private fun parseApiDate(value: String): Long {
    val normalized = if (value.endsWith("Z")) value else "${value}Z"
    val formats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'"
    )

    for (format in formats) {
        runCatching {
            val parser = SimpleDateFormat(format, Locale.US)
            parser.timeZone = TimeZone.getTimeZone("UTC")
            return parser.parse(normalized)?.time ?: System.currentTimeMillis()
        }
    }

    return System.currentTimeMillis()
}
