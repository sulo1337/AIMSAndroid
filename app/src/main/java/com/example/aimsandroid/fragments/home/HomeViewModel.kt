package com.example.aimsandroid.fragments.home

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.view.View
import androidx.lifecycle.*
import com.example.aimsandroid.database.BillOfLading
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
    private var prefs: SharedPreferences
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

    suspend fun resolveNextWaypoint(tripWithWaypoints: TripWithWaypoints) {
        val waypoints = tripWithWaypoints.waypoints
        var nextWaypointId = -1L
        for(waypoint in waypoints){
            val waypointWithBillOfLading = tripRepository.getWaypointWithBillOfLading(waypoint.seqNum, waypoint.owningTripId)
            if(waypointWithBillOfLading.billOfLading == null){
                nextWaypointId = waypoint.seqNum
                break
            } else if(waypointWithBillOfLading.billOfLading.complete == false) {
                nextWaypointId = waypoint.seqNum
                break
            }
        }
        prefs.edit().putLong("nextWaypointSeqNumber", nextWaypointId).apply()
    }

    fun saveForm(billOfLading: BillOfLading, bolBitmap: Bitmap, signatureBitmap: Bitmap) {
        viewModelScope.launch {
            tripRepository.insertBillOfLading(billOfLading)
            saveBitmaps(bolBitmap, signatureBitmap)
            tripCompleteCheck()
        }
    }

    private fun tripCompleteCheck() {
        viewModelScope.launch {
            val waypoints = currentTrip.value?.waypoints
            if(waypoints!=null){
                var complete = true
                for(waypoint in waypoints){
                    val waypointWithBillOfLading = tripRepository.getWaypointWithBillOfLading(waypoint.seqNum, waypoint.owningTripId)
                    if(waypointWithBillOfLading.billOfLading == null) {
                        complete = false
                        break
                    } else if(waypointWithBillOfLading.billOfLading.complete == false) {
                        complete = false
                        break
                    }
                }
                if(complete) {
                    prefs.edit().putLong("currentTripId", -1L).apply()
                    currentTrip = tripRepository.getTripWithWaypointsByTripId(-1L)
                }
            }
        }
    }

    private fun saveBitmaps(bolBitmap: Bitmap, signatureBitmap: Bitmap) {

    }

    init {
        prefs =application.getSharedPreferences("com.example.aimsandroid", Context.MODE_PRIVATE)
        _currentTripId = prefs.getLong("currentTripId", -1L)
        currentTrip =  tripRepository.getTripWithWaypointsByTripId(_currentTripId)

        //TODO
        latitude.observeForever {
            map?.positionIndicator?.isVisible = true;
        }

        //TODO
        longitude.observeForever {
            map?.positionIndicator?.isVisible = true;
        }
    }
}