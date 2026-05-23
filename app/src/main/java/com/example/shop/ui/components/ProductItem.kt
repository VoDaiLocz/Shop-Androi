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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        val resolvedImageUrl = Constants.toBackendImageUrl(imageUrl)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(154.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(9.dp))
                .background(ShopColors.Surface)
                .padding(8.dp)
        ) {
            if (resolvedImageUrl.isBlank()) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = name,
                    tint = ShopColors.Wood,
                    modifier = Modifier
                        .size(72.dp)
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
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                )
            }

            Text(
                text = price,
                color = ShopColors.WoodDark,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(ShopColors.Surface.copy(alpha = 0.9f), ShopShapes.Pill)
                    .padding(horizontal = 7.dp, vertical = 4.dp)
            )
        }

        Text(
            text = name,
            maxLines = 2,
            style = MaterialTheme.typography.bodySmall,
            color = ShopColors.TextPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 7.dp, start = 8.dp, end = 8.dp)
        )

        if (discount.isNotBlank() || oldPrice.isNotBlank()) {
            Text(
                text = listOf(oldPrice, discount).filter { it.isNotBlank() }.joinToString("  "),
                style = MaterialTheme.typography.labelSmall,
                color = ShopColors.TextSecondary,
                modifier = Modifier.padding(start = 6.dp, top = 2.dp)
            )
        }
    }
}
