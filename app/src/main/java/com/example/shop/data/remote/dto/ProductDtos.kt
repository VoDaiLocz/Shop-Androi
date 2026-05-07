package com.example.shop.data.remote.dto

import com.example.shop.data.model.Product

data class ProductResponse(
    val id: Int,
    val name: String,
    val price: Double,
    val description: String,
    val imageUrl: String,
    val quantity: Int,
    val categoryId: Int,
    val categoryName: String
)

data class CreateProductRequest(
    val name: String,
    val price: Double,
    val description: String,
    val quantity: Int,
    val categoryId: Int,
    val imageUrl: String? = null
)

data class UpdateProductRequest(
    val name: String,
    val price: Double,
    val description: String,
    val quantity: Int,
    val categoryId: Int,
    val imageUrl: String? = null
)

fun ProductResponse.toProduct(): Product {
    return Product(
        id = id,
        name = name,
        price = price,
        description = description,
        imageUrl = imageUrl,
        quantity = quantity,
        categoryId = categoryId
    )
}
