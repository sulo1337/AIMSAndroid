package com.example.aimsandroid.utils

/**
 * This enum class contains all the possible values for trip event
 */
enum class TripStatusCode{
    ARRIVE_AT_SOURCE{
        override fun getStatusCode() = "ArriveSrc"
        override fun getStatusMessage() = "Arrive At Source"
    },

    ARRIVE_AT_SITE{
        override fun getStatusCode() = "ArriveSite"
        override fun getStatusMessage() = "Arrive At Site"
    },

    LEAVE_SRC {
        override fun getStatusCode() = "LeaveSrc"
        override fun getStatusMessage() = "Leaving Source"
    },

    LEAVE_SITE{
        override fun getStatusCode() = "LeaveSite"
        override fun getStatusMessage() = "Leaving Site"
    },

    ON_BREAK{
        override fun getStatusCode() = "OnBreak"
        override fun getStatusMessage() = "Driver on Break"
    },

    ON_DUTY{
        override fun getStatusCode() = "OnDuty"
        override fun getStatusMessage() = "Clocking in for work Day"
    },

    OFF_DUTY{
        override fun getStatusCode() = "OffDuty"
        override fun getStatusMessage() = "Clocking out for work day"
    },

    DRIVING{
        override fun getStatusCode() = "Driving"
        override fun getStatusMessage() = "Driving to next location"
    },

    SELECT_TRIP{
        override fun getStatusCode() = "SelTrip"
        override fun getStatusMessage() = "Select Trip"
    },

    SRC_SITE{
        override fun getStatusCode() = "SrcSite"
        override fun getStatusMessage() = "Heading to Site From Source"
    },

    SITE_SITE{
        override fun getStatusCode() = "SiteSite"
        override fun getStatusMessage() = "Heading to Site from Site"
    },

    TRIP_DONE {
        override fun getStatusCode() = "TripDone"
        override fun getStatusMessage() = "Trip Complete"
    };

    abstract fun getStatusCode(): String
    abstract fun getStatusMessage(): String
}