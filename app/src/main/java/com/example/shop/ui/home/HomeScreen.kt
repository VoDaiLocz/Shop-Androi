package com.example.shop.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.shop.ui.components.ProductItem
import com.example.shop.viewmodel.ProductViewModel
import com.example.shop.viewmodel.UserCategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenProduct: (String) -> Unit,
    onOpenCart: () -> Unit,
    productViewModel: ProductViewModel = hiltViewModel(),
    categoryViewModel: UserCategoryViewModel = hiltViewModel() // Inject thêm CategoryViewModel
) {
    val products by productViewModel.products.collectAsState()
    val categories by categoryViewModel.categories.collectAsState()

    // Lưu ID của Category đang được chọn (null nghĩa là chọn "Tất cả")
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }

    // Lọc sản phẩm theo Category đã chọn
    val filteredProducts = if (selectedCategoryId == null) {
        products
    } else {
        products.filter { it.categoryId == selectedCategoryId }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Shop của tôi", fontWeight = FontWeight.Bold) },
            actions = {
                IconButton(onClick = { onOpenCart() }) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Giỏ hàng")
                }
            }
        )

        // --- THANH CHỌN CATEGORY---
        Text(
            text = "Danh mục",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                CategoryChip(
                    name = "Tất cả",
                    isSelected = selectedCategoryId == null,
                    onClick = { selectedCategoryId = null }
                )
            }

            items(categories) { category ->
                CategoryChip(
                    name = category.name,
                    isSelected = selectedCategoryId == category.id,
                    onClick = { selectedCategoryId = category.id }
                )
            }
        }

        // --- DANH SÁCH SẢN PHẨM ---
        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Không tìm thấy sản phẩm nào trong mục này.")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredProducts) { product ->
                    ProductItem(
                        name = product.name,
                        price = "${product.price} VNĐ",
                        oldPrice = "",
                        discount = "",
                        onClick = {
                            onOpenProduct(product.id.toString())
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun CategoryChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}