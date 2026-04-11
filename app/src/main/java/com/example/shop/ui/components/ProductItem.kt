package com.example.shop.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.example.shop.R

@Composable
fun ProductItem(
    name: String,
    price: String,
    oldPrice: String,
    discount: String,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .padding(6.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {

            // IMAGE
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Column(modifier = Modifier.padding(8.dp)) {

                Text(
                    text = name,
                    maxLines = 2,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = price,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = oldPrice,
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = discount,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}