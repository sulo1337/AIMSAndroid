package com.example.aimsandroid

import android.content.Context
import com.example.aimsandroid.R
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.aimsandroid.mapdownloader.MapListView


/**
 * This class is an Android activity class to create map download activity in the application
 * Boiler-plate code from HERE-API
 */
class MapDownloadActivity : android.app.ListActivity() {
    private val REQUEST_CODE_ASK_PERMISSIONS = 1
    private var m_mapListView: MapListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_download)
        if (hasPermissions()) {
            setupMapListView()
        }
    }

    /**
     * Only when the app's target SDK is 23 or higher, it requests each dangerous permissions it
     * needs when the app is running.
     */
    private fun hasPermissions(): Boolean {
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
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                permissions[index]
                            )
                        ) {
                            Toast.makeText(
                                this,
                                "Required permission " + permissions[index] + " not granted. "
                                        + "Please go to settings and turn on for sample app",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                this,
                                "Required permission " + permissions[index] + " not granted",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    index++
                }
                setupMapListView()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun setupMapListView() {
        // All permission requests are being handled. Create map fragment view. Please note
        // the HERE Mobile SDK requires all permissions defined above to operate properly.
        m_mapListView = MapListView(this)
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        m_mapListView!!.onListItemClicked(l, v, position, id)
    }
}