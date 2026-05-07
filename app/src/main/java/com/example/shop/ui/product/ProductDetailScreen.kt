package com.example.shop.ui.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.shop.admin.viewmodel.AdminProductViewModel
import com.example.shop.data.model.CartItem
import com.example.shop.viewmodel.AuthViewModel
import com.example.shop.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onNavigateBack: () -> Unit,
    onAddToCart: () -> Unit,
    viewModel: AdminProductViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val id = productId.toIntOrNull() ?: 0

    val product by viewModel.getProductById(id).collectAsState(initial = null)

    val currentUser by authViewModel.currentUser.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết sản phẩm") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        product?.let { item ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Hiển thị ảnh sản phẩm
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.LightGray)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text("Ảnh sản phẩm", color = Color.Gray)
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${item.price} VNĐ",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = "Số lượng còn lại: ${item.quantity}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (item.quantity > 0) Color.DarkGray else Color.Red
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Mô tả sản phẩm",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (item.description.isBlank()) "Không có mô tả cho sản phẩm này." else item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // 2. Cập nhật logic xử lý khi nhấn nút "Thêm vào giỏ hàng"
                    Button(
                        onClick = {
                            val user = currentUser
                            if (user != null){
                                // Tạo đối tượng CartItem từ thông tin sản phẩm hiện tại
                                val newItem = CartItem(
                                    userId = user.id,
                                    productId = item.id.toString(),
                                    productName = item.name,
                                    price = item.price,
                                    quantity = 1, // Mặc định thêm 1 sản phẩm
                                    imageUrl = "" // Bạn có thể thêm item.imageUrl nếu có
                                )
                                // Gọi ViewModel để thêm sản phẩm vào cart qua backend.
                                cartViewModel.addToCart(newItem)

                                // Sau khi lưu xong, thực hiện chuyển sang màn hình Cart (như đã định nghĩa ở NavGraph)
                                onAddToCart()
                            } else {
                                println("Lỗi: Bạn cần đăng nhập để thêm vào giỏ hàng")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = MaterialTheme.shapes.medium,
                        enabled = item.quantity > 0
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (item.quantity > 0) "Thêm vào giỏ hàng" else "Hết hàng")
                    }
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (id == 0) Text("Sản phẩm không tồn tại") else CircularProgressIndicator()
            }
        }
    }
}
