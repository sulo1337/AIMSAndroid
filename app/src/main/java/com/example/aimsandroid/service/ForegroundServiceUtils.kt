package com.example.aimsandroid.service

import API_KEY
import android.util.Log
import com.example.aimsandroid.database.BillOfLading
import com.example.aimsandroid.database.TripEvent
import com.example.aimsandroid.utils.FetchApiEventListener
import getProductId
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
                if(billOfLading.waypointSiteId == null) {
                    val response = tripRepository.putTripProductPickupAsync(
                        driverId,
                        billOfLading.tripIdFk.toString(),
                        billOfLading.waypointSourceId.toString(),
                        getProductId(billOfLading.product),
                        billOfLading.billOfLadingNumber.toString(),
                        billOfLading.loadingStarted.toString(),
                        billOfLading.loadingEnded.toString(),
                        billOfLading.grossQuantity.toString(),
                        billOfLading.netQuantity.toString(),
                        API_KEY
                    ).await()
                    if (response.data.responseStatus[0].statusCode == 1000) {
                        Log.i("aimsDebugData", "Sent bill of lading: SOURCE $billOfLading")
                        billOfLading.synced = true
                        tripRepository.insertBillOfLadingNoNetwork(billOfLading)
                    }
                } else if (billOfLading.waypointSourceId == null) {
                    val response = tripRepository.putTripProductDeliveryAsync(
                        driverId,
                        billOfLading.tripIdFk.toString(),
                        billOfLading.waypointSiteId.toString(),
                        getProductId(billOfLading.product),
                        billOfLading.loadingEnded.toString(),
                        billOfLading.grossQuantity.toString(),
                        billOfLading.netQuantity.toString(),
                        billOfLading.finalMeterReading.toString(),
                        API_KEY
                    ).await()
                    if(response.data.responseStatus[0].statusCode == 1000) {
                        billOfLading.synced = true
                        Log.i("aimsDebugData", "Sent bill of lading: SITE $billOfLading")
                        tripRepository.insertBillOfLadingNoNetwork(billOfLading)
                    }
                }
            } catch (e: UnknownHostException) {
                if(billOfLading.waypointSiteId == null) {
                    Log.w("aimsDebugData", "No internet connection, saving for later: SOURCE $billOfLading")
                } else {
                    Log.w("aimsDebugData", "No internet connection, saving for later: SITE $billOfLading")
                }

            } catch (e: Exception) {
                if(billOfLading.waypointSiteId == null) {
                    Log.w("aimsDebugData", "Unexpected error occurred while sending: SOURCE $billOfLading")
                } else {
                    Log.w("aimsDebugData", "Unexpected error occurred while sending: SITE $billOfLading")
                }
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
                    Log.i("aimsDebugData", "Sent trip status: $tripEvent")
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