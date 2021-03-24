package com.example.aimsandroid.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TripDao {
    @Transaction
    @Query("select * from trips")
    fun getTripsWithWaypoints(): List<TripWithWaypoints>
}

@Database(entities = [Trip::class, WayPoint::class, TripWithWaypoints::class], version = 1)
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