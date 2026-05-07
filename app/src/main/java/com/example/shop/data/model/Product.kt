package com.example.shop.data.model

data class Product(
    val id: Int = 0,
    val name: String,
    val price: Double,
    val description: String,
    val imageUrl: String,
    val quantity: Int,
    val categoryId: Int
)

