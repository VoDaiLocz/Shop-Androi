package com.example.shop.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "addresses")
data class Address(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val name: String,
    val phoneNumber: String,
    val detail: String,
    val city: String,
    val isDefault: Boolean = false
)
