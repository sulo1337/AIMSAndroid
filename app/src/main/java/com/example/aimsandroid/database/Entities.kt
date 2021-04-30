package com.example.aimsandroid.database

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import java.io.Serializable
import java.util.*

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
    val fill: String?
)

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
    val billOfLadingNumber: Long?,
    val loadingStarted: String?,
    val loadingEnded: String?,
    val grossQuantity: Double?,
    val netQuantity: Double?,
    val arrivedAt: String?
)

data class WaypointWithBillOfLading(
    @Embedded val waypoint: WayPoint?,
    @Embedded val billOfLading: BillOfLading?
)

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

@Entity(tableName = "trips_events_table")
data class TripEvent(
    @PrimaryKey(autoGenerate = true)
    val eventId: Long,
    val driverId: String,
    val tripId: Long,
    val statusCode: String,
    val statusMessage: String,
    val datetime: String,
    var synced: Boolean
)