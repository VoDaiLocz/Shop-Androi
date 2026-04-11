package com.example.shop.ui.notification

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NotificationScreen() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Notifications")

        LazyColumn {
            items(listOf("Order shipped", "New sale")) {
                Text(it, modifier = Modifier.padding(8.dp))
            }
        }
    }
}
