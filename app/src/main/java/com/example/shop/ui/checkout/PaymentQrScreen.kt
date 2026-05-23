package com.example.shop.ui.checkout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.shop.data.remote.dto.OrderPaymentStatusResponse
import com.example.shop.ui.theme.ShopColors
import com.example.shop.ui.theme.ShopShapes
import com.example.shop.utils.formatVnd
import com.example.shop.viewmodel.OrderViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentQrScreen(
    orderId: Int,
    onNavigateBack: () -> Unit,
    onPaid: () -> Unit,
    viewModel: OrderViewModel = hiltViewModel()
) {
    var payment by remember { mutableStateOf<OrderPaymentStatusResponse?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(orderId) {
        while (isActive) {
            val next = viewModel.getPaymentStatus(orderId)
            if (next == null) {
                errorMessage = "Không thể kiểm tra trạng thái thanh toán."
            } else {
                payment = next
                errorMessage = null
                if (next.paymentStatus.equals("Paid", ignoreCase = true)) {
                    viewModel.refreshCart()
                    onPaid()
                    break
                }
            }
            delay(3000)
        }
    }

    Scaffold(
        containerColor = ShopColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Thanh toán SePay",
                        color = ShopColors.TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShopColors.Background)
            )
        }
    ) { padding ->
        val currentPayment = payment
        if (currentPayment == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ShopColors.TextPrimary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = ShopShapes.Card,
                color = ShopColors.Surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Quét mã để thanh toán",
                        style = MaterialTheme.typography.titleMedium,
                        color = ShopColors.TextPrimary,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PaymentQrImage(currentPayment.paymentQrUrl)

                    Spacer(modifier = Modifier.height(18.dp))

                    PaymentLine("Số tiền", formatMoney(currentPayment.totalPrice))
                    PaymentLine("Nội dung", currentPayment.paymentCode.orEmpty())
                    PaymentLine("Trạng thái", paymentStatusText(currentPayment.paymentStatus))

                    errorMessage?.let { message ->
                        Text(
                            text = message,
                            color = ShopColors.Error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedButton(
                onClick = onNavigateBack,
                shape = ShopShapes.Button,
                border = BorderStroke(1.dp, ShopColors.TextPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Text("Quay lại")
            }
        }
    }
}

@Composable
private fun PaymentQrImage(qrUrl: String?) {
    Box(
        modifier = Modifier
            .size(236.dp)
            .clip(ShopShapes.Card)
            .background(ShopColors.SurfaceSoft),
        contentAlignment = Alignment.Center
    ) {
        if (qrUrl.isNullOrBlank()) {
            Text(
                "Chưa có mã QR",
                color = ShopColors.TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            AsyncImage(
                model = qrUrl,
                contentDescription = "SePay QR",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            )
        }
    }
}

@Composable
private fun PaymentLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = ShopColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
        Text(
            value.ifBlank { "-" },
            color = ShopColors.TextPrimary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
    HorizontalDivider(color = ShopColors.Border.copy(alpha = 0.7f))
}

private fun paymentStatusText(status: String): String {
    return when {
        status.equals("Paid", ignoreCase = true) -> "Đã thanh toán"
        status.equals("Failed", ignoreCase = true) -> "Cần kiểm tra"
        else -> "Đang chờ"
    }
}

private fun formatMoney(value: Double): String = value.formatVnd()
