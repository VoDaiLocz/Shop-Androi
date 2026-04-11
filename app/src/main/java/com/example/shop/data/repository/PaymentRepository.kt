package com.example.shop.data.repository

import com.example.shop.data.model.Payment

interface PaymentRepository {
    suspend fun getPayments(userId: String): List<Payment>
    suspend fun addPayment(payment: Payment)
}
