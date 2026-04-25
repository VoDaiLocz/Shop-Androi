package com.example.shop.data.repository

import com.example.shop.data.local.dao.AddressDao
import com.example.shop.data.model.Address
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class AddressRepository @Inject constructor(
    private val addressDao: AddressDao
){
    // Lấy danh sách địa chỉ của User dưới dạng luồng dữ liệu (Flow)
    fun getAddressesByUserId(userId: Int): Flow<List<Address>> =
        addressDao.getAddressesByUserId(userId)

    suspend fun addAddress(address: Address){
        addressDao.insertAddress(address)
    }

    suspend fun deleteAddress(address: Address){
        addressDao.deleteAddress(address)
    }

    suspend fun setAsDefault(addressId: Int, userId: Int){
        addressDao.setAsDefault(addressId, userId)
    }
}
