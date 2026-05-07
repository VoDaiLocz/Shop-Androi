package com.example.shop.data.model

data class Address(
    val id: Int = 0,
    val userId: Int,
    val name: String,
    val phoneNumber: String,
    val detail: String,
    val city: String,
    val isDefault: Boolean = false
)
