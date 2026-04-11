package com.example.shop.ui.address

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddressScreen(
    onAdd: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {

        Text("My Addresses")

        Button(onClick = onAdd) {
            Text("Add Address")
        }

        LazyColumn {
            items(listOf("123 ABC", "456 XYZ")) {
                Text(it, modifier = Modifier.padding(8.dp))
            }
        }
    }
}