package com.example.aimsandroid.fragments.home

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.aimsandroid.repository.LocationRepository
import com.here.android.mpa.common.GeoCoordinate
import com.here.android.mpa.mapping.AndroidXMapFragment
import com.here.android.mpa.mapping.Map

class HomeViewModel(application: Application, map: Map) : AndroidViewModel(application) {

    var map: Map = map
//    private lateinit var mapFragment: AndroidXMapFragment

    private val locationRepository = LocationRepository(application)
    val latitude = locationRepository.latitude
    val longitude = locationRepository.longitude

    fun recenterMap() {
        if(!this.latitude.value?.equals(0.0)!! && !this.longitude.value?.equals(0.0)!!) {
            this.map.setCenter(GeoCoordinate(latitude.value!!, longitude.value!!), Map.Animation.BOW, map.maxZoomLevel*0.75, 0.0f, 0.1f)
        }
    }

    fun recenterMapNoAnimation() {
        if(!this.latitude.value?.equals(0.0)!! && !this.longitude.value?.equals(0.0)!!) {
            this.map.setCenter(GeoCoordinate(latitude.value!!, longitude.value!!), Map.Animation.NONE, map.maxZoomLevel*0.75, 0.0f, 0.1f)
        }
    }
}