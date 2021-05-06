package com.example.aimsandroid.mapdownloader

import com.example.aimsandroid.R
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import colorGreen
import colorSecondaryLight
import com.here.android.mpa.odml.MapPackage

/**
 * Boiler-plate code from HERE API to populate list of downloadable maps
 * */
class MapListAdapter(context: Context?, resource: Int, private val m_list: List<MapPackage>) :
    ArrayAdapter<MapPackage?>(context!!, resource, m_list) {

    override fun getCount(): Int {
        return m_list.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val mapPackage = m_list[position]
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                R.layout.list_item, parent,
                false
            )
        }

        /*
         * Display title and size information of each map package.Please refer to HERE Android SDK
         * API doc for all supported APIs.
         */
        val mapPackageName = convertView!!.findViewById<View>(R.id.mapPackageName) as TextView
        val mapPackageSize = convertView.findViewById<View>(R.id.mapPackageSize) as TextView
        val icon = convertView.findViewById<View>(R.id.mapPackageStateIcon) as ImageView
        mapPackageName.text = mapPackage.title

        when(mapPackage.installationState.toString()){
            "INSTALLED" -> {
                mapPackageName.setTextColor(colorGreen)
                icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_close))
                icon.setColorFilter(Color.RED)
            }
            else -> {
                mapPackageName.setTextColor(colorSecondaryLight)
                icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_download))
                icon.setColorFilter(colorGreen)
            }
        }
        mapPackageSize.text = (mapPackage.size/1024).toString() + "MB"
        return convertView
    }
}