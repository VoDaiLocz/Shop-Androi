package com.example.shop.navigation

import AddCategoryScreen
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.shop.admin.ui.category.ManageCategoryScreen
import com.example.shop.admin.ui.category.UpdateCategoryScreen
import com.example.shop.admin.ui.dashboard.DashboardScreen
import com.example.shop.admin.ui.product.AddProductScreen
import com.example.shop.admin.ui.product.ManageProductScreen
import com.example.shop.admin.ui.product.UpdateProductScreen
import com.example.shop.ui.auth.LoginScreen
import com.example.shop.ui.auth.RegisterScreen
import com.example.shop.ui.cart.CartScreen
import com.example.shop.ui.checkout.CheckoutScreen
import com.example.shop.ui.home.HomeScreen
import com.example.shop.ui.order.OrderScreen
import com.example.shop.ui.product.ProductDetailScreen
import com.example.shop.ui.product.ProductScreen
import com.example.shop.ui.profile.ProfileScreen
import com.example.shop.viewmodel.AuthViewModel
import com.example.shop.viewmodel.ProductViewModel

@Composable
fun MainNavGraph(
    navController: NavHostController,
    padding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        modifier = Modifier.padding(padding)
    ) {
        // --- AUTHENTICATION ---
        composable(Routes.LOGIN) {
            val authViewModel: AuthViewModel = hiltViewModel()
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { user ->
                    if (user.role.trim().equals("ADMIN", ignoreCase = true)) {
                        navController.navigate(Routes.ADMIN_DASHBOARD) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        composable(Routes.REGISTER) {
            val authViewModel: AuthViewModel = hiltViewModel()
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = { navController.popBackStack() },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        // --- ADMIN DASHBOARD ---
        composable(Routes.ADMIN_DASHBOARD) {
            DashboardScreen(
                onManageProducts = { navController.navigate(Routes.ADMIN_MANAGE_PRODUCT) },
                onManageCategories = { navController.navigate(Routes.ADMIN_MANAGE_CATEGORY) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ADMIN_DASHBOARD) { inclusive = true }
                    }
                }
            )
        }
        // ===================ADMIN PRODUCT====================================
        composable(Routes.ADMIN_MANAGE_PRODUCT) {
            ManageProductScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddProduct = { navController.navigate(Routes.ADMIN_ADD_PRODUCT) },
                onNavigateToUpdateProduct = { productId ->
                    navController.navigate("${Routes.ADMIN_UPDATE_PRODUCT}/$productId")                }
            )
        }

        composable(Routes.ADMIN_ADD_PRODUCT) {
            AddProductScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Routes.ADMIN_UPDATE_PRODUCT}/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 0
            UpdateProductScreen(
                productId = productId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        //==================== ADMIN CATEGORY====================================
        composable(Routes.ADMIN_MANAGE_CATEGORY) {
            ManageCategoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddCategory = { navController.navigate(Routes.ADMIN_ADD_CATEGORY) },
                onNavigateToUpdateCategory = { categoryId ->
                    navController.navigate("${Routes.ADMIN_UPDATE_CATEGORY}/$categoryId")
                }
            )
        }

        composable(Routes.ADMIN_ADD_CATEGORY) {
            AddCategoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Routes.ADMIN_UPDATE_CATEGORY}/{categoryId}",
            arguments = listOf(navArgument("categoryId") { type = NavType.IntType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: 0
            UpdateCategoryScreen(
                categoryId = categoryId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- USER SCREENS ---
        composable(Routes.HOME) {
            HomeScreen(
                onOpenProduct = { id -> navController.navigate("${Routes.PRODUCT_DETAIL}/$id") },
                onOpenCategory = { id -> navController.navigate("${Routes.PRODUCT}/$id") }, // ĐÂY LÀ CHỖ CHUYỂN TRANG
                onOpenCart = { navController.navigate(Routes.CART) }
            )
        }

        composable("${Routes.PRODUCT}/{categoryId}") { backStackEntry ->
            val catId = backStackEntry.arguments?.getString("categoryId")?.toInt() ?: -1
            ProductScreen(
                categoryId = catId,
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() },
                onClickItem = { id -> navController.navigate("${Routes.PRODUCT_DETAIL}/$id") }
            )
        }


        composable("${Routes.PRODUCT_DETAIL}/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: "0"
            ProductDetailScreen(
                productId = id,
                onAddToCart = {
                    navController.navigate(Routes.CART)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }


        composable(Routes.CART) {
            CartScreen(
                onNavigateBack = { navController.popBackStack() }, // THÊM DÒNG NÀY
                onCheckout = { navController.navigate(Routes.CHECKOUT) }
            )
        }

        composable(Routes.CHECKOUT) {
            CheckoutScreen(onPlaceOrder = {
                navController.navigate(Routes.ORDER)
            })
        }

        composable(Routes.ORDER) { OrderScreen() }
        composable(Routes.PROFILE) { ProfileScreen() }
    }
}