package com.example.shop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = ShopShapes.Card,
        colors = CardDefaults.elevatedCardColors(containerColor = ShopColors.Surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            val resolvedImageUrl = Constants.toBackendImageUrl(imageUrl)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(ShopShapes.Image)
                    .background(ShopColors.SurfaceSoft)
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
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (discount.isNotBlank()) {
                    Surface(
                        color = ShopColors.WoodDark,
                        shape = CircleShape,
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = discount,
                            color = ShopColors.Surface,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) {
                Text(
                    text = name,
                    maxLines = 2,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ShopColors.TextPrimary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = price,
                    color = ShopColors.WoodDark,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                if (oldPrice.isNotBlank()) {
                    Text(
                        text = oldPrice,
                        style = MaterialTheme.typography.bodySmall,
                        color = ShopColors.TextSecondary
                    )
                }
            }
        }
    }
}
