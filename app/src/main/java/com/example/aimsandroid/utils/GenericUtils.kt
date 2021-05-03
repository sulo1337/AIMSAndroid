import android.app.Activity
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.aimsandroid.R
import com.example.aimsandroid.database.WayPoint
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*


val API_KEY: String = "f20f8b25-b149-481c-9d2c-41aeb76246ef"
val PHONE_NUMBER: String = "8007292467"
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
    return  SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
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

val colorGreen = Color.rgb(0,171,102)
val colorBlue = Color.rgb(0,150,255)
val colorSecondaryLight = Color.rgb(241,134,64)