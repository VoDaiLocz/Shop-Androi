package com.example.shop.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shop.ui.components.ProductItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenProduct: () -> Unit,
    onOpenCart: () -> Unit
) {

    Column {

        // HEADER
        TopAppBar(
            title = { Text("Shopee Fake") },
            actions = {
                IconButton(onClick = { onOpenCart() }) {
                    Text("🛒")
                }
            }
        )

        // GRID
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp)
        ) {

            items(20) {

                ProductItem(
                    name = "Áo thun nam form rộng",
                    price = "99.000đ",
                    oldPrice = "150.000đ",
                    discount = "-30%",
                    onClick = {
                        onOpenProduct()
                    }
                )
            }
        }
    }
}