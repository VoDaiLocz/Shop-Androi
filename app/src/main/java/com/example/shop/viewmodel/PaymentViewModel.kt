package com.example.shop.viewmodel

import androidx.lifecycle.ViewModel
import com.example.shop.data.model.Payment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PaymentViewModel : ViewModel() {

    private val _payments = MutableStateFlow<List<Payment>>(emptyList())
    val payments: StateFlow<List<Payment>> = _payments

    fun add(payment: Payment) {
        _payments.value = _payments.value + payment
    }
}
