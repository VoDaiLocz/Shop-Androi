package com.example.shop.viewmodel

import androidx.activity.result.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shop.data.model.Address
import com.example.shop.data.repository.AddressRepository
import com.example.shop.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val addressRepository: AddressRepository,
    private val authRepository: AuthRepository
)  : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val userAddresses: StateFlow<List<Address>> = authRepository.currentUser
        .flatMapLatest { user ->
            if (user != null) {
                addressRepository.getAddressesByUserId(user.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope,
            SharingStarted.WhileSubscribed(5000), emptyList())

    // Hàm thêm địa chỉ
    fun addAddress(name: String, phone: String, detail: String, city: String) {
        val userId = authRepository.currentUser.value?.id ?: return
        viewModelScope.launch {
            val newAddress = Address(
                userId = userId,
                name = name,
                phoneNumber = phone,
                detail = detail,
                city = city,
                isDefault = userAddresses.value.isEmpty() // Nếu là địa chỉ đầu tiên thì đặt làm mặc định
            )
            addressRepository.addAddress(newAddress)
        }
    }

    // Hàm xóa địa chỉ
    fun deleteAddress(address: Address) {
        viewModelScope.launch {
            addressRepository.deleteAddress(address)
        }
    }
}
