package com.example.aimsandroid

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        window.statusBarColor = ContextCompat.getColor(this, R.color.secondaryLightColor)

        if(checkLogin()) {
            startMainActivity()
        } else {
            startLoginActivity()
        }
    }

    private fun checkLogin(): Boolean {
        val prefs: SharedPreferences = application.getSharedPreferences("com.example.aimsandroid", Context.MODE_PRIVATE)
        val driverId = prefs.getString("driverId", "x")
        val driverKey = prefs.getString("driverKey", "x")
        val driverName = prefs.getString("driverName", "x")
        if(driverId == "x" || driverKey == "x" || driverName == "x") {
            return false
        }
        return true
    }

    private fun startLoginActivity() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 1000)
    }

    private fun startMainActivity() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 1000)
    }
}