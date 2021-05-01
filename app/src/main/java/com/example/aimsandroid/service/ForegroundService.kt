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
    private var doWorkRunnable: Runnable? = null
    private var handler: Handler? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        if(intent?.action.equals("Online")) {
            notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText("Communicating with the dispatcher...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(null)
                .build()
            startForeground(1, notification)
            doWork()
        } else {
            notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText("Running on offline mode...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(null)
                .build()
            startForeground(1, notification)
            endWork()
        }
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
        handler = Handler(Looper.getMainLooper())
        doWorkRunnable = Runnable {
            CoroutineScope(Job()).launch {
                withContext(Dispatchers.IO){
                    syncTripsData()
                    doWorkRunnable?.let { handler!!.postDelayed(it, INTERVAL) }
                }
            }
        }
        handler!!.post(doWorkRunnable!!)
        return
    }

    private fun endWork() {
        if(handler!=null && doWorkRunnable != null) {
            handler!!.removeCallbacks(doWorkRunnable!!)
        }
    }

    private suspend fun syncTripsData() {
        Log.i("aimsDebug", "Syncing...")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        stopForeground(true)
        stopSelf()
        super.onDestroy()
    }
}