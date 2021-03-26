package com.example.aimsandroid.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.Deferred

@Dao
interface TripDao {
    @Transaction
    @Query("select * from trips_table")
    fun getTripsWithWaypoints(): LiveData<List<TripWithWaypoints>>

    @Transaction
    @Query("select * from trips_table where tripId = :tripId")
    fun getTripWithWaypointsByTripId(tripId: Long): LiveData<TripWithWaypoints>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: Trip): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaypoint(wayPoint: WayPoint): Long

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTrips(trips: List<Trip>)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllWaypoints(wayPoints: List<WayPoint>)
}

@Database(entities = [Trip::class, WayPoint::class], version = 1)
abstract class TripDatabase: RoomDatabase() {
    abstract val tripDao: TripDao
}

private lateinit var INSTANCE: TripDatabase

fun getDatabase(context: Context): TripDatabase {
    synchronized(TripDatabase::class.java) {
        if(!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
            TripDatabase::class.java, "trips").build()
        }
    }
    return INSTANCE
}