package com.example.aimsandroid.repository

import android.util.Log
import com.example.aimsandroid.network.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TripRepository {
    suspend fun refreshTrips() {
        withContext(Dispatchers.IO){
            val tripData = Network.dispatcher.getTripsAsync("D1", "f20f8b25-b149-481c-9d2c-41aeb76246ef").await()
            Log.i("networkData", tripData.toString())
        }
    }
}