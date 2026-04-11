package com.example.shop.data.repository

import com.example.shop.data.model.Review

interface ReviewRepository {
    suspend fun getReviewsForProduct(productId: String): List<Review>
    suspend fun addReview(review: Review)
}
