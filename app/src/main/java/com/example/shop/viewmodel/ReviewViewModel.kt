package com.example.shop.viewmodel

import androidx.lifecycle.ViewModel
import com.example.shop.data.model.Review
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ReviewViewModel : ViewModel() {

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews

    fun add(review: Review) {
        _reviews.value = _reviews.value + review
    }
}
