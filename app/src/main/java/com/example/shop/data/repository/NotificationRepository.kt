package com.example.shop.data.repository

import com.example.shop.data.model.Notification

interface NotificationRepository {
    suspend fun getNotifications(userId: String): List<Notification>
}
