package com.example.shop.ui.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.shop.viewmodel.AuthViewModel
import com.example.shop.viewmodel.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit,
    onPlaceOrder: () -> Unit,
    orderViewModel: OrderViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    //Lấy User hiện tại
    val currentUser by authViewModel.currentUser.collectAsState()

    // Lấy giỏ hàng - OrderViewModel cần có hàm lấy cart theo UserId
    // Nếu OrderViewModel chưa có, bạn nên truyền UserId vào init hoặc dùng collectAsState
    val cartItems by orderViewModel.cartItems.collectAsState()

    var address by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val totalPrice = cartItems.sumOf { it.price * it.quantity }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thanh toán") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(text = "Thông tin giao hàng", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Địa chỉ nhận hàng") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.LocationOn, null) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Số điện thoại") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Phone, null) }
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Tóm tắt đơn hàng", style = MaterialTheme.typography.titleMedium)

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(cartItems) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${item.productName} x${item.quantity}", modifier = Modifier.weight(1f))
                        Text("${item.price * item.quantity} VNĐ", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Tổng cộng", fontSize = 14.sp)
                        Text(
                            text = "$totalPrice VNĐ",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = {
                            val user = currentUser
                            if (user != null && address.isNotBlank() && phoneNumber.isNotBlank()) {
                                // 3. Gọi đặt hàng với user hiện tại qua backend.
                                orderViewModel.placeOrder(
                                    userId = user.id, // Đảm bảo dùng user.id không dùng 0
                                    address = address,
                                    phoneNumber = phoneNumber,
                                    totalPrice = totalPrice
                                ) { success ->
                                    if (success) {
                                        errorMessage = null
                                        onPlaceOrder()
                                    } else {
                                        errorMessage = "Đặt hàng thất bại. Vui lòng kiểm tra giỏ hàng hoặc tồn kho."
                                    }
                                }
                            }
                        },
                        // Nút chỉ bật khi có đủ thông tin và có hàng trong giỏ
                        enabled = cartItems.isNotEmpty() && address.isNotBlank() && phoneNumber.isNotBlank() && currentUser != null
                    ) {
                        Text("Xác nhận")
                    }
                }
            }
        }
    }
}
