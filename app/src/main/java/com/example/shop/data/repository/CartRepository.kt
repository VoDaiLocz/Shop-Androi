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
    val allItems: Flow<List<CartItem>> = cartDao.getAllCartItems()

    suspend fun addToCart(cartItem: CartItem) {
        val existingItem = cartDao.getItemByProductId(cartItem.productId)

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