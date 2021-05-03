package com.example.aimsandroid.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow

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

    @Query("select * from trips_table where tripId = :tripId")
    suspend fun getTrip(tripId: Long): Trip

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTripEvent(tripEvent: TripEvent)

    @Query("select * from billoflading_table where synced = 0 and complete = 1")
    suspend fun getUnSyncedBillOfLading(): List<BillOfLading>

    @Query("select * from trips_events_table where synced = 0")
    suspend fun getUnSyncedTripEvents(): List<TripEvent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeTable(timeTable: TimeTable): Long

    @Query("select * from time_table where nullif(clockedOut, '') is null")
    suspend fun getLatestTimeTable(): TimeTable

    @Query("select * from time_table")
    suspend fun getAllTimeTable(): List<TimeTable>
}

@Database(entities = [Trip::class, WayPoint::class, BillOfLading::class, TripStatus::class, TripEvent::class, TimeTable::class], version = 3)
abstract class TripDatabase: RoomDatabase() {
    abstract val tripDao: TripDao
}

private lateinit var INSTANCE: TripDatabase

fun getDatabase(context: Context, driverCode: String): TripDatabase {
    synchronized(TripDatabase::class.java) {
        if(!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
            TripDatabase::class.java, driverCode+"_trips").build()
        }
    }
    return INSTANCE
}