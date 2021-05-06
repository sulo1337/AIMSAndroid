package com.example.aimsandroid.mapdownloader

import com.example.aimsandroid.R
import android.app.ListActivity
import android.content.DialogInterface
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.here.android.mpa.common.ApplicationContext
import com.here.android.mpa.common.MapEngine
import com.here.android.mpa.common.MapSettings
import com.here.android.mpa.common.OnEngineInitListener
import com.here.android.mpa.odml.MapLoader
import com.here.android.mpa.odml.MapLoader.ResultCode
import com.here.android.mpa.odml.MapPackage
import java.io.File

/**
 * Boiler-plate code from HERE-API to view downloadable map
 * */
internal class
MapListView(private val m_activity: ListActivity) {
    private var m_progressTextView: TextView? = null
    private var m_mapLoader: MapLoader? = null
    private var m_listAdapter: MapListAdapter? = null
    private var m_currentMapPackageList // Global variable to keep track of the map
            : List<MapPackage>? = null

    private fun initMapEngine() {
        // This will use external storage to save map cache data, it is also possible to set
        // private app's path
        val path: String = File(m_activity.getExternalFilesDir(null), ".here-map-data")
            .getAbsolutePath()
        // This method will throw IllegalArgumentException if provided path is not writable
        MapSettings.setDiskCacheRootPath(path)
        MapEngine.getInstance().init(
            ApplicationContext(m_activity)
        ) { error ->
            if (error == OnEngineInitListener.Error.NONE) {
                /*
                         * Similar to other HERE Android SDK objects, the MapLoader can only be
                         * instantiated after the MapEngine has been initialized successfully.
                         */
                mapPackages
            } else {
                Log.e(TAG, "Failed to initialize MapEngine: $error")
                AlertDialog.Builder(m_activity).setMessage(
                    """
                        Error : ${error.name}
                        
                        ${error.details}
                        """.trimIndent()
                )
                    .setTitle("Engine Initialization Error")
                    .setNegativeButton(
                        "Cancel",
                        DialogInterface.OnClickListener { dialog, which -> m_activity.finish() }).create().show()
            }
        }
    }

    private fun initUIElements() {
        val cancelButton: Button = m_activity.findViewById<View>(R.id.cancelBtn) as Button
        cancelButton.setOnClickListener {
            m_mapLoader!!.cancelCurrentOperation()
        }
        m_progressTextView = m_activity.findViewById<View>(R.id.progressTextView) as TextView
        val mapUpdateButton: Button = m_activity.findViewById<View>(R.id.mapUpdateBtn) as Button
        mapUpdateButton.setOnClickListener{
            /*
                 * Because all operations of MapLoader are mutually exclusive, if there is any other
                 * operation which has been triggered previously but yet to receive its call
                 * back,the current operation cannot be triggered at the same time.
                 */
            val success = m_mapLoader!!.checkForMapDataUpdate()
            if (!success) {
                Toast.makeText(
                    m_activity, "MapLoader is being busy with other operations",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Add a MapLoader listener to monitor its status
    private val mapPackages: Unit
        private get() {
            Log.d(TAG, "getMapPackages()")
            m_mapLoader = MapLoader.getInstance()
            // Add a MapLoader listener to monitor its status
            m_mapLoader!!.addListener(m_listener)
            m_mapLoader!!.mapPackages
            initUIElements()
        }

    // Handles the click action on map list item.
    fun onListItemClicked(l: ListView?, v: View?, position: Int, id: Long) {
        val clickedMapPackage = m_currentMapPackageList!![position]
        val children = clickedMapPackage.children
        if (children.size > 0) {
            // Children map packages exist.Show them on the screen.
            refreshListView(ArrayList(children))
        } else {
            /*
             * No children map packages are available, we should perform downloading or
             * un-installation action.
             */
            val idList: MutableList<Int> = ArrayList()
            idList.add(clickedMapPackage.id)
            if (clickedMapPackage
                    .installationState == MapPackage.InstallationState.INSTALLED
            ) {
                val success = m_mapLoader!!.uninstallMapPackages(idList)
                if (!success) {
                    Toast.makeText(
                        m_activity, "MapLoader is being busy with other operations",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(m_activity, "Uninstalling...", Toast.LENGTH_SHORT).show()
                }
            } else {
                val success = m_mapLoader!!.installMapPackages(idList)
                if (!success) {
                    Toast.makeText(
                        m_activity, "MapLoader is being busy with other operations",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        m_activity, "Downloading " + clickedMapPackage.title,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Listener to monitor all activities of MapLoader.
    private val m_listener: MapLoader.Listener = object : MapLoader.Listener {
        override fun onProgress(i: Int) {
            if (i < 100) {
                m_progressTextView!!.text = "Progress: $i"
            } else {
                m_progressTextView!!.text = "Installing..."
            }
            Log.d(TAG, "onProgress()")
        }

        override fun onInstallationSize(l: Long, l1: Long) {}
        override fun onGetMapPackagesComplete(
            rootMapPackage: MapPackage?,
            resultCode: ResultCode
        ) {
            Log.d(TAG, "onGetMapPackagesComplete()")
            /*
             * Please note that to get the latest MapPackage status, the application should always
             * use the rootMapPackage that being returned here. The same applies to other listener
             * call backs.
             */if (resultCode == ResultCode.OPERATION_SUCCESSFUL) {
                val children = rootMapPackage!!.children[0].children[0].children
                refreshListView(ArrayList(children))
            } else if (resultCode == ResultCode.OPERATION_BUSY) {
                // The map loader is still busy, just try again.
                m_mapLoader!!.mapPackages
            }
        }

        override fun onCheckForUpdateComplete(
            updateAvailable: Boolean, current: String?, update: String?,
            resultCode: ResultCode
        ) {
            Log.d(TAG, "onCheckForUpdateComplete()")
            if (resultCode == ResultCode.OPERATION_SUCCESSFUL) {
                if (updateAvailable) {
                    // Update the map if there is a new version available
                    val success = m_mapLoader!!.performMapDataUpdate()
                    if (!success) {
                        Toast.makeText(
                            m_activity, "MapLoader is being busy with other operations",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            m_activity, "Starting map update from current version:"
                                    + current + " to " + update, Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        m_activity, "Current map version: $current is the latest",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else if (resultCode == ResultCode.OPERATION_BUSY) {
                // The map loader is still busy, just try again.
                m_mapLoader!!.checkForMapDataUpdate()
            }
        }

        override fun onPerformMapDataUpdateComplete(
            rootMapPackage: MapPackage?,
            resultCode: ResultCode
        ) {
            Log.d(TAG, "onPerformMapDataUpdateComplete()")
            if (resultCode == ResultCode.OPERATION_SUCCESSFUL) {
                Toast.makeText(m_activity, "Map update is completed", Toast.LENGTH_SHORT).show()
                refreshListView(ArrayList(rootMapPackage!!.children[0].children[0].children))
            }
        }

        override fun onInstallMapPackagesComplete(
            rootMapPackage: MapPackage?,
            resultCode: ResultCode
        ) {
            m_progressTextView!!.text = ""
            if (resultCode == ResultCode.OPERATION_SUCCESSFUL) {
                Toast.makeText(m_activity, "Installation is completed", Toast.LENGTH_SHORT).show()
                val children = rootMapPackage!!.children[0].children[0].children
                refreshListView(ArrayList(children))
            } else if (resultCode == ResultCode.OPERATION_CANCELLED) {
                Toast.makeText(m_activity, "Installation is cancelled...", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        override fun onUninstallMapPackagesComplete(
            rootMapPackage: MapPackage?,
            resultCode: ResultCode
        ) {
            if (resultCode == ResultCode.OPERATION_SUCCESSFUL) {
                Toast.makeText(m_activity, "Uninstallation is completed", Toast.LENGTH_SHORT)
                    .show()
                val children = rootMapPackage!!.children[0].children[0].children
                refreshListView(ArrayList(children))
            } else if (resultCode == ResultCode.OPERATION_CANCELLED) {
                Toast.makeText(m_activity, "Uninstallation is cancelled...", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    /*
     * Helper function to refresh the map list upon the completion of any MapLoader
     * operations.Please note that for the code simplicity, this app refreshes the list to display
     * the highest level of the map hierarchies i.e continent map whenever a map
     * installation/un-installation has been completed.Application developers can implement their
     * own logic in this case to handle how they want to present to end users
     */
    private fun refreshListView(list: ArrayList<MapPackage>) {
        if (m_listAdapter != null) {
            m_listAdapter!!.clear()
            m_listAdapter!!.addAll(list)
            m_listAdapter!!.notifyDataSetChanged()
        } else {
            m_listAdapter = MapListAdapter(
                m_activity, android.R.layout.simple_list_item_1,
                list
            )
            m_activity.listAdapter = m_listAdapter
        }
        m_currentMapPackageList = list
    }

    companion object {
        private val TAG = MapListView::class.java.simpleName
    }

    // package list currently being displayed on
    // screen
    init {
        initMapEngine()
    }
}
