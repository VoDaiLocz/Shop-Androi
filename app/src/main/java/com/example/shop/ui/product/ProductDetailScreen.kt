package com.example.shop.ui.product

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.shop.R
import com.example.shop.admin.viewmodel.AdminProductViewModel
import com.example.shop.data.model.CartItem
import com.example.shop.ui.theme.ShopColors
import com.example.shop.ui.theme.ShopShapes
import com.example.shop.utils.Constants
import com.example.shop.utils.formatVnd
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
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = ShopColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detail",
                        color = ShopColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreHoriz, contentDescription = "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShopColors.Background)
            )
        },
        bottomBar = {
            product?.let { item ->
                DetailBottomBar(
                    enabled = item.quantity > 0,
                    onClick = {
                        val user = currentUser
                        if (user == null) {
                            errorMessage = "Bạn cần đăng nhập để thêm vào giỏ hàng."
                            return@DetailBottomBar
                        }

                        cartViewModel.addToCart(
                            CartItem(
                                userId = user.id,
                                productId = item.id,
                                productName = item.name,
                                price = item.price,
                                quantity = 1,
                                imageUrl = item.imageUrl
                            )
                        )
                        errorMessage = null
                        onAddToCart()
                    }
                )
            }
        }
    ) { paddingValues ->
        val item = product
        if (item == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                if (id == 0) Text("Sản phẩm không tồn tại") else CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
        ) {
            ProductHeroImage(
                imageUrl = item.imageUrl,
                name = item.name
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = item.name,
                style = MaterialTheme.typography.headlineMedium,
                color = ShopColors.TextPrimary,
                fontWeight = FontWeight.Normal,
                lineHeight = 31.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Color",
                style = MaterialTheme.typography.labelSmall,
                color = ShopColors.TextPrimary
            )

            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ColorSwatch(color = Color(0xFFF1EAD9), selected = true)
                ColorSwatch(color = Color(0xFFC8B9A8), selected = false)
                ColorSwatch(color = Color(0xFF5A4638), selected = false)
            }

            Spacer(modifier = Modifier.height(18.dp))

            ProductInfoCard(
                price = formatMoney(item.price),
                description = item.description,
                stock = item.quantity
            )

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 18.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProductHeroImage(
    imageUrl: String,
    name: String
) {
    val resolvedImageUrl = Constants.toBackendImageUrl(imageUrl)
    val hasImage = resolvedImageUrl.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(158.dp)
            .clip(ShopShapes.Card)
            .background(ShopColors.Promo),
        contentAlignment = Alignment.Center
    ) {
        if (!hasImage) {
            Text(
                text = "Furniture",
                color = ShopColors.Wood,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        } else {
            AsyncImage(
                model = resolvedImageUrl,
                contentDescription = name,
                placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                error = painterResource(id = R.drawable.ic_launcher_foreground),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    selected: Boolean
) {
    Surface(
        shape = ShopShapes.Pill,
        color = color,
        border = if (selected) BorderStroke(1.dp, ShopColors.TextPrimary) else null,
        modifier = Modifier.size(25.dp)
    ) {}
}

@Composable
private fun ProductInfoCard(
    price: String,
    description: String,
    stock: Int
) {
    Surface(
        shape = ShopShapes.Card,
        color = ShopColors.Surface,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                //.height(64.dp)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(0.86f)) {
                Text(
                    text = "Price",
                    style = MaterialTheme.typography.labelSmall,
                    color = ShopColors.TextSecondary
                )
                Text(
                    text = price,
                    style = MaterialTheme.typography.titleLarge,
                    color = ShopColors.TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (stock > 0) "Kho: $stock sản phẩm" else "Hết hàng",
                    style = MaterialTheme.typography.labelSmall,
                    color = if(stock > 5) Color(0xFF4CAF50) else Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(45.dp)
                    .background(ShopColors.Border)
            )

            Text(
                text = if (description.isBlank()) {
                    "Inspired by the footstools found in club spaces and bedrooms at Soho House."
                } else {
                    description
                },
                style = MaterialTheme.typography.labelSmall,
                color = ShopColors.TextSecondary,
                lineHeight = 13.sp,
                maxLines = 4,
                modifier = Modifier
                    .weight(1.14f)
                    .padding(start = 10.dp)
            )
        }
    }
}

@Composable
private fun DetailBottomBar(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = ShopColors.Background,
        shadowElevation = 0.dp
    ) {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp)
                .height(45.dp),
            shape = ShopShapes.Button,
            enabled = enabled,
            border = BorderStroke(1.dp, if (enabled) ShopColors.TextPrimary else ShopColors.Border),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = ShopColors.TextPrimary,
                disabledContentColor = ShopColors.TextSecondary
            )
        ) {
            Text(if (enabled) "Buy Now" else "Hết hàng")
        }
    }
}

private fun formatMoney(value: Double): String = value.formatVnd()
