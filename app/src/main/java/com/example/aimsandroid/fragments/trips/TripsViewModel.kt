package com.example.aimsandroid.fragments.trips

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aimsandroid.database.BillOfLading
import com.example.aimsandroid.database.Trip
import com.example.aimsandroid.database.getDatabase
import com.example.aimsandroid.repository.TripRepository
import com.example.aimsandroid.utils.FetchApiEventListener
import com.example.aimsandroid.utils.OnSaveListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TripsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = getDatabase(application)
    private val tripRepository = TripRepository(database)
    val trips = tripRepository.trips

    private val _refreshing = MutableLiveData<Boolean>()
    val refreshing: LiveData<Boolean>
        get() = _refreshing

    suspend fun checkTripCompleted(tripId: Long): Boolean {
        Log.i("aimsDebug", "insideCheckTripCompleted")
        return tripRepository.getTripStatus(tripId) != null
    }

    fun refreshTrips(fetchApiEventListener: FetchApiEventListener){
        viewModelScope.launch {
            _refreshing.value = true
            tripRepository.refreshTrips(fetchApiEventListener)
            _refreshing.value = false
        }
    }

    fun saveForm(billOfLading: BillOfLading, bolBitmap: Bitmap, onSaveListener: OnSaveListener) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                tripRepository.insertBillOfLading(billOfLading)
                saveBitmaps(bolBitmap)
                withContext(Dispatchers.Main){
                    onSaveListener.onSave()
                }
            }
        }
    }

    suspend fun saveBitmaps(bolBitmap: Bitmap){

    }
}