package com.example.shop.data.repository

import com.example.shop.data.local.dao.ProductDao
import com.example.shop.data.model.Product
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProductRepository @Inject constructor(
    private val dao: ProductDao
) {
    val allProducts: Flow<List<Product>> = dao.getAll()

    suspend fun add(product: Product) {
        dao.insert(product)
    }
}