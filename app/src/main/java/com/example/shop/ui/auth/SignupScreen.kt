package com.example.shop.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {

        Text("Sign Up")

        Button(onClick = onSignupSuccess) {
            Text("Create Account")
        }
    }
}
