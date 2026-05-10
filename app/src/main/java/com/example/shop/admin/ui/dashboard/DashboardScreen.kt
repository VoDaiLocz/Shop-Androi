package com.example.shop.admin.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onManageProducts: () -> Unit,
    onManageCategories: () -> Unit,
    onManageOrders: () -> Unit,
    onManageUsers: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Đăng xuất")
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
            Text(
                text = "Chào mừng Admin,",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Hôm nay bạn muốn quản lý gì?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Lưới các chức năng quản lý
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    AdminMenuCard(
                        title = "Sản phẩm",
                        icon = Icons.Default.Inventory,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        onClick = onManageProducts
                    )
                }
                item {
                    AdminMenuCard(
                        title = "Danh mục",
                        icon = Icons.Default.Category,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = onManageCategories
                    )
                }
                item {
                    AdminMenuCard(
                        title = "Đơn hàng",
                        icon = Icons.Default.ShoppingCart,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        onClick =  onManageOrders
                    )
                }
                item {
                    AdminMenuCard(
                        title = "Người dùng",
                        icon = Icons.Default.Person,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        onClick = onManageUsers
                    )
                }
            }
        }
    }
}

@Composable
fun AdminMenuCard(
    title: String,
    icon: ImageVector,
    containerColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable { onClick() },
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
