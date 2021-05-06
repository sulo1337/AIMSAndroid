package com.example.aimsandroid.repository

import API_KEY
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.aimsandroid.database.*
import com.example.aimsandroid.network.Network
import com.example.aimsandroid.utils.FetchApiEventListener
import com.example.aimsandroid.utils.TripStatusCode
import getCurrentDateTimeString
import getProductId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.net.UnknownHostException

/**
 * This class handles all the necessary database/network calls
 * Handles CRUD operations on trip information
 * */
class TripRepository(private val application: Application) {
    private val prefs = application.getSharedPreferences("com.example.aimsandroid", Context.MODE_PRIVATE)
    var driverId = prefs.getString("driverId", "x")!!.trim()
    var database = getDatabase(application, driverId)
    val trips = database.tripDao.getTripsWithWaypoints()
    fun getTripWithWaypointsByTripId(tripId: Long) = database.tripDao.getTripWithWaypointsByTripId(tripId)
    suspend fun getTripByTripId(tripId: Long) = database.tripDao.getTrip(tripId)
    suspend fun insertTrip(trip: Trip) = database.tripDao.insertTrip(trip)
    suspend fun setTripStatus(tripStatus: TripStatus) = database.tripDao.setTripStatus(tripStatus)
    suspend fun getTripStatus(tripId: Long) = database.tripDao.getTripStatus(tripId)
    suspend fun insertWaypoint(wayPoint: WayPoint) = database.tripDao.insertWaypoint(wayPoint)
    fun getBillOfLading(seqNum: Long, owningTripId: Long) = database.tripDao.getBillOfLading(seqNum, owningTripId)
    suspend fun getWaypointWithBillOfLading(seqNum: Long, owningTripId: Long) = database.tripDao.getWayPointWithBillOfLading(seqNum, owningTripId)
    suspend fun insertAllTrips(trips: List<Trip>) = database.tripDao.insertAllTrips(trips)
    suspend fun insertAllWaypoints(waypoints:List<WayPoint>) = database.tripDao.insertAllWaypoints(waypoints)
    suspend fun insertTimeTable(timeTable: TimeTable) = database.tripDao.insertTimeTable(timeTable)
    suspend fun getLatestTimeTable() = database.tripDao.getLatestTimeTable()
    suspend fun getAllTimeTable() = database.tripDao.getAllTimeTable()
    suspend fun insertBillOfLading(billOfLading: BillOfLading) {
        withContext(Dispatchers.IO) {
            val waypointWithBillOfLading = getWaypointWithBillOfLading(billOfLading.wayPointSeqNum, billOfLading.tripIdFk)
            if(waypointWithBillOfLading.waypoint != null) {
                if(waypointWithBillOfLading.waypoint.waypointTypeDescription == "Source") {
                    insertSourceBillOfLading(billOfLading)
                } else {
                    insertSiteBillOfLading(billOfLading)
                }
            }
        }
    }

