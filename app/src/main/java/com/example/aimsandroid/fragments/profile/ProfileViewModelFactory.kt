package com.example.aimsandroid.fragments.profile

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/*
* View model factory to generate view model
* Boiler plate code from Android sdk
* */
class ProfileViewModelFactory(private val application: Application) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == ProfileViewModel::class.java) {
            return ProfileViewModel(application) as T
        } else {
            return super.create(modelClass)
        }
    }
}