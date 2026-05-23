package com.example.shop.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.shop.ui.theme.ShopColors
import com.example.shop.ui.theme.ShopShapes
import com.example.shop.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToOrders: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = ShopColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile",
                        color = ShopColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShopColors.Background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(18.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = ShopShapes.Card,
                colors = CardDefaults.elevatedCardColors(containerColor = ShopColors.Surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(96.dp),
                        shape = ShopShapes.Pill,
                        color = ShopColors.SurfaceSoft
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.padding(22.dp),
                            tint = ShopColors.Wood
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    currentUser?.let { user ->
                        Text(
                            text = user.username,
                            fontSize = 24.sp,
                            color = ShopColors.TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = user.email,
                            fontSize = 15.sp,
                            color = ShopColors.TextSecondary
                        )

                        Surface(
                            shape = ShopShapes.Pill,
                            color = ShopColors.SurfaceSoft,
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            Text(
                                text = user.role,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                color = ShopColors.Wood,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            ProfileActionButton(text = "Lịch sử đơn hàng", onClick = onNavigateToOrders) {
                Icon(Icons.Default.History, contentDescription = null)
            }

            Spacer(modifier = Modifier.height(12.dp))

            ProfileActionButton(text = "Cài đặt ứng dụng", onClick = onNavigateToSettings) {
                Icon(Icons.Default.Settings, contentDescription = null)
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = ShopColors.Border)
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    authViewModel.logout()
                    onLogout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = ShopShapes.Button,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đăng xuất")
            }
        }
    }
}

@Composable
private fun ProfileActionButton(
    text: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = ShopShapes.Button,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = ShopColors.TextPrimary)
    ) {
        icon()
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}
