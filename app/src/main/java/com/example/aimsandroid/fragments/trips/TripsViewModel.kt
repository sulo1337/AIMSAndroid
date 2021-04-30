package com.example.aimsandroid.fragments.trips

import RotateBitmap
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class TripsViewModel(application: Application) : AndroidViewModel(application) {
    private val tripRepository = TripRepository(application)
    val trips = tripRepository.trips

    private val _refreshing = MutableLiveData<Boolean>()
    val refreshing: LiveData<Boolean>
        get() = _refreshing

    fun refreshTrips(fetchApiEventListener: FetchApiEventListener){
        viewModelScope.launch {
            _refreshing.value = true
            tripRepository.refreshTrips(fetchApiEventListener)
            _refreshing.value = false
        }
    }

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

    suspend fun saveBitmaps(bolBitmap: Bitmap?, billOfLading: BillOfLading){
        if(bolBitmap!=null){
            withContext(Dispatchers.IO){
                try {
                    val filePath =  getApplication<Application>().getDir(Environment.DIRECTORY_PICTURES, Context.MODE_PRIVATE).absolutePath + "/AIMS/bol/"
                    val dir = File(filePath)
                    if(!dir.exists()) {
                        dir.mkdirs()
                    }
                    val file = File(dir, "bol_"+billOfLading.tripIdFk.toString()+"_"+billOfLading.wayPointSeqNum.toString()+".jpeg")
                    val fOut = FileOutputStream(file)
                    val rotatedBolBitmap = RotateBitmap(bolBitmap, 90.0f)
                    rotatedBolBitmap.compress(Bitmap.CompressFormat.JPEG, 75, fOut)
                    fOut.flush()
                    fOut.close()
                    Log.i("aimsDebugFiles", "BOL image saved at path ${file.absolutePath}")
                } catch (e: Exception) {
                    Log.w("aimsDebugFiles", "Error while saving BOL image: $e")
                }
            }
        }
    }

    suspend fun getSignatureUri(tripIdFk: Long, wayPointSeqNum: Long, fileLoaderListener: FileLoaderListener){
        withContext(Dispatchers.IO) {
            try {
                val filePath = getApplication<Application>().getDir(Environment.DIRECTORY_PICTURES, Context.MODE_PRIVATE).absolutePath + "/AIMS/signature/"
                val dir = File(filePath)
                val file = File(dir, "signature_"+tripIdFk.toString()+"_"+wayPointSeqNum.toString()+".png")
                fileLoaderListener.onSuccess(file.toUri())
            } catch (e: Exception) {
                fileLoaderListener.onError(e.toString())
            }
        }
    }

    suspend fun getBolUri(tripIdFk: Long, wayPointSeqNum: Long, fileLoaderListener: FileLoaderListener){
        withContext(Dispatchers.IO) {
            try {
                val filePath = getApplication<Application>().getDir(Environment.DIRECTORY_PICTURES, Context.MODE_PRIVATE).absolutePath + "/AIMS/bol/"
                val dir = File(filePath)
                val file = File(dir, "bol_"+tripIdFk.toString()+"_"+wayPointSeqNum.toString()+".jpeg")
                fileLoaderListener.onSuccess(file.toUri())
            } catch (e: Exception) {
                fileLoaderListener.onError(e.toString())
            }
        }
    }

    fun onTripEvent(tripId: Long, tripStatusCode: TripStatusCode){
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                tripRepository.onTripEvent(tripId, tripStatusCode)
            }
        }
    }
}