package com.example.aimsandroid.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.aimsandroid.database.Trip
import com.example.aimsandroid.database.TripDatabase
import com.example.aimsandroid.database.TripWithWaypoints
import com.example.aimsandroid.database.WayPoint
import com.example.aimsandroid.network.Network
import com.example.aimsandroid.network.TripSection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class TripRepository(private val database: TripDatabase) {
    val trips = database.tripDao.getTripsWithWaypoints()
    fun getTripWithWaypointsByTripId(tripId: Long) = database.tripDao.getTripWithWaypointsByTripId(tripId)
    suspend fun insertTrip(trip: Trip) = database.tripDao.insertTrip(trip)
    suspend fun insertWaypoint(wayPoint: WayPoint) = database.tripDao.insertWaypoint(wayPoint)
    suspend fun insertAllTrips(trips: List<Trip>) = database.tripDao.insertAllTrips(trips)
    suspend fun insertAllWaypoints(waypoints:List<WayPoint>) = database.tripDao.insertAllWaypoints(waypoints)
    @Throws(Exception::class)
    suspend fun refreshTrips() {
        withContext(Dispatchers.IO){
            try {
                val tripData = Network.dispatcher.getTripsAsync("D1", "f20f8b25-b149-481c-9d2c-41aeb76246ef").await()
                val data =tripData.data
                val responseStatus = data.responseStatus
                Log.i("aims_network", responseStatus.toString())
                val tripSections = data.tripSections
                val trips = ArrayList<Trip>()
                val waypoints = ArrayList<WayPoint>()
                for(tripSection in tripSections){
                    val trip = tripSection.getTrip()
                    val waypoint = tripSection.getWaypoint()
                    trips.add(trip)
                    waypoints.add(waypoint)
                }
                insertAllTrips(trips)
                insertAllWaypoints(waypoints)
            } catch (e: Exception){
                Log.i("aims_debug_repository", e.message!!)
            }
        }
    }
}