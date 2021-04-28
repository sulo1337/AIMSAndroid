package com.example.aimsandroid.utils

import android.net.Uri

interface FileLoaderListener {
    fun onSuccess(uri: Uri)
    fun onError(error: String)
}