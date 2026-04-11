package com.example.shop.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {

        Text("Login")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onLoginSuccess) {
            Text("Login")
        }
    }
}
