package com.example.socialbatterymanager.shared.utils

fun <T> hasDuplicates(list: List<T>): Boolean {
    val seen = mutableSetOf<T>()
    for (item in list) {
        if (!seen.add(item)) return true
    }
    return false
}
