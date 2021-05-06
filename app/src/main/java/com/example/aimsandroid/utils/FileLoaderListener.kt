package com.example.aimsandroid.utils

import android.net.Uri

/**
 * Custom listener interface to load files
 */
interface FileLoaderListener {
    fun onSuccess(uri: Uri)
    fun onError(error: String)
}