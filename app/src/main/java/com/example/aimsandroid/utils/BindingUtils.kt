package com.example.aimsandroid.utils

import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.aimsandroid.database.Review
import com.example.aimsandroid.database.TripWithWaypoints
import com.example.aimsandroid.fragments.trips.TripsAdapter

@BindingAdapter("bindSourceTitle")
fun bindSourceTitle(textView: TextView, data: TripWithWaypoints){
    //TODO implement this
    textView.text = data.waypoints[0].destinationName
}

@BindingAdapter("bindSiteTitle")
fun bindSiteTitle(textView: TextView, data: TripWithWaypoints){
    //TODO implement this
    textView.text = data.waypoints[1].destinationName
}

@BindingAdapter("bindSourceTitleAddress")
fun bindSourceTitleAddress(textView: TextView, data: TripWithWaypoints){
    //TODO implement this
    textView.text = data.waypoints[0].address1.trim() + ", " + data.waypoints[0].city.trim() + ", "+data.waypoints[0].state.trim()+ " "+data.waypoints[0].postalCode.toString();
}

@BindingAdapter("bindSiteTitleAddress")
fun bindSiteTitleAddress(textView: TextView, data: TripWithWaypoints){
    //TODO implement this
    textView.text = data.waypoints[1].address1.trim() + ", " + data.waypoints[1].city.trim() + ", "+data.waypoints[1].state.trim()+ " "+data.waypoints[1].postalCode.toString();
}

@BindingAdapter("bindSourceNumber")
fun bindSourceNumber(textView: TextView, data: TripWithWaypoints){
    var numSource = 0;
    for(waypoint in data.waypoints){
        if(waypoint.waypointTypeDescription.equals("Source")){
            numSource +=1
        }
    }
    if(numSource == 1){
        textView.visibility = View.GONE
    } else {
        textView.text = "+"+(numSource-1).toString()+" sources"
    }
}

@BindingAdapter("bindSiteNumber")
fun bindSiteNumber(textView: TextView, data: TripWithWaypoints){
    var numSite = 0;
    for(waypoint in data.waypoints){
        if(waypoint.waypointTypeDescription.equals("Site Container")){
            numSite +=1
        }
    }
    if(numSite == 1){
        textView.visibility = View.GONE
    } else {
        textView.text = "+"+(numSite-1).toString()+" destinations"
    }
}