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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onNavigateBack: () -> Unit, // Thêm hàm quay lại
    onAddToCart: () -> Unit,
    // Sử dụng AdminProductViewModel hoặc ProductViewModel (miễn là có hàm getProductById)
    viewModel: AdminProductViewModel = hiltViewModel()
) {
    // Chuyển đổi ID từ String sang Int an toàn
    val id = productId.toIntOrNull() ?: 0

    // Lấy dữ liệu sản phẩm từ ViewModel
    val product by viewModel.getProductById(id).collectAsState(initial = null)

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
                // Giả lập vùng hiển thị ảnh sản phẩm
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.LightGray)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text("Ảnh sản phẩm", color = Color.Gray)
                        // Nếu bạn dùng Coil để load ảnh:
                        // AsyncImage(model = item.imageUrl, contentDescription = null)
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

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

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

                    Button(
                        onClick = { onAddToCart() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = MaterialTheme.shapes.medium,
                        enabled = item.quantity > 0 // Vô hiệu hóa nếu hết hàng
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (item.quantity > 0) "Thêm vào giỏ hàng" else "Hết hàng")
                    }
                }
            }
        } ?: run {
            // Hiển thị trạng thái đang tải hoặc lỗi
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (id == 0) Text("Sản phẩm không tồn tại") else CircularProgressIndicator()
            }
        }
    }
}