package com.example.shop.viewmodel

import androidx.lifecycle.ViewModel
import com.example.shop.data.model.Address
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AddressViewModel : ViewModel() {

    private val _addresses = MutableStateFlow<List<Address>>(emptyList())
    val addresses: StateFlow<List<Address>> = _addresses

    fun add(address: Address) {
        _addresses.value = _addresses.value + address
    }
}
