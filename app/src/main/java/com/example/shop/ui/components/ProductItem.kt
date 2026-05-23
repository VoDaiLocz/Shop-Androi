package com.example.shop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.shop.R
import com.example.shop.ui.theme.ShopColors
import com.example.shop.ui.theme.ShopShapes
import com.example.shop.utils.Constants

@Composable
fun ProductItem(
    name: String,
    price: String,
    oldPrice: String,
    discount: String,
    imageUrl: String,
    onClick: () -> Unit
) {
    val resolvedImageUrl = Constants.toBackendImageUrl(imageUrl)

    Surface(
        shape = ShopShapes.Card,
        color = ShopColors.Surface,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(178.dp)
            .clickable { onClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(122.dp)
                    .clip(ShopShapes.Image)
                    .background(ShopColors.Surface),
                contentAlignment = Alignment.Center
            ) {
                if (resolvedImageUrl.isBlank()) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = name,
                        tint = ShopColors.Wood,
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.Center)
                    )
                } else {
                    AsyncImage(
                        model = resolvedImageUrl,
                        contentDescription = name,
                        placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                        error = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(0.dp)
                    )
                }
            }

            Text(
                text = name,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall,
                color = ShopColors.TextPrimary,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(start = 10.dp, top = 8.dp, end = 10.dp)
            )

            Text(
                text = price,
                color = ShopColors.TextPrimary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 10.dp, top = 1.dp, end = 10.dp)
            )

            if (discount.isNotBlank() || oldPrice.isNotBlank()) {
                Text(
                    text = listOf(oldPrice, discount).filter { it.isNotBlank() }.joinToString("  "),
                    style = MaterialTheme.typography.labelSmall,
                    color = ShopColors.TextSecondary,
                    modifier = Modifier.padding(start = 10.dp, top = 2.dp, end = 10.dp)
                )
            }
        }
    }
}
