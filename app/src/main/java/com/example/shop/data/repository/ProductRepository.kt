package com.example.shop.data.repository

import android.content.Context
import android.net.Uri
import com.example.shop.data.model.Product
import com.example.shop.data.remote.api.ProductApi
import com.example.shop.data.remote.dto.CreateProductRequest
import com.example.shop.data.remote.dto.UpdateProductRequest
import com.example.shop.data.remote.dto.toProduct
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productApi: ProductApi,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
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

    suspend fun insertProduct(product: Product): Product? {
        val authorization = authRepository.getAuthorizationHeader() ?: return null
        return runCatching {
            val createdProduct = productApi.createProduct(
                authorization,
                CreateProductRequest(
                    name = product.name,
                    price = product.price,
                    description = product.description,
                    quantity = product.quantity,
                    categoryId = product.categoryId,
                    imageUrl = product.imageUrl
                )
            ).toProduct()
            refreshProducts()
            createdProduct
        }.getOrNull()
    }

    suspend fun updateProduct(product: Product): Product? {
        val authorization = authRepository.getAuthorizationHeader() ?: return null
        return runCatching {
            val updatedProduct = productApi.updateProduct(
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
            ).toProduct()
            refreshProducts()
            updatedProduct
        }.getOrNull()
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

    suspend fun uploadProductImage(productId: Int, imageUri: Uri): Boolean {
        val authorization = authRepository.getAuthorizationHeader() ?: return false
        val imagePart = createImagePart(imageUri) ?: return false

        return runCatching {
            productApi.uploadImage(authorization, productId, imagePart)
            refreshProducts()
            true
        }.getOrDefault(false)
    }

    private suspend fun createImagePart(imageUri: Uri): MultipartBody.Part? = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(imageUri) ?: "image/jpeg"
        val extension = when (mimeType) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }

        val bytes = contentResolver.openInputStream(imageUri)?.use { stream ->
            stream.readBytes()
        } ?: return@withContext null

        val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        MultipartBody.Part.createFormData("file", "product-image.$extension", body)
    }
}
