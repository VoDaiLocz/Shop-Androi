package com.example.shop.data.repository

import com.example.shop.data.model.CartItem
import com.example.shop.data.remote.api.CartApi
import com.example.shop.data.remote.dto.AddCartItemRequest
import com.example.shop.data.remote.dto.UpdateCartItemRequest
import com.example.shop.data.remote.dto.toCartItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor(
    private val cartApi: CartApi,
    private val authRepository: AuthRepository
) {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())

    fun getItemsForUser(_userId: Int): Flow<List<CartItem>> {
        return _cartItems.asStateFlow()
    }

    suspend fun refreshCart() {
        val authorization = authRepository.getAuthorizationHeader()
        if (authorization == null) {
            _cartItems.value = emptyList()
            return
        }

        runCatching {
            cartApi.getCart(authorization)
        }.onSuccess { response ->
            _cartItems.value = response.items.map { it.toCartItem() }
        }.onFailure {
            _cartItems.value = emptyList()
        }
    }

    suspend fun addToCart(cartItem: CartItem) {
        val authorization = authRepository.getAuthorizationHeader() ?: return
        val productId = cartItem.productId.toIntOrNull() ?: return
        if (cartItem.quantity <= 0) return

        runCatching {
            cartApi.addItem(
                authorization,
                AddCartItemRequest(productId = productId, quantity = cartItem.quantity)
            )
        }.onSuccess { response ->
            _cartItems.value = response.items.map { it.toCartItem() }
        }
    }

    suspend fun updateQuantity(item: CartItem) {
        val authorization = authRepository.getAuthorizationHeader() ?: return
        if (item.quantity <= 0) return

        runCatching {
            cartApi.updateItem(
                authorization,
                item.id,
                UpdateCartItemRequest(quantity = item.quantity)
            )
        }.onSuccess { response ->
            _cartItems.value = response.items.map { it.toCartItem() }
        }
    }

    suspend fun deleteItem(item: CartItem) {
        val authorization = authRepository.getAuthorizationHeader() ?: return

        runCatching {
            cartApi.deleteItem(authorization, item.id)
        }.onSuccess { response ->
            _cartItems.value = response.items.map { it.toCartItem() }
        }
    }

    suspend fun clearCart() {
        val authorization = authRepository.getAuthorizationHeader() ?: return

        runCatching {
            cartApi.clearCart(authorization)
        }.onSuccess {
            _cartItems.value = emptyList()
        }
    }
}
