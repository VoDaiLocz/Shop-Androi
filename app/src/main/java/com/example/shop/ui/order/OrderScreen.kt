package com.example.shop.ui.order

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.shop.data.model.OrderWithItems
import com.example.shop.utils.formatVnd
import com.example.shop.viewmodel.AuthViewModel
import com.example.shop.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    orderViewModel: OrderViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    // Lấy danh sách đơn hàng của user hiện tại từ backend.
    val orders by orderViewModel.getOrderHistory(currentUser?.id ?: 0)
        .collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Lịch sử đơn hàng") })
        }
    ) { padding ->
        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Bạn chưa có đơn hàng nào")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
            ) {
                items(orders) { orderWithItems ->
                    OrderHistoryCard(orderWithItems)
                }
            }
        }
    }
}

@Composable
fun OrderHistoryCard(orderWithItems: OrderWithItems) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Đơn hàng #${orderWithItems.order.orderId}", fontWeight = FontWeight.Bold)
                Text(
                    text = orderWithItems.order.status,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "Ngày đặt: ${formatDate(orderWithItems.order.orderDate)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            orderWithItems.items.forEach { item ->
                Text("• ${item.productName} x${item.quantity}", fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tổng tiền: ${orderWithItems.order.totalPrice.formatVnd()}",
                modifier = Modifier.align(Alignment.End),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        }
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
