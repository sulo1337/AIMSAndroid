package com.example.aimsandroid.fragments.trips

import RotateBitmap
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Network
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.aimsandroid.database.BillOfLading
import com.example.aimsandroid.database.Trip
import com.example.aimsandroid.database.getDatabase
import com.example.aimsandroid.repository.TripRepository
import com.example.aimsandroid.utils.FetchApiEventListener
import com.example.aimsandroid.utils.FileLoaderListener
import com.example.aimsandroid.utils.OnSaveListener
import com.example.aimsandroid.utils.TripStatusCode
import getBolBitmapPath
import getSignatureBitmapPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

/*
* Android view model that handles the logic for overlying profile view
* */
class TripsViewModel(application: Application) : AndroidViewModel(application) {
    private val tripRepository = TripRepository(application)
    val trips = tripRepository.trips

    private val _refreshing = MutableLiveData<Boolean>()
    val refreshing: LiveData<Boolean>
        get() = _refreshing

    private val _online = MutableLiveData<Boolean>()
    val online: LiveData<Boolean>
        get() = _online
    private var prefs = application.getSharedPreferences("com.example.aimsandroid", Context.MODE_PRIVATE)
    private var driverId: String? = prefs.getString("driverId", "D1")

    //this method refreshes trips manually when driver swipes down the screen
    fun refreshTrips(fetchApiEventListener: FetchApiEventListener){
        viewModelScope.launch {
            _refreshing.value = true
            tripRepository.refreshTrips(fetchApiEventListener)
            _refreshing.value = false
        }
    }

    //this method updates given bill of lading form into the database with bill of lading picture
    fun saveForm(billOfLading: BillOfLading, bolBitmap: Bitmap?, onSaveListener: OnSaveListener) {
        viewModelScope.launch {
            onSaveListener.onSaving()
            withContext(Dispatchers.IO) {
                saveBitmaps(bolBitmap, billOfLading)
                tripRepository.insertBillOfLading(billOfLading)
                withContext(Dispatchers.Main){
                    onSaveListener.onSave()
                }
            }
        }
    }

    //this method saves bill of lading bitmap to local file system for given bill of lading
    suspend fun saveBitmaps(bolBitmap: Bitmap?, billOfLading: BillOfLading){
        if(bolBitmap!=null){
            withContext(Dispatchers.IO){
                try {
                    val filePath =  getApplication<Application>().getDir(Environment.DIRECTORY_PICTURES, Context.MODE_PRIVATE).absolutePath + "/AIMS/bol/"
                    val dir = File(filePath)
                    if(!dir.exists()) {
                        dir.mkdirs()
                    }
                    val file = File(dir, getBolBitmapPath(billOfLading.tripIdFk, billOfLading.wayPointSeqNum, driverId.toString()))
                    val fOut = FileOutputStream(file)
                    val rotatedBolBitmap = RotateBitmap(bolBitmap, 90.0f)
                    rotatedBolBitmap.compress(Bitmap.CompressFormat.JPEG, 75, fOut)
                    fOut.flush()
                    fOut.close()
                    Log.i("aimsDebugDataPersist", "BOL image saved at path ${file.absolutePath}")
                } catch (e: Exception) {
                    Log.w("aimsDebugDataPersist", "Error while saving BOL image: $e")
                }
            }
        }
    }

    //this method returns signature uri based on the trip and waypoint
    suspend fun getSignatureUri(tripIdFk: Long, wayPointSeqNum: Long, fileLoaderListener: FileLoaderListener){
        withContext(Dispatchers.IO) {
            try {
                val filePath = getApplication<Application>().getDir(Environment.DIRECTORY_PICTURES, Context.MODE_PRIVATE).absolutePath + "/AIMS/signature/"
                val dir = File(filePath)
                val file = File(dir, getSignatureBitmapPath(tripIdFk, wayPointSeqNum, driverId.toString()))
                fileLoaderListener.onSuccess(file.toUri())
            } catch (e: Exception) {
                fileLoaderListener.onError(e.toString())
            }
        }
    }

    //this method returns bill of lading uri based on the trip and waypoint
    suspend fun getBolUri(tripIdFk: Long, wayPointSeqNum: Long, fileLoaderListener: FileLoaderListener){
        withContext(Dispatchers.IO) {
            try {
                val filePath = getApplication<Application>().getDir(Environment.DIRECTORY_PICTURES, Context.MODE_PRIVATE).absolutePath + "/AIMS/bol/"
                val dir = File(filePath)
                val file = File(dir, getBolBitmapPath(tripIdFk, wayPointSeqNum, driverId.toString()))
                fileLoaderListener.onSuccess(file.toUri())
            } catch (e: Exception) {
                fileLoaderListener.onError(e.toString())
            }
        }
    }

    //this method saves the trip event in the database
    fun onTripEvent(tripId: Long, tripStatusCode: TripStatusCode){
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                tripRepository.onTripEvent(tripId, tripStatusCode)
            }
        }
    }

    //this method sets up a connection listener which gets triggered when connectivity mode changes on the app
    fun setupInternetListener() {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                setOnline(true)
            }
            override fun onLost(network: Network) {
                setOnline(false)
            }
        })
    }

    //this method sets online or offline state based on the boolean
    fun setOnline(value: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.Main){
                _online.value = value
            }
        }
    }

    init {
        setupInternetListener()
    }
}