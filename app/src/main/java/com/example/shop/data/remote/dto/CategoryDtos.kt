package com.example.shop.data.remote.dto

import com.example.shop.data.model.Category

data class CategoryResponse(
    val id: Int,
    val name: String,
    val imageUrl: String
)

data class CreateCategoryRequest(
    val name: String,
    val imageUrl: String
)

data class UpdateCategoryRequest(
    val name: String,
    val imageUrl: String
)

fun CategoryResponse.toCategory(): Category {
    return Category(
        id = id,
        name = name,
        imageUrl = imageUrl
    )
}
