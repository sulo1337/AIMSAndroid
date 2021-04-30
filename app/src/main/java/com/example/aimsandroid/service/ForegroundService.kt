package com.example.aimsandroid.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.os.postDelayed
import com.example.aimsandroid.MainActivity
import com.example.aimsandroid.R
import com.example.aimsandroid.repository.TripRepository
import kotlinx.coroutines.*
import java.lang.Runnable

class ForegroundService: Service() {

    val INTERVAL = 10000L
    val CHANNEL_ID = "AIMSNetworkService"

    private lateinit var notification: Notification
    private lateinit var doWorkRunnable: Runnable
    private val tripRepository = TripRepository(application)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentText("Communicating with the dispatcher...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setSound(null)
            .build()
        doWork()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "AIMSDispatcher",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun doWork() {
        val handler = Handler(Looper.getMainLooper())
        doWorkRunnable = Runnable {
            CoroutineScope(Job()).launch {
                withContext(Dispatchers.IO){
                    startForeground(1, notification)
                    syncTripsData()
                    stopForeground(true)
                    handler.postDelayed(doWorkRunnable, INTERVAL)
                }
            }
        }
        handler.post(doWorkRunnable)
        return
    }

    private suspend fun syncTripsData() {

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        stopForeground(true)
        super.onDestroy()
    }
}