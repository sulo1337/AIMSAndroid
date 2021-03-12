package com.example.aimsandroid.utils

import android.view.View

interface OnBottomSheetCallbacks {
    fun onStateChanged(bottomSheet: View, newState: Int)
}