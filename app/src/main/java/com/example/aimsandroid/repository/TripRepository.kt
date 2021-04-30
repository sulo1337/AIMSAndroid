package com.example.aimsandroid.repository

import API_KEY
import android.content.SharedPreferences
import android.util.Log
import com.example.aimsandroid.database.*
import com.example.aimsandroid.network.Network
import com.example.aimsandroid.utils.FetchApiEventListener
import com.example.aimsandroid.utils.TripStatusCode
import getCurrentDateTimeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.net.UnknownHostException

class TripRepository(private val database: TripDatabase, private val prefs: SharedPreferences) {
    val trips = database.tripDao.getTripsWithWaypoints()
    private val driverId = prefs.getString("driverId", "D1")!!.trim()
    fun getTripWithWaypointsByTripId(tripId: Long) = database.tripDao.getTripWithWaypointsByTripId(tripId)
    suspend fun getTripByTripId(tripId: Long) = database.tripDao.getTrip(tripId)
    suspend fun insertTrip(trip: Trip) = database.tripDao.insertTrip(trip)
    suspend fun setTripStatus(tripStatus: TripStatus) = database.tripDao.setTripStatus(tripStatus)
    suspend fun getTripStatus(tripId: Long) = database.tripDao.getTripStatus(tripId)
    suspend fun insertWaypoint(wayPoint: WayPoint) = database.tripDao.insertWaypoint(wayPoint)
    suspend fun insertBillOfLading(billOfLading: BillOfLading) = database.tripDao.insertBillOfLading(billOfLading)
    fun getBillOfLading(seqNum: Long, owningTripId: Long) = database.tripDao.getBillOfLading(seqNum, owningTripId)
    suspend fun getWaypointWithBillOfLading(seqNum: Long, owningTripId: Long) = database.tripDao.getWayPointWithBillOfLading(seqNum, owningTripId)
    suspend fun insertAllTrips(trips: List<Trip>) = database.tripDao.insertAllTrips(trips)
    suspend fun insertAllWaypoints(waypoints:List<WayPoint>) = database.tripDao.insertAllWaypoints(waypoints)

    suspend fun onTripEvent(tripId: Long, tripStatusCode: TripStatusCode) {
        withContext(Dispatchers.IO){
            val tripEvent = TripEvent(
                0L,
                driverId,
                tripId,
                tripStatusCode.getStatusCode().trim(),
                tripStatusCode.getStatusMessage().trim(),
                getCurrentDateTimeString().trim(),
                false
            )
            try{
                Network.dispatcher.putTripEventStatusAsync(
                    tripEvent.driverId,
                    tripEvent.tripId.toString().trim(),
                    tripEvent.statusCode,
                    tripEvent.statusMessage,
                    tripEvent.datetime,
                    API_KEY
                ).await()
                tripEvent.synced = true
            } catch(e: UnknownHostException){
                Log.i("aimsDebugRepository", "No internet connection, saving into database.")
            } catch (e: Exception){
                Log.i("aimsDebugRepository", e.toString())
            } finally {
                database.tripDao.insertTripEvent(tripEvent)
            }
        }
    }

    suspend fun refreshTrips(fetchApiEventListener: FetchApiEventListener) {
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