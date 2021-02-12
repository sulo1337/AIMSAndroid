package com.example.aimsandroid.homefragment

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

@SuppressLint("MissingPermission")
class HomeViewModel(locationManager: LocationManager) : ViewModel() {
    private val _latitude = MutableLiveData<String>()
    val latitude: LiveData<String>
        get() = _latitude

    private val _longitude = MutableLiveData<String>()
    val longitude: LiveData<String>
        get() = _longitude

    private val locationListener: LocationListener

    init {
        locationListener =  MyLocationListener(this)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000,  0.5f, locationListener)
    }

    fun onLocationChanged(location: Location) {
        _latitude.value = location.latitude.toString()
        _longitude.value = location.longitude.toString()
    }

    class MyLocationListener(argViewModel: HomeViewModel): LocationListener {
        private val viewModel = argViewModel

        override fun onLocationChanged(location: Location) {
            viewModel.onLocationChanged(location)
        }
    }
}

