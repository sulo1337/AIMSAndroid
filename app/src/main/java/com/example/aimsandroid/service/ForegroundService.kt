package com.example.aimsandroid.service


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.aimsandroid.MainActivity
import com.example.aimsandroid.R


class ForegroundService : Service() {
    override fun onCreate() {
        super.onCreate()
        initChannels(this.applicationContext)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action == START_ACTION) {
            val notificationIntent = Intent(this, MainActivity::class.java)
            notificationIntent.action = Intent.ACTION_MAIN
            notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
            val notification = NotificationCompat.Builder(this.applicationContext, CHANNEL)
                .setContentTitle("Guidance")
                .setContentText("Guidance in progress ...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setLocalOnly(true)
                .build()
            startForeground(FOREGROUND_SERVICE_ID, notification)
        } else if (intent.action == STOP_ACTION) {
            stopForeground(true)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    fun initChannels(context: Context) {
        if (Build.VERSION.SDK_INT < 26) {
            return
        }
        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL, "Foreground channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "Channel for foreground service"
        notificationManager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent): IBinder? {
        // Used only in case of bound services.
        return null
    }

    companion object {
        var FOREGROUND_SERVICE_ID = 101
        var START_ACTION = "com.here.android.example.voice.guidance.fs.action.start"
        var STOP_ACTION = "com.here.android.example.voice.guidance.fs.action.stop"
        private const val CHANNEL = "default"
    }
}