package com.example.aimsandroid.network

import com.example.aimsandroid.database.Trip
import com.example.aimsandroid.database.WayPoint
import com.squareup.moshi.Json
/**
 * This file contains all the necessary data transfer objects used to call the API
 * */
data class PutTripStatusResponseContainer(
    @Json(name = "data") val data: PutTripStatusResponse,
    @Json(name = "status") val status: String
)

data class PutTripStatusResponse(
    @Json(name = "resultSet1") val responseStatus: List<ResponseStatus>
)

data class PutTripProductPickupContainer(
    @Json(name = "data") val data: PutTripProductPickupResponse,
    @Json(name = "status") val status: String
)

data class PutTripProductPickupResponse(
    @Json(name = "resultSet1") val responseStatus: List<ResponseStatus>,
    @Json(name = "resultSet2") val responseData: List<PutTripProductPickupData>
)

data class PutTripProductPickupData(
    @Json(name = "DriverCode") val driverCode: String,
    @Json(name = "TripID") val tripId: Long,
    @Json(name = "SourceID") val sourceId: Long,
    @Json(name = "TripWayPointID") val tripWayPointId: Long,
    @Json(name = "id") val id: Long,
    @Json(name = "Productid") val productId: Long,
    @Json(name = "ManifestNumber") val manifestNumber: String,
    @Json(name = "LoadstartDateTime") val startDateTime: String,
    @Json(name = "LoadEndDateTime") val endDateTime: String,
    @Json(name = "SourceGrossQuantity") val grossQuantity: Double,
    @Json(name = "SourceNetQuantity") val netQuantity: Double
)

data class TripData(
    @Json(name = "resultSet2") val responseStatus: List<ResponseStatus>,
    @Json(name = "resultSet1") val tripSections: List<TripSection>?
)

data class ResponseContainer(
    @Json(name = "data") val data: TripData,
    @Json(name = "status") val status: String
)

data class ResponseStatus(
    @Json(name = "StatusCode") val statusCode: Int,
    @Json(name = "Status") val status: String
)

data class TripSection(
    @Json(name = "DriverCode") val driverCode: String,
    @Json(name = "DriverName") val driverName: String,
    @Json(name = "TruckId") val truckId: Long,
    @Json(name = "TruckCode") val truckCode: String,
    @Json(name = "TruckDesc") val truckDesc: String,
    @Json(name = "TrailerId") val trailerId: Long,
    @Json(name = "TrailerCode") val trailerCode: String,
    @Json(name = "TrailerDesc") val trailerDesc: String,
    @Json(name = "TripId") val tripId: Long,
    @Json(name = "TripName") val tripName: String,
    //TODO change to date datatype
    @Json(name = "TripDate") val tripDate: String,
    @Json(name = "SeqNum") val seqNum: Long,
    @Json(name = "WaypointTypeDescription") val waypointTypeDescription: String,
    @Json(name = "Latitude") val latitude: Double,
    @Json(name = "Longitude") val longitude: Double,
    @Json(name = "DestinationCode") val destinationCode: String,
    @Json(name = "DestinationName") val destinationName: String,
    @Json(name = "SiteContainerCode") val siteContainerCode: String?,
    @Json(name = "SiteContainerDescription") val siteContainerDescription: String?,
    @Json(name = "Address1") val address1: String,
    @Json(name = "Address2") val address2: String?,
    @Json(name = "City") val city: String,
    @Json(name = "StateAbbrev") val state: String,
    @Json(name = "PostalCode") val postalCode: Int,
    @Json(name = "DelReqNum") val delReqNum: Long?,
    @Json(name = "DelReqLineNum") val delReqLineNum: Long?,
    @Json(name = "ProductId") val productId: Long?,
    @Json(name = "ProductCode") val productCode: String?,
    @Json(name = "ProductDesc") val productDesc: String?,
    @Json(name = "RequestedQty") val requestedQty: Double?,
    @Json(name = "UOM") val uom: String?,
    @Json(name = "Fill") val fill: String?,
    @Json(name = "SourceID") val sourceId: Long?,
    @Json(name = "SiteID") val siteId: Long?
) {
    fun getTrip(): Trip {
        return Trip(
            tripId,
            tripName,
            trailerDesc,
            trailerCode,
            trailerId,
            truckDesc,
            truckCode,
            truckId,
            driverName,
            driverCode,
            false
        )
    }
    fun getWaypoint(): WayPoint{
        return WayPoint(
            tripId,
            seqNum,
            waypointTypeDescription.trim(),
            latitude,
            longitude,
            destinationCode,
            destinationName,
            siteContainerCode,
            siteContainerDescription,
            address1,
            address2,
            city,
            state,
            postalCode,
            delReqNum,
            productId,
            productCode,
            productDesc,
            requestedQty,
            uom,
            fill,
            sourceId,
            siteId,
            tripDate
        )
    }
}

data class GetDriverInfoResponseContainer(
    @Json(name = "data") val data: GetDriverInfoResponse,
    @Json(name = "status") val status: String
)

data class GetDriverInfoResponse(
    @Json(name = "resultSet1") val resultSet1: List<GetDriverInfoData>
)

data class GetDriverInfoData(
    @Json(name = "Id") val id: Long,
    @Json(name = "CompanyID") val companyId: Long,
    @Json(name = "Code") val code: String,
    @Json(name = "DriverName") val driverName: String,
    @Json(name = "DriverDescription") val driverDescription: String,
    @Json(name = "CompasDriverID") val compasDriverId: String,
    @Json(name = "TruckId") val truckId: Long,
    @Json(name = "TruckDescription") val truckDescription: String,
    @Json(name = "Active") val active: Boolean
)