package com.example.shop.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.example.shop.data.model.User
import com.example.shop.ui.theme.ShopColors
import com.example.shop.ui.theme.ShopShapes
import com.example.shop.utils.Constants
import com.example.shop.viewmodel.AuthViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (User) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isGoogleLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShopColors.Background)
            .padding(18.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Odading",
            style = MaterialTheme.typography.headlineLarge,
            color = ShopColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Minimalist furniture for your home",
            style = MaterialTheme.typography.bodyMedium,
            color = ShopColors.TextSecondary
        )

        Spacer(modifier = Modifier.height(28.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = ShopShapes.Card,
            colors = CardDefaults.elevatedCardColors(containerColor = ShopColors.Surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.titleLarge,
                    color = ShopColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = ShopShapes.Button
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = ShopShapes.Button
                )

                errorMessage?.let { message ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = message, color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        viewModel.login(email, password) { user ->
                            if (user != null) {
                                errorMessage = null
                                onLoginSuccess(user)
                            } else {
                                errorMessage = "Sai email hoặc mật khẩu!"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = ShopShapes.Button,
                    colors = ButtonDefaults.buttonColors(containerColor = ShopColors.WoodDark)
                ) {
                    Text("Login")
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            isGoogleLoading = true
                            errorMessage = null

                            val idToken = getGoogleIdToken(credentialManager, context)
                            if (idToken == null) {
                                errorMessage = "Không thể đăng nhập Google."
                                isGoogleLoading = false
                                return@launch
                            }

                            viewModel.loginWithGoogle(idToken) { user, backendError ->
                                isGoogleLoading = false
                                if (user != null) {
                                    onLoginSuccess(user)
                                } else {
                                    errorMessage = backendError ?: "Backend không chấp nhận tài khoản Google này."
                                }
                            }
                        }
                    },
                    enabled = !isGoogleLoading,
                    border = BorderStroke(1.dp, ShopColors.Border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = ShopShapes.Button,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ShopColors.TextPrimary)
                ) {
                    Text(if (isGoogleLoading) "Đang đăng nhập..." else "Sign in with Google")
                }

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(
                    onClick = onNavigateToRegister,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Don't have an account? Sign Up", color = ShopColors.Wood)
                }
            }
        }
    }
}

private suspend fun getGoogleIdToken(
    credentialManager: CredentialManager,
    context: android.content.Context
): String? {
    return runCatching {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(Constants.GOOGLE_WEB_CLIENT_ID)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(context, request)
        GoogleIdTokenCredential.createFrom(result.credential.data).idToken
    }.getOrNull()
}
