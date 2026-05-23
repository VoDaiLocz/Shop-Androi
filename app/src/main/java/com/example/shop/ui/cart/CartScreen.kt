package com.example.shop.ui.cart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.shop.data.model.CartItem
import com.example.shop.ui.theme.ShopColors
import com.example.shop.ui.theme.ShopShapes
import com.example.shop.utils.Constants
import com.example.shop.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onNavigateBack: () -> Unit,
    onCheckout: () -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    val items by viewModel.cartItems.collectAsState(initial = emptyList())
    val totalPrice = items.sumOf { it.price * it.quantity }

    Scaffold(
        containerColor = ShopColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Cart",
                        color = ShopColors.TextPrimary,
                        fontWeight = FontWeight.Medium
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
            if (items.isNotEmpty()) {
                BottomCheckoutSection(total = totalPrice, onCheckout = onCheckout)
            }
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Giỏ hàng đang trống",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ShopColors.TextSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.id }) { item ->
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
fun CartItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onDelete: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            CartProductImage(item = item)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 8.dp)
            ) {
                Text(
                    item.productName,
                    color = ShopColors.TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 21.sp,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 1.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 9.dp)
                ) {
                    QuantityButton(text = "−", onClick = onDecrease)
                    Text("${item.quantity}", color = ShopColors.TextPrimary, style = MaterialTheme.typography.bodyMedium)
                    QuantityButton(text = "+", onClick = onIncrease)
                }
            }

            Text(
                formatMoney(item.price * item.quantity),
                color = ShopColors.TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 50.dp)
            )
        }

        HorizontalDivider(color = ShopColors.Border.copy(alpha = 0.7f))
    }
}

@Composable
private fun QuantityButton(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(27.dp)
            .clickable(onClick = onClick),
        shape = ShopShapes.Pill,
        color = Color.Transparent,
        border = BorderStroke(1.dp, ShopColors.TextPrimary.copy(alpha = 0.45f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, color = ShopColors.TextPrimary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun CartProductImage(item: CartItem) {
    val resolvedImageUrl = Constants.toBackendImageUrl(item.imageUrl)

    Box(
        modifier = Modifier
            .size(86.dp)
            .clip(ShopShapes.Image)
            .background(ShopColors.SurfaceSoft),
        contentAlignment = Alignment.Center
    ) {
        if (resolvedImageUrl.isBlank()) {
            Text("Item", color = ShopColors.Wood, style = MaterialTheme.typography.labelMedium)
        } else {
            AsyncImage(
                model = resolvedImageUrl,
                contentDescription = item.productName,
                placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                error = painterResource(id = R.drawable.ic_launcher_foreground),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun BottomCheckoutSection(total: Double, onCheckout: () -> Unit) {
    Surface(
        color = ShopColors.Background,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            SummaryLine(label = "Subtotal:", value = formatMoney(total))
            SummaryLine(label = "Shipping:", value = "Free")
            HorizontalDivider(color = ShopColors.Border, modifier = Modifier.padding(top = 5.dp))
            SummaryLine(
                label = "Total:",
                value = formatMoney(total),
                labelStyle = MaterialTheme.typography.titleMedium,
                valueStyle = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 5.dp, bottom = 12.dp)
            )
            Button(
                onClick = onCheckout,
                shape = ShopShapes.Button,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Proceed to Checkout")
            }
        }
    }
}

@Composable
private fun SummaryLine(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    labelStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    valueStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = ShopColors.TextPrimary, style = labelStyle)
        Text(value, color = ShopColors.TextPrimary, style = valueStyle, fontWeight = FontWeight.Medium)
    }
}

private fun formatMoney(value: Double): String = "$" + String.format("%.2f", value)
