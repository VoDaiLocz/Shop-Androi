package com.example.shop.data.repository

import com.example.shop.data.model.Product
import com.example.shop.data.remote.api.ProductApi
import com.example.shop.data.remote.dto.CreateProductRequest
import com.example.shop.data.remote.dto.UpdateProductRequest
import com.example.shop.data.remote.dto.toProduct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productApi: ProductApi,
    private val authRepository: AuthRepository
) {
    private val _products = MutableStateFlow<List<Product>>(emptyList())

    fun getAllProducts(): Flow<List<Product>> = flow {
        refreshProducts()
        emitAll(_products.asStateFlow())
    }

    fun getProductsByCategory(catId: Int): Flow<List<Product>> =
        getAllProducts().map { products -> products.filter { product -> product.categoryId == catId } }

    fun getProductById(productId: Int): Flow<Product?> = flow<Product?> {
        emit(productApi.getProductById(productId).toProduct())
    }.catch {
        emit(null)
    }

    suspend fun insertProduct(product: Product): Boolean {
        val authorization = authRepository.getAuthorizationHeader() ?: return false
        return runCatching {
            productApi.createProduct(
                authorization,
                CreateProductRequest(
                    name = product.name,
                    price = product.price,
                    description = product.description,
                    quantity = product.quantity,
                    categoryId = product.categoryId,
                    imageUrl = product.imageUrl
                )
            )
            refreshProducts()
            true
        }.getOrDefault(false)
    }

    suspend fun updateProduct(product: Product): Boolean {
        val authorization = authRepository.getAuthorizationHeader() ?: return false
        return runCatching {
            productApi.updateProduct(
                authorization,
                product.id,
                UpdateProductRequest(
                    name = product.name,
                    price = product.price,
                    description = product.description,
                    quantity = product.quantity,
                    categoryId = product.categoryId,
                    imageUrl = product.imageUrl
                )
            )
            refreshProducts()
            true
        }.getOrDefault(false)
    }

    suspend fun deleteProduct(product: Product): Boolean {
        val authorization = authRepository.getAuthorizationHeader() ?: return false
        return runCatching {
            productApi.deleteProduct(authorization, product.id)
            refreshProducts()
            true
        }.getOrDefault(false)
    }

    private suspend fun refreshProducts() {
        runCatching {
            productApi.getProducts()
        }.onSuccess { products ->
            _products.value = products.map { it.toProduct() }
        }
    }
}
