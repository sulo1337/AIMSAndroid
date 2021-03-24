package com.example.aimsandroid.database

import androidx.room.*

@Entity(tableName = "review_table")
data class Review(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    @ColumnInfo(name="desc")
    var desc: String = "",
    @ColumnInfo(name="latitude")
    var latitude: Double = 0.0,
    @ColumnInfo(name="longitude")
    var longitude: Double = 0.0,
    @ColumnInfo(name="review")
    var atmosphere: String = ""
)

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey val tripId: Long,
    val tripName: String,
    val trailerDesc: String,
    val trailerCode: String,
    val trailerId: Long,
    val truckDesc: String,
    val truckCode: String,
    val truckId: Long,
    val driverName: String,
    val driverCode: String
)

@Entity(tableName="waypoint", primaryKeys = ["owningTripId", "seqNum"])
data class WayPoint(
    val owningTripId: Long,
    val seqNum: Long,
    val waypointTypeDescription: String,
    val latitude: Double,
    val longitude: Double,
    val destinationCode: String,
    val destinationName: String,
    val siteContainerCode: String?,
    val siteContainerDescription: String?,
    val address1: String,
    val address2: String?,
    val city: String,
    val state: String,
    val postalCode: Int,
    val delReqNum: Long?,
    val productId: Long?,
    val productCode: String?,
    val productDesc: String?,
    val requestedQty: Double?,
    val uom: String?,
    val fill: String?
)

data class TripWithWaypoints(
    @Embedded val trip: Trip,
    @Relation(
        parentColumn = "tripId",
        entityColumn = "owningTripId"
    )
    val waypoints: List<WayPoint>
)