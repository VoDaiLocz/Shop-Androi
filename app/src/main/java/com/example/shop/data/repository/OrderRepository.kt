package com.example.shop.data.repository

import com.example.shop.data.local.dao.CartDao
import com.example.shop.data.local.dao.OrderDao
import com.example.shop.data.model.CartItem
import com.example.shop.data.model.Order
import com.example.shop.data.model.OrderItem
import javax.inject.Inject

class OrderRepository @Inject constructor(
    private val orderDao: OrderDao,
    private val cartDao: CartDao
) {
    // Lấy lịch sử đơn hàng
    fun getOrdersByUserId(userId: Int) = orderDao.getOrdersWithItemsByUserId(userId)

    // Logic đặt hàng
    suspend fun placeOrder(order: Order, cartItems: List<CartItem>) {
        // 1. Insert Order
        val orderId = orderDao.insertOrder(order).toInt()

        // 2. Chuyển CartItem thành OrderItem
        val orderItems = cartItems.map { cart ->
            OrderItem(
                orderId = orderId,
                productId = cart.productId.toInt(),
                productName = cart.productName,
                quantity = cart.quantity,
                price = cart.price
            )
        }
        orderDao.insertOrderItems(orderItems)

        // 3. Xóa giỏ hàng sau khi đặt hàng thành công
        cartDao.clearCart(order.userId) // Bạn cần thêm hàm clearCart() trong CartDao
    }
}
