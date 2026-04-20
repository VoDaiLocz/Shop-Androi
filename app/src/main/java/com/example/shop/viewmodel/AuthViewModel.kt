package com.example.shop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shop.data.model.User
import com.example.shop.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    fun login(email: String, password: String, onResult: (User?) -> Unit) {
        viewModelScope.launch {
            val user = authRepository.login(email, password)
            if (user != null) {
                _isLoggedIn.value = true
                onResult(user) // Trả về toàn bộ thông tin user (bao gồm role)
            } else {
                onResult(null)
            }
        }
    }

    fun register(email: String, pass: String, name: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val newUser = User(
                username = name,
                email = email,
                password = pass,
                role = "USER"
            )

            val isSuccess = authRepository.register(newUser)
            onResult(isSuccess)
        }
    }
}