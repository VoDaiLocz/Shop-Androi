package com.example.shop.utils

object Constants {
    const val APP_NAME = "Shop"
    const val BASE_URL = "http://10.0.2.2:5053/"
    const val GOOGLE_WEB_CLIENT_ID =
        "826757086511-es3htk7un7lq7lvlpmkqppmp3h7nnjd2.apps.googleusercontent.com"

    fun toBackendImageUrl(imageUrl: String?): String {
        val value = imageUrl?.trim().orEmpty()
        if (value.isBlank()) return ""

        return when {
            value.startsWith("http://") || value.startsWith("https://") -> value
            value.startsWith("/") -> BASE_URL.trimEnd('/') + value
            value.startsWith("uploads/") -> BASE_URL + value
            else -> value
        }
    }
}
