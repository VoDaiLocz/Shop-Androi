package com.example.shop.ui.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.shop.data.model.CartItem
import com.example.shop.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onNavigateBack: () -> Unit,
    onCheckout: () -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    // Quan sát giỏ hàng lấy từ backend qua repository.
    val items by viewModel.cartItems.collectAsState(initial = emptyList())
    // Tính tổng tiền từ danh sách hiện tại
    val totalPrice = items.sumOf { it.price * it.quantity }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Giỏ hàng của tôi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        bottomBar = {
            if (items.isNotEmpty()) {
                BottomCheckoutSection(total = totalPrice, onCheckout = onCheckout)
            }
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(Modifier
                .fillMaxSize()
                .padding(padding), contentAlignment = Alignment.Center) {
                Text("Giỏ hàng đang trống", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.id }) { item -> // Thêm key để danh sách cuộn mượt hơn
                    CartItemRow(
                        item = item,
                        onIncrease = { viewModel.increaseQuantity(item) },
                        onDecrease = { viewModel.decreaseQuantity(item) },
                        onDelete = { viewModel.removeFromCart(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun CartItemRow(item: CartItem, onIncrease: () -> Unit, onDecrease: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Placeholder ảnh
            Box(Modifier
                .size(60.dp)
                .background(Color.LightGray))

            Column(modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)) {
                Text(item.productName, fontWeight = FontWeight.Bold)
                Text("${item.price} VNĐ", color = MaterialTheme.colorScheme.primary)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDecrease) { Text("-") }
                    Text("${item.quantity}")
                    IconButton(onClick = onIncrease) { Text("+") }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
            }
        }
    }
}

@Composable
fun BottomCheckoutSection(total: Double, onCheckout: () -> Unit) {
    Surface(tonalElevation = 4.dp) {
        Row(Modifier
            .fillMaxWidth()
            .padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Tổng tiền")
                Text("$total VNĐ", style = MaterialTheme.typography.titleLarge, color = Color.Red)
            }
            Button(onClick = onCheckout) { Text("Thanh toán") }
        }
    }
}
