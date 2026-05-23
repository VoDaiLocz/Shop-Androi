package com.example.shop.ui.checkout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.shop.ui.theme.ShopColors
import com.example.shop.ui.theme.ShopShapes
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
    val currentUser by authViewModel.currentUser.collectAsState()
    val cartItems by orderViewModel.cartItems.collectAsState()
    val totalPrice = cartItems.sumOf { it.price * it.quantity }

    var address by remember { mutableStateOf("123 Main St, Apt 4B\nNew York, NY 10001") }
    var phoneNumber by remember { mutableStateOf("0900000000") }
    var selectedPayment by remember { mutableStateOf("Visa **** 1234") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = ShopColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Checkout",
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            SectionTitle("Shipping Address")
            AddressCard(
                name = currentUser?.username?.ifBlank { "John Doe" } ?: "John Doe",
                address = address,
                onEdit = {
                    address = if (address.startsWith("123 Main")) {
                        "456 Oak Street\nNew York, NY 10002"
                    } else {
                        "123 Main St, Apt 4B\nNew York, NY 10001"
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            SectionTitle("Payment Method")
            PaymentOption("Visa **** 1234", selectedPayment) { selectedPayment = it }
            PaymentOption("Mastercard **** 5678", selectedPayment) { selectedPayment = it }
            PaymentOption("Apple Pay", selectedPayment) { selectedPayment = it }

            Spacer(modifier = Modifier.height(28.dp))

            SectionTitle("Order Summary")
            cartItems.take(2).forEach { item ->
                SummaryLine(
                    label = "${item.productName} x${item.quantity}",
                    value = formatMoney(item.price * item.quantity)
                )
            }
            SummaryLine(label = "Shipping", value = "Free", modifier = Modifier.padding(top = 6.dp))
            HorizontalDivider(color = ShopColors.Border, modifier = Modifier.padding(top = 8.dp))
            SummaryLine(
                label = "Total",
                value = formatMoney(totalPrice),
                modifier = Modifier.padding(top = 9.dp),
                valueWeight = FontWeight.Medium
            )

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = ShopColors.Error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            OutlinedButton(
                onClick = {
                    val user = currentUser
                    if (user != null && cartItems.isNotEmpty()) {
                        orderViewModel.placeOrder(
                            userId = user.id,
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
                enabled = cartItems.isNotEmpty() && currentUser != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = ShopShapes.Button,
                border = BorderStroke(1.dp, ShopColors.TextPrimary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ShopColors.TextPrimary)
            ) {
                Text("Place Order")
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = ShopColors.TextPrimary,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 9.dp)
    )
}

@Composable
private fun AddressCard(
    name: String,
    address: String,
    onEdit: () -> Unit
) {
    Surface(
        shape = ShopShapes.Card,
        color = ShopColors.Surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 10.dp, end = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = ShopColors.TextPrimary, fontWeight = FontWeight.Medium)
                Text(
                    address,
                    color = ShopColors.TextPrimary,
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            TextButton(
                onClick = onEdit,
                border = BorderStroke(1.dp, ShopColors.TextPrimary),
                shape = ShopShapes.Pill,
                modifier = Modifier.height(36.dp)
            ) {
                Text("Edit", color = ShopColors.TextPrimary)
            }
        }
    }
}

@Composable
private fun PaymentOption(
    label: String,
    selectedPayment: String,
    onSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = label == selectedPayment,
            onClick = { onSelected(label) },
            colors = RadioButtonDefaults.colors(
                selectedColor = ShopColors.TextPrimary,
                unselectedColor = ShopColors.TextSecondary
            ),
            modifier = Modifier.size(24.dp)
        )
        Text(
            label,
            color = ShopColors.TextPrimary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun SummaryLine(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueWeight: FontWeight = FontWeight.Normal
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = ShopColors.TextPrimary,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = ShopColors.TextPrimary,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = valueWeight,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

private fun formatMoney(value: Double): String = "$" + String.format("%.2f", value)
