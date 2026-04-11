package com.example.shop.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.shop.ui.auth.LoginScreen
import com.example.shop.ui.main.MainScreen
import com.example.shop.ui.onboarding.BoardingScreen
import com.example.shop.viewmodel.AuthViewModel

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
                val authViewModel: AuthViewModel = hiltViewModel()
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.AUTH) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Routes.REGISTER)
                    }
                )
            }
        }

        //MAIN FLOW
        composable(Routes.MAIN) {
            MainScreen(navController)
        }
    }
}