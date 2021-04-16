package com.example.aimsandroid

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.here.android.mpa.mapping.Map
import com.jakewharton.processphoenix.ProcessPhoenix


class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE_ASK_PERMISSIONS = 1
    private val RUNTIME_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false);
        val navController = findNavController(R.id.defaultNavHostFragment)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_bar)
        bottomNavigationView.setupWithNavController(navController)
        bottomNavigationView.setOnNavigationItemReselectedListener {  }

        handlePermissions()
//        //if we do not have location permission
//        if(ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            requestLocationPermission()
//        }
    }

    private fun handlePermissions() {
        if(!hasPermissions(this, *RUNTIME_PERMISSIONS)){
            ActivityCompat.requestPermissions(this, RUNTIME_PERMISSIONS, REQUEST_CODE_ASK_PERMISSIONS)
        }
    }

    private fun hasPermissions(context: Context, vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_ASK_PERMISSIONS -> {
                var index = 0
                while (index < permissions.size) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {

                        /*
                         * If the user turned down the permission request in the past and chose the
                         * Don't ask again option in the permission request system dialog.
                         */
                        if (!ActivityCompat
                                .shouldShowRequestPermissionRationale(this, permissions[index])
                        ) {
                            Toast.makeText(
                                this, "Required permission " + permissions[index]
                                        + " not granted. "
                                        + "Please go to settings and turn on for sample app",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                this, "Required permission " + permissions[index]
                                        + " not granted", Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    index++
                }
                restartApp()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun restartApp() {
        ProcessPhoenix.triggerRebirth(applicationContext)
    }

//    private fun requestLocationPermission() {
//        //if requesting location permission second time
//        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
//            AlertDialog.Builder(ContextThemeWrapper(this, R.style.dialog))
//                .setTitle("Permission needed")
//                .setMessage("Location permission is need to access your current location")
//                .setPositiveButton("OK", DialogInterface.OnClickListener() { dialogInterface: DialogInterface, i: Int ->
//                    val permissions: Array<String> = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
//                    ActivityCompat.requestPermissions(this, permissions, 1)
//                    dialogInterface.dismiss()
//                })
//                .setNegativeButton("Cancel", DialogInterface.OnClickListener(){ dialogInterface: DialogInterface, i: Int ->
//                    dialogInterface.dismiss()
//                })
//                .create()
//                .show()
//        }
//        //if requesting location permission first time
//        else {
//            val permissions: Array<String> = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
//            ActivityCompat.requestPermissions(this, permissions, 1)
//        }
//    }
}