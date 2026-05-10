package com.example.shop.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shop.data.remote.dto.ApiUserResponse
import com.example.shop.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminUserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _users = MutableStateFlow<List<ApiUserResponse>>(emptyList())
    val users = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = userRepository.getUsers()
            if (result == null) {
                _errorMessage.value = "Không tải được danh sách user."
            } else {
                _users.value = result
            }

            _isLoading.value = false
        }
    }

    fun deleteUser(userId: Int) {
        viewModelScope.launch {
            val deleted = userRepository.deleteUser(userId)
            if (deleted) {
                _users.value = _users.value.filterNot { user -> user.id == userId }
                _errorMessage.value = null
            } else {
                _errorMessage.value = "Không xóa được user này."
            }
        }
    }
}
