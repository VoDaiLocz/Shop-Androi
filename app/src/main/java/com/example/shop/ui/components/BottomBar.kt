package com.example.shop.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.shop.ui.theme.ShopColors

@Composable
fun BottomBar(
    navController: NavHostController,
    cartItemCount: Int
) {

    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(
        containerColor = ShopColors.Surface,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == Routes.HOME,
            onClick = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.HOME)
                    launchSingleTop = true
                }
            },
            icon = {
                Icon(Icons.Default.Home, contentDescription = "Home")
            },
            label = { Text("Home") },
            colors = odadingNavigationItemColors()
        )

        NavigationBarItem(
            selected = currentRoute == Routes.CART,
            onClick = {
                navController.navigate(Routes.CART) {
                    popUpTo(Routes.HOME)
                    launchSingleTop = true
                }
            },
            icon = {
                if (cartItemCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge {
                                Text(cartItemCount.toString())
                            }
                        }
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                    }
                } else {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                }
            },
            label = { Text("Cart") },
            colors = odadingNavigationItemColors()
        )

        NavigationBarItem(
            selected = currentRoute == Routes.PROFILE,
            onClick = {
                navController.navigate(Routes.PROFILE) {
                    popUpTo(Routes.HOME)
                    launchSingleTop = true
                }
            },
            icon = {
                Icon(Icons.Default.Person, contentDescription = "Profile")
            },
            label = { Text("Profile") },
            colors = odadingNavigationItemColors()
        )
    }
}

@Composable
private fun odadingNavigationItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = ShopColors.WoodDark,
    selectedTextColor = ShopColors.WoodDark,
    indicatorColor = ShopColors.SurfaceSoft,
    unselectedIconColor = ShopColors.TextSecondary,
    unselectedTextColor = ShopColors.TextSecondary
)
