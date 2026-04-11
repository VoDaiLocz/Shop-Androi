package com.example.shop.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.shop.data.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products")
    fun getAll(): Flow<List<Product>>

    @Insert
    suspend fun insert(product: Product)

    @Delete
    suspend fun delete(product: Product)
}