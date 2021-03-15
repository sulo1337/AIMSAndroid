package com.example.aimsandroid.fragments.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.aimsandroid.repository.LocationRepository

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val locationRepository = LocationRepository(application)

    val latitude = locationRepository.latitude
    val longitude = locationRepository.longitude
}