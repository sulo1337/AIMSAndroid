package com.example.aimsandroid.repository

import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class LocationRepository(private val application: Application) {

    private var locationManager: LocationManager = application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val locationListener: MyLocationListener = MyLocationListener(this)

    private val _latitude = MutableLiveData<Double>()
    val latitude: LiveData<Double>
        get() = _latitude

    private val _longitude = MutableLiveData<Double>()
    val longitude: LiveData<Double>
        get() = _longitude

    var prevLatitude = 0.0
    var prevLongitude = 0.0

    fun onLocationChanged(location: Location) {
        prevLatitude = this._latitude.value!!
        prevLongitude = this._longitude.value!!
        this._latitude.value = location.latitude
        this._longitude.value = location.longitude
    }

    init {
        _latitude.value = 0.0
        _longitude.value = 0.0
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200,  0.02f, locationListener)
            val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            _latitude.value = lastLocation?.latitude
            _longitude.value = lastLocation?.longitude
        } catch(e: SecurityException) {
            Log.i("locationRepository","no permission")
        }
    }

    class MyLocationListener(private val locationRepository: LocationRepository): LocationListener {
        override fun onLocationChanged(location: Location) {
            locationRepository.onLocationChanged(location)
        }
    }
}