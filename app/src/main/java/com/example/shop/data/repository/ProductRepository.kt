package com.example.shop.data.repository

import com.example.shop.data.model.Product
import com.example.shop.data.remote.api.ProductApi
import com.example.shop.data.remote.dto.CreateProductRequest
import com.example.shop.data.remote.dto.UpdateProductRequest
import com.example.shop.data.remote.dto.toProduct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productApi: ProductApi,
    private val authRepository: AuthRepository
) {
    fun getAllProducts(): Flow<List<Product>> = flow {
        emit(productApi.getProducts().map { it.toProduct() })
    }.catch {
        emit(emptyList())
    }

    fun getProductsByCategory(catId: Int): Flow<List<Product>> = flow {
        emit(productApi.getProducts(categoryId = catId).map { it.toProduct() })
    }.catch {
        emit(emptyList())
    }

    fun getProductById(productId: Int): Flow<Product?> = flow<Product?> {
        emit(productApi.getProductById(productId).toProduct())
    }.catch {
        emit(null)
    }

    suspend fun insertProduct(product: Product) {
        val authorization = authRepository.getAuthorizationHeader() ?: return
        runCatching {
            productApi.createProduct(
                authorization,
                CreateProductRequest(
                    name = product.name,
                    price = product.price,
                    description = product.description,
                    quantity = product.quantity,
                    categoryId = product.categoryId
                )
            )
        }
    }

    suspend fun updateProduct(product: Product) {
        val authorization = authRepository.getAuthorizationHeader() ?: return
        runCatching {
            productApi.updateProduct(
                authorization,
                product.id,
                UpdateProductRequest(
                    name = product.name,
                    price = product.price,
                    description = product.description,
                    quantity = product.quantity,
                    categoryId = product.categoryId
                )
            )
        }
    }

    suspend fun deleteProduct(product: Product) {
        val authorization = authRepository.getAuthorizationHeader() ?: return
        runCatching {
            productApi.deleteProduct(authorization, product.id)
        }
    }
}
