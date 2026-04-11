package com.example.shop.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppToolbar(title: String) {

    TopAppBar(
        title = {
            Text(text = title)
        }
    )
}