package com.example.shop.data.repository

import com.example.shop.data.local.dao.UserDao
import com.example.shop.data.model.User
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val userDao: UserDao
) {
    suspend fun login(email: String, password: String) = userDao.login(email, password)

    suspend fun register(user: User): Boolean {
        val existingUser = userDao.getUserByEmail(user.email)
        return if (existingUser == null){
            userDao.register(user)
            true
        } else {
            false
        }
    }
}
