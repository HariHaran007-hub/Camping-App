package com.rcappstudio.campingapp.utils

import android.icu.text.SimpleDateFormat
import java.util.*

val snakeRegex = " [a-zA-Z]".toRegex()

fun getDateTime(s: Long): String? {
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val netDate = Date(s)
        sdf.format(netDate)
    } catch (e: Exception) {
        e.toString()
    }
}

fun String.snakeToLowerCamelCase(): String {
    return snakeRegex.replace(this) {
        it.value.replace(" ", "")
            .toUpperCase()
    }
}