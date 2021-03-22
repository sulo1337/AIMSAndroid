package com.example.aimsandroid.repository

import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.aimsandroid.utils.MapPositionChangedListener
import com.here.android.mpa.common.GeoPosition
import com.here.android.mpa.common.LocationDataSourceHERE
import com.here.android.mpa.common.PositioningManager
import java.lang.ref.WeakReference

class LocationRepository(private val application: Application): PositioningManager.OnPositionChangedListener {
    private var mPositioningManager: PositioningManager
    private var mHereLocation: LocationDataSourceHERE
    private val _latitude = MutableLiveData<Double>()
    val latitude: LiveData<Double>
        get() = _latitude

    private val _longitude = MutableLiveData<Double>()
    val longitude: LiveData<Double>
        get() = _longitude

    var prevLatitude = 0.0
    var prevLongitude = 0.0

    fun onLocationChanged(latitude: Double, longitude: Double) {
        prevLatitude = this._latitude.value!!
        prevLongitude = this._longitude.value!!
        this._latitude.value = latitude
        this._longitude.value = longitude
    }

    init {
        _latitude.value = 0.0
        _longitude.value = 0.0
        mPositioningManager = PositioningManager.getInstance()
        mHereLocation = LocationDataSourceHERE.getInstance()
        mPositioningManager.setDataSource(mHereLocation)
        mPositioningManager.addListener(
            WeakReference<PositioningManager.OnPositionChangedListener>(
                this
            )
        )
        mPositioningManager.start(PositioningManager.LocationMethod.GPS_NETWORK_INDOOR)
    }

    override fun onPositionUpdated(p0: PositioningManager.LocationMethod?, p1: GeoPosition?, p2: Boolean) {
        p1?.let {
            onLocationChanged(it.coordinate.latitude, it.coordinate.longitude)
        }
    }

    override fun onPositionFixChanged(p0: PositioningManager.LocationMethod?, p1: PositioningManager.LocationStatus?) {

    }

}

