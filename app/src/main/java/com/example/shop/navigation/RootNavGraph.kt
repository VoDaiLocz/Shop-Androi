package com.example.shop.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.shop.ui.auth.LoginScreen
import com.example.shop.ui.main.MainScreen
import com.example.shop.ui.onboarding.BoardingScreen

@Composable
fun RootNavGraph() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.AUTH
    ) {

        //AUTH FLOW
        navigation(
            startDestination = Routes.BOARDING,
            route = Routes.AUTH
        ) {

            composable(Routes.BOARDING) {
                BoardingScreen {
                    navController.navigate(Routes.LOGIN)
                }
            }

            composable(Routes.LOGIN) {
                LoginScreen {
                    //login xong qua MAIN
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            }
        }

        //MAIN FLOW
        composable(Routes.MAIN) {
            MainScreen(navController)
        }
    }
}