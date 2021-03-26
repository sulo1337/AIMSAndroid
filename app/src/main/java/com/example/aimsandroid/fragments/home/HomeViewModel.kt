package com.example.aimsandroid.fragments.home

import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import androidx.lifecycle.*
import com.example.aimsandroid.database.TripWithWaypoints
import com.example.aimsandroid.database.getDatabase
import com.example.aimsandroid.repository.LocationRepository
import com.example.aimsandroid.repository.TripRepository
import com.here.android.mpa.common.GeoCoordinate
import com.here.android.mpa.guidance.NavigationManager
import com.here.android.mpa.mapping.AndroidXMapFragment
import com.here.android.mpa.mapping.Map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    var map: Map? = null
    private var _currentTripId: Long = -1L

    private val locationRepository = LocationRepository(application)
    private val database = getDatabase(application)
    private val tripRepository = TripRepository(database)

    var currentTrip: LiveData<TripWithWaypoints> = tripRepository.getTripWithWaypointsByTripId(_currentTripId)
    val latitude = locationRepository.latitude
    val longitude = locationRepository.longitude

    fun recenterMap() {
        if(!this.latitude.value?.equals(0.0)!! && !this.longitude.value?.equals(0.0)!!) {
            this.map?.setCenter(GeoCoordinate(latitude.value!!, longitude.value!!), Map.Animation.BOW, map?.maxZoomLevel!!*0.75, 0.0f, 0.1f)
        }
    }

    init {
        val prefs =application.getSharedPreferences("com.example.aimsandroid", Context.MODE_PRIVATE)
        _currentTripId = prefs.getLong("currentTripId", -1L)
        currentTrip =  tripRepository.getTripWithWaypointsByTripId(_currentTripId)
    }
}