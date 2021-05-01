package com.example.aimsandroid.service

import API_KEY
import android.util.Log
import com.example.aimsandroid.database.BillOfLading
import com.example.aimsandroid.database.TripEvent
import com.example.aimsandroid.utils.FetchApiEventListener
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.Exception
import java.net.UnknownHostException

suspend fun ForegroundService.syncTripsData() {
    val unSyncedBillOfLadingList = tripRepository.getUnSyncedBillOfLading()
    val unSyncedTripEventList = tripRepository.getUnSyncedTripEvents()
    val driverId = tripRepository.driverId

    val mutex = Mutex()
    mutex.withLock{
        for(billOfLading: BillOfLading in unSyncedBillOfLadingList){
            try {
                val response = tripRepository.putTripProductPickupAsync(
                    driverId,
                    "170",
                    "27",
                    //TODO implement product id
                    "1175",
                    billOfLading.billOfLadingNumber.toString(),
                    billOfLading.loadingStarted.toString(),
                    billOfLading.loadingEnded.toString(),
                    billOfLading.grossQuantity.toString(),
                    billOfLading.netQuantity.toString(),
                    API_KEY
                ).await()
                if (response.data.responseStatus[0].statusCode == 1000) {
                    Log.i("aimsDebugData", "Sent bill of lading: $billOfLading")
                    billOfLading.synced = true
                    tripRepository.insertBillOfLadingNoNetwork(billOfLading)
                }
            } catch (e: UnknownHostException) {
                Log.w("aimsDebugData", "No internet connection, saving for later: $billOfLading")
            } catch (e: Exception) {
                Log.w("aimsDebugData", "Unexpected error occurred while sending: $billOfLading")
            }
        }

        for(tripEvent: TripEvent in unSyncedTripEventList) {
            try {
                val response = tripRepository.putTripEventStatusAsync(
                    driverId,
                    tripEvent.tripId.toString(),
                    tripEvent.statusCode,
                    tripEvent.statusMessage,
                    tripEvent.datetime,
                    API_KEY
                ).await()
                if(response.data.responseStatus[0].statusCode == 1000) {
                    Log.i("aimsDebugSyncService", "Sent trip status: $tripEvent")
                    tripEvent.synced = true
                    tripRepository.insertTripEventNoNetwork(tripEvent)
                }
            } catch (e: UnknownHostException) {
                Log.w("aimsDebugData", "No internet connection, saving for later: $tripEvent")
            } catch (e: Exception) {
                Log.w("aimsDebugData", "Unexpected error occurred while sending: $tripEvent")
            }
        }

        tripRepository.refreshTrips(object : FetchApiEventListener{
            override fun onSuccess() {
                Log.i("aimsDebugDataSyncService", "Trips Up to Date")
            }

            override fun onError(error: String) {
                Log.w("aimsDebugDataSyncService", "Error while updating trips: $error")
            }
        })
    }
}