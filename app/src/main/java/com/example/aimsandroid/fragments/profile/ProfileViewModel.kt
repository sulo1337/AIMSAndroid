package com.example.aimsandroid.fragments.profile

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import calculateTotalHours
import com.example.aimsandroid.database.TimeTable
import com.example.aimsandroid.repository.TripRepository
import com.here.odnp.util.ClientLooper.getLooper
import getCurrentDateTimeString
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private var prefs = application.getSharedPreferences("com.example.aimsandroid", Context.MODE_PRIVATE)
    private var tripRepository = TripRepository(application)
    val trips = tripRepository.trips
    fun logout(logoutEventListener: ProfileFragment.LogoutEventListener) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                logoutEventListener.onLogoutStarted()
                withContext(Dispatchers.IO) {
                    prefs.edit().putString("driverId", "x").apply()
                    prefs.edit().putString("driverKey", "x").apply()
                    prefs.edit().putString("driverName", "x").apply()
                    prefs.edit().putLong("currentTripId", -1L).apply()
                    prefs.edit().putLong("nextWaypointSeqNumber", -1L).apply()
                }
                logoutEventListener.onLogoutComplete()
            }
        }
    }

    fun getDriverName(): String? {
        return prefs.getString("driverName", "Driver")
    }

    fun internetIsConnected(): Boolean {
        return try {
            val command = "ping -c 1 google.com"
            Runtime.getRuntime().exec(command).waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    fun getNumTrips(): Int {
        if(trips.value != null){
            return trips.value!!.size
        }
        return 0
    }

    fun getNumCompletedTrips() : Int {
        if(trips.value!=null) {
            return trips.value!!.filter{
                it.tripStatus?.complete == true
            }.size
        }
        return 0
    }

    fun isClockedIn(): Boolean{
        return prefs.getBoolean("clockedIn", false)
    }

    fun clockIn(param: ProfileFragment.EventListener) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                param.onStarted()
                withContext(Dispatchers.IO) {
                    val timeTable = TimeTable(0, getCurrentDateTimeString(), null)
                    val id = tripRepository.insertTimeTable(timeTable)
                    prefs.edit().putBoolean("clockedIn", true).apply()
                    withContext(Dispatchers.Main) {
                        param.onComplete()
                    }
                }
            }
        }
    }

    fun clockOut(param: ProfileFragment.EventListener) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                param.onStarted()
                withContext(Dispatchers.IO) {
                    val timeTable = tripRepository.getLatestTimeTable()
                    timeTable.clockedOut = getCurrentDateTimeString()
                    val id = tripRepository.insertTimeTable(timeTable)
                    prefs.edit().putBoolean("clockedIn", false).apply()
                    withContext(Dispatchers.Main) {
                        param.onComplete()
                    }
                }
            }
        }
    }

    fun getHoursCompleted(): String {
        val listTimeTable = runBlocking {
            tripRepository.getAllTimeTable()
        }
        return calculateTotalHours(listTimeTable)
    }
}