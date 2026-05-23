package com.example.shop.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.shop.navigation.Routes
import com.example.shop.ui.theme.ShopColors

@Composable
fun BottomBar(
    navController: NavHostController,
    cartItemCount: Int
) {

    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    Surface(
        color = ShopColors.Surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(54.dp)
                .padding(horizontal = 34.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavAction(
                label = "Home",
                icon = Icons.Default.Home,
                selected = currentRoute == Routes.HOME,
                onClick = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME)
                        launchSingleTop = true
                    }
                }
            )

            BottomNavAction(
                label = "Cart",
                icon = Icons.Default.ShoppingCart,
                selected = currentRoute == Routes.CART,
                badge = cartItemCount.takeIf { it > 0 },
                onClick = {
                    navController.navigate(Routes.CART) {
                        popUpTo(Routes.HOME)
                        launchSingleTop = true
                    }
                }
            )

            BottomNavAction(
                label = "Profile",
                icon = Icons.Default.Person,
                selected = currentRoute == Routes.PROFILE,
                onClick = {
                    navController.navigate(Routes.PROFILE) {
                        popUpTo(Routes.HOME)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
private fun BottomNavAction(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    badge: Int? = null,
    onClick: () -> Unit
) {
    val color = if (selected) ShopColors.TextPrimary else ShopColors.TextSecondary

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (badge != null) {
                BadgedBox(
                    badge = {
                        Badge(containerColor = ShopColors.Wood) {
                            Text(badge.toString(), color = ShopColors.Surface)
                        }
                    }
                ) {
                    Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(19.dp))
                }
            } else {
                Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(19.dp))
            }
        }

        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}
