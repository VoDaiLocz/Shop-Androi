package com.example.shop.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.example.shop.R
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
    var rememberMe by remember { mutableStateOf(false) }
    val screenBackground = Color(0xFFE7E5F6)
    val controlBackground = Color(0xFFF4F5FA)
    val primaryPurple = Color(0xFF7168FF)
    val linkBlue = Color(0xFF0A7FDB)
    val softBorder = Color(0xFFD6D7E4)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 34.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = ShopShapes.Pill,
            color = Color.White,
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.ic_odading_mark),
                    contentDescription = "Odading",
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Text(
            text = "Odading",
            style = MaterialTheme.typography.titleLarge,
            color = ShopColors.TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        SocialLoginButton(
            text = if (isGoogleLoading) "Đang đăng nhập..." else "Continue With Google",
            enabled = !isGoogleLoading,
            borderColor = softBorder,
            backgroundColor = controlBackground,
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
            }
        )

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Email or user name",
            style = MaterialTheme.typography.labelSmall,
            color = ShopColors.TextPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 7.dp)
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Email address") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = ShopShapes.Button
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Password",
            style = MaterialTheme.typography.labelSmall,
            color = ShopColors.TextPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 7.dp)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = ShopShapes.Button
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = primaryPurple,
                        uncheckedColor = ShopColors.TextSecondary
                    ),
                    modifier = Modifier.size(34.dp)
                )
                Text(
                    text = "Remember me",
                    style = MaterialTheme.typography.labelSmall,
                    color = ShopColors.TextPrimary
                )
            }

            Text(
                text = "Forgot Password",
                style = MaterialTheme.typography.labelSmall,
                color = linkBlue
            )
        }

        errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

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
            colors = ButtonDefaults.buttonColors(containerColor = primaryPurple)
        ) {
            Text("Log in")
        }

        Spacer(modifier = Modifier.height(14.dp))

        TextButton(
            onClick = onNavigateToRegister,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Do you have account? Sign Up", color = linkBlue)
        }
    }
}

@Composable
private fun SocialLoginButton(
    text: String,
    enabled: Boolean,
    borderColor: Color,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = ShopShapes.Button,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = backgroundColor,
            contentColor = ShopColors.TextPrimary
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = "Google",
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(20.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.size(18.dp))
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
