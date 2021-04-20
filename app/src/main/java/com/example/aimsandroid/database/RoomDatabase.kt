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
    suspend fun setTripStatus(tripStatus: TripStatus)

    @Query("select * from trips_status_table where tripId = :tripId")
    suspend fun getTripStatus(tripId: Long): TripStatus?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: Trip)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaypoint(wayPoint: WayPoint): Long

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTrips(trips: List<Trip>)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllWaypoints(wayPoints: List<WayPoint>)

    @Query("select w.*, b.* from waypoint_table w left join billoflading_table b on w.owningTripId = b.tripIdFk and w.seqNum = b.wayPointSeqNum where w.seqNum = :seqNum and w.owningTripId = :tripId")
    suspend fun getWayPointWithBillOfLading(seqNum: Long, tripId: Long): WaypointWithBillOfLading

    @Query("select * from billoflading_table where wayPointSeqNum = :seqNum and tripIdFk = :tripId")
    fun getBillOfLading(seqNum: Long, tripId: Long): LiveData<BillOfLading>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillOfLading(billOfLading: BillOfLading)
}

@Database(entities = [Trip::class, WayPoint::class, BillOfLading::class, TripStatus::class], version = 2)
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