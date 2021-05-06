package com.example.aimsandroid.utils

/**
 * Custom listener interface to fetch api
 */
interface FetchApiEventListener {
    fun onSuccess()
    fun onError(error: String)
}