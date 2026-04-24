package com.example.shop.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun CartItemView(
    name: String,
    quantity: Int,price: String,
    imageUrl: String = ""
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            // Giả sử sau này bạn dùng Coil để hiện ảnh ở đây
            // AsyncImage(model = imageUrl, ...)

            Column {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Text("Số lượng: $quantity")
                Text(price, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}