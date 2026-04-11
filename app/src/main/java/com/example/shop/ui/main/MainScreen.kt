package com.example.shop.ui.main

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.shop.navigation.MainNavGraph
import com.example.shop.ui.components.BottomBar

@Composable
fun MainScreen(rootNavController: NavHostController) {

    val navController = rememberNavController()

    //LẤY ROUTE HIỆN TẠI
    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    //CHỈ HIỆN BottomBar Ở NHỮNG MÀN NÀY
    val showBottomBar = currentRoute in listOf(
        Routes.HOME,
        Routes.CART,
        Routes.PROFILE
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomBar(navController)
            }
        }
    ) { padding ->

        MainNavGraph(
            navController = navController,
            padding = padding
        )
    }
}