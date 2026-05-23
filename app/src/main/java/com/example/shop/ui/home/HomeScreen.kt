package com.example.shop.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.shop.R
import com.example.shop.data.model.Category
import com.example.shop.ui.components.ProductItem
import com.example.shop.ui.theme.ShopColors
import com.example.shop.ui.theme.ShopShapes
import com.example.shop.utils.formatVnd
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
        contentPadding = PaddingValues(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            HomeHeader(onOpenCart = onOpenCart)
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            CategoryTabs(
                categories = categories,
                onOpenCategory = onOpenCategory
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            PromoBanner()
        }

        items(products.take(10), key = { product -> product.id }) { product ->
            ProductItem(
                name = product.name,
                price = product.price.formatVnd(),
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
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = { }, modifier = Modifier.size(38.dp)) {
            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = ShopColors.TextPrimary)
        }

        Text(
            text = "Odading",
            style = MaterialTheme.typography.titleSmall,
            color = ShopColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = onOpenCart, modifier = Modifier.size(38.dp)) {
            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = ShopColors.TextPrimary)
        }
    }
}

@Composable
private fun CategoryTabs(
    categories: List<Category>,
    onOpenCategory: (Int) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 7.dp, bottom = 7.dp),
        modifier = Modifier.fillMaxWidth()
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

@Composable
private fun PromoBanner() {
    Surface(
        shape = ShopShapes.Card,
        color = ShopColors.Surface,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.banner_art_living),
            contentDescription = "The Art of Living",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
                .clip(ShopShapes.Card)
        )
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
        color = if (isSelected) ShopColors.Surface else Color.Transparent,
        border = if (isSelected) BorderStroke(1.dp, ShopColors.TextPrimary) else null
    ) {
        Text(
            text = name,
            modifier = Modifier
                .height(30.dp)
                .padding(horizontal = 13.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelSmall,
            color = ShopColors.TextPrimary,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
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
