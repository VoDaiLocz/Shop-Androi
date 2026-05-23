package com.example.shop.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.shop.R
import com.example.shop.ui.components.ProductItem
import com.example.shop.ui.theme.ShopColors
import com.example.shop.ui.theme.ShopShapes
import com.example.shop.utils.Constants
import com.example.shop.viewmodel.ProductViewModel
import com.example.shop.viewmodel.UserCategoryViewModel

@Composable
fun HomeScreen(
    onOpenProduct: (String) -> Unit,
    onOpenCategory: (Int) -> Unit,
    onOpenCart: () -> Unit,
    productViewModel: ProductViewModel = hiltViewModel(),
    categoryViewModel: UserCategoryViewModel = hiltViewModel()
) {
    val products by productViewModel.products.collectAsState()
    val categories by categoryViewModel.categories.collectAsState()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .background(ShopColors.Background),
        contentPadding = PaddingValues(18.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            HomeHeader(onOpenCart = onOpenCart)
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            PromoBanner(imageUrl = products.firstOrNull()?.imageUrl.orEmpty())
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 2.dp),
                modifier = Modifier.padding(top = 6.dp)
            ) {
                item {
                    CategoryChip(
                        name = "ALL",
                        isSelected = true,
                        onClick = { onOpenCategory(-1) }
                    )
                }

                items(categories) { category ->
                    CategoryChip(
                        name = shortCategoryName(category.name).uppercase(),
                        isSelected = false,
                        onClick = { onOpenCategory(category.id) }
                    )
                }
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Popular Furniture", style = MaterialTheme.typography.titleMedium, color = ShopColors.TextPrimary, fontWeight = FontWeight.Bold)
                Text(
                    text = "${products.size} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = ShopColors.TextSecondary
                )
            }
        }

        items(products.take(10), key = { product -> product.id }) { product ->
            ProductItem(
                name = product.name,
                price = "$${product.price.toInt()}",
                oldPrice = "",
                discount = "",
                imageUrl = product.imageUrl,
                onClick = { onOpenProduct(product.id.toString()) }
            )
        }
    }
}

@Composable
private fun HomeHeader(onOpenCart: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = { }) {
            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = ShopColors.TextPrimary)
        }

        Text(
            text = "Odading",
            style = MaterialTheme.typography.titleMedium,
            color = ShopColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )

        Row {
            IconButton(onClick = { }) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = ShopColors.TextPrimary)
            }
            IconButton(onClick = onOpenCart) {
                Icon(Icons.Default.ShoppingCart, contentDescription = "Giỏ hàng", tint = ShopColors.TextPrimary)
            }
        }
    }
}

@Composable
private fun PromoBanner(imageUrl: String) {
    val resolvedImageUrl = Constants.toBackendImageUrl(imageUrl)

    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        color = ShopColors.Surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(122.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                .background(ShopColors.Surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ShopColors.Surface)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 14.dp, end = 138.dp)
            ) {
                Text(
                    text = "Promo for first purchase",
                    style = MaterialTheme.typography.titleLarge,
                    color = ShopColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Special Offers",
                    style = MaterialTheme.typography.bodySmall,
                    color = ShopColors.TextSecondary
                )
                Text(
                    text = "40% Off Prices",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ShopColors.WoodDark,
                    fontWeight = FontWeight.Bold
                )
            }

            if (resolvedImageUrl.isNotBlank()) {
                AsyncImage(
                    model = resolvedImageUrl,
                    contentDescription = "Promo furniture",
                    placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                    error = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxWidth(0.46f)
                        .height(112.dp)
                        .padding(end = 8.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = ShopShapes.Pill,
        color = if (isSelected) ShopColors.WoodDark else ShopColors.Background,
        border = if (isSelected) BorderStroke(1.dp, ShopColors.WoodDark) else null
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) ShopColors.Surface else ShopColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun shortCategoryName(name: String): String {
    return when {
        name.length <= 8 -> name
        name.contains(" ") -> name.split(" ").first()
        else -> name.take(8)
    }
}
