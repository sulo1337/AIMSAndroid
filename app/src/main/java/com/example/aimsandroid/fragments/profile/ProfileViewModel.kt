package com.example.aimsandroid.fragments.profile

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aimsandroid.repository.TripRepository
import com.here.odnp.util.ClientLooper.getLooper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private var prefs = application.getSharedPreferences("com.example.aimsandroid", Context.MODE_PRIVATE)
    private var tripRepository = TripRepository(application)

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
}