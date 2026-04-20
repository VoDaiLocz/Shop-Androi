package com.example.shop.viewmodel

import androidx.lifecycle.ViewModel
import com.example.shop.data.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {
    private val _user = MutableStateFlow(
        User(
            id = 1,
            username = "Nguyễn Văn A",
            email = "test@gmail.com",
            password = "123456",
            role = "USER"
        )
    )
    val user: StateFlow<User> = _user

    fun updateName(newName: String) {
        _user.value = _user.value.copy(username = newName)
    }
}