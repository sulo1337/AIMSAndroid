package com.example.aimsandroid.fragments.trips

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aimsandroid.fragments.home.HomeViewModel

/*
* View model factory to generate view model
* Boiler plate code from Android sdk
* */
class TripsViewModelFactory(private val application: Application) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == TripsViewModel::class.java) {
            return TripsViewModel(application) as T
        } else {
            return super.create(modelClass)
        }
    }
}