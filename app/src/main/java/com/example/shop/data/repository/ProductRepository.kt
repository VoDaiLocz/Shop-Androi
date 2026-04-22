package com.example.shop.data.repository

import com.example.shop.data.local.dao.ProductDao
import com.example.shop.data.model.Product
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProductRepository @Inject constructor(private val productDao: ProductDao) {
    fun getAllProducts() = productDao.getAll()

    //User: Xem sản phẩm theo danh mục
    fun getProductsByCategory(catId: Int) = productDao.getProductsByCategory(catId)

    fun getProductById(productId: Int): Flow<Product?> = productDao.getProductById(productId)

    //Admin: Thêm sản phẩm
    suspend fun insertProduct(product: Product) = productDao.insert(product)

    //Admin: Cập nhật
    suspend fun updateProduct(product: Product) = productDao.update(product)

    //Admin: Xóa sản phẩm
    suspend fun deleteProduct(product: Product) = productDao.delete(product)
}