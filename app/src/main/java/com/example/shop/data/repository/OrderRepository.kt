package com.example.shop.data.repository

import com.example.shop.data.local.dao.CartDao
import com.example.shop.data.local.dao.OrderDao
import com.example.shop.data.model.CartItem
import com.example.shop.data.model.Order
import com.example.shop.data.model.OrderItem
import com.example.shop.data.model.OrderWithItems
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OrderRepository @Inject constructor(
    private val orderDao: OrderDao,
    private val cartDao: CartDao
) {
    // --- DÀNH CHO NGƯỜI DÙNG (CLIENT) ---

    // Lấy lịch sử đơn hàng của User
    fun getOrdersByUserId(userId: Int): Flow<List<OrderWithItems>> =
        orderDao.getOrdersWithItemsByUserId(userId)

    // Logic đặt hàng
    suspend fun placeOrder(order: Order, cartItems: List<CartItem>) {
        // 1. Insert Order và lấy ID vừa tạo
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
        cartDao.clearCart(order.userId)
    }

    // --- DÀNH CHO QUẢN TRỊ VIÊN (ADMIN) ---

    // 1. Admin lấy toàn bộ đơn hàng của tất cả người dùng
    fun getALLOrders(): Flow<List<OrderWithItems>> = orderDao.getALLOrders()

    // 2. Admin cập nhật trạng thái đơn hàng (Ví dụ: Chuyển từ PENDING sang SHIPPING)
    suspend fun updateOrderStatus(orderId: Int, status: String) {
        orderDao.updateOrderStatus(orderId, status)
    }
}