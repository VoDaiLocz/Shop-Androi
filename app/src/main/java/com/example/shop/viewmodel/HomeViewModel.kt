package com.example.shop.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {

    private val _banner = MutableStateFlow(listOf("Banner 1", "Banner 2"))
    val banner: StateFlow<List<String>> = _banner
}
