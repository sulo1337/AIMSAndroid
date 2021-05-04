package com.example.aimsandroid

import API_KEY
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.aimsandroid.databinding.ActivityLoginBinding
import com.example.aimsandroid.network.Network
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import getLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.net.UnknownHostException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var loader: AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        binding.loginButton.setOnClickListener {
            validateLoginInfo()
        }
        prefs = application.getSharedPreferences("com.example.aimsandroid", Context.MODE_PRIVATE)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.secondaryLightColor)
        loader = getLoader(this)
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
        if(binding.driverKey.text.toString()!="0000") {
            error = true
            binding.driverKey.error = "Incorrect key!"
        }
        if(!error) {
            loader.show()
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    try{
                        val response = Network.dispatcher.getDriverInfoAsync(API_KEY, binding.driverId.text.toString(), "true").await()
                        if(response.data.resultSet1.isNotEmpty()) {
                            prefs.edit().putString("driverId", response.data.resultSet1[0].code.trim()).apply()
                            prefs.edit().putString("driverKey",binding.driverKey.text.toString().trim()).apply()
                            prefs.edit().putString("driverName", response.data.resultSet1[0].driverName.trim()).apply()
                            prefs.edit().putBoolean("clockedIn", false).apply()
                            withContext(Dispatchers.Main) {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    loader.hide()
                                    startMainActivity()
                                }, 1000)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(applicationContext, "Driver does not exist", Toast.LENGTH_LONG).show()
                                hideLoader()
                            }
                        }
                    } catch (e: UnknownHostException) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(applicationContext, "No internet connection!", Toast.LENGTH_LONG).show()
                            hideLoader()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(applicationContext, "Login failed $e", Toast.LENGTH_LONG).show()
                            hideLoader()
                        }
                    }
                }
            }
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
        finish()
    }

    private fun hideLoader() {
        Handler(Looper.getMainLooper()).postDelayed({
            loader.hide()
        }, 1000)
    }
}