package com.example.aimsandroid

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.aimsandroid.databinding.ActivityLoginBinding
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        binding.loginButton.setOnClickListener {
            validateLoginInfo()
        }

        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.secondaryLightColor)
    }

    private fun validateLoginInfo() {
        var error = false
        if(binding.driverId.text.toString().isEmpty()) {
            error = true
            binding.driverId.error = "Required"
        }
        if(binding.driverKey.text.toString().isEmpty()) {
            error = true
            binding.driverKey.error = "Required"
        }
        if(!error) {
            val prefs = application.getSharedPreferences("com.example.aimsandroid", Context.MODE_PRIVATE)
            prefs.edit().putString("driverId", binding.driverId.text.toString()).apply()
            prefs.edit().putString("driverKey",binding.driverKey.text.toString()).apply()
            startMainActivity()
            finish()
        }
    }

    private fun checkPermissions() {
        TedPermission.with(this)
            .setPermissionListener(object: PermissionListener {
                override fun onPermissionGranted() {
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    Toast.makeText(applicationContext, "Background location permission denied", Toast.LENGTH_SHORT).show()
                }
            })
            .setDeniedMessage("Please allow location permission.\n\nPlease select \"Allow all the time\" for location to enable background navigation.")
            .setGotoSettingButtonText("Open App Settings")
            .setPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .check()
    }

    private fun startMainActivity() {
        val mainActivityIntent = Intent(this,MainActivity::class.java)
        startActivity(mainActivityIntent)
    }

}