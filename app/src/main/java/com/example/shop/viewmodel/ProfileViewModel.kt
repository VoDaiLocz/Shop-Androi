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
            1,
            "Nguyễn Văn A",
            "test@gmail.com",
            "123456")
    )
    val user: StateFlow<User> = _user

    fun updateName(name: String) {
        _user.value = _user.value.copy(name = name)
    }
}