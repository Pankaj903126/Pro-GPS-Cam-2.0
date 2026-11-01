package com.example

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

/**
 * Data class representing a pending photo taken offline awaiting reverse-geocoding sync.
 */
data class PendingPhoto(
    val imagePath: String,
    val rawImagePath: String? = null,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val templateId: String? = null
)

/**
 * Lightweight manager for offline geotagging queue and EXIF metadata handling.
 * Uses SharedPreferences and Gson with zero heavy database overhead.
 */
object OfflineSyncManager {
    private const val TAG = "OfflineSyncManager"
    private const val PREFS_NAME = "offline_sync_queue_prefs"
    private const val KEY_PENDING_PHOTOS = "pending_photos_list"
    private val gson = Gson()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Add a pending offline photo to the persistent sync queue.
     */
    @Synchronized
    fun addToQueue(context: Context, photo: PendingPhoto) {
        val currentList = getPendingPhotos(context).toMutableList()
        // Avoid duplicate entries for the exact same image
        currentList.removeAll { it.imagePath == photo.imagePath }
        currentList.add(photo)
        saveQueue(context, currentList)
        Log.d(TAG, "Added photo to offline queue. Total pending: ${currentList.size}")
    }

    /**
     * Retrieve all pending offline photos stored in the sync queue.
     */
    @Synchronized
    fun getPendingPhotos(context: Context): List<PendingPhoto> {
        val json = getPrefs(context).getString(KEY_PENDING_PHOTOS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<PendingPhoto>>() {}.type
            gson.fromJson<List<PendingPhoto>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error deserializing pending photos queue", e)
            emptyList()
        }
    }

    /**
     * Remove a photo from the queue upon successful reverse-geocoding and restamping.
     */
    @Synchronized
    fun removeFromQueue(context: Context, imagePath: String) {
        val currentList = getPendingPhotos(context).toMutableList()
        val removed = currentList.removeAll { it.imagePath == imagePath }
        if (removed) {
            saveQueue(context, currentList)
            Log.d(TAG, "Removed photo from queue: $imagePath. Remaining: ${currentList.size}")
        }
    }

    /**
     * Get the count of pending offline photos.
     */
    @Synchronized
    fun getPendingCount(context: Context): Int {
        return getPendingPhotos(context).size
    }

    /**
     * Clear all pending photos from queue.
     */
    @Synchronized
    fun clearQueue(context: Context) {
        getPrefs(context).edit().remove(KEY_PENDING_PHOTOS).apply()
    }

    private fun saveQueue(context: Context, list: List<PendingPhoto>) {
        try {
            val json = gson.toJson(list)
            getPrefs(context).edit().putString(KEY_PENDING_PHOTOS, json).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving pending photos queue", e)
        }
    }

    // =========================================================================
    // EXIF Helper Functions (DMS Format Conversion & GPS/Timestamp Embedding)
    // =========================================================================

    /**
     * Converts decimal degrees (e.g. 28.613939) into standard EXIF DMS rational string format:
     * "Degrees/1,Minutes/1,Seconds_x1000/1000"
     */
    fun decimalToDMS(decimal: Double): String {
        val absVal = abs(decimal)
        val degrees = absVal.toInt()
        val minutesDouble = (absVal - degrees) * 60.0
        val minutes = minutesDouble.toInt()
        val secondsDouble = (minutesDouble - minutes) * 60.0
        val secondsInt = (secondsDouble * 1000.0).toLong()
        return "$degrees/1,$minutes/1,$secondsInt/1000"
    }

    /**
     * Apply GPS latitude, longitude, and date/time tags to an ExifInterface instance.
     */
    fun applyGpsMetadata(
        exif: ExifInterface,
        latitude: Double,
        longitude: Double,
        timestamp: Long = System.currentTimeMillis()
    ) {
        try {
            val latDMS = decimalToDMS(latitude)
            val lngDMS = decimalToDMS(longitude)
            val latRef = if (latitude >= 0.0) "N" else "S"
            val lngRef = if (longitude >= 0.0) "E" else "W"

            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, latDMS)
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, latRef)
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, lngDMS)
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, lngRef)

            val now = Date(timestamp)
            val utcDateFormat = SimpleDateFormat("yyyy:MM:dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val utcTimeFormat = SimpleDateFormat("HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val localDateTimeFormat = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US)

            exif.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, utcDateFormat.format(now))
            exif.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, utcTimeFormat.format(now))
            exif.setAttribute(ExifInterface.TAG_DATETIME, localDateTimeFormat.format(now))
            exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, localDateTimeFormat.format(now))
            exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, localDateTimeFormat.format(now))

            // androidx.exifinterface high-level helper
            try {
                exif.setLatLong(latitude, longitude)
            } catch (_: Exception) {}

            exif.saveAttributes()
            Log.d(TAG, "EXIF GPS metadata embedded successfully: $latitude ($latRef), $longitude ($lngRef)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to embed EXIF GPS metadata", e)
        }
    }

    /**
     * Write EXIF GPS tags directly to a File.
     */
    fun writeExifGps(
        file: File,
        latitude: Double,
        longitude: Double,
        timestamp: Long = System.currentTimeMillis()
    ) {
        if (!file.exists()) return
        try {
            val exif = ExifInterface(file)
            applyGpsMetadata(exif, latitude, longitude, timestamp)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write EXIF to file: ${file.absolutePath}", e)
        }
    }

    /**
     * Write EXIF GPS tags to a content URI or file path string.
     */
    fun writeExifGps(
        context: Context,
        pathOrUri: String,
        latitude: Double,
        longitude: Double,
        timestamp: Long = System.currentTimeMillis()
    ) {
        try {
            if (pathOrUri.startsWith("content://")) {
                val uri = Uri.parse(pathOrUri)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    context.contentResolver.openFileDescriptor(uri, "rw")?.use { pfd ->
                        val exif = ExifInterface(pfd.fileDescriptor)
                        applyGpsMetadata(exif, latitude, longitude, timestamp)
                    }
                }
            } else {
                val file = File(pathOrUri)
                if (file.exists()) {
                    writeExifGps(file, latitude, longitude, timestamp)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write EXIF to path/URI: $pathOrUri", e)
        }
    }

    // =========================================================================
    // Network Connectivity Helper
    // =========================================================================

    /**
     * Checks whether an active internet connection is currently available and validated.
     */
    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            val activeNetwork = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: Exception) {
            Log.w(TAG, "Network check error", e)
            false
        }
    }
}
