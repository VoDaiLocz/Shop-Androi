package com.example.shop.data.repository

import com.example.shop.data.local.dao.CartDao
import com.example.shop.data.model.CartItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor(
    private val cartDao: CartDao
) {
    // 1. Thêm hàm này để ViewModel gọi (Sửa lỗi Unresolved reference 'getItemsForUser')
    fun getItemsForUser(userId: Int): Flow<List<CartItem>> {
        return cartDao.getAllCartItems(userId)
    }

    suspend fun addToCart(cartItem: CartItem) {
        // 2. Sửa lệnh check: Phải tìm theo cả ProductId và UserId
        val existingItem = cartDao.getItemByProductId(cartItem.productId, cartItem.userId)

        if (existingItem != null) {
            val updatedItem = existingItem.copy(
                quantity = existingItem.quantity + cartItem.quantity
            )
            cartDao.updateCartItem(updatedItem)
        } else {
            cartDao.insertCartItem(cartItem)
        }
    }

    // Cập nhật số lượng (tăng/giảm) của một item đã có trong giỏ
    suspend fun updateQuantity(item: CartItem) {
        cartDao.updateCartItem(item)
    }

    // Xóa sản phẩm khỏi giỏ hàng
    suspend fun deleteItem(item: CartItem) {
        cartDao.deleteCartItem(item)
    }
}