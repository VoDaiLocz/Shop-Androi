package com.example.shop.data.repository

import com.example.shop.data.model.Address

interface AddressRepository {
    suspend fun getAddresses(userId: String): List<Address>
    suspend fun addAddress(address: Address)
}
