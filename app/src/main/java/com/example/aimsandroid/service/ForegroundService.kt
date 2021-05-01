package com.example.aimsandroid.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.aimsandroid.R
import com.example.aimsandroid.repository.TripRepository
import kotlinx.coroutines.*
import java.lang.Runnable

class ForegroundService: Service() {

    companion object {
        var running = false
        fun isRunning(): Boolean {
            return running
        }
    }

    val INTERVAL = 30000L
    val CHANNEL_ID = "AIMSNetworkService"
    val NOTIFICATION_ID = 1
    private lateinit var notification: Notification
    private var internetAvailable: Boolean? = null
    private var doWorkRunnable: Runnable? = null
    private var handler: Handler? = null
    lateinit var tripRepository: TripRepository

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        buildNotification("Communicating with the dispatcher...")
        startForeground(NOTIFICATION_ID, notification)
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                internetAvailable = true
                buildNotification("Communicating with the dispatcher...")
                updateNotification()
            }
            override fun onLost(network: Network) {
                internetAvailable = false
                buildNotification("Running on offline mode...")
                updateNotification()
            }
        })
        tripRepository = TripRepository(application)
        running = true
        doWork()
        return START_NOT_STICKY
    }

    private fun buildNotification(message: String) {
        notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setSound(null)
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
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
                    if(internetIsConnected()) {
                        buildNotification("Communicating with the dispatcher...")
                        syncTripsData()
                    } else {
                        buildNotification("Running on offline mode...")
                    }
                    updateNotification()
                    doWorkRunnable?.let { handler!!.postDelayed(it, INTERVAL) }
                }
            }
        }
        handler!!.post(doWorkRunnable!!)
        return
    }

    fun internetIsConnected(): Boolean {
        return try {
            val command = "ping -c 1 google.com"
            Runtime.getRuntime().exec(command).waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        stopForeground(true)
        stopSelf()
//        if(doWorkRunnable != null) {
//            handler?.removeCallbacks(doWorkRunnable!!)
//            handler?.removeCallbacksAndMessages(null)
//            doWorkRunnable = null
//        }
        running = false
        super.onDestroy()
    }
}