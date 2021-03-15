package com.example.aimsandroid.fragments.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class HomeViewModelFactory(private val application: Application, private val map: com.here.android.mpa.mapping.Map) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == HomeViewModel::class.java) {
            return HomeViewModel(application, map) as T
        } else {
            return super.create(modelClass)
        }
    }
}