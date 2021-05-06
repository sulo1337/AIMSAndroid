package com.example.aimsandroid.database

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import java.io.Serializable
import java.util.*

/** These are all the entities that are stored in the local android Room(SQLITE) database*/
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

//This entity represents trips table where trips information is stored
@Entity(tableName = "trips_table")
data class Trip(
    @PrimaryKey
    val tripId: Long,
    val tripName: String,
    val trailerDesc: String,
    val trailerCode: String,
    val trailerId: Long,
    val truckDesc: String,
    val truckCode: String,
    val truckId: Long,
    val driverName: String,
    val driverCode: String,
    var complete: Boolean
)


//This entity represents trips status table where status for each trip is stored
@Entity(tableName = "trips_status_table")
data class TripStatus(
    @ForeignKey(
        entity = Trip::class,
        parentColumns = ["tripId"],
        childColumns = ["tripId"],
        onDelete = CASCADE
    )
    @PrimaryKey
    val tripId: Long,
    val complete: Boolean
)

//This entity represents waypoint table which includes details of each waypoint
@Entity(tableName="waypoint_table", primaryKeys = ["owningTripId", "seqNum"])
data class WayPoint(
    @ForeignKey(
        entity = Trip::class,
        parentColumns = ["tripId"],
        childColumns = ["owningTripId"],
        onDelete = CASCADE
    )
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
    val fill: String?,
    val sourceId: Long?,
    val siteId: Long?,
    val date: String?
)

//This entity represents bill of lading table used to store form data
@Entity(tableName = "billoflading_table", primaryKeys = ["tripIdFk", "wayPointSeqNum"])
data class BillOfLading(
    @ForeignKey(
        entity = WayPoint::class,
        parentColumns = ["owningTripId", "seqNum"],
        childColumns = ["owningTripId", "wayPointSeqNum"],
        onDelete = CASCADE
    )
    val wayPointSeqNum: Long,
    val tripIdFk: Long,
    val complete: Boolean?,
    val deliveryTicketNumber: Long?,
    val initialMeterReading: Double?,
    val finalMeterReading: Double?,
    val pickedUpBy: String?,
    val comments: String?,
    val billOfLadingNumber: String?,
    val loadingStarted: String?,
    val loadingEnded: String?,
    val product: String?,
    val grossQuantity: Double?,
    val netQuantity: Double?,
    val arrivedAt: String?,
    var synced: Boolean,
    val waypointSourceId: Long?,
    val waypointSiteId: Long?
)

//Embedded class for relational database
data class WaypointWithBillOfLading(
    @Embedded val waypoint: WayPoint?,
    @Embedded val billOfLading: BillOfLading?
)

//Embedded class for relational databases
data class TripWithWaypoints(
    @Embedded val trip: Trip,
    @Relation(
        parentColumn = "tripId",
        entityColumn = "owningTripId"
    )
    val waypoints: List<WayPoint>,
    @Relation(
        parentColumn = "tripId",
        entityColumn = "tripId"
    )
    val tripStatus: TripStatus?
)

//This entity represents trip events table that store events that occur throughout the trip
@Entity(tableName = "trips_events_table")
data class TripEvent(
    val statusCode: String,
    val driverId: String,
    @PrimaryKey(autoGenerate = true)
    val eventId: Long,
    val tripId: Long,
    val statusMessage: String,
    val datetime: String,
    var synced: Boolean
)

//This entity is used to store clock in and clock out information
@Entity(tableName = "time_table")
data class TimeTable(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val clockedIn: String,
    var clockedOut: String?
)