    /**
     * This method inserts the bill of lading for source into network and database
     * @param billOfLading bill of lading to be inserted
     * */
    private suspend fun insertSourceBillOfLading(billOfLading: BillOfLading) {
        if(billOfLading.complete == true) {
            try {
                val response = putTripProductPickupAsync(
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
                if(response.data.responseStatus[0].statusCode == 1000) {
                    billOfLading.synced = true
                    Log.i("aimsDebugData", "Sent bill of lading: SOURCE $billOfLading")
                }
            } catch (e: UnknownHostException) {
                billOfLading.synced = false
                Log.w("aimsDebugData", "No internet connection, saving for later: SOURCE $billOfLading")
            } catch (e: Exception) {
                billOfLading.synced = false
                Log.w("aimsDebugData", "Unexpected error occurred while sending: SOURCE$billOfLading")
            } finally {
                database.tripDao.insertBillOfLading(billOfLading)
            }
        } else {
            database.tripDao.insertBillOfLading(billOfLading)
        }
    }

    /**
     * This method inserts the bill of lading for site container into network and database
     * @param billOfLading bill of lading to be inserted
     * */
    private suspend fun insertSiteBillOfLading(billOfLading: BillOfLading){
        if(billOfLading.complete == true) {
            try {
                val response = putTripProductDeliveryAsync(
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
                }
            } catch (e: UnknownHostException) {
                billOfLading.synced = false
                Log.w("aimsDebugData", "No internet connection, saving for later: SITE $billOfLading")
            } catch (e: Exception) {
                billOfLading.synced = false
                Log.w("aimsDebugData", "Unexpected error occurred while sending: SITE $billOfLading")
            } finally {
                database.tripDao.insertBillOfLading(billOfLading)
            }
        } else {
            database.tripDao.insertBillOfLading(billOfLading)
        }
    }

    /**
     * This method inserts trip event object into database and network
     * @param tripStatusCode Trip Status
     * @param tripId Trip Id
     * */
    suspend fun onTripEvent(tripId: Long, tripStatusCode: TripStatusCode) {
        withContext(Dispatchers.IO){
            val tripEvent = TripEvent(
                tripStatusCode.getStatusCode().trim(),
                driverId,
                0L,
                tripId,
                tripStatusCode.getStatusMessage().trim(),
                getCurrentDateTimeString(),
                false
            )
            try{
                val response = putTripEventStatusAsync(
                    tripEvent.driverId,
                    tripEvent.tripId.toString(),
                    tripEvent.statusCode,
                    tripEvent.statusMessage,
                    tripEvent.datetime,
                    API_KEY
                ).await()
                response.let{
                    if(it.data.responseStatus[0].statusCode == 1000){
                        tripEvent.synced = true
                        Log.i("aimsDebugData", "Sent trip status: $tripEvent")
                    }
                }
            } catch(e: UnknownHostException){
                Log.w("aimsDebugData", "No internet connection, saving for later: $tripEvent")
            } catch (e: Exception){
                Log.w("aimsDebugData", "Unexpected error while saving: $tripEvent")
            } finally {
                database.tripDao.insertTripEvent(tripEvent)
            }
        }
    }

    /**
     * This method refreshes trip by calling the api, gets the latest information
     * @param fetchApiEventListener Listener interface that contains callbacks
     * */
    suspend fun refreshTrips(fetchApiEventListener: FetchApiEventListener) {
        refresh()
        if(driverId!="x"){
            withContext(Dispatchers.IO){
                try {
                    val response = Network.dispatcher.getTripsAsync(driverId, API_KEY).await()
                    val data =response.data
                    val responseStatus = data.responseStatus
                    val tripSections = data.tripSections
                    val trips = ArrayList<Trip>()
                    val waypoints = ArrayList<WayPoint>()
                    if(tripSections!=null){
                        for(tripSection in tripSections){
                            val trip = tripSection.getTrip()
                            val waypoint = tripSection.getWaypoint()
                            trips.add(trip)
                            waypoints.add(waypoint)
                        }
                        insertAllTrips(trips)
                        insertAllWaypoints(waypoints)
                    }
                    fetchApiEventListener.onSuccess()
                } catch (e: Exception){
                    fetchApiEventListener.onError(e.toString())
                }
            }
        }
    }

    /**
     * These four methods maps the self descriptive method names to the Dao
     * */
    suspend fun getUnSyncedBillOfLading() = database.tripDao.getUnSyncedBillOfLading()
    suspend fun getUnSyncedTripEvents() = database.tripDao.getUnSyncedTripEvents()
    suspend fun insertBillOfLadingNoNetwork(billOfLading: BillOfLading) = database.tripDao.insertBillOfLading(billOfLading)
    suspend fun insertTripEventNoNetwork(tripEvent: TripEvent) = database.tripDao.insertTripEvent(tripEvent)

    /**
     * This methods performs a put request for pickup with all the information
     */
    fun putTripProductPickupAsync(
        driverId: String,
        tripId: String,
        sourceId: String,
        productId: String,
        bolNum: String,
        startTime: String,
        endTime: String,
        grossQty: String,
        netQty: String,
        API_KEY: String
    ) = Network.dispatcher.putTripProductPickupAsync(
        driverId.trim(),
        tripId.trim(),
        sourceId.trim(),
        productId.trim(),
        bolNum.trim(),
        startTime.trim(),
        endTime.trim(),
        grossQty.trim(),
        netQty.trim(),
        API_KEY
    )

    /**
     * This methods performs a put request for delivery with all the information
     */
    fun putTripProductDeliveryAsync(
        driverId: String,
        tripId: String,
        siteId: String,
        productId: String,
        startTime: String,
        grossQty: String,
        netQty: String,
        remainingQty: String,
        API_KEY: String
    ) = Network.dispatcher.putTripProductDeliveryAsync(
        driverId.trim(),
        tripId.trim(),
        siteId.trim(),
        productId.trim(),
        startTime.trim(),
        grossQty.trim(),
        netQty.trim(),
        remainingQty.trim(),
        API_KEY
    )

    /**
     * This methods performs a put request for trip event with all the information
     */
    fun putTripEventStatusAsync(
        driverId: String,
        tripId: String,
        statusCode: String,
        statusMessage: String,
        statusDate: String,
        API_KEY: String
    ) = Network.dispatcher.putTripEventStatusAsync(
        driverId.trim(),
        tripId.trim(),
        statusCode.trim(),
        statusMessage.trim(),
        statusDate.trim(),
        API_KEY
    )

    /**
     * This method refreshes the database and the driver context for this repository
     */
    fun refresh() {
        driverId = prefs.getString("driverId", "x")!!.trim()
        database = getDatabase(application, driverId)
    }
}