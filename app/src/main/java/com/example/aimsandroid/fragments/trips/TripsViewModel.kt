package com.example.aimsandroid.fragments.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aimsandroid.repository.TripRepository
import kotlinx.coroutines.launch

class TripsViewModel : ViewModel() {
    private val tripRepository = TripRepository()
    init {
        viewModelScope.launch {
            tripRepository.refreshTrips()
        }
    }
}