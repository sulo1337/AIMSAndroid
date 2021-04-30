import android.app.Activity
import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import com.example.aimsandroid.R
import com.example.aimsandroid.database.WayPoint


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

val colorGreen = Color.rgb(0,171,102)
val colorBlue = Color.rgb(0,150,255)
val colorSecondaryLight = Color.rgb(241,134,64)