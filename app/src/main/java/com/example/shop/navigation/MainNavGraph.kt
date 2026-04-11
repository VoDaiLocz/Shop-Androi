package com.example.shop.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.shop.ui.cart.CartScreen
import com.example.shop.ui.checkout.CheckoutScreen
import com.example.shop.ui.home.HomeScreen
import com.example.shop.ui.order.OrderScreen
import com.example.shop.ui.product.ProductDetailScreen
import com.example.shop.ui.product.ProductScreen
import com.example.shop.ui.profile.ProfileScreen
import com.example.shop.viewmodel.ProductViewModel

@Composable
fun MainNavGraph(
    navController: NavHostController,
    padding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = Modifier.padding(padding)
    ) {

        composable(Routes.HOME) {
            HomeScreen(
                onOpenProduct = { navController.navigate(Routes.PRODUCT) },
                onOpenCart = { navController.navigate(Routes.CART) }
            )
        }

        // Màn hình Danh sách sản phẩm
        composable(Routes.PRODUCT) {
            val productViewModel: ProductViewModel = hiltViewModel()

            ProductScreen(
                viewModel = productViewModel,
                onClickItem = { productIdInt ->
                    navController.navigate("${Routes.PRODUCT_DETAIL}/$productIdInt")
                }
            )
        }

        composable("${Routes.PRODUCT_DETAIL}/{id}") { backStackEntry ->
            val idString = backStackEntry.arguments?.getString("id") ?: "0"

            // Nếu ProductDetailScreen cần lấy dữ liệu sản phẩm,
            // bạn cũng có thể dùng hiltViewModel() tại đây.
            ProductDetailScreen(
                productId = idString,
                onAddToCart = {
                    navController.navigate(Routes.CART)
                }
            )
        }

        composable(Routes.CART) {
            CartScreen {
                navController.navigate(Routes.CHECKOUT)
            }
        }

        composable(Routes.CHECKOUT) {
            CheckoutScreen {
                navController.navigate(Routes.ORDER)
            }
        }

        composable(Routes.ORDER) {
            OrderScreen()
        }

        composable(Routes.PROFILE) {
            ProfileScreen()
        }
    }
}