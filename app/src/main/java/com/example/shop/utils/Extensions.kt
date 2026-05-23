package com.example.shop.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

// Add common extension functions here

fun String.orEmptySafe(): String = this

fun Double.formatVnd(): String {
    val symbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = '.'
    }
    return DecimalFormat("#,###", symbols).format(this) + " đ"
}
