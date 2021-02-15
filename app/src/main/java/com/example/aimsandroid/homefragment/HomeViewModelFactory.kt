package com.example.aimsandroid.homefragment

import android.app.Application
import android.location.LocationManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class HomeViewModelFactory (private val locationManager: LocationManager, private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(locationManager, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }

}