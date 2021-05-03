package com.example.aimsandroid.utils

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.aimsandroid.database.TripWithWaypoints
import com.example.aimsandroid.database.WayPoint
import getFullAddress
import getWaypointDate

@BindingAdapter("bindSourceTitle")
fun bindSourceTitle(textView: TextView, data: TripWithWaypoints){
    var firstSource = findFirst(data.waypoints, "Source")
    if(firstSource != null) {
        textView.text = firstSource.destinationName.trim()
    } else {
        textView.text = "No source information"
    }
}

@BindingAdapter("bindSiteTitle")
fun bindSiteTitle(textView: TextView, data: TripWithWaypoints){
    val firstSite = findFirst(data.waypoints, "Site Container")
    if(firstSite != null) {
        textView.text = firstSite.destinationName.trim()
    } else {
        textView.text = "No source information"
    }
}

@BindingAdapter("bindSourceTitleAddress")
fun bindSourceTitleAddress(textView: TextView, data: TripWithWaypoints){
    val firstSource = findFirst(data.waypoints, "Source")
    if(firstSource != null) {
        textView.text = getFullAddress(firstSource)
    } else {
        textView.text = "No address information"
    }
}

@BindingAdapter("bindSiteTitleAddress")
fun bindSiteTitleAddress(textView: TextView, data: TripWithWaypoints){
    val firstSite = findFirst(data.waypoints, "Site Container")
    if(firstSite!=null) {
        textView.text = getFullAddress(firstSite)
    } else {
        textView.text = "No address information"
    }
}

@BindingAdapter("bindSourceNumber")
fun bindSourceNumber(textView: TextView, data: TripWithWaypoints){
    val numSource = numTypes(data.waypoints, "Source")
    if(numSource == 1){
        textView.visibility = View.GONE
    } else {
        textView.text = "+"+(numSource-1).toString()+" sources"
    }
}

@BindingAdapter("bindSiteNumber")
fun bindSiteNumber(textView: TextView, data: TripWithWaypoints){
    val numSite = numTypes(data.waypoints, "Site Container")
    if(numSite == 1){
        textView.visibility = View.GONE
    } else {
        textView.text = "+"+(numSite-1).toString()+" destinations"
    }
}

@BindingAdapter("bindSourceDeadline")
fun bindSourceDeadline(textView: TextView, data: TripWithWaypoints) {
    val firstSource = findFirst(data.waypoints, "Source")
    if(firstSource != null) {
        textView.text = "Load by ${firstSource!!.date?.let { getWaypointDate(it) }}"
    } else {
        textView.text = ""
    }
}

@BindingAdapter("bindSiteDeadline")
fun bindSiteDeadline(textView: TextView, data: TripWithWaypoints) {
    val firstSite = findFirst(data.waypoints, "Site Container")
    if(firstSite != null) {
        textView.text = "Deliver by ${firstSite!!.date?.let { getWaypointDate(it) }}"
    } else {
        textView.text = ""
    }
}

fun findFirst(waypoints: List<WayPoint>, type: String): WayPoint?{
    var first: WayPoint? = null
    for(waypoint in waypoints) {
        if(waypoint.waypointTypeDescription.trim().equals(type)) {
            first = waypoint
            break
        }
    }
    return first
}

fun numTypes(waypoints: List<WayPoint>, type: String): Int {
    var numType = 0;
    for(waypoint in waypoints){
        if(waypoint.waypointTypeDescription.trim().equals(type)){
            numType +=1
        }
    }
    return numType
}