package com.example.shop.ui.main

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.shop.navigation.MainNavGraph
import com.example.shop.ui.components.BottomBar
import com.example.shop.viewmodel.CartViewModel

@Composable
fun MainScreen(
    cartViewModel: CartViewModel = hiltViewModel()
) {

    val navController = rememberNavController()
    val cartItems by cartViewModel.cartItems.collectAsState()
    val cartItemCount = cartItems.sumOf { item -> item.quantity }

    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Routes.PROFILE
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomBar(
                    navController = navController,
                    cartItemCount = cartItemCount
                )
            }
        }
    ) { padding ->

        MainNavGraph(
            navController = navController,
            padding = padding
        )
    }
}
