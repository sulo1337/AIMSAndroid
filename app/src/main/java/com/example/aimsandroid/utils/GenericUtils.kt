import android.app.Activity
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.icu.text.DateFormatSymbols
import androidx.appcompat.app.AlertDialog
import com.example.aimsandroid.R
import com.example.aimsandroid.database.TimeTable
import com.example.aimsandroid.database.WayPoint
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*


val API_KEY: String = "f20f8b25-b149-481c-9d2c-41aeb76246ef"
val PHONE_NUMBER: String = "8007292467"
val DATE_TIME_PATTERN: String = "yyyy-MM-dd HH:mm:ss"
fun SharedPreferences.Editor.putDouble(key: String, double: Double) =
    putLong(key, java.lang.Double.doubleToRawLongBits(double))

fun SharedPreferences.getDouble(key: String, default: Double) =
    java.lang.Double.longBitsToDouble(getLong(key, java.lang.Double.doubleToRawLongBits(default)))

fun RotateBitmap(source: Bitmap, angle: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}

fun getFullAddress(waypoint: WayPoint): String {
    return waypoint.address1.trim() + ", " + waypoint.city.trim() + ", "+waypoint.state.trim()+ " "+waypoint.postalCode.toString();
}

fun sortWaypointBySeqNum(waypoints: List<WayPoint>): List<WayPoint> {
    return waypoints.sortedWith(compareBy({it.owningTripId},{it.seqNum}))
}

fun getLoader(activity: Activity): AlertDialog{
    val alertDialog = AlertDialog.Builder(activity, R.style.TransparentAlertDialogTheme)
        .setView(R.layout.alert_progress_bar)
        .setCancelable(false)
        .create()
    alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    return alertDialog
}

fun getCurrentDateTimeString(): String {
    return  SimpleDateFormat(DATE_TIME_PATTERN, Locale.US).format(Date())
}

fun getStackTraceString(e: Throwable): String {
    val sw = StringWriter()
    val pw = PrintWriter(sw)
    e.printStackTrace(pw)
    return sw.toString()
}

fun getGreeting(): String {
    val c = Calendar.getInstance()
    val timeOfDay = c[Calendar.HOUR_OF_DAY]

    if (timeOfDay in 0..11) {
        return "Good Morning"
    } else if (timeOfDay in 12..15) {
        return "Good Afternoon"
    } else if (timeOfDay in 16..20) {
        return "Good Evening"
    } else if (timeOfDay in 21..23) {
        return "Good Night"
    } else {
        return "Good Night"
    }
}

fun calculateTotalHours(listTimeTable: List<TimeTable>): String{
    var totalHours: Double = 0.0
    try {
        val sdf = SimpleDateFormat(DATE_TIME_PATTERN, Locale.US)
        for(timeTable in listTimeTable) {
            val startDate = sdf.parse(timeTable.clockedIn)!!
            val endDate: Date
            if(timeTable.clockedOut != null) {
                endDate = sdf.parse(timeTable.clockedOut!!)!!
            } else {
                endDate = sdf.parse(getCurrentDateTimeString())!!
            }
            totalHours += endDate.time - startDate.time
        }
    } catch (e: Exception) {

    }
    totalHours /= 1000*60*60
    return String.format("%.1f", totalHours)
}

fun getWaypointDate(date: String) : String{
    try{
        return "${date.substring(8, 10)} ${getMonthForInt(Integer.parseInt(date.substring(5,7))-1)} ${date.substring(0,4)}"
    } catch (e: Exception) {
        return date
    }
}

fun getMonthForInt(num: Int) : String {
    var month = "wrong"
    val dfs = DateFormatSymbols()
    val months: Array<String> = dfs.months
    if (num in 0..11) {
        month = months[num]
    }
    return month
}

fun getSignatureBitmapPath(tripId: Long, seqNum: Long, driverId: String) : String{
    return "signature_"+tripId.toString()+"_"+seqNum.toString()+"_"+driverId.toString()+".png"
}

fun getBolBitmapPath(tripId: Long, seqNum: Long, driverId: String) : String {
    return "bol_"+tripId.toString()+"_"+seqNum.toString()+"_"+driverId.toString()+".jpeg"
}

fun getFuel(fuelType : String?): String {
    if(fuelType == null) {
        return "N/A"
    }
    return fuelType
}
fun getReqQty(waypoint: WayPoint): String {
    if(waypoint.requestedQty == null || waypoint.uom == null) {
        return "N/A"
    }
    return String.format("%.2f %s", waypoint.requestedQty, waypoint.uom)
}

val colorGreen = Color.rgb(0,171,102)
val colorBlue = Color.rgb(0,150,255)
val colorSecondaryLight = Color.rgb(241,134,64)