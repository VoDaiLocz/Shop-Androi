package com.example.shop.data.remote.api

import com.example.shop.data.remote.dto.CreateProductRequest
import com.example.shop.data.remote.dto.ProductResponse
import com.example.shop.data.remote.dto.UpdateProductRequest
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApi {
    @GET("api/products")
    suspend fun getProducts(@Query("categoryId") categoryId: Int? = null): List<ProductResponse>

    @GET("api/products/{id}")
    suspend fun getProductById(@Path("id") id: Int): ProductResponse

    @POST("api/products")
    suspend fun createProduct(
        @Header("Authorization") authorization: String,
        @Body request: CreateProductRequest
    ): ProductResponse

    @PUT("api/products/{id}")
    suspend fun updateProduct(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int,
        @Body request: UpdateProductRequest
    ): ProductResponse

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    )

    @Multipart
    @POST("api/products/{id}/image")
    suspend fun uploadImage(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int,
        @Part file: MultipartBody.Part
    ): ProductResponse
}
