package com.example.shop.data.repository

import com.example.shop.data.model.Address
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressRepository @Inject constructor() {
    private val addresses = MutableStateFlow<List<Address>>(emptyList())
    private var nextId = 1

    fun getAddressesByUserId(userId: Int): Flow<List<Address>> =
        addresses.map { list -> list.filter { address -> address.userId == userId } }

    suspend fun addAddress(address: Address) {
        val newAddress = address.copy(id = nextId++)
        addresses.value = addresses.value + newAddress
    }

    suspend fun deleteAddress(address: Address) {
        addresses.value = addresses.value.filterNot { item -> item.id == address.id }
    }

    suspend fun setAsDefault(addressId: Int, userId: Int) {
        addresses.value = addresses.value.map { address ->
            if (address.userId == userId) {
                address.copy(isDefault = address.id == addressId)
            } else {
                address
            }
        }
    }
}
