package com.example.shop.ui.product

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.shop.ui.components.ProductItem
import com.example.shop.viewmodel.ProductViewModel

@Composable
fun ProductScreen(
    viewModel: ProductViewModel,
    onClickItem: (Int) -> Unit
) {
    val products by viewModel.products.collectAsState()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize()
    ) {
        items(products) { product ->
            ProductItem(
                name = product.name,
                price = "${product.price}đ",
                oldPrice = "0đ",
                discount = "0%",

                onClick = { onClickItem(product.id) }
            )
        }
    }
}