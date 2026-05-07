package com.example.shop.data.repository

import com.example.shop.data.local.dao.UserDao
import com.example.shop.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val userDao: UserDao
) {
    //Thêm biến này để lưu trữ User hiện tại
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    suspend fun login(email: String, password: String): User? {
        val user = userDao.login(email, password)
        //Gán user tìm được vào StateFlow nếu login thành công
        _currentUser.value = user
        return user
    }

    suspend fun register(user: User): Boolean {
        val existingUser = userDao.getUserByEmail(user.email)
        return if (existingUser == null){
            userDao.register(user)
            true
        } else {
            false
        }
    }

    //Thêm hàm logout
    fun logout() {
        _currentUser.value = null
    }
}