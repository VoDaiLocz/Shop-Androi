package com.example.shop.data.remote.api

import com.example.shop.data.remote.dto.CategoryResponse
import com.example.shop.data.remote.dto.CreateCategoryRequest
import com.example.shop.data.remote.dto.UpdateCategoryRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CategoryApi {
    @GET("api/categories")
    suspend fun getCategories(): List<CategoryResponse>

    @GET("api/categories/{id}")
    suspend fun getCategoryById(@Path("id") id: Int): CategoryResponse

    @POST("api/categories")
    suspend fun createCategory(
        @Header("Authorization") authorization: String,
        @Body request: CreateCategoryRequest
    ): CategoryResponse

    @PUT("api/categories/{id}")
    suspend fun updateCategory(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int,
        @Body request: UpdateCategoryRequest
    ): CategoryResponse

    @DELETE("api/categories/{id}")
    suspend fun deleteCategory(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    )
}
