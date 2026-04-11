package com.example.shop.ui.favorite

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FavoriteScreen() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Favorite Products")

        LazyColumn {
            items(listOf("Iphone", "Samsung")) {
                Text(it, modifier = Modifier.padding(8.dp))
            }
        }
    }
}