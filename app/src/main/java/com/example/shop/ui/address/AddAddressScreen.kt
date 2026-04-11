package com.example.shop.ui.address

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddAddressScreen(
    onSave: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {

        Text("Add Address")

        Button(onClick = onSave) {
            Text("Save")
        }
    }
}
