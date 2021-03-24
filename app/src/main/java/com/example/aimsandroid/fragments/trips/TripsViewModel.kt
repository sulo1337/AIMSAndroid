package com.example.aimsandroid.fragments.trips

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aimsandroid.database.Trip
import com.example.aimsandroid.database.getDatabase
import com.example.aimsandroid.repository.TripRepository
import kotlinx.coroutines.launch

class TripsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = getDatabase(application)
    private val tripRepository = TripRepository(database)
    val trips = tripRepository.trips
    init {
        viewModelScope.launch {
            tripRepository.refreshTrips()
        }
    }
}