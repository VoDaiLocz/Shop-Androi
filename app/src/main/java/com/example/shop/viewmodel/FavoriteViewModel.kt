package com.example.shop.viewmodel

import androidx.lifecycle.ViewModel
import com.example.shop.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FavoriteViewModel : ViewModel() {

    private val _favorites = MutableStateFlow<List<Product>>(emptyList())
    val favorites: StateFlow<List<Product>> = _favorites

    fun add(product: Product) {
        _favorites.value = _favorites.value + product
    }
}