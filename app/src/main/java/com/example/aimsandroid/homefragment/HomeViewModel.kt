package com.example.aimsandroid.homefragment

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.example.aimsandroid.database.Review
import com.example.aimsandroid.database.getDatabase
import com.example.aimsandroid.repository.LocationRepository
import com.example.aimsandroid.repository.ReviewsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("MissingPermission")
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = getDatabase(application)
    private val reviewsRepository = ReviewsRepository(database)
    private val locationRepository = LocationRepository(application)

    val reviews = reviewsRepository.reviews

    val latitude = locationRepository.latitude
    val longitude = locationRepository.longitude

    var prevLatitude = 0.0
    var prevLongitude = 0.0

    init {
        latitude.observeForever(Observer {
            it?.let {
                prevLatitude = locationRepository.prevLatitude
            }
        })
        longitude.observeForever(Observer {
            it?.let{
                prevLongitude = locationRepository.prevLongitude
            }
        })
    }

    fun onClickSubmit(description: String, atmosphere: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.reviewDao.insertReview(Review(0L, description, latitude.value!!, longitude.value!!, atmosphere))
            }
        }
    }
}

