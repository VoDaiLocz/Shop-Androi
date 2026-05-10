package com.example.shop.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomBar(
    navController: NavHostController,
    cartItemCount: Int
) {

    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {
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
            label = { Text("Home") }
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
            label = { Text("Cart") }
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
            label = { Text("Profile") }
        )
    }
}
