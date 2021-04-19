package com.example.aimsandroid.utils

interface FetchApiEventListener {
    fun onSuccess()
    fun onError(error: String)
}