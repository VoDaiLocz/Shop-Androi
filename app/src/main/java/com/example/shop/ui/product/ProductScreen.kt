package com.example.shop.ui.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.shop.ui.components.ProductItem
import com.example.shop.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    categoryId: Int, // THÊM: Nhận ID danh mục từ HomeScreen truyền sang
    viewModel: ProductViewModel,
    onNavigateBack: () -> Unit,
    onClickItem: (Int) -> Unit
) {
    // Lấy toàn bộ danh sách sản phẩm từ ViewModel
    val products by viewModel.products.collectAsState()

    // Lọc sản phẩm theo CategoryId ngay tại trang này
    val filteredProducts = remember(products, categoryId) {
        if (categoryId == -1) {
            products // Nếu ID là -1 thì hiện tất cả
        } else {
            products.filter { it.categoryId == categoryId }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (categoryId == -1) "Tất cả sản phẩm" else "Sản phẩm theo mục",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Không tìm thấy sản phẩm nào.")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredProducts) { product ->
                    ProductItem(
                        name = product.name,
                        price = "${product.price} VNĐ",
                        oldPrice = "",
                        discount = "",
                        onClick = { onClickItem(product.id) }
                    )
                }
            }
        }
    }
}