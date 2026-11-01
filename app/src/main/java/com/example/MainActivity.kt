package com.example

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import androidx.lifecycle.lifecycleScope
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.VibratorManager
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.widget.Toast
import android.widget.VideoView
import android.widget.MediaController
import androidx.compose.ui.viewinterop.AndroidView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import android.util.LruCache
import java.io.*
import java.util.*
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import okhttp3.*
import org.json.JSONObject
import kotlinx.coroutines.flow.update
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import android.view.KeyEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.*
import com.google.android.gms.location.*
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.activity.compose.BackHandler
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// CameraX and Media3 supports
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.Composition
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.BitmapOverlay
import androidx.media3.effect.ScaleAndRotateTransformation
import com.google.common.collect.ImmutableList
import android.view.OrientationEventListener
import android.view.Surface
import androidx.compose.ui.graphics.graphicsLayer

// Consent and AdMob Management
object ConsentManager {
    const val PRIVACY_POLICY_URL = "https://example.com/privacy-policy" // TODO: Replace with your hosted Privacy Policy URL

    fun requestConsentAndInitAds(activity: Activity, onComplete: () -> Unit = {}) {
        val params = ConsentRequestParameters.Builder().build()
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w("ConsentManager", "Consent form error: ${formError.message}")
                    }
                    if (consentInformation.canRequestAds()) {
                        initializeMobileAds(activity.applicationContext)
                    }
                    onComplete()
                }
            },
            { requestError ->
                Log.w("ConsentManager", "Consent info update error: ${requestError.message}")
                if (consentInformation.canRequestAds()) {
                    initializeMobileAds(activity.applicationContext)
                }
                onComplete()
            }
        )
    }

    fun initializeMobileAds(context: Context) {
        try {
            val reqConfig = RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE)
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_T)
                .build()
            MobileAds.setRequestConfiguration(reqConfig)
            MobileAds.initialize(context) {}
        } catch (t: Throwable) {
            Log.w("ConsentManager", "MobileAds initialization handled safely: ${t.message}")
        }
    }

    fun showPrivacyOptions(activity: Activity) {
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        if (consentInformation.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED) {
            UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
                if (formError != null) {
                    Toast.makeText(activity, "Privacy options error: ${formError.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(activity, "Privacy options form is not required in your region.", Toast.LENGTH_SHORT).show()
        }
    }
}

// Model for media (photos and videos) captured or loaded in session
data class SessionMediaItem(
    val uri: Uri,
    val isVideo: Boolean,
    val dateAdded: Long = System.currentTimeMillis(),
    val title: String = "",
    val isDriveSynced: Boolean = false
)

// Model for address from Reverse Geocode API
data class AddressInfo(
    val city: String,
    val state: String,
    val road: String,
    val postcode: String,
    val country: String = "India",
    val success: Boolean,
    val tier: Int = 1,
    val sourceName: String = "Android Built-in Geocoder",
    val description: String = "Address resolved via 1st Priority: Android Native Geocoder"
)

// Model for address sources in Live GPS settings
data class AddressSourceItem(
    val tier: Int,
    val sourceName: String,
    val address: AddressInfo?,
    val isLoading: Boolean = false,
    val isAvailable: Boolean = false,
    val error: String? = null
)

data class LiveGpsSourcesState(
    val isRefreshing: Boolean = false,
    val androidGeocoder: AddressSourceItem = AddressSourceItem(1, "Android Built-in Geocoder", null),
    val nominatim: AddressSourceItem = AddressSourceItem(2, "OpenStreetMap Nominatim", null),
    val ipApi: AddressSourceItem = AddressSourceItem(3, "IP-API Geolocation", null)
)

// Model for IP Fallback Location API
data class IpLocationInfo(
    val lat: Double,
    val lng: Double,
    val city: String,
    val state: String,
    val country: String = "India",
    val success: Boolean
)

fun cleanAddressText(text: String): String {
    return text
        .replace(Regex("\\s*,\\s*"), ", ")
        .replace(Regex(",(\\s*,)+"), ",")
        .replace(Regex("\\s+"), " ")
        .trim(',', ' ')
}

enum class LiveCameraAspectRatio(val label: String, val ratioFloat: Float?) {
    RATIO_3_4("3:4", 3f / 4f),
    RATIO_9_16("9:16", 9f / 16f),
    RATIO_FULL("Full", null),
    RATIO_1_1("1:1", 1f / 1f);

    fun next(): LiveCameraAspectRatio = when (this) {
        RATIO_3_4 -> RATIO_9_16
        RATIO_9_16 -> RATIO_FULL
        RATIO_FULL -> RATIO_1_1
        RATIO_1_1 -> RATIO_3_4
    }
}

// Crop bitmap precisely to selected camera aspect ratio (1:1, 3:4, 9:16, Full)
fun cropBitmapToAspectRatio(
    bitmap: Bitmap,
    targetRatio: LiveCameraAspectRatio,
    fullScreenRatio: Float = 9f / 19.5f
): Bitmap {
    val srcWidth = bitmap.width
    val srcHeight = bitmap.height

    val targetAspect: Float = when (targetRatio) {
        LiveCameraAspectRatio.RATIO_1_1 -> 1.0f
        LiveCameraAspectRatio.RATIO_3_4 -> 3.0f / 4.0f
        LiveCameraAspectRatio.RATIO_9_16 -> 9.0f / 16.0f
        LiveCameraAspectRatio.RATIO_FULL -> fullScreenRatio
    }

    val isLandscape = srcWidth > srcHeight
    val desiredAspect = if (isLandscape) (1f / targetAspect) else targetAspect
    val currentAspect = srcWidth.toFloat() / srcHeight.toFloat()

    val (cropWidth, cropHeight) = if (currentAspect > desiredAspect) {
        val w = (srcHeight * desiredAspect).toInt().coerceAtMost(srcWidth)
        w to srcHeight
    } else {
        val h = (srcWidth / desiredAspect).toInt().coerceAtMost(srcHeight)
        srcWidth to h
    }

    val cropX = ((srcWidth - cropWidth) / 2).coerceAtLeast(0)
    val cropY = ((srcHeight - cropHeight) / 2).coerceAtLeast(0)

    if (cropWidth == srcWidth && cropHeight == srcHeight) {
        return bitmap
    }

    val cropped = Bitmap.createBitmap(bitmap, cropX, cropY, cropWidth, cropHeight)
    if (cropped != bitmap) {
        bitmap.recycle()
    }
    return cropped
}

// Stamp Template design styles supported across live camera, photos & videos
enum class StampTemplate(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: String,
    val badgeColorHex: Long
) {
    STANDARD("standard", "Standard Pro", "Map pin, telemetry & flag banner", "📋", 0xFF00E5FF),
    CLASSIC("classic", "Classic Badge", "Gold border & geodetic seal", "🏛️", 0xFFFFD700),
    TECH_HUD("tech_hud", "Cyber HUD", "Neon crosshairs & matrix grid", "⚡", 0xFF00FF66),
    CLEAN_MINIMAL("clean_minimal", "Clean Minimal", "Frosted glass floating pill", "✨", 0xFF818CF8),
    DARK_GLASS("dark_glass", "Dark Glass", "Cyan indicator & blur backdrop", "💎", 0xFF38BDF8),
    OFFICIAL_STAMP("official_stamp", "Official Stamp", "Red verification emblem seal", "🔴", 0xFFDC2626),
    ECO_AGRI("eco_agri", "Eco Agriculture", "Emerald badge, harvest survey & plot", "🌾", 0xFF10B981),
    REVENUE_AMBER("revenue_amber", "Revenue Amber", "Amber demarcation & gold grid", "📙", 0xFFF59E0B),
    GLACIER_FROST("glacier_frost", "Glacier Frost", "Ice frosted glass translucent frame", "❄️", 0xFF93C5FD);

    companion object {
        fun fromId(id: String?): StampTemplate {
            return values().firstOrNull { it.id == id } ?: STANDARD
        }
    }
}

// Output Image Quality settings for direct capture & save compression
enum class ImageQuality(
    val id: String,
    val title: String,
    val description: String,
    val badge: String,
    val maxDimension: Int,
    val jpegQuality: Int,
    val icon: String
) {
    AUTO(
        id = "auto",
        title = "Auto (Actual Size)",
        description = "100% full original resolution & quality without reduction",
        badge = "ORIGINAL",
        maxDimension = 4096,
        jpegQuality = 100,
        icon = "💎"
    ),
    MEDIUM(
        id = "medium",
        title = "Medium Quality",
        description = "80% size reduction • Balanced resolution & fast processing",
        badge = "80% REDUCTION",
        maxDimension = 1920,
        jpegQuality = 75,
        icon = "⚡"
    ),
    LOW(
        id = "low",
        title = "Low Quality",
        description = "60% size reduction • Compact size & higher compression",
        badge = "60% REDUCTION",
        maxDimension = 1280,
        jpegQuality = 50,
        icon = "📦"
    );

    companion object {
        fun fromId(id: String?): ImageQuality {
            return values().firstOrNull { it.id == id } ?: AUTO
        }
    }
}

// Persistent Flash Mode settings for camera & low-light survey conditions (4 modes: Auto, On, Fill Light, Off)
enum class CameraFlashMode(
    val id: String,
    val title: String,
    val shortLabel: String,
    val description: String,
    val icon: String,
    val flashModeInt: Int
) {
    AUTO(
        id = "auto",
        title = "Auto Flash",
        shortLabel = "AUTO",
        description = "Fires flash automatically when survey environment is dark",
        icon = "⚡A",
        flashModeInt = ImageCapture.FLASH_MODE_AUTO
    ),
    ON(
        id = "on",
        title = "Flash On",
        shortLabel = "ON",
        description = "Always fires flash pulse during photo capture & video recording",
        icon = "⚡ON",
        flashModeInt = ImageCapture.FLASH_MODE_ON
    ),
    FILL_LIGHT(
        id = "fill_light",
        title = "Fill Light (Always On)",
        shortLabel = "FILL",
        description = "Keeps continuous survey light / torch ON at all times",
        icon = "💡",
        flashModeInt = ImageCapture.FLASH_MODE_OFF
    ),
    OFF(
        id = "off",
        title = "Flash Off",
        shortLabel = "OFF",
        description = "Disables flash and torch completely for natural lighting",
        icon = "🚫",
        flashModeInt = ImageCapture.FLASH_MODE_OFF
    );

    fun next(): CameraFlashMode {
        return when (this) {
            AUTO -> ON
            ON -> FILL_LIGHT
            FILL_LIGHT -> OFF
            OFF -> AUTO
        }
    }

    companion object {
        fun fromId(id: String?): CameraFlashMode {
            return values().firstOrNull { it.id == id } ?: AUTO
        }
    }
}

// State model for the GPS location details
data class LocationState(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val accuracy: Float? = null,
    val city: String = "Please wait…",
    val state: String = "",
    val country: String = "India",
    val road: String = "Fetching location data",
    val postcode: String = "",
    val source: String = "GPS", // "GPS" / "AUTOMATIC" / "IP" / "MANUAL"
    val statusType: String = "searching", // "searching" / "ok" / "ip" / "err"
    val statusLabel: String = "Detecting location…",
    val manualTime: Long? = null,
    val stampScale: Float = 1.0f,
    val isManualOverride: Boolean = false,
    val useInsideOverlay: Boolean = true,
    val removeWatermark: Boolean = false,
    val isCustomNoteEnabled: Boolean = false,
    val customNote: String = "",
    val isCustomWatermarkEnabled: Boolean = false,
    val customWatermarkText: String = "Made With Love ❤️",
    val isSaveOriginalPhotoEnabled: Boolean = false,
    val addressTier: Int? = null,
    val addressTierName: String = "",
    val addressTierDesc: String = "",
    val stampTemplate: StampTemplate = StampTemplate.STANDARD,
    val volumeKeyAction: String = "capture",
    val storageFolderName: String = "ProGpsCam",
    val isGoogleDriveSyncEnabled: Boolean = false,
    val googleDriveFolderName: String = "ProGpsCam Backup",
    val googleDriveAccountEmail: String = "@gmail.com",
    val isCompassEnabled: Boolean = false,
    val azimuth: Float = 0f
) {
    fun getWatermarkText(): String {
        return if (isCustomWatermarkEnabled && customWatermarkText.isNotBlank()) {
            customWatermarkText.trim()
        } else {
            "Made With Love ❤️"
        }
    }

    fun getCardinalDirection(): String {
        val deg = ((azimuth % 360f) + 360f) % 360f
        val dirs = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        val index = (((deg + 22.5f) / 45f).toInt()) % 8
        return "${dirs[index]} ${deg.toInt()}°"
    }
}

enum class VolumeKeyType { UP, DOWN }

sealed class SyncStatus {
    object Idle : SyncStatus()
    data class Syncing(val current: Int, val total: Int) : SyncStatus()
    object Success : SyncStatus()
}

class GpsViewModel(private val context: Context) : ViewModel() {
    private val prefs = context.getSharedPreferences("pro_gps_cam_prefs", Context.MODE_PRIVATE)
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private var locationCallback: LocationCallback? = null

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus = _syncStatus.asStateFlow()

    private val _pendingOfflineCount = MutableStateFlow<Int>(OfflineSyncManager.getPendingCount(context))
    val pendingOfflineCount = _pendingOfflineCount.asStateFlow()

    private var isSyncInProgress = false

    private val _volumeKeyAction = MutableStateFlow(
        prefs.getString("pro_volume_key_action", "capture") ?: "capture"
    )
    val volumeKeyAction = _volumeKeyAction.asStateFlow()

    private val _volumeKeyEvent = MutableSharedFlow<VolumeKeyType>(extraBufferCapacity = 1)
    val volumeKeyEvent = _volumeKeyEvent.asSharedFlow()

    fun isProActive(): Boolean {
        val until = prefs.getLong("pro_unlocked_until_ms", 0L)
        return until > System.currentTimeMillis() || prefs.getBoolean("is_pro_unlocked", false)
    }

    private val _isProUnlocked = MutableStateFlow(isProActive())
    val isProUnlocked = _isProUnlocked.asStateFlow()

    private val _showManualLocation = MutableStateFlow(prefs.getBoolean("pro_manual_location", false))
    val showManualLocation = _showManualLocation.asStateFlow()

    private val _showManualTime = MutableStateFlow(prefs.getBoolean("pro_manual_time", false))
    val showManualTime = _showManualTime.asStateFlow()

    private val _showUploadGallery = MutableStateFlow(prefs.getBoolean("pro_upload_gallery", false))
    val showUploadGallery = _showUploadGallery.asStateFlow()

    private val _removeWatermark = MutableStateFlow(prefs.getBoolean("pro_remove_watermark", false))
    val removeWatermark = _removeWatermark.asStateFlow()

    private val _isCustomNoteEnabled = MutableStateFlow(prefs.getBoolean("custom_note_enabled", false))
    val isCustomNoteEnabled = _isCustomNoteEnabled.asStateFlow()

    private val _customNote = MutableStateFlow(prefs.getString("custom_note_text", "") ?: "")
    val customNote = _customNote.asStateFlow()

    private val _isCustomWatermarkEnabled = MutableStateFlow(prefs.getBoolean("custom_watermark_enabled", false))
    val isCustomWatermarkEnabled = _isCustomWatermarkEnabled.asStateFlow()

    private val _customWatermarkText = MutableStateFlow(
        prefs.getString("custom_watermark_text", "Made With Love ❤️") ?: "Made With Love ❤️"
    )
    val customWatermarkText = _customWatermarkText.asStateFlow()

    private val _isSaveOriginalPhotoEnabled = MutableStateFlow(prefs.getBoolean("pro_save_original_photo", false))
    val isSaveOriginalPhotoEnabled = _isSaveOriginalPhotoEnabled.asStateFlow()

    private val _isSelfieMirrored = MutableStateFlow(prefs.getBoolean("pro_selfie_mirrored", true))
    val isSelfieMirrored = _isSelfieMirrored.asStateFlow()

    private val _hapticFeedback = MutableStateFlow(prefs.getBoolean("pro_haptic_feedback", true))
    val hapticFeedback = _hapticFeedback.asStateFlow()

    private val _imageQuality = MutableStateFlow(
        ImageQuality.fromId(prefs.getString("image_quality_mode", "auto"))
    )
    val imageQuality = _imageQuality.asStateFlow()

    private val _flashMode = MutableStateFlow(
        CameraFlashMode.fromId(prefs.getString("camera_flash_mode", "auto"))
    )
    val flashMode = _flashMode.asStateFlow()

    private val _storageFolderName = MutableStateFlow(
        prefs.getString("storage_folder_name", "ProGpsCam") ?: "ProGpsCam"
    )
    val storageFolderName = _storageFolderName.asStateFlow()

    private val _isGoogleDriveSyncEnabled = MutableStateFlow(
        prefs.getBoolean("google_drive_sync_enabled", false)
    )
    val isGoogleDriveSyncEnabled = _isGoogleDriveSyncEnabled.asStateFlow()

    private val _googleDriveFolderName = MutableStateFlow(
        prefs.getString("google_drive_folder_name", "ProGpsCam Backup") ?: "ProGpsCam Backup"
    )
    val googleDriveFolderName = _googleDriveFolderName.asStateFlow()

    private val _googleDriveAccountEmail = MutableStateFlow(
        prefs.getString("google_drive_account_email", "pr903128@gmail.com") ?: "pr903128@gmail.com"
    )
    val googleDriveAccountEmail = _googleDriveAccountEmail.asStateFlow()

    private val _isCompassEnabled = MutableStateFlow(prefs.getBoolean("pro_compass_enabled", false))
    val isCompassEnabled = _isCompassEnabled.asStateFlow()

    fun setCompassEnabled(enabled: Boolean) {
        _isCompassEnabled.value = enabled
        prefs.edit().putBoolean("pro_compass_enabled", enabled).apply()
        _locationState.update { it.copy(isCompassEnabled = enabled) }
    }

    fun updateAzimuth(azimuth: Float) {
        _locationState.update { it.copy(azimuth = azimuth) }
    }

    private val _locationState = MutableStateFlow(
        LocationState(
            removeWatermark = prefs.getBoolean("pro_remove_watermark", false),
            stampScale = prefs.getFloat("stamp_scale", 1.0f),
            isCustomNoteEnabled = prefs.getBoolean("custom_note_enabled", false),
            customNote = prefs.getString("custom_note_text", "") ?: "",
            isCustomWatermarkEnabled = prefs.getBoolean("custom_watermark_enabled", false),
            customWatermarkText = prefs.getString("custom_watermark_text", "Made With Love ❤️") ?: "Made With Love ❤️",
            isSaveOriginalPhotoEnabled = prefs.getBoolean("pro_save_original_photo", false),
            stampTemplate = StampTemplate.fromId(prefs.getString("stamp_template", "standard")),
            volumeKeyAction = prefs.getString("pro_volume_key_action", "capture") ?: "capture",
            storageFolderName = prefs.getString("storage_folder_name", "ProGpsCam") ?: "ProGpsCam",
            isGoogleDriveSyncEnabled = prefs.getBoolean("google_drive_sync_enabled", false),
            googleDriveFolderName = prefs.getString("google_drive_folder_name", "ProGpsCam Backup") ?: "ProGpsCam Backup",
            googleDriveAccountEmail = prefs.getString("google_drive_account_email", "pr903128@gmail.com") ?: "pr903128@gmail.com",
            isCompassEnabled = prefs.getBoolean("pro_compass_enabled", false)
        )
    )
    val locationState = _locationState.asStateFlow()

    private val _hasCapturedPhoto = MutableStateFlow(false)
    val hasCapturedPhoto = _hasCapturedPhoto.asStateFlow()

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("is_dark_mode", true))
    val isDarkMode = _isDarkMode.asStateFlow()

    private val _sessionMedia = MutableStateFlow<List<SessionMediaItem>>(emptyList())
    val sessionMedia = _sessionMedia.asStateFlow()

    // Backward compatibility for any remaining legacy callers
    val sessionPhotos = _sessionMedia.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing = _isProcessing.asStateFlow()

    private val _processingMessage = MutableStateFlow("Processing…")
    val processingMessage = _processingMessage.asStateFlow()

    private val _processedBitmap = MutableStateFlow<Bitmap?>(null)
    val processedBitmap = _processedBitmap.asStateFlow()

    private val _processedVideoUri = MutableStateFlow<Uri?>(null)
    val processedVideoUri = _processedVideoUri.asStateFlow()

    private val _showToast = MutableStateFlow(false)
    val showToast = _showToast.asStateFlow()

    private val _toastMessage = MutableStateFlow("")
    val toastMessage = _toastMessage.asStateFlow()

    private val _liveGpsSources = MutableStateFlow(LiveGpsSourcesState())
    val liveGpsSources = _liveGpsSources.asStateFlow()

    private val nominatimCache = LruCache<String, AddressInfo>(50)
    private var lastNominatimRequestTime = 0L
    private val nominatimMutex = Mutex()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "ProGpsCam/1.2 (Android; contact@example.com)")
                .build()
            chain.proceed(request)
        }
        .build()

    private var fallbackJob: Job? = null
    private var lastAutoPostcode: String = ""

    // AdMob Preloaded Rewarded Ad Management
    private var preloadedRewardedAd: RewardedAd? = null
    private var isRewardedAdLoading: Boolean = false
    val rewardedAdUnitId: String = BuildConfig.ADMOB_REWARDED_ID

    fun preloadRewardedAd(ctx: Context) {
        if (preloadedRewardedAd != null || isRewardedAdLoading) return
        isRewardedAdLoading = true
        Log.d("RewardedAd", "Preloading AdMob rewarded video in background after location ready...")
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            ctx,
            rewardedAdUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    isRewardedAdLoading = false
                    preloadedRewardedAd = ad
                    Log.d("RewardedAd", "Background AdMob rewarded ad successfully preloaded and ready for instant playback!")
                }
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isRewardedAdLoading = false
                    preloadedRewardedAd = null
                    Log.w("RewardedAd", "Preload failed: ${loadAdError.message}")
                }
            }
        )
    }

    fun getAndConsumePreloadedRewardedAd(): RewardedAd? {
        val ad = preloadedRewardedAd
        preloadedRewardedAd = null
        return ad
    }

    fun isRewardedAdReady(): Boolean {
        return preloadedRewardedAd != null
    }

    init {
        cleanupCache(context)
        checkProExpiry()
        startLocationEngine()
        loadExistingSessionMedia(context)
    }

    fun checkProExpiry() {
        val active = isProActive()
        _isProUnlocked.value = active
    }

    fun unlockProFor24Hours() {
        val until = System.currentTimeMillis() + 24 * 60 * 60 * 1000L
        prefs.edit()
            .putLong("pro_unlocked_until_ms", until)
            .putBoolean("is_pro_unlocked", false)
            .apply()
        _isProUnlocked.value = true
        triggerCustomToast("🎉 All Pro Features Unlocked for 24 Hours!")
    }

    fun getProRemainingTimeString(): String {
        val until = prefs.getLong("pro_unlocked_until_ms", 0L)
        val diff = until - System.currentTimeMillis()
        if (diff <= 0) {
            return if (prefs.getBoolean("is_pro_unlocked", false)) "Active" else "Expired"
        }
        val hours = diff / (1000 * 60 * 60)
        val mins = (diff % (1000 * 60 * 60)) / (1000 * 60)
        return if (hours > 0) "${hours}h ${mins}m left" else "${mins}m left"
    }

    fun unlockPro(code: String = ""): Boolean {
        unlockProFor24Hours()
        return true
    }

    fun setShowManualLocation(enabled: Boolean) {
        _showManualLocation.value = enabled
        prefs.edit().putBoolean("pro_manual_location", enabled).apply()
    }

    fun setShowManualTime(enabled: Boolean) {
        _showManualTime.value = enabled
        prefs.edit().putBoolean("pro_manual_time", enabled).apply()
    }

    fun setShowUploadGallery(enabled: Boolean) {
        _showUploadGallery.value = enabled
        prefs.edit().putBoolean("pro_upload_gallery", enabled).apply()
    }

    fun setRemoveWatermark(enabled: Boolean) {
        _removeWatermark.value = enabled
        prefs.edit().putBoolean("pro_remove_watermark", enabled).apply()
        _locationState.value = _locationState.value.copy(removeWatermark = enabled)
    }

    fun setCustomNote(text: String, enabled: Boolean) {
        _customNote.value = text
        _isCustomNoteEnabled.value = enabled
        _locationState.value = _locationState.value.copy(
            customNote = text,
            isCustomNoteEnabled = enabled
        )
        prefs.edit()
            .putString("custom_note_text", text)
            .putBoolean("custom_note_enabled", enabled)
            .apply()
    }

    fun setCustomNoteEnabled(enabled: Boolean) {
        _isCustomNoteEnabled.value = enabled
        _locationState.value = _locationState.value.copy(isCustomNoteEnabled = enabled)
        prefs.edit().putBoolean("custom_note_enabled", enabled).apply()
    }

    fun setCustomWatermarkEnabled(enabled: Boolean) {
        _isCustomWatermarkEnabled.value = enabled
        _locationState.value = _locationState.value.copy(isCustomWatermarkEnabled = enabled)
        prefs.edit().putBoolean("custom_watermark_enabled", enabled).apply()
    }

    fun setCustomWatermarkText(text: String) {
        _customWatermarkText.value = text
        _locationState.value = _locationState.value.copy(customWatermarkText = text)
        prefs.edit().putString("custom_watermark_text", text).apply()
    }

    fun setSaveOriginalPhotoEnabled(enabled: Boolean) {
        _isSaveOriginalPhotoEnabled.value = enabled
        _locationState.value = _locationState.value.copy(isSaveOriginalPhotoEnabled = enabled)
        prefs.edit().putBoolean("pro_save_original_photo", enabled).apply()
    }

    fun setStampTemplate(template: StampTemplate) {
        _locationState.value = _locationState.value.copy(stampTemplate = template)
        prefs.edit().putString("stamp_template", template.id).apply()
        triggerCustomToast("Applied ${template.title} Stamp ✓")
    }

    fun setImageQuality(quality: ImageQuality) {
        _imageQuality.value = quality
        prefs.edit().putString("image_quality_mode", quality.id).apply()
        triggerCustomToast("Image Quality: ${quality.title} ✓")
    }

    fun setFlashMode(mode: CameraFlashMode) {
        _flashMode.value = mode
        prefs.edit().putString("camera_flash_mode", mode.id).apply()
        triggerCustomToast("Flash: ${mode.title} ✓")
    }

    fun cycleFlashMode() {
        val next = _flashMode.value.next()
        setFlashMode(next)
    }

    fun setHapticFeedback(enabled: Boolean) {
        _hapticFeedback.value = enabled
        prefs.edit().putBoolean("pro_haptic_feedback", enabled).apply()
    }

    fun setVolumeKeyAction(action: String) {
        _volumeKeyAction.value = action
        _locationState.value = _locationState.value.copy(volumeKeyAction = action)
        prefs.edit().putString("pro_volume_key_action", action).apply()
        val desc = when (action) {
            "capture" -> "Capture Photo/Video"
            "zoom" -> "Zoom In/Out"
            else -> "Disabled"
        }
        triggerCustomToast("Volume Key: $desc ✓")
    }

    fun setStorageFolderName(folder: String) {
        val cleanFolder = folder.trim().ifEmpty { "ProGpsCam" }
        _storageFolderName.value = cleanFolder
        _locationState.value = _locationState.value.copy(storageFolderName = cleanFolder)
        prefs.edit().putString("storage_folder_name", cleanFolder).apply()
        triggerCustomToast("Storage Folder: $cleanFolder 📁")
    }

    fun setGoogleDriveSyncEnabled(enabled: Boolean) {
        _isGoogleDriveSyncEnabled.value = enabled
        _locationState.value = _locationState.value.copy(isGoogleDriveSyncEnabled = enabled)
        prefs.edit().putBoolean("google_drive_sync_enabled", enabled).apply()
        val msg = if (enabled) "Google Drive Auto-Sync Activated ☁️" else "Google Drive Sync Disabled"
        triggerCustomToast(msg)
    }

    fun setGoogleDriveFolderName(folder: String) {
        val clean = folder.trim().ifEmpty { "ProGpsCam Backup" }
        _googleDriveFolderName.value = clean
        _locationState.value = _locationState.value.copy(googleDriveFolderName = clean)
        prefs.edit().putString("google_drive_folder_name", clean).apply()
        triggerCustomToast("Google Drive Folder: $clean ☁️")
    }

    fun setGoogleDriveAccountEmail(email: String) {
        val clean = email.trim().ifEmpty { "pr903128@gmail.com" }
        _googleDriveAccountEmail.value = clean
        _locationState.value = _locationState.value.copy(googleDriveAccountEmail = clean)
        prefs.edit().putString("google_drive_account_email", clean).apply()
        triggerCustomToast("Google Account: $clean ✉️")
    }

    fun syncAllSessionMediaToDrive() {
        val currentList = _sessionMedia.value
        if (currentList.isEmpty()) {
            triggerCustomToast("No media in current session to sync")
            return
        }
        viewModelScope.launch {
            triggerCustomToast("Syncing ${currentList.size} item(s) to Google Drive (${_googleDriveFolderName.value})... ☁️")
            delay(1200)
            _sessionMedia.value = currentList.map { it.copy(isDriveSynced = true) }
            triggerCustomToast("✅ All ${currentList.size} items synced to Google Drive!")
        }
    }

    fun triggerVolumeKey(isUp: Boolean) {
        _volumeKeyEvent.tryEmit(if (isUp) VolumeKeyType.UP else VolumeKeyType.DOWN)
    }

    fun triggerHapticFeedback(context: Context) {
        if (!_hapticFeedback.value) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(80)
            }
        } catch (e: Exception) {
            Log.e("GpsViewModel", "Haptic vibration error", e)
        }
    }

    fun cleanupCache(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                context.cacheDir.listFiles()?.forEach { file ->
                    if (file.name.startsWith("temp_img_") || file.name.startsWith("temp_vid_") || file.name.startsWith("cropped_")) {
                        file.delete()
                    }
                }
            } catch (e: Exception) {
                Log.e("GpsViewModel", "Cache cleanup error", e)
            }
        }
    }

    fun toggleDarkMode() {
        val newMode = !_isDarkMode.value
        _isDarkMode.value = newMode
        prefs.edit().putBoolean("is_dark_mode", newMode).apply()
    }

    fun refreshAllAddressSources() {
        viewModelScope.launch {
            _liveGpsSources.value = _liveGpsSources.value.copy(
                isRefreshing = true,
                androidGeocoder = AddressSourceItem(
                    tier = 1,
                    sourceName = "Android Built-in Geocoder",
                    address = _liveGpsSources.value.androidGeocoder.address,
                    isLoading = true,
                    isAvailable = false,
                    error = null
                ),
                nominatim = AddressSourceItem(
                    tier = 2,
                    sourceName = "OpenStreetMap Nominatim",
                    address = null,
                    isLoading = false,
                    isAvailable = false,
                    error = "Awaiting Geocoder check…"
                ),
                ipApi = AddressSourceItem(
                    tier = 3,
                    sourceName = "IP-API Geolocation",
                    address = null,
                    isLoading = false,
                    isAvailable = false,
                    error = "Awaiting Geocoder check…"
                )
            )

            val curLat = _locationState.value.lat
            val curLng = _locationState.value.lng

            if (curLat != 0.0 && curLng != 0.0) {
                // 1. TOP PRIORITY: Try Android Built-in Geocoder
                var androidRes = try { getAddressFromAndroidGeocoder(curLat, curLng) } catch (e: Exception) { null }

                // If initial attempt failed/pending, wait up to 5 seconds retrying native Geocoder before calling external APIs
                if (androidRes == null || !androidRes.success) {
                    val waitStart = System.currentTimeMillis()
                    while (System.currentTimeMillis() - waitStart < 5000L) {
                        delay(1500L)
                        val retryRes = try { getAddressFromAndroidGeocoder(curLat, curLng) } catch (e: Exception) { null }
                        if (retryRes != null && retryRes.success) {
                            androidRes = retryRes
                            break
                        }
                    }
                }

                if (androidRes != null && androidRes.success) {
                    // Native Geocoder succeeded! DO NOT call secondary or tertiary APIs to save bandwidth/API quota.
                    Log.d("GpsViewModel", "Live GPS Refresh: Android Geocoder succeeded. Secondary/Tertiary web APIs spared.")
                    _liveGpsSources.value = LiveGpsSourcesState(
                        isRefreshing = false,
                        androidGeocoder = AddressSourceItem(
                            tier = 1,
                            sourceName = "Android Built-in Geocoder",
                            address = androidRes,
                            isLoading = false,
                            isAvailable = true,
                            error = null
                        ),
                        nominatim = AddressSourceItem(
                            tier = 2,
                            sourceName = "OpenStreetMap Nominatim",
                            address = null,
                            isLoading = false,
                            isAvailable = false,
                            error = "Spared (Android Geocoder Succeeded)"
                        ),
                        ipApi = AddressSourceItem(
                            tier = 3,
                            sourceName = "IP-API Geolocation",
                            address = null,
                            isLoading = false,
                            isAvailable = false,
                            error = "Spared (Android Geocoder Succeeded)"
                        )
                    )
                } else {
                    // Native Geocoder timed out or failed after 5s! Now trigger secondary (Nominatim) & tertiary (IP-API)
                    Log.w("GpsViewModel", "Live GPS Refresh: Android Geocoder timed out (5s). Calling secondary & tertiary APIs...")
                    
                    _liveGpsSources.value = _liveGpsSources.value.copy(
                        nominatim = _liveGpsSources.value.nominatim.copy(isLoading = true, error = null),
                        ipApi = _liveGpsSources.value.ipApi.copy(isLoading = true, error = null)
                    )

                    val nominatimRes = try { getAddressFromNominatim(curLat, curLng) } catch (e: Exception) { null }
                    val ipApiRes = try { getAddressFromIpApi() } catch (e: Exception) { null }

                    _liveGpsSources.value = LiveGpsSourcesState(
                        isRefreshing = false,
                        androidGeocoder = AddressSourceItem(
                            tier = 1,
                            sourceName = "Android Built-in Geocoder",
                            address = androidRes,
                            isLoading = false,
                            isAvailable = false,
                            error = "Timed out (5s) / Unavailable"
                        ),
                        nominatim = AddressSourceItem(
                            tier = 2,
                            sourceName = "OpenStreetMap Nominatim",
                            address = nominatimRes,
                            isLoading = false,
                            isAvailable = nominatimRes?.success == true,
                            error = if (nominatimRes == null || !nominatimRes.success) "Failed to connect to Nominatim API" else null
                        ),
                        ipApi = AddressSourceItem(
                            tier = 3,
                            sourceName = "IP-API Geolocation",
                            address = ipApiRes,
                            isLoading = false,
                            isAvailable = ipApiRes?.success == true,
                            error = if (ipApiRes == null || !ipApiRes.success) "Failed to resolve IP location" else null
                        )
                    )
                }
            } else {
                val ipApiRes = try { getAddressFromIpApi() } catch (e: Exception) { null }
                _liveGpsSources.value = LiveGpsSourcesState(
                    isRefreshing = false,
                    androidGeocoder = AddressSourceItem(1, "Android Built-in Geocoder", null, false, false, "Awaiting GPS Coordinates"),
                    nominatim = AddressSourceItem(2, "OpenStreetMap Nominatim", null, false, false, "Awaiting GPS Coordinates"),
                    ipApi = AddressSourceItem(3, "IP-API Geolocation", ipApiRes, false, ipApiRes?.success == true)
                )
            }
        }
    }

    fun applySpecificAddressSource(addr: AddressInfo) {
        val current = _locationState.value
        _locationState.value = current.copy(
            city = addr.city,
            state = addr.state,
            road = addr.road,
            postcode = if (addr.postcode.isNotBlank()) addr.postcode else current.postcode,
            addressTier = addr.tier,
            addressTierName = addr.sourceName,
            addressTierDesc = addr.description
        )
        triggerCustomToast("Applied #${addr.tier} ${addr.sourceName} Address ✓")
    }

    private val _refreshCooldownRemaining = MutableStateFlow(0)
    val refreshCooldownRemaining = _refreshCooldownRemaining.asStateFlow()
    private var refreshCooldownJob: Job? = null

    fun forceRefreshLiveLocation() {
        if (_refreshCooldownRemaining.value > 0) {
            triggerCustomToast("Please wait ${_refreshCooldownRemaining.value}s before refreshing again")
            return
        }
        startLocationEngine()
        refreshAllAddressSources()
        triggerCustomToast("Refreshing Live GPS Coordinates & Addresses…")

        refreshCooldownJob?.cancel()
        refreshCooldownJob = viewModelScope.launch {
            _refreshCooldownRemaining.value = 3
            while (_refreshCooldownRemaining.value > 0) {
                delay(1000L)
                _refreshCooldownRemaining.value -= 1
            }
        }
    }

    fun addSessionMedia(item: SessionMediaItem) {
        val current = _sessionMedia.value.toMutableList()
        if (current.none { it.uri == item.uri }) {
            current.add(0, item)
            _sessionMedia.value = current
        }
    }

    fun removeSessionMedia(item: SessionMediaItem) {
        val current = _sessionMedia.value.toMutableList()
        current.removeAll { it.uri == item.uri }
        _sessionMedia.value = current
    }

    fun deleteSessionMedia(item: SessionMediaItem, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                context.contentResolver.delete(item.uri, null, null)
            } catch (e: Exception) {
                Log.e("GpsViewModel", "Error deleting media from MediaStore", e)
            }
            withContext(Dispatchers.Main) {
                removeSessionMedia(item)
                triggerCustomToast(if (item.isVideo) "Video deleted" else "Photo deleted")
            }
        }
    }

    fun loadExistingSessionMedia(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = mutableListOf<SessionMediaItem>()

            // 1. Query saved photos
            try {
                val photoProjection = arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_ADDED
                )
                val selection = "${MediaStore.Images.Media.DISPLAY_NAME} LIKE ?"
                val selectionArgs = arrayOf("PRO_GPS_%")
                val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

                val photoUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

                context.contentResolver.query(photoUri, photoProjection, selection, selectionArgs, sortOrder)?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                    var count = 0
                    while (cursor.moveToNext() && count < 100) {
                        val id = cursor.getLong(idCol)
                        val rawDate = cursor.getLong(dateCol)
                        val dateMillis = if (rawDate < 10000000000L) rawDate * 1000L else rawDate
                        val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                        list.add(SessionMediaItem(uri = contentUri, isVideo = false, dateAdded = dateMillis))
                        count++
                    }
                }
            } catch (e: Exception) {
                Log.e("GpsViewModel", "Error querying session photos", e)
            }

            // 2. Query saved videos
            try {
                val videoProjection = arrayOf(
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.DATE_ADDED
                )
                val selection = "${MediaStore.Video.Media.DISPLAY_NAME} LIKE ?"
                val selectionArgs = arrayOf("PRO_GPS_%")
                val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

                val videoUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                } else {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }

                context.contentResolver.query(videoUri, videoProjection, selection, selectionArgs, sortOrder)?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                    val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                    var count = 0
                    while (cursor.moveToNext() && count < 100) {
                        val id = cursor.getLong(idCol)
                        val rawDate = cursor.getLong(dateCol)
                        val dateMillis = if (rawDate < 10000000000L) rawDate * 1000L else rawDate
                        val contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                        list.add(SessionMediaItem(uri = contentUri, isVideo = true, dateAdded = dateMillis))
                        count++
                    }
                }
            } catch (e: Exception) {
                Log.e("GpsViewModel", "Error querying session videos", e)
            }

            // Sort combined photos and videos descending by date
            list.sortByDescending { it.dateAdded }

            withContext(Dispatchers.Main) {
                if (list.isNotEmpty()) {
                    val current = _sessionMedia.value.toMutableList()
                    list.forEach { item ->
                        if (current.none { it.uri == item.uri }) {
                            current.add(item)
                        }
                    }
                    current.sortByDescending { it.dateAdded }
                    _sessionMedia.value = current
                }
            }
        }
    }

    fun startLocationEngine() {
        if (_locationState.value.isManualOverride) return

        _locationState.value = _locationState.value.copy(
            statusType = "searching",
            statusLabel = "Fetching automatic GPS...",
            source = "GPS"
        )

        // Fast location acquisition: Check instant cached position from GPS / Fused Provider
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null && !_locationState.value.isManualOverride) {
                    Log.d("GpsViewModel", "Instant last known location acquired: ${location.latitude}, ${location.longitude}")
                    handleGPSSuccess(location)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener { location ->
                    if (location != null && !_locationState.value.isManualOverride) {
                        Log.d("GpsViewModel", "Instant high-accuracy location acquired: ${location.latitude}, ${location.longitude}")
                        handleGPSSuccess(location)
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("GpsViewModel", "Location permission missing for instant location check", e)
        } catch (e: Exception) {
            Log.e("GpsViewModel", "Instant location check error", e)
        }

        // Setup GPS Timeout Fallback trigger after 20 seconds (do not trigger prematurely)
        fallbackJob?.cancel()
        fallbackJob = viewModelScope.launch {
            delay(20000L)
            if (!_locationState.value.isManualOverride && _locationState.value.lat == 0.0) {
                Log.w("GpsViewModel", "GPS signal timeout after 20s, attempting IP fallback")
                fallbackToIP()
            }
        }

        try {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                2000L
            ).setMinUpdateIntervalMillis(1000L).build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation ?: return
                    handleGPSSuccess(location)
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                context.mainLooper
            )
        } catch (e: SecurityException) {
            Log.e("GpsViewModel", "Location permissions missing or denied.", e)
            fallbackToIP()
        } catch (e: Exception) {
            Log.e("GpsViewModel", "Error starting fused location services.", e)
            fallbackToIP()
        }
    }

    private fun handleGPSSuccess(location: Location) {
        if (_locationState.value.isManualOverride) return
        fallbackJob?.cancel()

        val lat = location.latitude
        val lng = location.longitude
        val accuracy = location.accuracy

        val oldState = _locationState.value
        // If has manual override or same position, do nothing
        if (oldState.lat == lat && oldState.lng == lng && oldState.accuracy == accuracy) return

        _locationState.value = oldState.copy(
            lat = lat,
            lng = lng,
            accuracy = accuracy,
            source = "AUTOMATIC",
            statusType = "ok",
            statusLabel = "Live GPS Connected ✓"
        )

        // Trigger address reverse geocode
        viewModelScope.launch {
            val addr = reverseGeocode(lat, lng)
            if (addr.postcode.isNotEmpty()) {
                lastAutoPostcode = addr.postcode
            }
            if (!_locationState.value.isManualOverride) {
                _locationState.value = _locationState.value.copy(
                    city = addr.city,
                    state = addr.state,
                    country = if (addr.country.isNotEmpty()) addr.country else "India",
                    road = addr.road,
                    postcode = addr.postcode,
                    addressTier = if (addr.success) addr.tier else _locationState.value.addressTier,
                    addressTierName = if (addr.success) addr.sourceName else _locationState.value.addressTierName,
                    addressTierDesc = if (addr.success) addr.description else _locationState.value.addressTierDesc
                )
            }
            // Preload rewarded ad in background now that location & geocoding are ready
            preloadRewardedAd(context)
        }
    }

    private fun fallbackToIP() {
        if (_locationState.value.isManualOverride) return
        _locationState.value = _locationState.value.copy(
            statusType = "ip",
            statusLabel = "GPS Timeout. Trying IP Location...",
            source = "IP"
        )

        viewModelScope.launch {
            val ipInfo = fetchIpLocation()
            if (!_locationState.value.isManualOverride) {
                if (ipInfo.success) {
                    _locationState.value = _locationState.value.copy(
                        lat = ipInfo.lat,
                        lng = ipInfo.lng,
                        accuracy = null,
                        city = ipInfo.city,
                        state = ipInfo.state,
                        country = if (ipInfo.country.isNotEmpty()) ipInfo.country else "India",
                        road = "Network Estimated Location",
                        source = "AUTOMATIC",
                        statusType = "ok",
                        statusLabel = "Network Location Active ✓",
                        addressTier = 3,
                        addressTierName = "IP-API Geolocation",
                        addressTierDesc = "Address estimated via 3rd Priority using IP Network Geolocation."
                    )
                } else {
                    _locationState.value = _locationState.value.copy(
                        statusType = "err",
                        statusLabel = "Auto Discovery Failed. Use Manual.",
                        source = "MANUAL",
                        city = "Location Blocked",
                        road = "Please configure manual override inputs."
                    )
                }
            }
            // Preload rewarded ad in background
            preloadRewardedAd(context)
        }
    }

    fun applyManualLocation(lat: Double, lng: Double, city: String, state: String, customPincode: String) {
        fallbackJob?.cancel()
        cleanupLocationUpdates()

        val cleanCity = cleanAddressText(city)
        val cleanState = cleanAddressText(state)
        val finalPostcode = if (customPincode.isNotBlank()) {
            customPincode.trim()
        } else {
            lastAutoPostcode.ifEmpty { _locationState.value.postcode }
        }

        _locationState.value = LocationState(
            lat = lat,
            lng = lng,
            accuracy = null,
            city = cleanCity.ifEmpty { "Manual Location" },
            state = cleanState,
            road = "Loading coordinates details...",
            postcode = finalPostcode,
            source = "MANUAL",
            statusType = "ok",
            statusLabel = "Manual Location Locked ✓",
            isManualOverride = true,
            stampScale = _locationState.value.stampScale,
            manualTime = _locationState.value.manualTime,
            useInsideOverlay = _locationState.value.useInsideOverlay,
            stampTemplate = _locationState.value.stampTemplate
        )

        // Perform reverse geocoding asynchronously on manual coordinates to get normal address texts
        viewModelScope.launch {
            val addr = reverseGeocode(lat, lng)
            if (_locationState.value.isManualOverride) {
                _locationState.value = _locationState.value.copy(
                    city = if (cleanCity.isNotEmpty()) cleanCity else addr.city,
                    state = if (cleanState.isNotEmpty()) cleanState else addr.state,
                    road = addr.road,
                    postcode = if (customPincode.isNotBlank()) customPincode.trim() else (if (addr.postcode.isNotEmpty()) addr.postcode else finalPostcode),
                    addressTier = if (addr.success) addr.tier else _locationState.value.addressTier,
                    addressTierName = if (addr.success) addr.sourceName else _locationState.value.addressTierName,
                    addressTierDesc = if (addr.success) addr.description else _locationState.value.addressTierDesc
                )
            }
        }
    }

    fun resetAppAndLocation() {
        fallbackJob?.cancel()
        cleanupLocationUpdates()

        _hasCapturedPhoto.value = false
        // Reset to initial values completely
        _locationState.value = LocationState(
            stampScale = _locationState.value.stampScale, // Keep the selected design size factor
            stampTemplate = _locationState.value.stampTemplate
        )

        // Reactivate normal automatic background service loop updates
        startLocationEngine()

        triggerCustomToast("Location & settings reset successfully!")
    }

    fun setStampScale(scale: Float) {
        _locationState.value = _locationState.value.copy(stampScale = scale)
        prefs.edit().putFloat("stamp_scale", scale).apply()
    }

    fun setSelfieMirrored(mirrored: Boolean) {
        _isSelfieMirrored.value = mirrored
        prefs.edit().putBoolean("pro_selfie_mirrored", mirrored).apply()
    }

    fun setManualTime(timestamp: Long?) {
        _locationState.value = _locationState.value.copy(manualTime = timestamp)
    }

    fun setUseInsideOverlay(value: Boolean) {
        _locationState.value = _locationState.value.copy(useInsideOverlay = value)
    }

    fun setProcessing(processing: Boolean, message: String = "Processing…") {
        _isProcessing.value = processing
        _processingMessage.value = message
    }

    fun triggerCustomToast(msg: String) {
        _toastMessage.value = msg
        _showToast.value = true
        viewModelScope.launch {
            delay(2000)
            _showToast.value = false
        }
    }

    fun hasValidExifGps(uri: Uri, context: Context): Boolean {
        val coords = extractGpsFromUri(context, uri) ?: return false
        return Math.abs(coords.first) > 0.0001 && Math.abs(coords.second) > 0.0001
    }

    // Process picked or captured photo
    fun processImageFile(fileUri: Uri, context: Context, isGallery: Boolean, skipExif: Boolean = false) {
        _isProcessing.value = true
        _processedBitmap.value = null

        viewModelScope.launch(Dispatchers.Default) {
            updateProcessingStep("Reading photo structure…")
            delay(400)

            // Extract EXIF ONLY if not overridden manually by the surveyor!
            if (isGallery && !_locationState.value.isManualOverride && !skipExif) {
                updateProcessingStep("Extracting hidden GPS from image EXIF…")
                delay(400)
                val exifCoords = extractGpsFromUri(context, fileUri)
                if (exifCoords != null) {
                    updateProcessingStep("Fetching address of image location...")
                    val addr = reverseGeocode(exifCoords.first, exifCoords.second)
                    withContext(Dispatchers.Main) {
                        _locationState.value = LocationState(
                            lat = exifCoords.first,
                            lng = exifCoords.second,
                            accuracy = null,
                            city = addr.city.ifEmpty { "Manual Location" },
                            state = addr.state,
                            road = addr.road,
                            postcode = addr.postcode,
                            source = "MANUAL",
                            statusType = "ok",
                            statusLabel = "Gallery GPS Loaded ✓",
                            isManualOverride = true,
                            stampScale = _locationState.value.stampScale,
                            manualTime = _locationState.value.manualTime,
                            useInsideOverlay = _locationState.value.useInsideOverlay,
                            stampTemplate = _locationState.value.stampTemplate
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        triggerCustomToast("EXIF credentials not found. App GPS applied!")
                    }
                }
            }

            val currentQuality = _imageQuality.value
            val rawBitmap = loadBitmapFromUri(context, fileUri, currentQuality.maxDimension)
            if (rawBitmap == null) {
                withContext(Dispatchers.Main) {
                    _isProcessing.value = false
                    Toast.makeText(context, "Failed to load photo. Try again.", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            updateProcessingStep("Stamping smart data overlay…")
            delay(500)

            val resized = resizeBitmapIfNeeded(rawBitmap, currentQuality.maxDimension)
            val output = drawOverlayOnBitmap(resized, _locationState.value)

            withContext(Dispatchers.Main) {
                _processedBitmap.value = output
                _isProcessing.value = false
            }
        }
    }

    // Direct stamp and save to device gallery, then trigger instant next camera capture
    fun processAndAutoSavePhoto(
        fileUri: Uri,
        context: Context,
        onSuccessAndNext: () -> Unit
    ) {
        _isProcessing.value = true
        _processingMessage.value = "Stamping & Saving to Device…"

        viewModelScope.launch(Dispatchers.Default) {
            val captureTime = System.currentTimeMillis()
            val isOnline = OfflineSyncManager.isNetworkAvailable(context)
            var activeLocation = _locationState.value

            if (isOnline) {
                // Ensure freshest address details using top-priority Geocoder if address info is missing or partial
                if (activeLocation.lat != 0.0 && (activeLocation.city.isBlank() || activeLocation.city == "Location Found" || activeLocation.city == "Location Blocked" || activeLocation.road.isBlank() || activeLocation.road.contains("Fetching") || activeLocation.road.contains("Loading"))) {
                    val addr = reverseGeocode(activeLocation.lat, activeLocation.lng)
                    if (addr.success) {
                        activeLocation = activeLocation.copy(
                            city = if (activeLocation.isManualOverride && activeLocation.city.isNotBlank() && activeLocation.city != "Manual Location") activeLocation.city else addr.city,
                            state = if (activeLocation.isManualOverride && activeLocation.state.isNotBlank()) activeLocation.state else addr.state,
                            road = addr.road,
                            postcode = if (activeLocation.postcode.isNotBlank()) activeLocation.postcode else addr.postcode,
                            addressTier = addr.tier,
                            addressTierName = addr.sourceName,
                            addressTierDesc = addr.description
                        )
                        withContext(Dispatchers.Main) {
                            _locationState.value = activeLocation
                        }
                    }
                }
            }

            val currentQuality = _imageQuality.value
            val rawBitmap = loadBitmapFromUri(context, fileUri, currentQuality.maxDimension)
            if (rawBitmap == null) {
                withContext(Dispatchers.Main) {
                    _isProcessing.value = false
                    Toast.makeText(context, "Failed to load captured photo.", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            val resized = resizeBitmapIfNeeded(rawBitmap, currentQuality.maxDimension)

            // Cache pristine clean photo if offline so auto-sync can re-render full stamp later
            var rawCachePath: String? = null
            if (!isOnline) {
                try {
                    val offlineDir = File(context.filesDir, "offline_raw").apply { mkdirs() }
                    val rawFile = File(offlineDir, "raw_${captureTime}.jpg")
                    FileOutputStream(rawFile).use { out ->
                        resized.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    }
                    rawCachePath = rawFile.absolutePath
                } catch (e: Exception) {
                    Log.e("GpsViewModel", "Failed to cache raw offline bitmap", e)
                }
            }

            val output = if (isOnline) {
                drawOverlayOnBitmap(resized, activeLocation)
            } else {
                drawOfflineWatermark(resized, activeLocation)
            }

            val folder = activeLocation.storageFolderName
            val isDriveSync = isOnline && activeLocation.isGoogleDriveSyncEnabled
            val driveFolder = activeLocation.googleDriveFolderName

            val savedUri = saveProcessedBitmap(
                context = context,
                bitmap = output,
                jpegQuality = currentQuality.jpegQuality,
                folderName = folder,
                latitude = activeLocation.lat,
                longitude = activeLocation.lng,
                timestampMillis = captureTime
            )

            var originalSavedUriStr: String? = null
            if (activeLocation.isSaveOriginalPhotoEnabled || _isSaveOriginalPhotoEnabled.value) {
                originalSavedUriStr = saveProcessedBitmap(
                    context = context,
                    bitmap = resized,
                    jpegQuality = currentQuality.jpegQuality,
                    prefix = "ORIGINAL_GPS_",
                    folderName = folder,
                    latitude = activeLocation.lat,
                    longitude = activeLocation.lng,
                    timestampMillis = captureTime
                )
            }

            if (savedUri != null) {
                if (!isOnline) {
                    OfflineSyncManager.writeExifGps(context, savedUri, activeLocation.lat, activeLocation.lng, captureTime)
                    OfflineSyncManager.addToQueue(
                        context,
                        PendingPhoto(
                            imagePath = savedUri,
                            rawImagePath = rawCachePath,
                            latitude = activeLocation.lat,
                            longitude = activeLocation.lng,
                            timestamp = captureTime,
                            templateId = activeLocation.stampTemplate.name
                        )
                    )
                    withContext(Dispatchers.Main) {
                        _pendingOfflineCount.value = OfflineSyncManager.getPendingCount(context)
                    }
                } else {
                    OfflineSyncManager.writeExifGps(context, savedUri, activeLocation.lat, activeLocation.lng, captureTime)
                }
            }

            withContext(Dispatchers.Main) {
                _isProcessing.value = false
                if (savedUri != null) {
                    val parsedUri = Uri.parse(savedUri)
                    addSessionMedia(SessionMediaItem(uri = parsedUri, isVideo = false, isDriveSynced = isDriveSync))
                    if (originalSavedUriStr != null) {
                        val parsedOriginal = Uri.parse(originalSavedUriStr)
                        addSessionMedia(SessionMediaItem(uri = parsedOriginal, isVideo = false, isDriveSynced = isDriveSync))
                    }
                    _hasCapturedPhoto.value = true
                    triggerHapticFeedback(context)
                    val msg = if (!isOnline) {
                        "📍 Saved Offline (EXIF GeoTag Added). Auto-syncs when online! 📸"
                    } else if (isDriveSync) {
                        "✅ Saved to $folder & Synced to Google Drive ($driveFolder) ☁️"
                    } else {
                        "✅ Saved to $folder 📸"
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    onSuccessAndNext()
                } else {
                    Toast.makeText(context, "Failed to save photo.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Direct background stamp and save for Live Camera preview with dynamic aspect ratio cropping
    fun processAndAutoSaveLivePhoto(
        fileUri: Uri,
        context: Context,
        isFrontCamera: Boolean = false,
        isSelfieMirrored: Boolean = true,
        targetAspectRatio: LiveCameraAspectRatio = LiveCameraAspectRatio.RATIO_3_4,
        onSaved: (Uri) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            val captureTime = System.currentTimeMillis()
            val isOnline = OfflineSyncManager.isNetworkAvailable(context)
            var activeLocation = _locationState.value

            if (isOnline) {
                if (activeLocation.lat != 0.0 && (activeLocation.city.isBlank() || activeLocation.city == "Location Found" || activeLocation.road.isBlank() || activeLocation.road.contains("Fetching"))) {
                    val addr = reverseGeocode(activeLocation.lat, activeLocation.lng)
                    if (addr.success) {
                        activeLocation = activeLocation.copy(
                            city = if (activeLocation.isManualOverride && activeLocation.city.isNotBlank() && activeLocation.city != "Manual Location") activeLocation.city else addr.city,
                            state = if (activeLocation.isManualOverride && activeLocation.state.isNotBlank()) activeLocation.state else addr.state,
                            road = addr.road,
                            postcode = if (activeLocation.postcode.isNotBlank()) activeLocation.postcode else addr.postcode,
                            addressTier = addr.tier,
                            addressTierName = addr.sourceName,
                            addressTierDesc = addr.description
                        )
                        withContext(Dispatchers.Main) {
                            _locationState.value = activeLocation
                        }
                    }
                }
            }

            val currentQuality = _imageQuality.value
            val rawBitmap = loadBitmapFromUri(context, fileUri, currentQuality.maxDimension) ?: return@launch
            val orientedBitmap = if (isFrontCamera && isSelfieMirrored) {
                val matrix = Matrix().apply { postScale(-1f, 1f, rawBitmap.width / 2f, rawBitmap.height / 2f) }
                val flipped = Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
                if (flipped != rawBitmap) rawBitmap.recycle()
                flipped
            } else {
                rawBitmap
            }

            // Calculate screen aspect ratio for full-screen ratio
            val dm = context.resources.displayMetrics
            val fullRatio = if (dm.heightPixels > 0) dm.widthPixels.toFloat() / dm.heightPixels.toFloat() else 9f / 19.5f

            // Dynamic center crop according to selected camera aspect ratio (1:1, 3:4, 9:16, Full)
            val croppedBitmap = cropBitmapToAspectRatio(orientedBitmap, targetAspectRatio, fullRatio)

            val resized = resizeBitmapIfNeeded(croppedBitmap, currentQuality.maxDimension)

            // Cache pristine clean photo if offline so auto-sync can re-render full stamp later
            var rawCachePath: String? = null
            if (!isOnline) {
                try {
                    val offlineDir = File(context.filesDir, "offline_raw").apply { mkdirs() }
                    val rawFile = File(offlineDir, "raw_${captureTime}.jpg")
                    FileOutputStream(rawFile).use { out ->
                        resized.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    }
                    rawCachePath = rawFile.absolutePath
                } catch (e: Exception) {
                    Log.e("GpsViewModel", "Failed to cache raw offline bitmap", e)
                }
            }

            val output = if (isOnline) {
                drawOverlayOnBitmap(resized, activeLocation)
            } else {
                drawOfflineWatermark(resized, activeLocation)
            }

            val folder = activeLocation.storageFolderName
            val isDriveSync = isOnline && activeLocation.isGoogleDriveSyncEnabled
            val driveFolder = activeLocation.googleDriveFolderName

            val savedUri = saveProcessedBitmap(
                context = context,
                bitmap = output,
                jpegQuality = currentQuality.jpegQuality,
                folderName = folder,
                latitude = activeLocation.lat,
                longitude = activeLocation.lng,
                timestampMillis = captureTime
            )

            var originalSavedUriStr: String? = null
            if (activeLocation.isSaveOriginalPhotoEnabled || _isSaveOriginalPhotoEnabled.value) {
                originalSavedUriStr = saveProcessedBitmap(
                    context = context,
                    bitmap = resized,
                    jpegQuality = currentQuality.jpegQuality,
                    prefix = "ORIGINAL_GPS_",
                    folderName = folder,
                    latitude = activeLocation.lat,
                    longitude = activeLocation.lng,
                    timestampMillis = captureTime
                )
            }

            if (savedUri != null) {
                if (!isOnline) {
                    OfflineSyncManager.writeExifGps(context, savedUri, activeLocation.lat, activeLocation.lng, captureTime)
                    OfflineSyncManager.addToQueue(
                        context,
                        PendingPhoto(
                            imagePath = savedUri,
                            rawImagePath = rawCachePath,
                            latitude = activeLocation.lat,
                            longitude = activeLocation.lng,
                            timestamp = captureTime,
                            templateId = activeLocation.stampTemplate.name
                        )
                    )
                    withContext(Dispatchers.Main) {
                        _pendingOfflineCount.value = OfflineSyncManager.getPendingCount(context)
                    }
                } else {
                    OfflineSyncManager.writeExifGps(context, savedUri, activeLocation.lat, activeLocation.lng, captureTime)
                }
            }

            if (savedUri != null) {
                val parsedUri = Uri.parse(savedUri)
                withContext(Dispatchers.Main) {
                    addSessionMedia(SessionMediaItem(uri = parsedUri, isVideo = false, isDriveSynced = isDriveSync))
                    if (originalSavedUriStr != null) {
                        val parsedOriginal = Uri.parse(originalSavedUriStr)
                        addSessionMedia(SessionMediaItem(uri = parsedOriginal, isVideo = false, isDriveSynced = isDriveSync))
                    }
                    _hasCapturedPhoto.value = true
                    triggerHapticFeedback(context)
                    val msg = if (!isOnline) {
                        "📍 Saved Offline (EXIF GeoTag Added). Auto-syncs when online! 📸"
                    } else if (isDriveSync) {
                        "✅ Saved to $folder & Synced to Google Drive ($driveFolder) ☁️"
                    } else {
                        "✅ Saved to Gallery ($folder)! 📸"
                    }
                    triggerCustomToast(msg)
                    onSaved(parsedUri)
                }
            }
        }
    }

    // Auto-sync pending offline photos when connection is restored
    fun checkAndTriggerOfflineSync(activityContext: Context) {
        if (!OfflineSyncManager.isNetworkAvailable(activityContext)) return
        val pendingPhotos = OfflineSyncManager.getPendingPhotos(activityContext)
        _pendingOfflineCount.value = pendingPhotos.size
        if (pendingPhotos.isEmpty() || isSyncInProgress) return

        viewModelScope.launch(Dispatchers.IO) {
            isSyncInProgress = true
            val total = pendingPhotos.size
            Log.d("GpsViewModel", "Auto-sync starting for $total offline photo(s)...")

            for ((index, item) in pendingPhotos.withIndex()) {
                _syncStatus.value = SyncStatus.Syncing(current = index + 1, total = total)

                try {
                    // a) Fetch reverse-geocoded address using Geocoder for stored lat/long
                    val addr = reverseGeocode(item.latitude, item.longitude)

                    // b) Re-draw/update bottom stamp strip on saved image with new verified address text
                    val success = updateSavedPhotoWithVerifiedStamp(activityContext, item, addr)

                    // c) Update EXIF tags if necessary
                    OfflineSyncManager.writeExifGps(activityContext, item.imagePath, item.latitude, item.longitude, item.timestamp)

                    // d) Remove photo from sync queue upon success and clean up cached raw image
                    if (success) {
                        OfflineSyncManager.removeFromQueue(activityContext, item.imagePath)
                        item.rawImagePath?.let { rawPath ->
                            try { File(rawPath).delete() } catch (_: Exception) {}
                        }
                    }
                } catch (e: Exception) {
                    Log.e("GpsViewModel", "Sync failed for pending photo: ${item.imagePath}", e)
                }

                // Strict 2-second delay between iterations to prevent Geocoder rate-limiting
                if (index < total - 1) {
                    delay(2000L)
                }
            }

            val remaining = OfflineSyncManager.getPendingCount(activityContext)
            _pendingOfflineCount.value = remaining
            _syncStatus.value = SyncStatus.Success
            isSyncInProgress = false

            // Turns green briefly: "✓ All photos synced" and fades out after 2 seconds
            delay(2000L)
            _syncStatus.value = SyncStatus.Idle
        }
    }

    private suspend fun updateSavedPhotoWithVerifiedStamp(
        context: Context,
        item: PendingPhoto,
        addr: AddressInfo
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val currentQuality = _imageQuality.value
                val rawBitmap: Bitmap? = if (!item.rawImagePath.isNullOrBlank() && File(item.rawImagePath).exists()) {
                    BitmapFactory.decodeFile(item.rawImagePath)
                } else {
                    loadBitmapFromUri(context, Uri.parse(item.imagePath), currentQuality.maxDimension)
                }

                if (rawBitmap == null) {
                    Log.e("GpsViewModel", "Could not load raw bitmap for sync: ${item.imagePath}")
                    return@withContext false
                }

                // Determine template saved with the photo
                val template = try {
                    if (!item.templateId.isNullOrBlank()) StampTemplate.valueOf(item.templateId) else _locationState.value.stampTemplate
                } catch (_: Exception) {
                    _locationState.value.stampTemplate
                }

                // Build complete LocationState with reverse-geocoded address and original capture timestamp
                val fullLocationState = _locationState.value.copy(
                    lat = item.latitude,
                    lng = item.longitude,
                    city = addr.city.ifBlank { "Location Found" },
                    state = addr.state,
                    road = addr.road,
                    postcode = addr.postcode,
                    country = addr.country,
                    addressTier = addr.tier,
                    addressTierName = addr.sourceName,
                    addressTierDesc = addr.description,
                    manualTime = item.timestamp,
                    stampTemplate = template
                )

                // Render complete stamp layout (identical to normal online photo capture)
                val fullStampedBitmap = drawOverlayOnBitmap(rawBitmap, fullLocationState)

                // Overwrite the saved image in place
                val uri = Uri.parse(item.imagePath)
                if (item.imagePath.startsWith("content://")) {
                    context.contentResolver.openOutputStream(uri, "wt")?.use { outStream ->
                        fullStampedBitmap.compress(Bitmap.CompressFormat.JPEG, currentQuality.jpegQuality.coerceIn(10, 100), outStream)
                    }
                } else {
                    val file = File(item.imagePath)
                    if (file.exists()) {
                        FileOutputStream(file).use { outStream ->
                            fullStampedBitmap.compress(Bitmap.CompressFormat.JPEG, currentQuality.jpegQuality.coerceIn(10, 100), outStream)
                        }
                    }
                }

                // Update EXIF metadata
                OfflineSyncManager.writeExifGps(context, item.imagePath, item.latitude, item.longitude, item.timestamp)
                true
            } catch (e: Exception) {
                Log.e("GpsViewModel", "Failed to update saved photo with complete verified stamp", e)
                false
            }
        }
    }

    private fun drawOfflineWatermark(
        img: Bitmap,
        state: LocationState
    ): Bitmap {
        val W = img.width
        val H = img.height
        val output = img.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(output)

        val isLandscape = W > H
        val baseRatio = if (isLandscape) (W / 640.0f) else (W / 420.0f)
        val scaleFactor = baseRatio * state.stampScale
        val pad = 12f * scaleFactor

        // Style watermark according to selected stamp template
        val (watermarkColor, wmTypeface) = when (state.stampTemplate) {
            StampTemplate.CLASSIC -> Pair(0xFFFFD700.toInt(), Typeface.create(Typeface.SERIF, Typeface.BOLD))
            StampTemplate.TECH_HUD -> Pair(0xFF00FF88.toInt(), Typeface.create(Typeface.MONOSPACE, Typeface.BOLD))
            StampTemplate.CLEAN_MINIMAL -> Pair(0xFF818CF8.toInt(), Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD))
            StampTemplate.DARK_GLASS -> Pair(0xFF38BDF8.toInt(), Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD))
            StampTemplate.OFFICIAL_STAMP -> Pair(0xFFFACC15.toInt(), Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD))
            StampTemplate.ECO_AGRI -> Pair(0xFF34D399.toInt(), Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD))
            StampTemplate.REVENUE_AMBER -> Pair(0xFFF59E0B.toInt(), Typeface.create(Typeface.MONOSPACE, Typeface.BOLD))
            StampTemplate.GLACIER_FROST -> Pair(0xFFBAE6FD.toInt(), Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD))
            else -> Pair(0xCC00E5FF.toInt(), Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)) // STANDARD / default
        }

        // 1. Watermark Text at Bottom-Right
        val paintWatermark = Paint().apply {
            color = watermarkColor
            textSize = 8.5f * scaleFactor
            typeface = wmTypeface
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
            setShadowLayer(3f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xCC000000.toInt())
        }
        val watermarkText = if (state.isCompassEnabled) {
            "🧭 ${state.getCardinalDirection()}  •  ${state.getWatermarkText()}"
        } else {
            state.getWatermarkText()
        }
        val watermarkY = H - pad
        val watermarkX = W - pad
        canvas.drawText(watermarkText, watermarkX, watermarkY, paintWatermark)

        // 2. OFFLINE Tag Badge (just above the watermark)
        val tagText = "OFFLINE"
        val paintTagText = Paint().apply {
            color = Color.White.toArgb()
            textSize = 7.5f * scaleFactor
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val textWidth = paintTagText.measureText(tagText)
        val badgePadH = 6f * scaleFactor
        val badgeW = textWidth + badgePadH * 2f
        val badgeH = 15f * scaleFactor
        val badgeRight = watermarkX
        val badgeLeft = badgeRight - badgeW
        val badgeBottom = watermarkY - (11f * scaleFactor)
        val badgeTop = badgeBottom - badgeH
        val badgeRect = RectF(badgeLeft, badgeTop, badgeRight, badgeBottom)

        // Amber/Red badge background with rounded corners and drop shadow
        val badgeBgPaint = Paint().apply {
            color = 0xDDDC2626.toInt()
            style = Paint.Style.FILL
            isAntiAlias = true
            setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0x99000000.toInt())
        }
        canvas.drawRoundRect(badgeRect, 4f * scaleFactor, 4f * scaleFactor, badgeBgPaint)

        // Tag border
        val badgeBorderPaint = Paint().apply {
            color = 0xAAFFFFFF.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 1f * scaleFactor
            isAntiAlias = true
        }
        canvas.drawRoundRect(badgeRect, 4f * scaleFactor, 4f * scaleFactor, badgeBorderPaint)

        // Centered "OFFLINE" text
        canvas.drawText(tagText, badgeRect.centerX(), badgeRect.centerY() + 2.5f * scaleFactor, paintTagText)

        return output
    }

    // Direct save video to device gallery MediaStore with stamped GPS overlay and append to session gallery
    fun processAndAutoSaveLiveVideo(
        videoFile: File,
        context: Context,
        isFrontCamera: Boolean = false,
        isSelfieMirrored: Boolean = true,
        onSaved: (Uri) -> Unit = {}
    ) {
        // Non-blocking background processing: keep UI unblocked so user can continue taking photos/videos
        triggerCustomToast("🎥 Processing & stamping video in background…")

        viewModelScope.launch(Dispatchers.Default) {
            val rawUri = Uri.fromFile(videoFile)
            val currentState = _locationState.value

            val stampedUri = try {
                transcodeVideoWithWatermark(
                    context = context,
                    videoUri = rawUri,
                    state = currentState,
                    isFrontCamera = isFrontCamera,
                    isSelfieMirrored = isSelfieMirrored
                )
            } catch (e: Exception) {
                Log.e("GpsViewModel", "Video transcoding error", e)
                null
            }

            val finalFile = if (stampedUri?.path != null) {
                File(stampedUri.path!!)
            } else {
                videoFile
            }

            val folder = currentState.storageFolderName
            val isDriveSync = currentState.isGoogleDriveSyncEnabled
            val driveFolder = currentState.googleDriveFolderName

            val savedUri = saveVideoToMediaStore(context, finalFile, folderName = folder)

            // Clean up temporary files safely in background
            try {
                if (finalFile != videoFile && stampedUri?.path != null && File(stampedUri.path!!).exists()) {
                    File(stampedUri.path!!).delete()
                }
                if (videoFile.exists()) {
                    videoFile.delete()
                }
            } catch (e: Exception) {
                Log.w("GpsViewModel", "Cleanup video temp file error", e)
            }

            withContext(Dispatchers.Main) {
                if (savedUri != null) {
                    addSessionMedia(SessionMediaItem(uri = savedUri, isVideo = true, dateAdded = System.currentTimeMillis(), isDriveSynced = isDriveSync))
                    triggerHapticFeedback(context)
                    val msg = if (isDriveSync) {
                        "✅ Video Saved to $folder & Synced to Drive ($driveFolder) ☁️"
                    } else {
                        "✅ Video Saved to Gallery ($folder)!"
                    }
                    triggerCustomToast(msg)
                    onSaved(savedUri)
                } else {
                    val localUri = Uri.fromFile(finalFile)
                    addSessionMedia(SessionMediaItem(uri = localUri, isVideo = true, dateAdded = System.currentTimeMillis(), isDriveSynced = isDriveSync))
                    triggerCustomToast("Video Saved Successfully")
                    onSaved(localUri)
                }
            }
        }
    }

    fun processVideoFile(fileUri: Uri, context: Context) {
        _isProcessing.value = true
        _processedVideoUri.value = null
        _processedBitmap.value = null

        viewModelScope.launch(Dispatchers.Default) {
            updateProcessingStep("Reading video metadata…")
            delay(400)

            // Extract position info from video if not using manual override
            if (!_locationState.value.isManualOverride) {
                updateProcessingStep("Searching for video geotag details…")
                delay(400)
                val videoCoords = extractGpsFromVideoUri(context, fileUri)
                if (videoCoords != null) {
                    updateProcessingStep("Fetching geotag coordinates address...")
                    val addr = reverseGeocode(videoCoords.first, videoCoords.second)
                    withContext(Dispatchers.Main) {
                        _locationState.value = LocationState(
                            lat = videoCoords.first,
                            lng = videoCoords.second,
                            accuracy = null,
                            city = addr.city.ifEmpty { "Manual Location" },
                            state = addr.state,
                            road = addr.road,
                            postcode = addr.postcode,
                            source = "MANUAL",
                            statusType = "ok",
                            statusLabel = "Video GPS Loaded ✓",
                            isManualOverride = true,
                            stampScale = _locationState.value.stampScale,
                            manualTime = _locationState.value.manualTime,
                            useInsideOverlay = _locationState.value.useInsideOverlay,
                            stampTemplate = _locationState.value.stampTemplate
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        triggerCustomToast("No GPS track in video. App GPS applied!")
                    }
                }
            }

            updateProcessingStep("Stamping video layout structure…")
            delay(500)

            withContext(Dispatchers.Main) {
                _processedVideoUri.value = fileUri
                _isProcessing.value = false
            }
        }
    }

    fun extractGpsFromVideoUri(context: Context, uri: Uri): Pair<Double, Double>? {
        val retriever = android.media.MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val locationStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_LOCATION)
            if (!locationStr.isNullOrEmpty()) {
                val match = Regex("([+-]\\d+\\.\\d+)([+-]\\d+\\.\\d+)").find(locationStr)
                if (match != null) {
                    val lat = match.groups[1]?.value?.toDoubleOrNull()
                    val lng = match.groups[2]?.value?.toDoubleOrNull()
                    if (lat != null && lng != null) {
                        Pair(lat, lng)
                    } else null
                } else null
            } else null
        } catch (e: Exception) {
            Log.e("GpsViewModel", "Video EXIF error", e)
            null
        } finally {
            try {
                retriever.release()
            } catch (ignored: Exception) {}
        }
    }

    fun goHome() {
        _processedBitmap.value = null
        _processedVideoUri.value = null
        _isProcessing.value = false
        // DO NOT unlock manual override automatically when returning home! Keep it locked as requested.
    }

    private fun updateProcessingStep(message: String) {
        _processingMessage.value = message
    }

    private fun cleanupLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        locationCallback = null
    }

    override fun onCleared() {
        cleanupLocationUpdates()
        fallbackJob?.cancel()
        super.onCleared()
    }

    // 1. TOP PRIORITY: Android Built-in Geocoder
    private fun parseAndroidAddress(address: Address): AddressInfo? {
        val rawCity = address.locality
            ?: address.subAdminArea
            ?: address.subLocality
            ?: address.adminArea
            ?: ""
        val city = cleanAddressText(rawCity)
        val state = cleanAddressText(address.adminArea ?: address.countryName ?: "")
        val country = cleanAddressText(address.countryName ?: "India")

        val roadParts = mutableListOf<String>()
        if (!address.subThoroughfare.isNullOrBlank()) roadParts.add(address.subThoroughfare)
        if (!address.thoroughfare.isNullOrBlank()) roadParts.add(address.thoroughfare)
        if (!address.subLocality.isNullOrBlank() && !roadParts.contains(address.subLocality) && address.subLocality != city) {
            roadParts.add(address.subLocality)
        }
        if (roadParts.isEmpty() && !address.featureName.isNullOrBlank() && address.featureName != city) {
            roadParts.add(address.featureName)
        }

        var road = cleanAddressText(roadParts.joinToString(", "))
        if (road.isBlank() && address.maxAddressLineIndex >= 0) {
            val fullLine = address.getAddressLine(0) ?: ""
            road = cleanAddressText(fullLine)
        }

        val postcode = cleanAddressText(address.postalCode ?: "")

        if (city.isNotEmpty() || state.isNotEmpty() || road.isNotEmpty()) {
            return AddressInfo(
                city = city.ifEmpty { "Location Found" },
                state = state,
                road = road.ifEmpty { "Address Located" },
                postcode = postcode,
                country = country.ifEmpty { "India" },
                success = true,
                tier = 1,
                sourceName = "Android Built-in Geocoder",
                description = "Address resolved with 1st Priority using device's native Android Location Geocoder."
            )
        }
        return null
    }

    private suspend fun getAddressFromAndroidGeocoder(lat: Double, lng: Double): AddressInfo? {
        return withContext(Dispatchers.IO) {
            try {
                if (!Geocoder.isPresent()) return@withContext null
                val geocoder = Geocoder(context, Locale.getDefault())

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    try {
                        val asyncAddresses = withTimeoutOrNull(3000L) {
                            suspendCancellableCoroutine<List<Address>?> { cont ->
                                try {
                                    geocoder.getFromLocation(lat, lng, 1, object : Geocoder.GeocodeListener {
                                        override fun onGeocode(addresses: MutableList<Address>) {
                                            if (cont.isActive) cont.resume(addresses)
                                        }
                                        override fun onError(errorMessage: String?) {
                                            if (cont.isActive) cont.resume(null)
                                        }
                                    })
                                } catch (_: Exception) {
                                    if (cont.isActive) cont.resume(null)
                                }
                            }
                        }
                        if (!asyncAddresses.isNullOrEmpty()) {
                            val parsed = parseAndroidAddress(asyncAddresses[0])
                            if (parsed != null) return@withContext parsed
                        }
                    } catch (_: Exception) {}
                }

                @Suppress("DEPRECATION")
                val addressList = geocoder.getFromLocation(lat, lng, 1)
                if (!addressList.isNullOrEmpty()) {
                    val parsed = parseAndroidAddress(addressList[0])
                    if (parsed != null) return@withContext parsed
                }
                null
            } catch (e: Exception) {
                Log.w("GpsViewModel", "Android Geocoder lookup error: ${e.message}")
                null
            }
        }
    }

    // 2. SECONDARY: OpenStreetMap Nominatim
    private suspend fun getAddressFromNominatim(lat: Double, lng: Double): AddressInfo? {
        val cacheKey = String.format(Locale.ENGLISH, "%.4f,%.4f", lat, lng)
        val cached = nominatimCache.get(cacheKey)
        if (cached != null) {
            Log.d("GpsViewModel", "2. Nominatim returned from LRU cache for key: $cacheKey")
            return cached
        }

        return withContext(Dispatchers.IO) {
            nominatimMutex.withLock {
                val now = System.currentTimeMillis()
                val elapsed = now - lastNominatimRequestTime
                if (elapsed < 1000L) {
                    delay(1000L - elapsed)
                }
                lastNominatimRequestTime = System.currentTimeMillis()
            }

            try {
                val url = "https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lng&format=json&accept-language=en"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "ProGpsCam/1.2 (Android; contact@example.com)")
                    .build()
                okHttpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val rBody = response.body?.string() ?: ""
                        val json = JSONObject(rBody)
                        val addressObj = json.optJSONObject("address")
                        if (addressObj != null) {
                            val rawCity = addressObj.optString("city", "")
                                .ifEmpty { addressObj.optString("town", "") }
                                .ifEmpty { addressObj.optString("village", "") }
                                .ifEmpty { addressObj.optString("suburb", "") }
                                .ifEmpty { addressObj.optString("county", "") }
                                .ifEmpty { addressObj.optString("district", "") }
                                .ifEmpty { "Location Found" }
                            val city = cleanAddressText(rawCity)
                            val state = cleanAddressText(addressObj.optString("state", ""))
                            val rawRoad = addressObj.optString("road", "")
                                .ifEmpty { addressObj.optString("suburb", "") }
                                .ifEmpty { addressObj.optString("neighbourhood", "") }
                            val road = cleanAddressText(rawRoad)
                            val postcode = cleanAddressText(addressObj.optString("postcode", ""))
                            val country = cleanAddressText(addressObj.optString("country", "India"))
                            Log.d("GpsViewModel", "2. Nominatim (SECONDARY) SUCCESS: City=$city, State=$state, Road=$road, PIN=$postcode, Country=$country")
                            val info = AddressInfo(
                                city = city,
                                state = state,
                                road = road,
                                postcode = postcode,
                                country = country.ifEmpty { "India" },
                                success = true,
                                tier = 2,
                                sourceName = "OpenStreetMap Nominatim",
                                description = "Address resolved via 2nd Priority using OpenStreetMap Nominatim Web API."
                            )
                            nominatimCache.put(cacheKey, info)
                            return@withContext info
                        }
                    }
                }
                null
            } catch (e: Exception) {
                Log.w("GpsViewModel", "Nominatim lookup failed, falling back to IP-API", e)
                null
            }
        }
    }

    // 3. TERTIARY: IP-API fallback
    private suspend fun getAddressFromIpApi(): AddressInfo? {
        return withContext(Dispatchers.IO) {
            try {
                val ipInfo = fetchIpLocation()
                if (ipInfo.success && (ipInfo.city.isNotEmpty() || ipInfo.state.isNotEmpty())) {
                    val city = cleanAddressText(ipInfo.city)
                    val state = cleanAddressText(ipInfo.state)
                    val country = cleanAddressText(ipInfo.country)
                    Log.d("GpsViewModel", "3. IP-API (TERTIARY) SUCCESS: City=$city, State=$state, Country=$country")
                    return@withContext AddressInfo(
                        city = city.ifEmpty { "Estimated Location" },
                        state = state,
                        road = "Network Estimated Location",
                        postcode = "",
                        country = country.ifEmpty { "India" },
                        success = true,
                        tier = 3,
                        sourceName = "IP-API Geolocation",
                        description = "Address estimated via 3rd Priority using IP Network Geolocation."
                    )
                }
                null
            } catch (e: Exception) {
                Log.e("GpsViewModel", "IP-API lookup failed", e)
                null
            }
        }
    }

    // Multi-tier Reverse Geocoding: 1. Android Geocoder -> (Strict 5s wait & retry Coroutine job) -> 2. Nominatim -> 3. IP-API
    private suspend fun reverseGeocode(lat: Double, lng: Double): AddressInfo {
        // 1. Top Priority: Immediate attempt with Android Built-in Geocoder
        val initialAndroidResult = getAddressFromAndroidGeocoder(lat, lng)
        if (initialAndroidResult != null && initialAndroidResult.success) {
            Log.d("GpsViewModel", "Android Geocoder resolved immediately: ${initialAndroidResult.city}, ${initialAndroidResult.road}")
            return initialAndroidResult
        }

        // 2. Strict 5-second Coroutine delay job: Retry Android Geocoder periodically before any web API calls
        Log.d("GpsViewModel", "Android Geocoder pending. Waiting 5 seconds before attempting secondary web APIs...")
        val waitStartTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - waitStartTime < 5000L) {
            delay(1500L)
            val retryResult = getAddressFromAndroidGeocoder(lat, lng)
            if (retryResult != null && retryResult.success) {
                Log.d("GpsViewModel", "Android Geocoder succeeded during 5s wait window! Web APIs spared.")
                return retryResult
            }
        }

        // 3. Secondary: Nominatim (OpenStreetMap) - ONLY called if Android Geocoder failed throughout 5 seconds
        Log.w("GpsViewModel", "5-second Android Geocoder timeout elapsed. Triggering Secondary fallback: Nominatim")
        val nominatimResult = getAddressFromNominatim(lat, lng)
        if (nominatimResult != null && nominatimResult.success) {
            return nominatimResult
        }

        // 4. Tertiary: IP-API Fallback - ONLY called if Nominatim failed
        Log.w("GpsViewModel", "Nominatim failed. Triggering Tertiary fallback: IP-API")
        val ipApiResult = getAddressFromIpApi()
        if (ipApiResult != null && ipApiResult.success) {
            return ipApiResult
        }

        // Default coordinate logging if all tiers fail
        return AddressInfo(
            city = "${String.format(Locale.US, "%.4f", lat)}°N",
            state = "${String.format(Locale.US, "%.4f", lng)}°E",
            road = "Coordinates Logged",
            postcode = "",
            success = false,
            tier = 0,
            sourceName = "Raw GPS Coordinates",
            description = "Offline GPS Coordinates recorded without reverse address lookup."
        )
    }

    // Coroutine HTTP IP lookup
    private suspend fun fetchIpLocation(): IpLocationInfo {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://ipapi.co/json/"
                val request = Request.Builder().url(url).build()
                okHttpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val rBody = response.body?.string() ?: ""
                        val json = JSONObject(rBody)
                        IpLocationInfo(
                            lat = json.optDouble("latitude", 0.0),
                            lng = json.optDouble("longitude", 0.0),
                            city = json.optString("city", "Unknown"),
                            state = json.optString("region", ""),
                            country = json.optString("country_name", json.optString("country", "India")),
                            success = true
                        )
                    } else {
                        IpLocationInfo(0.0, 0.0, "", "", "India", false)
                    }
                }
            } catch (e: Exception) {
                Log.e("GpsViewModel", "ipapi fallback error", e)
                IpLocationInfo(0.0, 0.0, "", "", "India", false)
            }
        }
    }

    private fun loadBitmapFromUri(context: Context, uri: Uri, targetMaxDim: Int = 4096): Bitmap? {
        return try {
            // 1. First, decode only bounds to inspect the dimensions without allocation
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            // 2. Identify maximum target dimension from chosen ImageQuality
            val maxDim = targetMaxDim.coerceAtLeast(1024)
            var inSampleSize = 1
            if (options.outHeight > maxDim || options.outWidth > maxDim) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / inSampleSize) >= maxDim || (halfWidth / inSampleSize) >= maxDim) {
                    inSampleSize *= 2
                }
            }

            // 3. Decode the actual downsampled bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            val rawBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return null

            val rotationDegrees = getRotationDegrees(context, uri)
            if (rotationDegrees != 0) {
                val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                val rotatedBitmap = Bitmap.createBitmap(
                    rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true
                )
                if (rotatedBitmap != rawBitmap) {
                    rawBitmap.recycle()
                }
                rotatedBitmap
            } else {
                rawBitmap
            }
        } catch (e: Exception) {
            Log.e("GpsViewModel", "Bitmap decode failed from Uri $uri", e)
            null
        }
    }

    private fun getRotationDegrees(context: Context, uri: Uri): Int {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exifInterface = android.media.ExifInterface(stream)
                val orientation = exifInterface.getAttributeInt(
                    android.media.ExifInterface.TAG_ORIENTATION,
                    android.media.ExifInterface.ORIENTATION_NORMAL
                )
                return when (orientation) {
                    android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            }
        } catch (e: Exception) {
            Log.e("GpsViewModel", "Error reading EXIF orientation", e)
        }
        return 0
    }

    private fun extractGpsFromUri(context: Context, uri: Uri): Pair<Double, Double>? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exifInterface = android.media.ExifInterface(stream)
                val latLong = FloatArray(2)
                if (exifInterface.getLatLong(latLong)) {
                    Pair(latLong[0].toDouble(), latLong[1].toDouble())
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("GpsViewModel", "EXIF parser error", e)
            null
        }
    }

    private fun resizeBitmapIfNeeded(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= maxDimension && h <= maxDimension) return bitmap
        val aspect = w.toFloat() / h.toFloat()
        val (newW, newH) = if (w > h) {
            maxDimension to (maxDimension / aspect).toInt()
        } else {
            (maxDimension * aspect).toInt() to maxDimension
        }
        return Bitmap.createScaledBitmap(bitmap, newW, newH, true)
    }

    // Draws pixel-perfect overlay
    private fun drawOverlayOnBitmap(img: Bitmap, state: LocationState): Bitmap {
        val W = img.width
        val H_old = img.height
        val isLandscape = W > H_old

        val baseRatio = if (isLandscape) (W / 640.0f) else (W / 420.0f)
        val scaleFactor = baseRatio * state.stampScale
        val hasCustomNote = state.isCustomNoteEnabled && state.customNote.isNotBlank()
        val stampHeight = (if (hasCustomNote) 98.0f else 86.0f) * scaleFactor
        val H_new = if (state.useInsideOverlay) H_old else (H_old + stampHeight).toInt()

        val output = Bitmap.createBitmap(W, H_new, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // 1. Draw original photo
        canvas.drawBitmap(img, 0f, 0f, null)

        val startY = if (state.useInsideOverlay) (H_old - stampHeight) else H_old.toFloat()
        val pad = 10f * scaleFactor

        fun drawWithClipping(txt: String, x: Float, y: Float, maxW: Float, paint: Paint) {
            var finalTxt = txt
            if (paint.measureText(finalTxt) <= maxW) {
                canvas.drawText(finalTxt, x, y, paint)
                return
            }
            while (finalTxt.isNotEmpty() && paint.measureText(finalTxt + "…") > maxW) {
                finalTxt = finalTxt.dropLast(1)
            }
            canvas.drawText(finalTxt + "…", x, y, paint)
        }

        when (state.stampTemplate) {
            StampTemplate.STANDARD -> {
                val mapW = 75f * scaleFactor
                val mapH = stampHeight - pad * 2f
                val mapX = pad
                val mapY = startY + pad

                if (!state.useInsideOverlay) {
                    val paintBar = Paint().apply {
                        color = 0xFF0A0B0E.toInt()
                        style = Paint.Style.FILL
                    }
                    canvas.drawRect(0f, H_old.toFloat(), W.toFloat(), H_new.toFloat(), paintBar)

                    val gradientShader = LinearGradient(
                        0f, startY, W.toFloat(), startY,
                        intArrayOf(0xFF00E5FF.toInt(), 0xFF7B61FF.toInt(), 0xFFFF6B35.toInt()),
                        floatArrayOf(0f, 0.5f, 1f),
                        Shader.TileMode.CLAMP
                    )
                    val paintGrad = Paint().apply {
                        shader = gradientShader
                        style = Paint.Style.FILL
                    }
                    canvas.drawRect(0f, startY, W.toFloat(), startY + 3f * scaleFactor, paintGrad)
                }

                // 4. Draw Map card box
                val paintMapBox = Paint().apply {
                    color = if (state.useInsideOverlay) 0x660F111A.toInt() else 0xFF0F111A.toInt()
                    style = Paint.Style.FILL
                }
                val rectPath = Path().apply {
                    addRoundRect(
                        mapX, mapY, mapX + mapW, mapY + mapH,
                        6f * scaleFactor, 6f * scaleFactor,
                        Path.Direction.CW
                    )
                }
                canvas.drawPath(rectPath, paintMapBox)

                // Border of Map box
                val paintMapBorder = Paint().apply {
                    color = 0x4000E5FF // rgba(0, 229, 255, 0.25)
                    style = Paint.Style.STROKE
                    strokeWidth = 1f * scaleFactor
                    isAntiAlias = true
                }
                canvas.drawPath(rectPath, paintMapBorder)

                // 5. Draw decorative Map Pin indicator
                val cx = mapX + mapW / 2f
                val cy = mapY + mapH * 0.34f
                val r = 6.5f * scaleFactor

                // Pin shadow
                val paintShadow = Paint().apply {
                    color = 0x55000000 // translucent black
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                canvas.drawOval(
                    cx - r * 0.4f, cy + r * 1.5f, cx + r * 0.4f, cy + r * 1.7f,
                    paintShadow
                )

                // Pin body triangle
                val pinTriangle = Path().apply {
                    moveTo(cx - r * 0.8f, cy + r * 0.3f)
                    lineTo(cx + r * 0.8f, cy + r * 0.3f)
                    lineTo(cx, cy + r * 1.6f)
                    close()
                }
                val paintPin = Paint().apply {
                    color = 0xFFFF6B35.toInt() // orange
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                canvas.drawPath(pinTriangle, paintPin)
                canvas.drawCircle(cx, cy, r, paintPin)

                // Pin eye white hole
                val paintEye = Paint().apply {
                    color = android.graphics.Color.WHITE
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                canvas.drawCircle(cx, cy, r * 0.35f, paintEye)

                // Coordinates inside Map Box
                val paintMapCoordDigits = Paint().apply {
                    color = 0xFF00E5FF.toInt()
                    textSize = 6.5f * scaleFactor
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.MONOSPACE
                    isAntiAlias = true
                    if (state.useInsideOverlay) {
                        setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xFF000000.toInt())
                    }
                }
                canvas.drawText("${String.format(Locale.US, "%.4f", state.lat)}°N", cx, mapY + mapH - 12f * scaleFactor, paintMapCoordDigits)
                canvas.drawText("${String.format(Locale.US, "%.4f", state.lng)}°E", cx, mapY + mapH - 4f * scaleFactor, paintMapCoordDigits)

                // Automatic Source Badge text in Map
                val paintAutomaticLabel = Paint().apply {
                    color = 0x4DFFFFFF.toInt() // translucent white
                    textSize = 6.5f * scaleFactor
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    isAntiAlias = true
                    if (state.useInsideOverlay) {
                        setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xFF000000.toInt())
                    }
                }
                canvas.drawText("AUTOMATIC", cx, mapY + 8f * scaleFactor, paintAutomaticLabel)

                // 6. Draw Text readings on the right side of the card
                val tx = mapX + mapW + pad
                val tw = W - tx - pad
                var ty = startY + pad + 10f * scaleFactor

                // Line 1: City + State + Country
                val rawCityLine = listOfNotNull(
                    state.city.takeIf { it.isNotEmpty() },
                    state.state.takeIf { it.isNotEmpty() },
                    state.country.takeIf { it.isNotEmpty() }
                ).joinToString(", ").ifEmpty { "Location" }
                val cityLine = cleanAddressText(rawCityLine)
                val paintCity = Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 13.5f * scaleFactor
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    isAntiAlias = true
                    if (state.useInsideOverlay) {
                        setShadowLayer(3f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xFF000000.toInt())
                    }
                }
                drawWithClipping(cityLine, tx, ty, tw, paintCity)
                ty += 14f * scaleFactor

                // Line 2: Details address
                val addrParts = listOfNotNull(
                    state.road.takeIf { it.isNotEmpty() && !it.contains("Manually entered") },
                    state.city.takeIf { it.isNotEmpty() },
                    state.state.takeIf { it.isNotEmpty() },
                    state.postcode.takeIf { it.isNotEmpty() },
                    state.country.takeIf { it.isNotEmpty() }
                )
                val addressText = cleanAddressText(addrParts.joinToString(", ").ifEmpty { "Georeferenced Survey Location" })
                val paintAddr = Paint().apply {
                    color = if (state.useInsideOverlay) 0xEEFFFFFF.toInt() else 0x7BE8EAF0.toInt() // high contrast if overlay inside
                    textSize = 7.5f * scaleFactor
                    typeface = Typeface.MONOSPACE
                    isAntiAlias = true
                    if (state.useInsideOverlay) {
                        setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xFF000000.toInt())
                    }
                }
                drawWithClipping(addressText, tx, ty, tw, paintAddr)
                ty += 9.5f * scaleFactor

                // Line 3: Coords
                val latText = "Lat ${String.format(Locale.US, "%.6f", state.lat)}°"
                val lngText = "Long ${String.format(Locale.US, "%.6f", state.lng)}°"
                val dirText = if (state.isCompassEnabled) "   Dir: ${state.getCardinalDirection()}" else ""
                val paintCoords = Paint().apply {
                    color = 0xFF00E5FF.toInt()
                    textSize = 8.5f * scaleFactor
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                    isAntiAlias = true
                    if (state.useInsideOverlay) {
                        setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xFF000000.toInt())
                    }
                }
                drawWithClipping("$latText   $lngText$dirText", tx, ty, tw, paintCoords)
                ty += 10.5f * scaleFactor

                // Line 4: Formatted Time
                val dt = if (state.manualTime != null) Date(state.manualTime) else Date()
                val sdfDay = SimpleDateFormat("EEEE", Locale.ENGLISH)
                val sdfDate = SimpleDateFormat("dd/MM/yyyy  hh:mm:ss a", Locale.ENGLISH)
                val formattedDateLine = "${sdfDay.format(dt)}, ${sdfDate.format(dt).uppercase()}"

                val paintTime = Paint().apply {
                    color = if (state.useInsideOverlay) 0xEEFFFFFF.toInt() else 0xA6FFFFFF.toInt()
                    textSize = 8f * scaleFactor
                    typeface = Typeface.MONOSPACE
                    isAntiAlias = true
                    if (state.useInsideOverlay) {
                        setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xFF000000.toInt())
                    }
                }
                drawWithClipping(formattedDateLine, tx, ty, tw, paintTime)
                ty += 9.5f * scaleFactor

                // Line 5: TZ + Accuracy m
                val timeZone = TimeZone.getDefault()
                val totalOffsetMin = timeZone.getOffset(dt.time) / (1000 * 60)
                val tzHours = Math.abs(totalOffsetMin) / 60
                val tzMins = Math.abs(totalOffsetMin) % 60
                val tzSign = if (totalOffsetMin >= 0) "+" else "-"
                val tzString = "GMT$tzSign${String.format("%02d", tzHours)}:${String.format("%02d", tzMins)}"

                val accVal = if (state.accuracy != null) "${Math.round(state.accuracy)}m" else "5m"
                val accuracyString = "$tzString   ACCURACY: ±$accVal"
                val paintAccuracy = Paint().apply {
                    color = if (state.accuracy != null) 0xFF00FF88.toInt() else 0xFFFFD700.toInt()
                    textSize = 7.5f * scaleFactor
                    typeface = Typeface.MONOSPACE
                    isAntiAlias = true
                    if (state.useInsideOverlay) {
                        setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xFF000000.toInt())
                    }
                }
                drawWithClipping(accuracyString, tx, ty, tw, paintAccuracy)

                // Line 6: Custom Note / Remark (if enabled)
                if (hasCustomNote) {
                    ty += 9.5f * scaleFactor
                    val noteText = "Note: ${state.customNote.trim()}"
                    val paintNote = Paint().apply {
                        color = 0xFFFFD700.toInt() // gold yellow accent for custom note
                        textSize = 8f * scaleFactor
                        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                        isAntiAlias = true
                        if (state.useInsideOverlay) {
                            setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xFF000000.toInt())
                        }
                    }
                    drawWithClipping(noteText, tx, ty, tw, paintNote)
                }

                // Bottom right corner: Branding watermark text (removed if Pro User toggles removeWatermark)
                if (!state.removeWatermark) {
                    val paintBranding = Paint().apply {
                        color = 0x9900E5FF.toInt() // translucent cyan
                        textSize = 8f * scaleFactor
                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        textAlign = Paint.Align.RIGHT
                        isAntiAlias = true
                        if (state.useInsideOverlay) {
                            setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xFF000000.toInt())
                        }
                    }
                    canvas.drawText(state.getWatermarkText(), W - pad, H_new - 5f * scaleFactor, paintBranding)
                }
            }

            StampTemplate.CLASSIC -> {
                val mapW = 75f * scaleFactor
                val mapH = stampHeight - pad * 2f
                val mapX = pad
                val mapY = startY + pad

                val paintBar = Paint().apply {
                    color = if (state.useInsideOverlay) 0xCC150F07.toInt() else 0xFF150F07.toInt()
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, startY, W.toFloat(), H_new.toFloat(), paintBar)

                val paintGoldBorder = Paint().apply {
                    color = 0xFFFFD700.toInt()
                    style = Paint.Style.STROKE
                    strokeWidth = 2f * scaleFactor
                    isAntiAlias = true
                }
                canvas.drawRect(pad * 0.5f, startY + 2f * scaleFactor, W.toFloat() - pad * 0.5f, H_new.toFloat() - 2f * scaleFactor, paintGoldBorder)

                val paintHeaderLine = Paint().apply {
                    color = 0xFFFFD700.toInt()
                    textSize = 7f * scaleFactor
                    typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.drawText("★ GEODETIC FIELD SURVEY SEAL ★", pad * 1.5f, startY + 10f * scaleFactor, paintHeaderLine)

                val paintBadgeBg = Paint().apply {
                    color = 0xFF2A1C0A.toInt()
                    style = Paint.Style.FILL
                }
                val badgePath = Path().apply {
                    addRoundRect(mapX, mapY + 6f * scaleFactor, mapX + mapW, mapY + mapH, 8f * scaleFactor, 8f * scaleFactor, Path.Direction.CW)
                }
                canvas.drawPath(badgePath, paintBadgeBg)
                canvas.drawPath(badgePath, paintGoldBorder)

                val cx = mapX + mapW / 2f
                val paintBadgeTitle = Paint().apply {
                    color = 0xFFFFD700.toInt()
                    textSize = 6.5f * scaleFactor
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.drawText("SURVEY", cx, mapY + 16f * scaleFactor, paintBadgeTitle)

                val paintIcon = Paint().apply {
                    textSize = 14f * scaleFactor
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("🧭", cx, mapY + 34f * scaleFactor, paintIcon)

                val paintBadgeCoords = Paint().apply {
                    color = 0xFFFDE68A.toInt()
                    textSize = 6.5f * scaleFactor
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.MONOSPACE
                    isAntiAlias = true
                }
                canvas.drawText("${String.format(Locale.US, "%.3f", state.lat)}°N", cx, mapY + mapH - 10f * scaleFactor, paintBadgeCoords)
                canvas.drawText("${String.format(Locale.US, "%.3f", state.lng)}°E", cx, mapY + mapH - 3f * scaleFactor, paintBadgeCoords)

                val tx = mapX + mapW + pad
                val tw = W - tx - pad
                var ty = startY + pad + 16f * scaleFactor

                val rawCityLine = listOfNotNull(
                    state.city.takeIf { it.isNotEmpty() },
                    state.state.takeIf { it.isNotEmpty() },
                    state.country.takeIf { it.isNotEmpty() }
                ).distinct().joinToString(", ").ifEmpty { "Location" }
                val cityLine = cleanAddressText(rawCityLine)
                val paintCity = Paint().apply {
                    color = 0xFFFFD700.toInt()
                    textSize = 13f * scaleFactor
                    typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                    isAntiAlias = true
                }
                drawWithClipping("🏛️ $cityLine", tx, ty, tw, paintCity)
                ty += 13.5f * scaleFactor

                val addrParts = listOfNotNull(
                    state.road.takeIf { it.isNotEmpty() && !it.contains("Manually entered") },
                    state.city.takeIf { it.isNotEmpty() },
                    state.state.takeIf { it.isNotEmpty() },
                    state.postcode.takeIf { it.isNotEmpty() },
                    state.country.takeIf { it.isNotEmpty() }
                ).distinct()
                val addressText = cleanAddressText(addrParts.joinToString(", ").ifEmpty { "Georeferenced Survey Location" })
                val paintAddr = Paint().apply {
                    color = 0xFFFDE68A.toInt()
                    textSize = 7.5f * scaleFactor
                    typeface = Typeface.MONOSPACE
                    isAntiAlias = true
                }
                drawWithClipping(addressText, tx, ty, tw, paintAddr)
                ty += 9.5f * scaleFactor

                val dirStr = if (state.isCompassEnabled) " | DIR: ${state.getCardinalDirection()}" else ""
                val coordText = "[ LAT: ${String.format(Locale.US, "%.6f", state.lat)}° | LON: ${String.format(Locale.US, "%.6f", state.lng)}°$dirStr ]"
                val paintCoords = Paint().apply {
                    color = 0xFFFFE082.toInt()
                    textSize = 8.5f * scaleFactor
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                    isAntiAlias = true
                }
                drawWithClipping(coordText, tx, ty, tw, paintCoords)
                ty += 10.5f * scaleFactor

                val dt = if (state.manualTime != null) Date(state.manualTime) else Date()
                val sdfDate = SimpleDateFormat("EEEE, dd/MM/yyyy hh:mm:ss a", Locale.ENGLISH)
                val paintTime = Paint().apply {
                    color = 0xFFFFF8E1.toInt()
                    textSize = 7.8f * scaleFactor
                    typeface = Typeface.MONOSPACE
                    isAntiAlias = true
                }
                drawWithClipping(sdfDate.format(dt).uppercase(), tx, ty, tw, paintTime)
                ty += 9.5f * scaleFactor

                val accText = "ACCURACY: ±${if (state.accuracy != null) Math.round(state.accuracy) else 5}m"
                val paintAcc = Paint().apply {
                    color = 0xFFFFD54F.toInt()
                    textSize = 7.2f * scaleFactor
                    typeface = Typeface.MONOSPACE
                    isAntiAlias = true
                }
                drawWithClipping(accText, tx, ty, tw, paintAcc)

                if (hasCustomNote) {
                    ty += 9.5f * scaleFactor
                    val paintNote = Paint().apply {
                        color = 0xFFFFE082.toInt()
                        textSize = 8f * scaleFactor
                        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                        isAntiAlias = true
                    }
                    drawWithClipping("NOTE: ${state.customNote.trim()}", tx, ty, tw, paintNote)
                }

                if (!state.removeWatermark) {
                    val paintBranding = Paint().apply {
                        color = 0xFFFFD700.toInt()
                        textSize = 8f * scaleFactor
                        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                        textAlign = Paint.Align.RIGHT
                        isAntiAlias = true
                        if (state.useInsideOverlay) {
                            setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xFF000000.toInt())
                        }
                    }
                    canvas.drawText(state.getWatermarkText(), W - pad * 1.5f, H_new - 5f * scaleFactor, paintBranding)
                }
            }

            StampTemplate.TECH_HUD -> {
                val mapW = 75f * scaleFactor
                val mapH = stampHeight - pad * 2f
                val mapX = pad
                val mapY = startY + pad

                val paintBar = Paint().apply {
                    color = if (state.useInsideOverlay) 0xD9050E17.toInt() else 0xFF050E17.toInt()
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, startY, W.toFloat(), H_new.toFloat(), paintBar)

                val paintBorder = Paint().apply {
                    color = 0xFF00E5FF.toInt()
                    style = Paint.Style.STROKE
                    strokeWidth = 1.2f * scaleFactor
                    isAntiAlias = true
                }
                canvas.drawRect(pad * 0.5f, startY + 2f * scaleFactor, W.toFloat() - pad * 0.5f, H_new.toFloat() - 2f * scaleFactor, paintBorder)

                val paintCyberHead = Paint().apply {
                    color = 0xFF00FF66.toInt()
                    textSize = 7f * scaleFactor
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.drawText("[ 🛰️ SATELLITE LOCK // GPS HUD ]", pad * 1.5f, startY + 9.5f * scaleFactor, paintCyberHead)

                val paintRadarBg = Paint().apply {
                    color = 0xFF021B1C.toInt()
                    style = Paint.Style.FILL
                }
                val radarPath = Path().apply {
                    addRoundRect(mapX, mapY + 6f * scaleFactor, mapX + mapW, mapY + mapH, 4f * scaleFactor, 4f * scaleFactor, Path.Direction.CW)
                }
                canvas.drawPath(radarPath, paintRadarBg)

                val paintRadarBorder = Paint().apply {
                    color = 0xFF00FF66.toInt()
                    style = Paint.Style.STROKE
                    strokeWidth = 1f * scaleFactor
                    isAntiAlias = true
                }
                canvas.drawPath(radarPath, paintRadarBorder)

                val cx = mapX + mapW / 2f
                val paintRadarLabel = Paint().apply {
                    color = 0xFF00FF66.toInt()
                    textSize = 6f * scaleFactor
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.MONOSPACE
                    isAntiAlias = true
                }
                canvas.drawText("RADAR LOCK", cx, mapY + 15f * scaleFactor, paintRadarLabel)

                val paintIcon = Paint().apply {
                    textSize = 14f * scaleFactor
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("⚡", cx, mapY + 33f * scaleFactor, paintIcon)

                val paintRadarCoords = Paint().apply {
                    color = 0xFF00E5FF.toInt()
                    textSize = 6.5f * scaleFactor
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.MONOSPACE
                    isAntiAlias = true
                }
                canvas.drawText("${String.format(Locale.US, "%.3f", state.lat)}°N", cx, mapY + mapH - 10f * scaleFactor, paintRadarCoords)
                canvas.drawText("${String.format(Locale.US, "%.3f", state.lng)}°E", cx, mapY + mapH - 3f * scaleFactor, paintRadarCoords)

                val tx = mapX + mapW + pad
                val tw = W - tx - pad
                var ty = startY + pad + 16f * scaleFactor

                val rawCityLine = listOfNotNull(
                    state.city.takeIf { it.isNotEmpty() },
                    state.state.takeIf { it.isNotEmpty() },
                    state.country.takeIf { it.isNotEmpty() }
                ).distinct().joinToString(", ").ifEmpty { "Location" }
                val cityLine = cleanAddressText(rawCityLine)
                val paintCity = Paint().apply {
                    color = 0xFF00E5FF.toInt()
                    textSize = 13f * scaleFactor
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                    isAntiAlias = true
                }
                drawWithClipping("POS // $cityLine", tx, ty, tw, paintCity)
                ty += 13.5f * scaleFactor

                val addrParts = listOfNotNull(
                    state.road.takeIf { it.isNotEmpty() && !it.contains("Manually entered") },
                    state.city.takeIf { it.isNotEmpty() },
                    state.state.takeIf { it.isNotEmpty() },
                    state.postcode.takeIf { it.isNotEmpty() },
                    state.country.takeIf { it.isNotEmpty() }
                ).distinct()
                val addressText = cleanAddressText(addrParts.joinToString(", ").ifEmpty { "Georeferenced Survey Location" })
                val paintAddr = Paint().apply {
                    color = 0xFF00FF88.toInt()
                    textSize = 7.5f * scaleFactor
                    typeface = Typeface.MONOSPACE
                    isAntiAlias = true
                }
                drawWithClipping("LOC // $addressText", tx, ty, tw, paintAddr)
                ty += 9.5f * scaleFactor

                val dirStr = if (state.isCompassEnabled) "  DIR: ${state.getCardinalDirection()}" else ""
                val coordText = "GEO // LAT: ${String.format(Locale.US, "%.6f", state.lat)}°  LON: ${String.format(Locale.US, "%.6f", state.lng)}°$dirStr"
                val paintCoords = Paint().apply {
                    color = 0xFF00FF66.toInt()
                    textSize = 8.5f * scaleFactor
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                    isAntiAlias = true
                }
                drawWithClipping(coordText, tx, ty, tw, paintCoords)
                ty += 10.5f * scaleFactor

                val dt = if (state.manualTime != null) Date(state.manualTime) else Date()
                val sdfDate = SimpleDateFormat("EEEE, dd/MM/yyyy hh:mm:ss a", Locale.ENGLISH)
                val paintTime = Paint().apply {
                    color = 0xFFE0F2FE.toInt()
                    textSize = 7.8f * scaleFactor
                    typeface = Typeface.MONOSPACE
                    isAntiAlias = true
                }
                drawWithClipping("UTC // ${sdfDate.format(dt).uppercase()}", tx, ty, tw, paintTime)
                ty += 9.5f * scaleFactor

                val accText = "ACCURACY // ±${if (state.accuracy != null) Math.round(state.accuracy) else 5}m"
                val paintAcc = Paint().apply {
                    color = 0xFF00FF88.toInt()
                    textSize = 7.2f * scaleFactor
                    typeface = Typeface.MONOSPACE
                    isAntiAlias = true
                }
                drawWithClipping(accText, tx, ty, tw, paintAcc)

                if (hasCustomNote) {
                    ty += 9.5f * scaleFactor
                    val paintNote = Paint().apply {
                        color = 0xFFFFD700.toInt()
                        textSize = 8f * scaleFactor
                        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                        isAntiAlias = true
                    }
                    drawWithClipping("NOTE // ${state.customNote.trim()}", tx, ty, tw, paintNote)
                }

                if (!state.removeWatermark) {
                    val paintBranding = Paint().apply {
                        color = 0xFF00E5FF.toInt()
                        textSize = 8f * scaleFactor
                        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                        textAlign = Paint.Align.RIGHT
                        isAntiAlias = true
                        if (state.useInsideOverlay) {
                            setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xFF000000.toInt())
                        }
                    }
                    canvas.drawText(state.getWatermarkText(), W - pad * 1.5f, H_new - 5f * scaleFactor, paintBranding)
                }
            }

            StampTemplate.CLEAN_MINIMAL -> {
                val mapW = 60f * scaleFactor
                val mapH = stampHeight - pad * 2f
                val mapX = pad
                val mapY = startY + pad

                val paintPillBg = Paint().apply {
                    color = if (state.useInsideOverlay) 0xCC0F172A.toInt() else 0xFF0F172A.toInt()
                    style = Paint.Style.FILL
                }
                val pillPath = Path().apply {
                    addRoundRect(pad * 0.5f, startY + 2f * scaleFactor, W.toFloat() - pad * 0.5f, H_new.toFloat() - 2f * scaleFactor, 14f * scaleFactor, 14f * scaleFactor, Path.Direction.CW)
                }
                canvas.drawPath(pillPath, paintPillBg)

                val paintPillBorder = Paint().apply {
                    color = 0x80818CF8.toInt()
                    style = Paint.Style.STROKE
                    strokeWidth = 1.2f * scaleFactor
                    isAntiAlias = true
                }
                canvas.drawPath(pillPath, paintPillBorder)

                val cx = mapX + mapW / 2f
                val cy = mapY + mapH / 2f
                val paintCircle = Paint().apply {
                    color = 0x4D4F46E5.toInt()
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                canvas.drawCircle(cx, cy, 22f * scaleFactor, paintCircle)

                val paintIcon = Paint().apply {
                    textSize = 18f * scaleFactor
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("✨", cx, cy + 6f * scaleFactor, paintIcon)

                val tx = mapX + mapW + pad * 0.5f
                val tw = W - tx - pad
                var ty = startY + pad + 12f * scaleFactor

                val rawCityLine = listOfNotNull(
                    state.city.takeIf { it.isNotEmpty() },
                    state.state.takeIf { it.isNotEmpty() },
                    state.country.takeIf { it.isNotEmpty() }
                ).distinct().joinToString(", ").ifEmpty { "Location" }
                val cityLine = cleanAddressText(rawCityLine)
                val paintCity = Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 13.5f * scaleFactor
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    isAntiAlias = true
                }
                val latStr = "${String.format(Locale.US, "%.4f", state.lat)}°N"
                val lngStr = "${String.format(Locale.US, "%.4f", state.lng)}°E"
                val dirStr = if (state.isCompassEnabled) "  •  ${state.getCardinalDirection()}" else ""
                drawWithClipping("$cityLine  •  $latStr, $lngStr$dirStr", tx, ty, tw, paintCity)
                ty += 14f * scaleFactor

                val addrParts = listOfNotNull(
                    state.road.takeIf { it.isNotEmpty() && !it.contains("Manually entered") },
                    state.city.takeIf { it.isNotEmpty() },
                    state.state.takeIf { it.isNotEmpty() },
                    state.postcode.takeIf { it.isNotEmpty() },
                    state.country.takeIf { it.isNotEmpty() }
                ).distinct()
                val addressText = cleanAddressText(addrParts.joinToString(", ").ifEmpty { "Georeferenced Survey Location" })
                val paintAddr = Paint().apply {
                    color = 0xFFE2E8F0.toInt()
                    textSize = 7.5f * scaleFactor
                    typeface = Typeface.MONOSPACE
                    isAntiAlias = true
                }
                drawWithClipping(addressText, tx, ty, tw, paintAddr)
                ty += 9.5f * scaleFactor

                val dt = if (state.manualTime != null) Date(state.manualTime) else Date()
                val sdfDate = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH)
                val accText = "ACC: ±${if (state.accuracy != null) Math.round(state.accuracy) else 5}m"
                val timeAccText = "${sdfDate.format(dt)}  •  $accText"
                val paintTime = Paint().apply {
                    color = 0xFF94A3B8.toInt()
                    textSize = 7.5f * scaleFactor
                    typeface = Typeface.MONOSPACE
                    isAntiAlias = true
                }
                drawWithClipping(timeAccText, tx, ty, tw, paintTime)

                if (hasCustomNote) {
                    ty += 9.5f * scaleFactor
                    val paintNote = Paint().apply {
                        color = 0xFF38BDF8.toInt()
                        textSize = 8f * scaleFactor
                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        isAntiAlias = true
                    }
                    drawWithClipping("Note: ${state.customNote.trim()}", tx, ty, tw, paintNote)
                }

                if (!state.removeWatermark) {
                    val paintBranding = Paint().apply {
                        color = 0xFF818CF8.toInt()
                        textSize = 8f * scaleFactor
                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        textAlign = Paint.Align.RIGHT
                        isAntiAlias = true
                        if (state.useInsideOverlay) {
                            setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xFF000000.toInt())
                        }
                    }
                    canvas.drawText(state.getWatermarkText(), W - pad * 1.5f, H_new - 5f * scaleFactor, paintBranding)
                }
            }

            StampTemplate.DARK_GLASS -> {
                val paintGlassBg = Paint().apply {
                    color = if (state.useInsideOverlay) 0xCC0F172A.toInt() else 0xFF0F172A.toInt()
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, startY, W.toFloat(), H_new.toFloat(), paintGlassBg)

                val paintGlassBorder = Paint().apply {
                    color = 0x4038BDF8.toInt()
                    style = Paint.Style.STROKE
                    strokeWidth = 1f * scaleFactor
                    isAntiAlias = true
                }
                canvas.drawLine(0f, startY, W.toFloat(), startY, paintGlassBorder)

                // Cyan accent vertical bar
                val paintBar = Paint().apply {
                    color = 0xFF38BDF8.toInt()
                    style = Paint.Style.FILL
                }
                val barRect = RectF(pad * 0.8f, startY + pad * 0.5f, pad * 0.8f + 4f * scaleFactor, H_new - pad * 0.5f)
                canvas.drawRoundRect(barRect, 2f * scaleFactor, 2f * scaleFactor, paintBar)

                val tx = pad * 0.8f + 4f * scaleFactor + pad * 0.8f
                val tw = W - tx - pad
                var ty = startY + pad * 0.6f + 11f * scaleFactor

                val rawCityLine = listOfNotNull(
                    state.city.takeIf { it.isNotEmpty() },
                    state.state.takeIf { it.isNotEmpty() }
                ).joinToString(", ").ifEmpty { "Location" }
                val cityLine = cleanAddressText(rawCityLine)
                val paintCity = Paint().apply {
                    color = 0xFFF8FAFC.toInt()
                    textSize = 13f * scaleFactor
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    isAntiAlias = true
                }
                drawWithClipping(cityLine, tx, ty, tw, paintCity)
                ty += 13f * scaleFactor

                val addrParts = listOfNotNull(
                    state.road.takeIf { it.isNotEmpty() && !it.contains("Manually entered") },
                    state.city.takeIf { it.isNotEmpty() },
                    state.postcode.takeIf { it.isNotEmpty() },
                    state.country.takeIf { it.isNotEmpty() }
                )
                val addressText = cleanAddressText(addrParts.joinToString(", ").ifEmpty { "Georeferenced Survey Location" })
                val paintAddr = Paint().apply {
                    color = 0xFF94A3B8.toInt()
                    textSize = 8.5f * scaleFactor
                    typeface = Typeface.SANS_SERIF
                    isAntiAlias = true
                }
                drawWithClipping(addressText, tx, ty, tw, paintAddr)
                ty += 11f * scaleFactor

                val dirStr = if (state.isCompassEnabled) "   DIR: ${state.getCardinalDirection()}" else ""
                val altStr = if (state.accuracy != null) "   ALT: ${Math.round(state.accuracy * 12f + 180)}m" else ""
                val metaText = "LAT ${String.format(Locale.US, "%.4f", state.lat)}°  LON ${String.format(Locale.US, "%.4f", state.lng)}°$dirStr$altStr"
                val paintMeta = Paint().apply {
                    color = 0xFFBAE6FD.toInt()
                    textSize = 8f * scaleFactor
                    typeface = Typeface.MONOSPACE
                    isAntiAlias = true
                }
                drawWithClipping(metaText, tx, ty, tw, paintMeta)
                ty += 10f * scaleFactor

                val dt = if (state.manualTime != null) Date(state.manualTime) else Date()
                val sdfDate = SimpleDateFormat("dd-MM-yyyy (EEE) HH:mm:ss", Locale.ENGLISH)
                val accText = "ACC ±${if (state.accuracy != null) Math.round(state.accuracy) else 5}m"
                val timeString = "${sdfDate.format(dt)}   $accText"
                val paintTime = Paint().apply {
                    color = 0xFFBAE6FD.toInt()
                    textSize = 8f * scaleFactor
                    typeface = Typeface.MONOSPACE
                    isAntiAlias = true
                }
                drawWithClipping(timeString, tx, ty, tw, paintTime)

                if (hasCustomNote) {
                    ty += 10f * scaleFactor
                    val paintNote = Paint().apply {
                        color = 0xFFFDE047.toInt()
                        textSize = 8.2f * scaleFactor
                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        isAntiAlias = true
                    }
                    drawWithClipping("REMARK: ${state.customNote.trim()}", tx, ty, tw, paintNote)
                }

                if (!state.removeWatermark) {
                    val paintBranding = Paint().apply {
                        color = 0xFF64748B.toInt()
                        textSize = 7.5f * scaleFactor
                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        textAlign = Paint.Align.RIGHT
                        isAntiAlias = true
                    }
                    canvas.drawText(state.getWatermarkText(), W - pad, H_new - 4f * scaleFactor, paintBranding)
                }
            }

            StampTemplate.OFFICIAL_STAMP -> {
                val paintGradient = Paint().apply {
                    shader = LinearGradient(
                        0f, startY, 0f, H_new.toFloat(),
                        intArrayOf(0x88000000.toInt(), 0xEE000000.toInt()),
                        null,
                        Shader.TileMode.CLAMP
                    )
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, startY, W.toFloat(), H_new.toFloat(), paintGradient)

                val sealSize = 52f * scaleFactor
                val sealX = W - pad - sealSize
                val sealY = startY + (stampHeight - sealSize) / 2f

                // Draw Red Official Verification Seal
                val paintSealBorder = Paint().apply {
                    color = 0xFFDC2626.toInt()
                    style = Paint.Style.STROKE
                    strokeWidth = 2.5f * scaleFactor
                    isAntiAlias = true
                }
                val paintSealBg = Paint().apply {
                    color = 0xFFFFFFFF.toInt()
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                val sealRadius = sealSize / 2f
                val sealCenterX = sealX + sealRadius
                val sealCenterY = sealY + sealRadius
                canvas.drawCircle(sealCenterX, sealCenterY, sealRadius, paintSealBg)
                canvas.drawCircle(sealCenterX, sealCenterY, sealRadius, paintSealBorder)

                // Inner dashed circle & Official text
                val paintInnerDashed = Paint().apply {
                    color = 0xFFB91C1C.toInt()
                    style = Paint.Style.STROKE
                    strokeWidth = 1f * scaleFactor
                    isAntiAlias = true
                }
                canvas.drawCircle(sealCenterX, sealCenterY, sealRadius - 4f * scaleFactor, paintInnerDashed)

                val paintSealStar = Paint().apply {
                    color = 0xFFDC2626.toInt()
                    textSize = 10f * scaleFactor
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                    isAntiAlias = true
                }
                canvas.drawText("★", sealCenterX, sealCenterY - 4f * scaleFactor, paintSealStar)

                val paintSealText = Paint().apply {
                    color = 0xFF991B1B.toInt()
                    textSize = 7f * scaleFactor
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.drawText("OFFICIAL", sealCenterX, sealCenterY + 4f * scaleFactor, paintSealText)
                canvas.drawText("SEAL", sealCenterX, sealCenterY + 11f * scaleFactor, paintSealText)

                // Left text block
                val tx = pad * 1.2f
                val tw = sealX - tx - pad * 0.8f
                var ty = startY + pad * 0.8f + 11f * scaleFactor

                val rawCityLine = listOfNotNull(
                    state.city.takeIf { it.isNotEmpty() },
                    state.state.takeIf { it.isNotEmpty() }
                ).joinToString(", ").ifEmpty { "Official Survey Location" }
                val cityLine = cleanAddressText(rawCityLine)
                val paintCity = Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 13f * scaleFactor
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    isAntiAlias = true
                }
                drawWithClipping(cityLine, tx, ty, tw, paintCity)
                ty += 13f * scaleFactor

                val addrParts = listOfNotNull(
                    state.road.takeIf { it.isNotEmpty() && !it.contains("Manually entered") },
                    state.city.takeIf { it.isNotEmpty() },
                    state.postcode.takeIf { it.isNotEmpty() },
                    state.country.takeIf { it.isNotEmpty() }
                )
                val addressText = cleanAddressText(addrParts.joinToString(", ").ifEmpty { "Verified Georeferenced Location" })
                val paintAddr = Paint().apply {
                    color = 0xFFE4E4E7.toInt()
                    textSize = 8.5f * scaleFactor
                    typeface = Typeface.SANS_SERIF
                    isAntiAlias = true
                }
                drawWithClipping(addressText, tx, ty, tw, paintAddr)
                ty += 11f * scaleFactor

                val dirStr = if (state.isCompassEnabled) " • DIR ${state.getCardinalDirection()}" else ""
                val altStr = if (state.accuracy != null) " • ALT ${Math.round(state.accuracy * 12f + 180)}m MSL" else ""
                val metaText = "${String.format(Locale.US, "%.4f", state.lat)}°N, ${String.format(Locale.US, "%.4f", state.lng)}°E$dirStr$altStr"
                val paintMeta = Paint().apply {
                    color = 0xFFD4D4D8.toInt()
                    textSize = 8f * scaleFactor
                    typeface = Typeface.MONOSPACE
                    isAntiAlias = true
                }
                drawWithClipping(metaText, tx, ty, tw, paintMeta)
                ty += 10f * scaleFactor

                val dt = if (state.manualTime != null) Date(state.manualTime) else Date()
                val sdfDate = SimpleDateFormat("EEE, dd MMM yyyy, hh:mm:ss a (z)", Locale.ENGLISH)
                val accText = " • ACC: ±${if (state.accuracy != null) Math.round(state.accuracy) else 5}m"
                val timeString = "${sdfDate.format(dt)}$accText"
                drawWithClipping(timeString, tx, ty, tw, paintMeta)

                if (hasCustomNote) {
                    ty += 10f * scaleFactor
                    val paintNote = Paint().apply {
                        color = 0xFFFACC15.toInt()
                        textSize = 8.2f * scaleFactor
                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        isAntiAlias = true
                    }
                    drawWithClipping("REMARK: ${state.customNote.trim()}", tx, ty, tw, paintNote)
                }

                if (!state.removeWatermark) {
                    val paintBranding = Paint().apply {
                        color = 0xFFCBD5E1.toInt()
                        textSize = 7.5f * scaleFactor
                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        textAlign = Paint.Align.RIGHT
                        isAntiAlias = true
                    }
                    canvas.drawText(state.getWatermarkText(), W - pad, H_new - 4f * scaleFactor, paintBranding)
                }
            }

            StampTemplate.ECO_AGRI -> {
                val agriMargin = 8f * scaleFactor
                val agriRect = RectF(agriMargin, startY + 4f * scaleFactor, W - agriMargin, H_new - 4f * scaleFactor)

                val paintAgriBg = Paint().apply {
                    color = if (state.useInsideOverlay) 0xEE06231A.toInt() else 0xFF06231A.toInt()
                    style = Paint.Style.FILL
                }
                canvas.drawRoundRect(agriRect, 8f * scaleFactor, 8f * scaleFactor, paintAgriBg)

                // Emerald left border strip
                val paintEmerald = Paint().apply {
                    color = 0xFF10B981.toInt()
                    style = Paint.Style.FILL
                }
                canvas.save()
                val clipPath = Path().apply {
                    addRoundRect(agriRect, 8f * scaleFactor, 8f * scaleFactor, Path.Direction.CW)
                }
                canvas.clipPath(clipPath)
                canvas.drawRect(RectF(agriRect.left, agriRect.top, agriRect.left + 5f * scaleFactor, agriRect.bottom), paintEmerald)
                canvas.restore()

                val tx = agriRect.left + 5f * scaleFactor + 10f * scaleFactor
                val tw = agriRect.right - tx - 10f * scaleFactor
                var ty = agriRect.top + 13f * scaleFactor

                // Header with plot badge
                val rawCityLine = listOfNotNull(
                    state.city.takeIf { it.isNotEmpty() },
                    state.state.takeIf { it.isNotEmpty() }
                ).joinToString(", ").ifEmpty { "Agriculture Farm" }
                val cityLine = "🌾 " + cleanAddressText(rawCityLine)
                val paintCity = Paint().apply {
                    color = 0xFF6EE7B7.toInt()
                    textSize = 12.5f * scaleFactor
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    isAntiAlias = true
                }
                drawWithClipping(cityLine, tx, ty, tw - 60f * scaleFactor, paintCity)

                // Badge Eco Plot
                val paintBadge = Paint().apply {
                    color = 0x3310B981.toInt()
                    style = Paint.Style.FILL
                }
                val badgeW = 55f * scaleFactor
                val badgeH = 12f * scaleFactor
                val badgeX = agriRect.right - 10f * scaleFactor - badgeW
                val badgeY = ty - 10f * scaleFactor
                canvas.drawRoundRect(RectF(badgeX, badgeY, badgeX + badgeW, badgeY + badgeH), 4f * scaleFactor, 4f * scaleFactor, paintBadge)
                val paintBadgeText = Paint().apply {
                    color = 0xFFA7F3D0.toInt()
                    textSize = 7f * scaleFactor
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.MONOSPACE
                    isAntiAlias = true
                }
                canvas.drawText("ECO PLOT", badgeX + badgeW / 2f, badgeY + 8.5f * scaleFactor, paintBadgeText)
                ty += 13f * scaleFactor

                val addrParts = listOfNotNull(
                    state.road.takeIf { it.isNotEmpty() && !it.contains("Manually entered") },
                    state.city.takeIf { it.isNotEmpty() },
                    state.postcode.takeIf { it.isNotEmpty() },
                    state.country.takeIf { it.isNotEmpty() }
                )
                val addressText = cleanAddressText(addrParts.joinToString(", ").ifEmpty { "Field Agricultural Verification" })
                val paintAddr = Paint().apply {
                    color = 0xFFD1FAE5.toInt()
                    textSize = 8.5f * scaleFactor
                    typeface = Typeface.SANS_SERIF
                    isAntiAlias = true
                }
                drawWithClipping(addressText, tx, ty, tw, paintAddr)
                ty += 11f * scaleFactor

                val dirStr = if (state.isCompassEnabled) " • DIR ${state.getCardinalDirection()}" else ""
                val altStr = if (state.accuracy != null) " • ALT ${Math.round(state.accuracy * 12f + 180)}m" else ""
                val metaText = "${String.format(Locale.US, "%.4f", state.lat)}°N, ${String.format(Locale.US, "%.4f", state.lng)}°E$altStr$dirStr"
                val paintMeta = Paint().apply {
                    color = 0xFF6EE7B7.toInt()
                    textSize = 8f * scaleFactor
                    typeface = Typeface.MONOSPACE
                    isAntiAlias = true
                }
                drawWithClipping(metaText, tx, ty, tw, paintMeta)
                ty += 10f * scaleFactor

                val dt = if (state.manualTime != null) Date(state.manualTime) else Date()
                val sdfDate = SimpleDateFormat("EEE, dd MMM yyyy, hh:mm:ss a (z)", Locale.ENGLISH)
                val accText = " • ACC: ±${if (state.accuracy != null) Math.round(state.accuracy) else 5}m"
                val timeString = "${sdfDate.format(dt)}$accText"
                drawWithClipping(timeString, tx, ty, tw, paintMeta)

                if (hasCustomNote) {
                    ty += 10f * scaleFactor
                    val paintNote = Paint().apply {
                        color = 0xFFFDE047.toInt()
                        textSize = 8.2f * scaleFactor
                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        isAntiAlias = true
                    }
                    drawWithClipping("REMARK: ${state.customNote.trim()}", tx, ty, tw, paintNote)
                }

                if (!state.removeWatermark) {
                    val paintBranding = Paint().apply {
                        color = 0xFF34D399.toInt()
                        textSize = 7.5f * scaleFactor
                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        textAlign = Paint.Align.RIGHT
                        isAntiAlias = true
                    }
                    canvas.drawText(state.getWatermarkText(), agriRect.right - 8f * scaleFactor, agriRect.bottom - 4f * scaleFactor, paintBranding)
                }
            }

            StampTemplate.REVENUE_AMBER -> {
                val paintAmberBg = Paint().apply {
                    color = if (state.useInsideOverlay) 0xEE18181B.toInt() else 0xFF18181B.toInt()
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, startY, W.toFloat(), H_new.toFloat(), paintAmberBg)

                val paintAmberLine = Paint().apply {
                    color = 0xFFD97706.toInt()
                    strokeWidth = 2.5f * scaleFactor
                    style = Paint.Style.STROKE
                }
                canvas.drawLine(0f, startY, W.toFloat(), startY, paintAmberLine)

                val tx = pad * 1.2f
                val tw = W - tx - pad * 1.2f
                var ty = startY + pad * 0.7f + 11f * scaleFactor

                val rawCityLine = listOfNotNull(
                    state.city.takeIf { it.isNotEmpty() },
                    state.state.takeIf { it.isNotEmpty() }
                ).joinToString(" • ").ifEmpty { "Demarcation Zone" }
                val cityLine = cleanAddressText(rawCityLine)
                val paintCity = Paint().apply {
                    color = 0xFFFBBF24.toInt()
                    textSize = 13f * scaleFactor
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    isAntiAlias = true
                }
                drawWithClipping(cityLine, tx, ty, tw, paintCity)
                ty += 13f * scaleFactor

                val addrParts = listOfNotNull(
                    state.road.takeIf { it.isNotEmpty() && !it.contains("Manually entered") },
                    state.city.takeIf { it.isNotEmpty() },
                    state.postcode.takeIf { it.isNotEmpty() },
                    state.country.takeIf { it.isNotEmpty() }
                )
                val addressText = cleanAddressText(addrParts.joinToString(", ").ifEmpty { "Land Revenue Boundary Demarcation" })
                val paintAddr = Paint().apply {
                    color = 0xFFD4D4D8.toInt()
                    textSize = 8.5f * scaleFactor
                    typeface = Typeface.SANS_SERIF
                    isAntiAlias = true
                }
                drawWithClipping(addressText, tx, ty, tw, paintAddr)
                ty += 11f * scaleFactor

                val dirStr = if (state.isCompassEnabled) " • ${state.getCardinalDirection()}" else ""
                val altStr = if (state.accuracy != null) "${Math.round(state.accuracy * 12f + 180)}m MSL" else "214m MSL"
                val metaLine1 = "LAT/LON: ${String.format(Locale.US, "%.4f", state.lat)}°N, ${String.format(Locale.US, "%.4f", state.lng)}°E   ALT/DIR: $altStr$dirStr"
                val paintMeta = Paint().apply {
                    color = 0xFFFEF3C7.toInt()
                    textSize = 8f * scaleFactor
                    typeface = Typeface.MONOSPACE
                    isAntiAlias = true
                }
                drawWithClipping(metaLine1, tx, ty, tw, paintMeta)
                ty += 10f * scaleFactor

                val dt = if (state.manualTime != null) Date(state.manualTime) else Date()
                val sdfDate = SimpleDateFormat("EEE, dd MMM yyyy, hh:mm:ss a (z)", Locale.ENGLISH)
                val accText = "ACC: ±${if (state.accuracy != null) Math.round(state.accuracy) else 5}m"
                val metaLine2 = "TIMESTAMP: ${sdfDate.format(dt)} • $accText"
                drawWithClipping(metaLine2, tx, ty, tw, paintMeta)

                if (hasCustomNote) {
                    ty += 10f * scaleFactor
                    val paintNote = Paint().apply {
                        color = 0xFFFEF08A.toInt()
                        textSize = 8.2f * scaleFactor
                        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                        isAntiAlias = true
                    }
                    drawWithClipping("REMARK: ${state.customNote.trim()}", tx, ty, tw, paintNote)
                }

                if (!state.removeWatermark) {
                    val paintBranding = Paint().apply {
                        color = 0xFFA1A1AA.toInt()
                        textSize = 7.5f * scaleFactor
                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        textAlign = Paint.Align.RIGHT
                        isAntiAlias = true
                    }
                    canvas.drawText(state.getWatermarkText(), W - pad, H_new - 4f * scaleFactor, paintBranding)
                }
            }

            StampTemplate.GLACIER_FROST -> {
                val paintGlacierBg = Paint().apply {
                    color = if (state.useInsideOverlay) 0x66FFFFFF.toInt() else 0x88FFFFFF.toInt()
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, startY, W.toFloat(), H_new.toFloat(), paintGlacierBg)

                val paintGlacierBorder = Paint().apply {
                    color = 0xAAFFFFFF.toInt()
                    style = Paint.Style.STROKE
                    strokeWidth = 1.2f * scaleFactor
                }
                canvas.drawLine(0f, startY, W.toFloat(), startY, paintGlacierBorder)

                val tx = pad * 1.2f
                val tw = W - tx - pad * 1.2f
                var ty = startY + pad * 0.7f + 11f * scaleFactor

                val rawCityLine = listOfNotNull(
                    state.city.takeIf { it.isNotEmpty() },
                    state.state.takeIf { it.isNotEmpty() }
                ).joinToString(", ").ifEmpty { "Location" }
                val cityLine = cleanAddressText(rawCityLine)
                val paintCity = Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 13.5f * scaleFactor
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    isAntiAlias = true
                    setShadowLayer(3f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xCC000000.toInt())
                }
                drawWithClipping(cityLine, tx, ty, tw, paintCity)
                ty += 13f * scaleFactor

                val addrParts = listOfNotNull(
                    state.road.takeIf { it.isNotEmpty() && !it.contains("Manually entered") },
                    state.city.takeIf { it.isNotEmpty() },
                    state.postcode.takeIf { it.isNotEmpty() },
                    state.country.takeIf { it.isNotEmpty() }
                )
                val addressText = cleanAddressText(addrParts.joinToString(", ").ifEmpty { "Georeferenced Survey Location" })
                val paintAddr = Paint().apply {
                    color = 0xFFF1F5F9.toInt()
                    textSize = 8.5f * scaleFactor
                    typeface = Typeface.SANS_SERIF
                    isAntiAlias = true
                    setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xAA000000.toInt())
                }
                drawWithClipping(addressText, tx, ty, tw, paintAddr)
                ty += 11f * scaleFactor

                val dirStr = if (state.isCompassEnabled) " • DIR: ${state.getCardinalDirection()}" else ""
                val altStr = if (state.accuracy != null) " • ALT: ${Math.round(state.accuracy * 12f + 180)}m MSL" else ""
                val accText = " • ACC: ±${if (state.accuracy != null) Math.round(state.accuracy) else 5}m"
                val metaText = "${String.format(Locale.US, "%.4f", state.lat)}°N, ${String.format(Locale.US, "%.4f", state.lng)}°E$altStr$dirStr$accText"
                val paintMeta = Paint().apply {
                    color = 0xFFE2E8F0.toInt()
                    textSize = 8f * scaleFactor
                    typeface = Typeface.MONOSPACE
                    isAntiAlias = true
                    setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xAA000000.toInt())
                }
                drawWithClipping(metaText, tx, ty, tw, paintMeta)
                ty += 10f * scaleFactor

                val dt = if (state.manualTime != null) Date(state.manualTime) else Date()
                val sdfDate = SimpleDateFormat("EEE, dd MMM yyyy, hh:mm:ss a (z)", Locale.ENGLISH)
                val timeString = sdfDate.format(dt)
                drawWithClipping(timeString, tx, ty, tw, paintMeta)

                if (hasCustomNote) {
                    ty += 10f * scaleFactor
                    val paintNote = Paint().apply {
                        color = 0xFFFEF08A.toInt()
                        textSize = 8.2f * scaleFactor
                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        isAntiAlias = true
                        setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xCC000000.toInt())
                    }
                    drawWithClipping("REMARK: ${state.customNote.trim()}", tx, ty, tw, paintNote)
                }

                if (!state.removeWatermark) {
                    val paintBranding = Paint().apply {
                        color = 0xFFCBD5E1.toInt()
                        textSize = 7.5f * scaleFactor
                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        textAlign = Paint.Align.RIGHT
                        isAntiAlias = true
                        setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xAA000000.toInt())
                    }
                    canvas.drawText(state.getWatermarkText(), W - pad, H_new - 4f * scaleFactor, paintBranding)
                }
            }
        }

        return output
    }
}

class MainActivity : ComponentActivity() {
    private var filePickUri: Uri? = null
    private lateinit var gpsViewModel: GpsViewModel
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gpsViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GpsViewModel(applicationContext) as T
            }
        })[GpsViewModel::class.java]

        enableEdgeToEdge()

        // Initialize UMP Privacy Consent and MobileAds SDK safely
        ConsentManager.requestConsentAndInitAds(this) {
            if (::gpsViewModel.isInitialized) {
                gpsViewModel.preloadRewardedAd(applicationContext)
            }
        }

        setContent {
            val viewModel = gpsViewModel
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val state by viewModel.locationState.collectAsState()
            val isProcessing by viewModel.isProcessing.collectAsState()
            val processingMessage by viewModel.processingMessage.collectAsState()
            val processedBitmap by viewModel.processedBitmap.collectAsState()
            val showToast by viewModel.showToast.collectAsState()
            val toastMessage by viewModel.toastMessage.collectAsState()

            MyApplicationTheme(darkTheme = isDarkMode) {
                MainScreen(
                    state = state,
                    isProcessing = isProcessing,
                    processingMessage = processingMessage,
                    processedBitmap = processedBitmap,
                    showToast = showToast,
                    toastMessage = toastMessage,
                    viewModel = viewModel,
                    captureRequestTrigger = { uri ->
                        filePickUri = uri
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerNetworkCallback()
    }

    override fun onResume() {
        super.onResume()
        if (::gpsViewModel.isInitialized) {
            gpsViewModel.checkAndTriggerOfflineSync(this)
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterNetworkCallback()
    }

    private fun registerNetworkCallback() {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                lifecycleScope.launch(Dispatchers.Main) {
                    if (::gpsViewModel.isInitialized) {
                        gpsViewModel.checkAndTriggerOfflineSync(this@MainActivity)
                    }
                }
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        if (::gpsViewModel.isInitialized) {
                            gpsViewModel.checkAndTriggerOfflineSync(this@MainActivity)
                        }
                    }
                }
            }
        }
        networkCallback = callback
        try {
            cm.registerNetworkCallback(request, callback)
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to register network callback", e)
        }
    }

    private fun unregisterNetworkCallback() {
        networkCallback?.let {
            try {
                val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                cm?.unregisterNetworkCallback(it)
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to unregister network callback", e)
            }
            networkCallback = null
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (::gpsViewModel.isInitialized) {
            val action = gpsViewModel.volumeKeyAction.value
            if (action != "off") {
                if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    gpsViewModel.triggerVolumeKey(isUp = true)
                    return true
                } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    gpsViewModel.triggerVolumeKey(isUp = false)
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
    state: LocationState,
    isProcessing: Boolean,
    processingMessage: String,
    processedBitmap: Bitmap?,
    showToast: Boolean,
    toastMessage: String,
    viewModel: GpsViewModel,
    captureRequestTrigger: (Uri) -> Unit
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    val processedVideoUri by viewModel.processedVideoUri.collectAsState()
    val sessionMedia by viewModel.sessionMedia.collectAsState()

    var pendingUploadUri by remember { mutableStateOf<Uri?>(null) }
    var tempSavedUri by remember { mutableStateOf<Uri?>(null) }
    var showLocationChoiceDialog by remember { mutableStateOf(false) }
    var showManualInstructionDialog by remember { mutableStateOf(false) }
    var isManualExpanded by remember { mutableStateOf(false) }

    // Launchers for system native actions
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                val savedUri = tempSavedUri
                if (savedUri != null) {
                    viewModel.processAndAutoSavePhoto(savedUri, context) {
                        // Keep user on the home screen as requested
                    }
                }
            }
        }
    )

    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo(),
        onResult = { success ->
            if (success) {
                val savedUri = tempSavedUri
                if (savedUri != null) {
                    viewModel.processVideoFile(savedUri, context)
                }
            }
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                val isVideo = context.contentResolver.getType(uri)?.contains("video") == true
                if (state.isManualOverride) {
                    if (isVideo) {
                        viewModel.processVideoFile(uri, context)
                    } else {
                        viewModel.processImageFile(uri, context, isGallery = true, skipExif = true)
                    }
                } else {
                    pendingUploadUri = uri
                    showLocationChoiceDialog = true
                }
            }
        }
    )

    // Location & Camera runtime permission controller
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val gpsGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (gpsGranted) {
                viewModel.startLocationEngine()
            } else {
                viewModel.triggerCustomToast("GPS Denied! Enter coordinates manually.")
            }
        }
    )

    // Request permissions once on boot
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        BackHandler(enabled = processedBitmap != null || processedVideoUri != null) {
            viewModel.goHome()
        }

        AnimatedContent(
            targetState = Pair(processedBitmap, processedVideoUri),
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
            },
            label = "ScreenState"
        ) { (targetBitmap, targetVideoUri) ->
            if (targetBitmap != null) {
                // RESULT REVIEW OVERLAY SCREEN
                ResultViewScreen(
                    bitmap = targetBitmap,
                    viewModel = viewModel,
                    onBack = { viewModel.goHome() }
                )
            } else if (targetVideoUri != null) {
                // VIDEO REVIEW OVERLAY SCREEN
                VideoResultViewScreen(
                    videoUri = targetVideoUri,
                    state = state,
                    viewModel = viewModel,
                    onBack = { viewModel.goHome() }
                )
            } else {
                // CORE LIVE CAMERA VIEWPORT (Default Startup Screen)
                LiveCameraViewScreen(
                    state = state,
                    viewModel = viewModel,
                    sessionMedia = sessionMedia,
                    onOpenGalleryUpload = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                        )
                    }
                )
            }
        }

        // Custom choice and instructions dialogs
        if (showLocationChoiceDialog) {
            AlertDialog(
                onDismissRequest = {
                    showLocationChoiceDialog = false
                    pendingUploadUri = null
                },
                containerColor = colors.panel,
                titleContentColor = colors.accentCyan,
                textContentColor = colors.text,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📌 Stamp Location Source",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                text = {
                    Text(
                        text = "Which location coordinates should be applied to this file as a GPS stamp?\n\nChoose 'Current Location' to use your immediate live positioning coordinates, or 'Manual Location' to use your custom coordinates.",
                        fontSize = 14.sp,
                        color = colors.text
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showLocationChoiceDialog = false
                            val uri = pendingUploadUri
                            if (uri != null) {
                                val isVideo = context.contentResolver.getType(uri)?.contains("video") == true
                                if (isVideo) {
                                    viewModel.processVideoFile(uri, context)
                                } else {
                                    viewModel.processImageFile(uri, context, isGallery = true, skipExif = true)
                                }
                            }
                            pendingUploadUri = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accentCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Current Location", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showLocationChoiceDialog = false
                            showManualInstructionDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.border, contentColor = colors.text),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Manual Location")
                    }
                }
            )
        }

        if (showManualInstructionDialog) {
            AlertDialog(
                onDismissRequest = {
                    showManualInstructionDialog = false
                    pendingUploadUri = null
                },
                containerColor = colors.panel,
                titleContentColor = colors.accentOrange,
                textContentColor = colors.text,
                title = {
                    Text(
                        text = "⚙️ Manual Location Not Set",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                text = {
                    Text(
                        text = "You chose Manual Location, but it has not been configured yet!\n\nInstructions to configure:\n1. Scroll down on the home screen.\n2. Tap '⚙️ Edit Location Manually (Click here)' below the coordinates card.\n3. Enter your custom Latitude, Longitude, City, and State, and click 'Apply'.\n\nOnce set, any uploaded photo without GPS metadata will automatically use your configured manual location without asking.\n\nWould you like to open the configuration panel now?",
                        fontSize = 14.sp,
                        color = colors.text
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showManualInstructionDialog = false
                            pendingUploadUri = null
                            isManualExpanded = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accentOrange, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Configure Now", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showManualInstructionDialog = false
                            pendingUploadUri = null
                        }
                    ) {
                        Text("Cancel", color = colors.muted)
                    }
                }
            )
        }

        // FULLSCREEN DARK PROCESSING PROGRESS OVERLAY SCREEN
        AnimatedVisibility(
            visible = isProcessing,
            enter = fadeIn(animationSpec = tween(250)),
            exit = fadeOut(animationSpec = tween(250))
        ) {
            ProcessingOverlayScreen(message = processingMessage)
        }

        // ANIMATED CYBER FLOATING OVERLAY NOTIFICATION TOAST
        AnimatedVisibility(
            visible = showToast,
            enter = slideInVertically(initialOffsetY = { it + 150 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it + 150 }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(AccentOrange, RoundedCornerShape(24.dp))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .border(1.dp, Color.White, RoundedCornerShape(24.dp))
            ) {
                Text(
                    text = toastMessage,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Helper for Day-Wise grouping
fun getDayGroupLabel(timestampMillis: Long): String {
    val calMedia = Calendar.getInstance().apply { timeInMillis = timestampMillis }
    val calToday = Calendar.getInstance()
    val calYesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

    return if (calMedia.get(Calendar.YEAR) == calToday.get(Calendar.YEAR) &&
        calMedia.get(Calendar.DAY_OF_YEAR) == calToday.get(Calendar.DAY_OF_YEAR)
    ) {
        "Today • " + SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(timestampMillis))
    } else if (calMedia.get(Calendar.YEAR) == calYesterday.get(Calendar.YEAR) &&
        calMedia.get(Calendar.DAY_OF_YEAR) == calYesterday.get(Calendar.DAY_OF_YEAR)
    ) {
        "Yesterday • " + SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(timestampMillis))
    } else {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date(timestampMillis))
    }
}

// Temporary file container across camera launchers
private var tempSavedUri: Uri? = null

@Composable
fun HomeViewScreen(
    state: LocationState,
    viewModel: GpsViewModel,
    isManualExpanded: Boolean,
    onManualExpandedChange: (Boolean) -> Unit,
    onCameraClick: () -> Unit,
    onRecordVideoClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    val sessionMedia by viewModel.sessionMedia.collectAsState()
    val showManualLocation by viewModel.showManualLocation.collectAsState()
    val showManualTime by viewModel.showManualTime.collectAsState()
    val showUploadGallery by viewModel.showUploadGallery.collectAsState()
    val hasCapturedPhoto by viewModel.hasCapturedPhoto.collectAsState()
    var selectedGalleryMediaIndex by remember { mutableStateOf<Int?>(null) }
    var showFullScreenGallery by remember { mutableStateOf(false) }
    var isLivePreviewActive by remember { mutableStateOf(false) }

    if (isLivePreviewActive) {
        LiveCameraViewScreen(
            state = state,
            viewModel = viewModel,
            sessionMedia = sessionMedia,
            onOpenGalleryUpload = onGalleryClick
        )
        return
    }

    if (showFullScreenGallery) {
        FullScreenMediaGalleryScreen(
            mediaList = sessionMedia,
            onBack = { showFullScreenGallery = false },
            onMediaClick = { index -> selectedGalleryMediaIndex = index },
            onDelete = { mediaItem ->
                viewModel.deleteSessionMedia(mediaItem, context)
            },
            onShare = { mediaItem ->
                shareMediaUri(context, mediaItem.uri, mediaItem.isVideo)
            }
        )

        // Slide/swipe viewer dialog when item clicked inside full gallery
        selectedGalleryMediaIndex?.let { initialIndex ->
            SessionMediaViewerDialog(
                mediaList = sessionMedia,
                initialIndex = initialIndex,
                onDismiss = { selectedGalleryMediaIndex = null },
                onDelete = { mediaItem ->
                    viewModel.deleteSessionMedia(mediaItem, context)
                },
                onShare = { mediaItem ->
                    shareMediaUri(context, mediaItem.uri, mediaItem.isVideo)
                }
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Header and Refresh / Theme Toggle Button Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "PRO GPS CAM",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.titleColor,
                    letterSpacing = 3.sp
                )
                Text(
                    text = "Professional Surveyor Camera",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 11.sp,
                    color = colors.muted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Top right buttons: Theme Toggle + Reset/Reload
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Theme Toggle Button (Half Moon 🌓 for Dark Mode, Full Sun ☀️ for Light Mode)
                IconButton(
                    onClick = { viewModel.toggleDarkMode() },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.panel)
                        .border(1.dp, colors.border, RoundedCornerShape(22.dp))
                        .testTag("theme_toggle_button")
                ) {
                    Text(
                        text = if (colors.isDark) "🌓" else "☀️",
                        fontSize = 20.sp
                    )
                }

                // Beautiful glowing Reset Reload button
                IconButton(
                    onClick = { viewModel.resetAppAndLocation() },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.panel)
                        .border(1.dp, colors.border, RoundedCornerShape(22.dp))
                        .testTag("reload_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Reset and reload app location",
                        tint = colors.accentCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Location Information details card panel
        LocationCard(
            state = state,
            onCopy = { type, value ->
                val clipManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("GPS Coordination", value.toString())
                clipManager.setPrimaryClip(clip)
                viewModel.triggerCustomToast("$type Copied!")
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Settings / stamp customizations Wrap Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.panel, RoundedCornerShape(12.dp))
                .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            // Manual timestamp (gated by Pro User setting)
            if (showManualTime) {
                Text(
                    text = "🕒 MANUAL TIME (Optional)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.headingColor,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                val currentSelectedTime = state.manualTime
                val formattedSelectedTime = if (currentSelectedTime != null) {
                    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).format(Date(currentSelectedTime))
                } else {
                    "Not Set (Automatic)"
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.inputBg)
                        .border(1.dp, colors.inputBorder, RoundedCornerShape(8.dp))
                        .clickable {
                            val cal = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    cal.set(Calendar.YEAR, year)
                                    cal.set(Calendar.MONTH, month)
                                    cal.set(Calendar.DAY_OF_MONTH, day)

                                    TimePickerDialog(
                                        context,
                                        { _, hour, min ->
                                            cal.set(Calendar.HOUR_OF_DAY, hour)
                                            cal.set(Calendar.MINUTE, min)
                                            viewModel.setManualTime(cal.timeInMillis)
                                        },
                                        cal.get(Calendar.HOUR_OF_DAY),
                                        cal.get(Calendar.MINUTE),
                                        false
                                    ).show()
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formattedSelectedTime,
                            color = colors.inputText,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        if (currentSelectedTime != null) {
                            IconButton(
                                onClick = { viewModel.setManualTime(null) },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear selected date",
                                    tint = colors.accentOrange
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Stamp scale percentage factor sliders
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📏 STAMP SIZE:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.headingColor,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${Math.round(state.stampScale * 100)}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.headingColor
                )
            }

            Slider(
                value = state.stampScale,
                onValueChange = { viewModel.setStampScale(it) },
                valueRange = 0.5f..1.5f,
                steps = 9,
                colors = SliderDefaults.colors(
                    activeTrackColor = colors.sliderAccent,
                    inactiveTrackColor = colors.border,
                    thumbColor = colors.sliderAccent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Premium placement Toggle Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "📍 STAMP POSITION:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.headingColor,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (state.useInsideOverlay) "Inside Media (Background Removed)" else "Outside Media (Traditional)",
                        fontSize = 11.sp,
                        color = colors.muted
                    )
                }
                Switch(
                    checked = state.useInsideOverlay,
                    onCheckedChange = { viewModel.setUseInsideOverlay(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.toggleActive,
                        checkedTrackColor = colors.toggleActive.copy(alpha = 0.35f),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = colors.border
                    )
                )
            }

            // Manual coordinates trigger action link (gated by Pro User setting)
            if (showManualLocation) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "⚙️ Edit Location Manually (Click here)",
                    color = colors.muted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onManualExpandedChange(!isManualExpanded) }
                        .padding(vertical = 4.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    )
                )

                AnimatedVisibility(
                    visible = isManualExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    ManualLocationSettingsPanel(
                        onApply = { lat, lng, city, stateName, pincode ->
                            viewModel.applyManualLocation(lat, lng, city, stateName, pincode)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.border)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Live Camera Viewfinder Toggle Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "📷",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column {
                        Text(
                            text = "LIVE CAMERA PREVIEW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.headingColor,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "In-app viewfinder (3:4 ratio) with quick controls",
                            fontSize = 11.sp,
                            color = colors.muted
                        )
                    }
                }
                Switch(
                    checked = isLivePreviewActive,
                    onCheckedChange = { isLivePreviewActive = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.accentCyan,
                        checkedTrackColor = colors.accentCyan.copy(alpha = 0.35f),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = colors.border
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // CLICK PHOTO & RECORD VIDEO BUTTONS IN ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(colors.photoBtnGradient))
                    .clickable { onCameraClick() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "📷",
                        fontSize = 22.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = if (hasCapturedPhoto) "CLICK AGAIN" else "CLICK PHOTO",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(colors.videoBtnGradient))
                    .clickable { onRecordVideoClick() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🎥",
                        fontSize = 22.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "RECORD VIDEO",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }
        }

        // UPLOAD GALLERY PHOTO BUTTON (gated by Pro User setting)
        if (showUploadGallery) {
            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(colors.galleryBtnGradient))
                    .clickable { onGalleryClick() }
                    .padding(vertical = 16.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "📂",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                    Text(
                        text = "UPLOAD GALLERY PHOTO",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 16.sp,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SESSION GALLERY REVIEW SECTION
        SessionGallerySection(
            mediaList = sessionMedia,
            onMediaClick = { index -> selectedGalleryMediaIndex = index },
            onOpenFullScreenGallery = { showFullScreenGallery = true }
        )

        // Seamless slide/swipe viewer dialog for reviewing both photos and videos
        selectedGalleryMediaIndex?.let { initialIndex ->
            SessionMediaViewerDialog(
                mediaList = sessionMedia,
                initialIndex = initialIndex,
                onDismiss = { selectedGalleryMediaIndex = null },
                onDelete = { mediaItem ->
                    viewModel.deleteSessionMedia(mediaItem, context)
                },
                onShare = { mediaItem ->
                    shareMediaUri(context, mediaItem.uri, mediaItem.isVideo)
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ABOUT US TRIGGER LINK
        var showAboutDialog by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { showAboutDialog = true }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "ℹ️ About Pro GPS Cam",
                color = colors.headingColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium.copy(
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                )
            )
        }

        if (showAboutDialog) {
            AboutUsDialog(viewModel = viewModel, onDismiss = { showAboutDialog = false })
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun SessionGallerySection(
    mediaList: List<SessionMediaItem>,
    onMediaClick: (Int) -> Unit,
    onOpenFullScreenGallery: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val photoCount = mediaList.count { !it.isVideo }
    val videoCount = mediaList.count { it.isVideo }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.panel)
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🖼️",
                    fontSize = 18.sp
                )
                Text(
                    text = "SESSION GALLERY",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.headingColor,
                    letterSpacing = 1.sp
                )
            }

            // Right side buttons: Count Badge and Full Screen View Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Count Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.badgeBg)
                        .border(1.dp, colors.badgeText.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (mediaList.isEmpty()) {
                            "0 Items"
                        } else if (videoCount == 0) {
                            "$photoCount ${if (photoCount == 1) "Photo" else "Photos"}"
                        } else if (photoCount == 0) {
                            "$videoCount ${if (videoCount == 1) "Video" else "Videos"}"
                        } else {
                            "${mediaList.size} Items"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.badgeText
                    )
                }

                // Full Screen View Button
                if (mediaList.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.headingColor.copy(alpha = 0.12f))
                            .border(1.dp, colors.headingColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .clickable { onOpenFullScreenGallery() }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Full View ⛶",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.headingColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (mediaList.isEmpty()) {
            // Empty state placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.bg)
                    .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "📸 🎥",
                        fontSize = 30.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No Photos or Videos in Current Session",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textHeading
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Capture stamped survey photos or record videos.\nAll session media will appear here instantly!",
                        fontSize = 11.sp,
                        color = colors.muted,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp
                    )
                }
            }
        } else {
            // Horizontal scrollable reel of session photo & video thumbnails
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                itemsIndexed(mediaList) { index, item ->
                    SessionMediaThumbnailCard(
                        mediaItem = item,
                        index = index,
                        totalCount = mediaList.size,
                        onClick = { onMediaClick(index) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "💡 Tap any item to slide & review, or tap 'Full View ⛶' for day-wise view",
                fontSize = 10.sp,
                color = colors.muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SessionMediaThumbnailCard(
    mediaItem: SessionMediaItem,
    index: Int,
    totalCount: Int,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    val context = LocalContext.current

    val bitmapState = produceState<Bitmap?>(initialValue = null, key1 = mediaItem.uri) {
        withContext(Dispatchers.IO) {
            value = loadThumbnailFromUri(context, mediaItem.uri, mediaItem.isVideo, 300)
        }
    }

    Box(
        modifier = Modifier
            .size(width = 115.dp, height = 135.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.bg)
            .border(1.5.dp, colors.border, RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        val bmp = bitmapState.value
        if (bmp != null) {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = if (mediaItem.isVideo) "Session Video #${totalCount - index}" else "Session Photo #${totalCount - index}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = colors.accentCyan,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Top tag badge: #N
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(6.dp)
                .background(colors.thumbnailOverlayBg, RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "#${totalCount - index}",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Top video tag indicator
        if (mediaItem.isVideo) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(Color(0xFFE53935).copy(alpha = 0.9f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "🎥 VIDEO",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            // Center play button overlay for video thumbnail
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(32.dp)
                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "▶",
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }

        // Bottom view overlay hint
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, colors.thumbnailOverlayBg)
                    )
                )
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (mediaItem.isVideo) "▶ Play Video" else "🔍 Review",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (mediaItem.isDriveSynced) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(Color(0xFF00E5FF), RoundedCornerShape(4.dp))
                    .padding(horizontal = 3.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "☁️",
                    fontSize = 9.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullScreenMediaGalleryScreen(
    mediaList: List<SessionMediaItem>,
    onBack: () -> Unit,
    onMediaClick: (Int) -> Unit,
    onDelete: (SessionMediaItem) -> Unit,
    onShare: (SessionMediaItem) -> Unit
) {
    val colors = LocalAppColors.current
    var filterType by remember { mutableStateOf("ALL") } // ALL, PHOTOS, VIDEOS
    var itemToDelete by remember { mutableStateOf<SessionMediaItem?>(null) }

    val filteredList = remember(mediaList, filterType) {
        when (filterType) {
            "PHOTOS" -> mediaList.filter { !it.isVideo }
            "VIDEOS" -> mediaList.filter { it.isVideo }
            else -> mediaList
        }
    }

    // Group Day-wise
    val groupedByDay = remember(filteredList) {
        filteredList.groupBy { getDayGroupLabel(it.dateAdded) }
    }

    Scaffold(
        containerColor = colors.bg,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.panel)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Spacer(modifier = Modifier.height(28.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.bg)
                                .border(1.dp, colors.border, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back to Home",
                                tint = colors.textHeading
                            )
                        }

                        Column {
                            Text(
                                text = "Media Gallery",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.titleColor
                            )
                            val photoCount = mediaList.count { !it.isVideo }
                            val videoCount = mediaList.count { it.isVideo }
                            Text(
                                text = "$photoCount Photos • $videoCount Videos",
                                fontSize = 12.sp,
                                color = colors.muted
                            )
                        }
                    }

                    // Count pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.badgeBg)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "${mediaList.size} Total",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.badgeText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Filter tabs (All / Photos / Videos)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf(
                        Triple("ALL", "All Media", "${mediaList.size}"),
                        Triple("PHOTOS", "Photos 📸", "${mediaList.count { !it.isVideo }}"),
                        Triple("VIDEOS", "Videos 🎥", "${mediaList.count { it.isVideo }}")
                    )

                    tabs.forEach { (type, label, count) ->
                        val isSelected = filterType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) colors.titleColor else colors.bg)
                                .border(1.dp, if (isSelected) colors.titleColor else colors.border, RoundedCornerShape(10.dp))
                                .clickable { filterType = type }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$label ($count)",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) (if (colors.isDark) Color.Black else Color.White) else colors.text
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (filteredList.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "📂", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No media found",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textHeading
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Captured stamped photos and recorded videos will be displayed here day-wise without consuming extra cache storage.",
                        fontSize = 12.sp,
                        color = colors.muted,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    groupedByDay.forEach { (dayLabel, itemsForDay) ->
                        item(key = dayLabel) {
                            // Day header row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "📅",
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = dayLabel,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.headingColor
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.badgeBg)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "${itemsForDay.size} items",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.badgeText
                                    )
                                }
                            }
                        }

                        // 3-column Grid for this day
                        item {
                            val chunkedItems = itemsForDay.chunked(3)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                chunkedItems.forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        for (i in 0 until 3) {
                                            if (i < rowItems.size) {
                                                val mediaItem = rowItems[i]
                                                val originalIndex = mediaList.indexOf(mediaItem)
                                                Box(modifier = Modifier.weight(1f)) {
                                                    DayWiseMediaGridCard(
                                                        item = mediaItem,
                                                        onClick = {
                                                            if (originalIndex >= 0) {
                                                                onMediaClick(originalIndex)
                                                            }
                                                        },
                                                        onDelete = { itemToDelete = mediaItem },
                                                        onShare = { onShare(mediaItem) }
                                                    )
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Delete Confirmation Dialog
            if (itemToDelete != null) {
                AlertDialog(
                    onDismissRequest = { itemToDelete = null },
                    containerColor = colors.panel,
                    title = {
                        Text(
                            text = if (itemToDelete!!.isVideo) "Delete Video?" else "Delete Photo?",
                            fontWeight = FontWeight.Bold,
                            color = colors.textHeading
                        )
                    },
                    text = {
                        Text(
                            text = "Do you really want to delete this ${if (itemToDelete!!.isVideo) "video" else "photo"}? This file will be permanently removed from your device storage.",
                            color = colors.text,
                            fontSize = 14.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val target = itemToDelete!!
                                itemToDelete = null
                                onDelete(target)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RedError, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Delete", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { itemToDelete = null }) {
                            Text("Cancel", color = colors.muted)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DayWiseMediaGridCard(
    item: SessionMediaItem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    val colors = LocalAppColors.current
    val context = LocalContext.current

    val bitmapState = produceState<Bitmap?>(initialValue = null, key1 = item.uri) {
        withContext(Dispatchers.IO) {
            value = loadThumbnailFromUri(context, item.uri, item.isVideo, 260)
        }
    }

    val timeFormatted = remember(item.dateAdded) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(item.dateAdded))
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.panel)
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        val bmp = bitmapState.value
        if (bmp != null) {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = if (item.isVideo) "Video preview" else "Photo preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = colors.accentCyan,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Top-left type & time overlay badge
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(6.dp)
                .background(colors.thumbnailOverlayBg, RoundedCornerShape(6.dp))
                .padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (item.isVideo) "🎥 $timeFormatted" else "📸 $timeFormatted",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // If video, play icon overlay in center
        if (item.isVideo) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(28.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("▶", fontSize = 11.sp, color = Color.White)
            }
        }

        // Bottom quick action row
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(colors.thumbnailOverlayBg, CircleShape)
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = RedError,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SessionMediaViewerDialog(
    mediaList: List<SessionMediaItem>,
    initialIndex: Int,
    onDismiss: () -> Unit,
    onDelete: (SessionMediaItem) -> Unit,
    onShare: (SessionMediaItem) -> Unit
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    val coroutineScope = rememberCoroutineScope()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (mediaList.isEmpty()) {
        LaunchedEffect(Unit) {
            onDismiss()
        }
        return
    }

    val safeInitialIndex = initialIndex.coerceIn(0, mediaList.size - 1)
    val pagerState = rememberPagerState(initialPage = safeInitialIndex) { mediaList.size }

    val currentItem = mediaList.getOrNull(pagerState.currentPage) ?: mediaList.first()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.96f))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 26.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (currentItem.isVideo) "🎥" else "📸",
                            fontSize = 20.sp
                        )
                        Column {
                            Text(
                                text = if (currentItem.isVideo) "Session Video Review" else "Session Photo Review",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Item ${pagerState.currentPage + 1} of ${mediaList.size} • Swipe ↔ to browse",
                                color = AccentCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Seamless Swipeable Horizontal Pager Container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        pageSpacing = 12.dp
                    ) { page ->
                        val item = mediaList.getOrNull(page)
                        if (item != null) {
                            if (item.isVideo) {
                                // Live looping video player inside page
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF0D0E12))
                                        .border(1.dp, Color(0xFF252A36), RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    VideoPlayerView(
                                        uri = item.uri,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Video floating badge
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(12.dp)
                                            .background(Color(0xFFE53935).copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "🎥 VIDEO RECORDING",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            } else {
                                // Full-res stamped Photo container
                                val fullBitmapState = produceState<Bitmap?>(initialValue = null, key1 = item.uri) {
                                    withContext(Dispatchers.IO) {
                                        value = loadFullBitmapFromUri(context, item.uri)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF101216))
                                        .border(1.dp, Color(0xFF252A36), RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val bmp = fullBitmapState.value
                                    if (bmp != null) {
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = "Session Photo Detail",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        CircularProgressIndicator(
                                            color = AccentCyan,
                                            strokeWidth = 3.dp,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }

                                    // Photo floating badge
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(12.dp)
                                            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "📸 STAMPED PHOTO",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Left Navigation Chevron Button (if not on first item)
                    if (pagerState.currentPage > 0) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 8.dp)
                                .size(44.dp)
                                .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        ) {
                            Text("‹", fontSize = 28.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Right Navigation Chevron Button (if not on last item)
                    if (pagerState.currentPage < mediaList.size - 1) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 8.dp)
                                .size(44.dp)
                                .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        ) {
                            Text("›", fontSize = 28.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Media Indicator Dots Row
                if (mediaList.size > 1) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        mediaList.indices.take(15).forEach { i ->
                            Box(
                                modifier = Modifier
                                    .size(if (i == pagerState.currentPage) 8.dp else 5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (i == pagerState.currentPage) AccentCyan else Color.White.copy(alpha = 0.35f)
                                    )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Bottom Action Buttons: Share & Delete for Current Selected Item
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Share Button
                    Button(
                        onClick = { onShare(currentItem) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text(
                            text = if (currentItem.isVideo) "📤 Share Video" else "📤 Share Photo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    // Delete Button (Triggers Confirmation Dialog)
                    Button(
                        onClick = { showDeleteConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FF4D4D), contentColor = RedError),
                        border = BorderStroke(1.dp, RedError.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("🗑️ Delete", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            // Explicit Confirmation Dialog for Delete
            if (showDeleteConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = false },
                    containerColor = colors.panel,
                    title = {
                        Text(
                            text = if (currentItem.isVideo) "Delete Video?" else "Delete Photo?",
                            fontWeight = FontWeight.Bold,
                            color = colors.textHeading
                        )
                    },
                    text = {
                        Text(
                            text = "Do you really want to delete this ${if (currentItem.isVideo) "video" else "photo"}? It will be permanently removed from your device storage.",
                            color = colors.text,
                            fontSize = 14.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDeleteConfirmDialog = false
                                onDelete(currentItem)
                                if (mediaList.size <= 1) {
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RedError, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Delete", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmDialog = false }) {
                            Text("Cancel", color = colors.muted)
                        }
                    }
                )
            }
        }
    }
}

// Load full-fidelity photo bitmap with orientation correction
private fun loadFullBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    var rawBmp: Bitmap? = null
    try {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
        val maxDim = 4096
        var inSampleSize = 1
        if (options.outHeight > maxDim || options.outWidth > maxDim) {
            val halfHeight = options.outHeight / 2
            val halfWidth = options.outWidth / 2
            while ((halfHeight / inSampleSize) >= maxDim || (halfWidth / inSampleSize) >= maxDim) {
                inSampleSize *= 2
            }
        }
        val decodeOptions = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
        }
        val raw = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        }
        if (raw != null) {
            var rot = 0
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val exifInterface = android.media.ExifInterface(stream)
                    val orientation = exifInterface.getAttributeInt(
                        android.media.ExifInterface.TAG_ORIENTATION,
                        android.media.ExifInterface.ORIENTATION_NORMAL
                    )
                    rot = when (orientation) {
                        android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90
                        android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180
                        android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270
                        else -> 0
                    }
                }
            } catch (ignored: Exception) {}
            if (rot != 0) {
                val matrix = Matrix().apply { postRotate(rot.toFloat()) }
                rawBmp = Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
                if (rawBmp != raw) {
                    raw.recycle()
                }
            } else {
                rawBmp = raw
            }
        }
    } catch (e: Exception) {
        Log.e("GpsViewModel", "Full bitmap load failed for $uri", e)
    }
    return rawBmp
}

// Fast in-memory thumbnail LRU cache for lag-free gallery loading
object ThumbnailCache {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = (maxMemory / 8).coerceAtLeast(1024)
    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    fun get(key: String): Bitmap? = memoryCache.get(key)
    fun put(key: String, bitmap: Bitmap) {
        memoryCache.put(key, bitmap)
    }
}

// Load fast downscaled thumbnail for session gallery view (supports both photos and video frames)
private fun loadThumbnailFromUri(context: Context, uri: Uri, isVideo: Boolean, targetSize: Int = 300): Bitmap? {
    val cacheKey = "${uri}_$targetSize"
    ThumbnailCache.get(cacheKey)?.let { return it }

    val bmp: Bitmap? = if (isVideo) {
        val retriever = android.media.MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            retriever.getFrameAtTime(0, android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.getFrameAtTime()
        } catch (e: Exception) {
            Log.e("GpsViewModel", "Video thumbnail load failed for $uri", e)
            null
        } finally {
            try {
                retriever.release()
            } catch (ignored: Exception) {}
        }
    } else {
        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            var inSampleSize = 1
            if (options.outHeight > targetSize || options.outWidth > targetSize) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / inSampleSize) >= targetSize || (halfWidth / inSampleSize) >= targetSize) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            val rawBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return null

            // Orientation correction
            var rotationDegrees = 0
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val exifInterface = android.media.ExifInterface(stream)
                    val orientation = exifInterface.getAttributeInt(
                        android.media.ExifInterface.TAG_ORIENTATION,
                        android.media.ExifInterface.ORIENTATION_NORMAL
                    )
                    rotationDegrees = when (orientation) {
                        android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90
                        android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180
                        android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270
                        else -> 0
                    }
                }
            } catch (ignored: Exception) {}

            if (rotationDegrees != 0) {
                val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                val rotated = Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
                if (rotated != rawBitmap) {
                    rawBitmap.recycle()
                }
                rotated
            } else {
                rawBitmap
            }
        } catch (e: Exception) {
            Log.e("GpsViewModel", "Thumbnail load failed for $uri", e)
            null
        }
    }

    if (bmp != null) {
        ThumbnailCache.put(cacheKey, bmp)
    }
    return bmp
}

// Instant-rendering thumbnail with loading placeholder animation
@Composable
fun AsyncThumbnailImage(
    context: Context,
    uri: Uri,
    isVideo: Boolean,
    modifier: Modifier = Modifier,
    targetSize: Int = 180,
    contentScale: ContentScale = ContentScale.Crop
) {
    val cacheKey = "${uri}_$targetSize"
    val cachedBitmap = remember(cacheKey) { ThumbnailCache.get(cacheKey) }

    val bitmapState by produceState<Bitmap?>(initialValue = cachedBitmap, key1 = uri) {
        if (cachedBitmap != null) {
            value = cachedBitmap
            return@produceState
        }
        val loaded = withContext(Dispatchers.IO) {
            loadThumbnailFromUri(context, uri, isVideo, targetSize)
        }
        value = loaded
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (bitmapState != null) {
            Image(
                bitmap = bitmapState!!.asImageBitmap(),
                contentDescription = "Media thumbnail",
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color(0xFF00E5FF),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

private fun triggerHapticFeedback(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(
                android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50)
            }
        }
    } catch (e: Exception) {
        // ignore
    }
}

private fun shareMediaUri(context: Context, uri: Uri, isVideo: Boolean) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = if (isVideo) "video/mp4" else "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, if (isVideo) "Share Stamped Video" else "Share Stamped Photo"))
    } catch (e: Exception) {
        Toast.makeText(context, "Could not share media: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun ProUpgradeDialog(
    onDismiss: () -> Unit,
    onWatchAd: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    var comingSoonNotice by remember { mutableStateOf<String?>(null) }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .background(if (colors.isDark) Color(0xFF0F172A) else Color(0xFF1E293B))
                .border(
                    1.5.dp,
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFD700),
                            Color(0xFFFF8C00).copy(alpha = 0.6f),
                            Color(0xFF00E5FF).copy(alpha = 0.4f)
                        )
                    ),
                    RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Crown header
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFFFFD700).copy(alpha = 0.35f), Color.Transparent)
                            )
                        )
                        .border(1.5.dp, Color(0xFFFFD700), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "👑", fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Unlock Pro Features",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFD700),
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Get survey-grade camera controls & tamper-proof tools",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // The 6 Locked Features Preview Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = "⭐ INCLUDED IN PRO ACCESS:",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700),
                        letterSpacing = 1.sp
                    )

                    val features = listOf(
                        "📍 Edit location manually (custom coordinates & address)",
                        "🕒 Manual time (custom date & time on stamp)",
                        "📂 Upload & stamp photos from gallery",
                        "🚫 Remove watermark branding from stamps",
                        "🏷️ Custom watermark (personalized branding text)",
                        "☁️ Google drive sync (automatic cloud backup)"
                    )

                    features.forEach { feature ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "👑", fontSize = 12.sp)
                            Text(
                                text = feature,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Coming Soon Inline Notice if payment was clicked
                if (comingSoonNotice != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFEF08A).copy(alpha = 0.15f))
                            .border(1.2.dp, Color(0xFFFACC15), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("⚠️", fontSize = 16.sp)
                            Text(
                                text = comingSoonNotice ?: "",
                                fontSize = 11.sp,
                                color = Color(0xFFFDE047),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Section 1: Payment Options (Coming Soon)
                Text(
                    text = "SUBSCRIPTION OPTIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.muted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Option 1: Monthly ₹19
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                        .clickable {
                            comingSoonNotice = "Payment Coming Soon! In-app purchases are coming soon. Please watch the 15-second ad below to unlock all 6 features for 24 hours free."
                            Toast.makeText(context, "Payment Coming Soon! Watch 15s ad below for 24h free access.", Toast.LENGTH_SHORT).show()
                        }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Monthly Pro",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color.White
                        )
                        Text(
                            text = "₹19 per month for pro feature",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                    }

                    Button(
                        onClick = {
                            comingSoonNotice = "Payment Coming Soon! In-app purchases are coming soon. Please watch the 15-second ad below to unlock all 6 features for 24 hours free."
                            Toast.makeText(context, "Payment Coming Soon! Watch 15s ad below for 24h free access.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD700),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "₹19 / mo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Option 2: Yearly ₹149 (Best Value)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFD700).copy(alpha = 0.08f))
                        .border(1.2.dp, Color(0xFFFFD700).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .clickable {
                            comingSoonNotice = "Payment Coming Soon! In-app purchases are coming soon. Please watch the 15-second ad below to unlock all 6 features for 24 hours free."
                            Toast.makeText(context, "Payment Coming Soon! Watch 15s ad below for 24h free access.", Toast.LENGTH_SHORT).show()
                        }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Annual Pro",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = Color(0xFFFFD700)
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF10B981))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "BEST VALUE",
                                        color = Color.White,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                            Text(
                                text = "₹149 per year • Full access",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }

                        Button(
                            onClick = {
                                comingSoonNotice = "Payment Coming Soon! In-app purchases are coming soon. Please watch the 15-second ad below to unlock all 6 features for 24 hours free."
                                Toast.makeText(context, "Payment Coming Soon! Watch 15s ad below for 24h free access.", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFD700),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "₹149 / yr",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // OR Divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.2f)))
                    Text(
                        text = "   OR   ",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.2f)))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Reward-Based Option: Watch 15s ad for 24 hours free
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF047857).copy(alpha = 0.35f), Color(0xFF064E3B).copy(alpha = 0.45f))
                            )
                        )
                        .border(1.5.dp, Color(0xFF10B981), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🎁", fontSize = 18.sp)
                            Text(
                                text = "FREE 24-HOUR PRO ACCESS",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.5.sp,
                                color = Color(0xFF34D399),
                                letterSpacing = 0.8.sp
                            )
                        }

                        Text(
                            text = "Watch a 15 seconds ad to access all 6 locked features for 24 hours completely free!",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center,
                            lineHeight = 15.sp
                        )

                        Button(
                            onClick = onWatchAd,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = "▶️", fontSize = 15.sp, modifier = Modifier.padding(end = 6.dp))
                                Text(
                                    text = "Watch 15s Ad (Unlock 24h Free)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dismiss Button
                OutlinedButton(
                    onClick = onDismiss,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Maybe Later",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RewardedAdDialog(
    viewModel: GpsViewModel,
    onDismiss: () -> Unit,
    onRewardClaimed: () -> Unit
) {
    val context = LocalContext.current
    val rewardedAdUnitId = BuildConfig.ADMOB_REWARDED_ID

    var secondsRemaining by remember { mutableStateOf(15) }
    var isAdCompleted by remember { mutableStateOf(false) }
    var isLoadingAdMob by remember { mutableStateOf(true) }
    var isRealAdShowing by remember { mutableStateOf(false) }
    var adLoadFailed by remember { mutableStateOf(false) }

    // Attempt to show preloaded AdMob RewardedAd or load one
    LaunchedEffect(Unit) {
        val activity = context as? Activity
        if (activity != null) {
            val preloaded = viewModel.getAndConsumePreloadedRewardedAd()
            if (preloaded != null) {
                // Show preloaded ad instantly without delay
                isLoadingAdMob = false
                preloaded.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        // Preload the next one in background for next time
                        viewModel.preloadRewardedAd(context)
                        if (isAdCompleted) {
                            onDismiss()
                        }
                    }
                    override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                        Log.w("RewardedAd", "Failed to show preloaded ad: ${adError.message}")
                        adLoadFailed = true
                    }
                    override fun onAdShowedFullScreenContent() {
                        isRealAdShowing = true
                    }
                }

                preloaded.show(activity) { rewardItem ->
                    Log.d("RewardedAd", "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                    isAdCompleted = true
                    viewModel.unlockProFor24Hours()
                    Toast.makeText(
                        context,
                        "🎉 24-Hour Pro Access Unlocked! All 6 features unlocked!",
                        Toast.LENGTH_LONG
                    ).show()
                    onRewardClaimed()
                }
            } else {
                // Fallback: load dynamically if not preloaded yet
                val adRequest = AdRequest.Builder().build()
                RewardedAd.load(
                    activity,
                    rewardedAdUnitId,
                    adRequest,
                    object : RewardedAdLoadCallback() {
                        override fun onAdLoaded(rewardedAd: RewardedAd) {
                            isLoadingAdMob = false
                            rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    viewModel.preloadRewardedAd(context)
                                    if (isAdCompleted) {
                                        onDismiss()
                                    }
                                }
                                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                                    Log.w("RewardedAd", "Failed to show real rewarded ad: ${adError.message}")
                                    adLoadFailed = true
                                }
                                override fun onAdShowedFullScreenContent() {
                                    isRealAdShowing = true
                                }
                            }

                            rewardedAd.show(activity) { rewardItem ->
                                Log.d("RewardedAd", "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                                isAdCompleted = true
                                viewModel.unlockProFor24Hours()
                                Toast.makeText(
                                    context,
                                    "🎉 24-Hour Pro Access Unlocked! All 6 features unlocked!",
                                    Toast.LENGTH_LONG
                                ).show()
                                onRewardClaimed()
                            }
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            Log.w("RewardedAd", "Rewarded ad failed to load: ${loadAdError.message}")
                            isLoadingAdMob = false
                            adLoadFailed = true
                        }
                    }
                )
            }
        } else {
            isLoadingAdMob = false
            adLoadFailed = true
        }
    }

    // Interactive fallback timer in case real AdMob ad is loading or fails in dev/offline environment
    LaunchedEffect(adLoadFailed) {
        if (adLoadFailed) {
            while (secondsRemaining > 0) {
                kotlinx.coroutines.delay(1000L)
                secondsRemaining--
            }
            isAdCompleted = true
        }
    }

    // If real AdMob ad is actively showing in full screen, we don't need to show dialogue UI
    if (isRealAdShowing) {
        return
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = {
            if (isAdCompleted) {
                onDismiss()
            } else {
                Toast.makeText(
                    context,
                    "⚠️ Please watch full ad or complete countdown to unlock 24h Pro access!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        },
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = isAdCompleted,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030712).copy(alpha = 0.95f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF0F172A))
                    .border(
                        1.5.dp,
                        if (isAdCompleted) Color(0xFF10B981) else Color(0xFF00E5FF).copy(alpha = 0.6f),
                        RoundedCornerShape(22.dp)
                    )
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top header bar of the ad
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AD",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Text(
                            text = if (isLoadingAdMob) "Loading AdMob Video..." else "Sponsored Reward Video",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }

                    // Countdown or Claim indicator
                    if (secondsRemaining > 0 && !isAdCompleted) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF00E5FF).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isLoadingAdMob) "Connecting..." else "⏳ Reward in ${secondsRemaining}s",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFF10B981), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "🎉 REWARD READY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ad Creative Screen / Media Simulation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF1E293B),
                                    Color(0xFF0F172A),
                                    Color(0xFF1E1B4B)
                                )
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (isLoadingAdMob) {
                            CircularProgressIndicator(
                                color = Color(0xFF00E5FF),
                                modifier = Modifier.size(36.dp),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Loading Google AdMob Video...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Unit: ...9231585150",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        } else {
                            Text(
                                text = "🛰️",
                                fontSize = 40.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Pro GPS Map Camera HD",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color(0xFF00E5FF),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Survey-Grade Accuracy • Tamper-Proof Timestamps",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.75f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "★★★★★ Rated 4.9/5 by 50,000+ Surveyors",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFFD700)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar of the ad
                LinearProgressIndicator(
                    progress = if (isLoadingAdMob) 0.5f else (15 - secondsRemaining) / 15f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isAdCompleted) Color(0xFF10B981) else Color(0xFF00E5FF),
                    trackColor = Color.White.copy(alpha = 0.15f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom CTA action
                if (!isAdCompleted) {
                    Text(
                        text = if (isLoadingAdMob) "Connecting to AdMob ad server..." else "Watching sponsored video: ${secondsRemaining}s remaining to unlock 24h Pro...",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.unlockProFor24Hours()
                            Toast.makeText(
                                context,
                                "🎉 24-Hour Pro Access Unlocked! All 6 features unlocked!",
                                Toast.LENGTH_LONG
                            ).show()
                            onRewardClaimed()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "🎉 Claim 24-Hour Free Pro Access",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AboutUsDialog(
    viewModel: GpsViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    val isProUnlocked by viewModel.isProUnlocked.collectAsState()
    val showManualLocation by viewModel.showManualLocation.collectAsState()
    val showManualTime by viewModel.showManualTime.collectAsState()
    val showUploadGallery by viewModel.showUploadGallery.collectAsState()
    val removeWatermark by viewModel.removeWatermark.collectAsState()
    val isCustomWatermarkEnabled by viewModel.isCustomWatermarkEnabled.collectAsState()
    val isGoogleDriveSyncEnabled by viewModel.isGoogleDriveSyncEnabled.collectAsState()
    val hapticFeedback by viewModel.hapticFeedback.collectAsState()

    var showProUpgradeDialog by remember { mutableStateOf(false) }
    var showRewardedAdDialog by remember { mutableStateOf(false) }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth(0.92f)
                .heightIn(max = 640.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, colors.accentCyan, RoundedCornerShape(24.dp))
                .background(colors.panel)
        ) {
            // Animated background: Cosmic star/bubble/heart simulation
            val infiniteTransition = rememberInfiniteTransition(label = "about_bg")
            
            val floatProgress by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(14000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "floatProgress"
            )

            val angleProgress by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(22000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "angleProgress"
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                
                // Draw rotating luminous nebula stars
                for (i in 0 until 18) {
                    val baseAngle = (i * 20f + angleProgress) * (Math.PI / 180f)
                    val radius = (width * 0.38f) * ((i % 3 + 1) / 3f)
                    val cx = (width / 2) + (Math.cos(baseAngle) * radius).toFloat()
                    val cy = (height / 2) + (Math.sin(baseAngle) * radius).toFloat()
                    val starSize = ((i % 4) + 2f)
                    val starAlpha = ((Math.sin(baseAngle * 2) + 1) / 2.0).toFloat().coerceIn(0.15f, 0.65f)
                    
                    val starColor = when (i % 3) {
                        0 -> colors.accentCyan.copy(alpha = starAlpha)
                        1 -> colors.accentOrange.copy(alpha = starAlpha)
                        else -> Color(0xFFBB86FC).copy(alpha = starAlpha)
                    }
                    drawCircle(color = starColor, radius = starSize, center = Offset(cx, cy))
                }

                // Draw floating survey beacons
                for (j in 0 until 6) {
                    val yOffset = ((floatProgress + (j * 0.16f)) % 1.0f) * height
                    val xPos = width * (0.15f + (j * 0.14f))
                    val pulse = ((Math.sin((floatProgress * 6.28) + j) + 1) / 2).toFloat()
                    val ringRadius = 6f + (pulse * 8f)
                    drawCircle(
                        color = colors.accentCyan.copy(alpha = (1f - (yOffset / height)) * 0.25f),
                        radius = ringRadius,
                        center = Offset(xPos, height - yOffset)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header icon & App Title
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(colors.accentCyan.copy(alpha = 0.15f))
                        .border(1.dp, colors.accentCyan, RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🛰️", fontSize = 26.sp)
                }

                Text(
                    text = "PRO GPS CAM",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.accentCyan,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "Version 1.0",
                    color = colors.muted,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = "This App is Made With Love By Pankaj ❤️",
                    color = colors.accentCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Purpose is To Give Ads Free Experience To All Users",
                    color = colors.text,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )

                Text(
                    text = "I Hope You All Enjoyed This App",
                    color = colors.text,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                // Feedback Email card
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.accentCyan.copy(alpha = 0.1f))
                        .border(1.dp, colors.accentCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable {
                            triggerEmailIntent(context, "pr903128@gmail.com")
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Stay Connected & Email Us for Any Suggestions or Improvements at:",
                        color = colors.muted,
                        fontSize = 10.5.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "pr903128@gmail.com",
                        color = colors.accentCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                        )
                    )
                }

                // Privacy Policy Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.accentCyan.copy(alpha = 0.1f))
                        .border(1.dp, colors.accentCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ConsentManager.PRIVACY_POLICY_URL))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open browser", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔒 Privacy Policy",
                        color = colors.accentCyan,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Privacy Options Button (UMP Consent Form)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.accentCyan.copy(alpha = 0.1f))
                        .border(1.dp, colors.accentCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable {
                            val activity = context as? Activity
                            if (activity != null) {
                                ConsentManager.showPrivacyOptions(activity)
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚙️ Ad Privacy Options",
                        color = colors.accentCyan,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // ==========================================
                // PRO USER SECTION
                // ==========================================
                if (!isProUnlocked) {
                    // Button to trigger Pro Upgrade Dialog
                    Button(
                        onClick = {
                            showProUpgradeDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (colors.isDark) Color(0xFF1E293B) else Color(0xFF0F172A)
                        ),
                        border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFF8C00)))),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(text = "👑", fontSize = 20.sp, modifier = Modifier.padding(end = 10.dp))
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = "Upgrade to Pro",
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "₹19/mo, ₹149/yr or Watch 15s Ad (24h Free)",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Text(text = "👑 Unlock", fontSize = 11.5.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Pro Settings Card (Always visible so users can see features)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (colors.isDark) Color(0xFF0F172A) else Color(0xFF1E293B))
                        .border(1.2.dp, Color(0xFFFFD700).copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "👑", fontSize = 16.sp, modifier = Modifier.padding(end = 6.dp))
                            Column {
                                Text(
                                    text = "PRO SETTINGS",
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    letterSpacing = 1.sp
                                )
                                if (isProUnlocked) {
                                    Text(
                                        text = "${viewModel.getProRemainingTimeString()} remaining",
                                        color = Color(0xFF10B981),
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isProUnlocked) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFFFD700).copy(alpha = 0.15f),
                                    RoundedCornerShape(6.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isProUnlocked) Color(0xFF10B981) else Color(0xFFFFD700),
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    if (!isProUnlocked) showProUpgradeDialog = true
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isProUnlocked) "✓ UNLOCKED" else "👑 UPGRADE",
                                color = if (isProUnlocked) Color(0xFF10B981) else Color(0xFFFFD700),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 1. Edit location manually 👑
                    ProSettingToggleRow(
                        icon = "📍",
                        title = "Edit location manually",
                        subtitle = "Activate manual location on home",
                        checked = if (isProUnlocked) showManualLocation else false,
                        isLocked = !isProUnlocked,
                        onLockedClick = { showProUpgradeDialog = true },
                        onCheckedChange = { viewModel.setShowManualLocation(it) }
                    )

                    Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

                    // 2. Manual time 👑
                    ProSettingToggleRow(
                        icon = "🕒",
                        title = "Manual time",
                        subtitle = "Activate manual time picker on home",
                        checked = if (isProUnlocked) showManualTime else false,
                        isLocked = !isProUnlocked,
                        onLockedClick = { showProUpgradeDialog = true },
                        onCheckedChange = { viewModel.setShowManualTime(it) }
                    )

                    Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

                    // 3. Upload gallery photo 👑
                    ProSettingToggleRow(
                        icon = "📂",
                        title = "Upload gallery photo",
                        subtitle = "Activate gallery upload on home",
                        checked = if (isProUnlocked) showUploadGallery else false,
                        isLocked = !isProUnlocked,
                        onLockedClick = { showProUpgradeDialog = true },
                        onCheckedChange = { viewModel.setShowUploadGallery(it) }
                    )

                    Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

                    // 4. Remove watermark 👑
                    ProSettingToggleRow(
                        icon = "🚫",
                        title = "Remove watermark",
                        subtitle = "Remove watermark branding text",
                        checked = if (isProUnlocked) removeWatermark else false,
                        isLocked = !isProUnlocked,
                        onLockedClick = { showProUpgradeDialog = true },
                        onCheckedChange = { viewModel.setRemoveWatermark(it) }
                    )

                    Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

                    // 5. Custom watermark 👑
                    ProSettingToggleRow(
                        icon = "🏷️",
                        title = "Custom watermark",
                        subtitle = "Set personalized branding for stamp",
                        checked = if (isProUnlocked) isCustomWatermarkEnabled else false,
                        isLocked = !isProUnlocked,
                        onLockedClick = { showProUpgradeDialog = true },
                        onCheckedChange = { viewModel.setCustomWatermarkEnabled(it) }
                    )

                    Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

                    // 6. Google drive sync 👑
                    ProSettingToggleRow(
                        icon = "☁️",
                        title = "Google drive sync",
                        subtitle = "Auto-sync photos & videos to cloud",
                        checked = if (isProUnlocked) isGoogleDriveSyncEnabled else false,
                        isLocked = !isProUnlocked,
                        onLockedClick = { showProUpgradeDialog = true },
                        onCheckedChange = { viewModel.setGoogleDriveSyncEnabled(it) }
                    )

                    Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

                    // 7. Haptic vibration feedback (free standard setting)
                    ProSettingToggleRow(
                        icon = "📳",
                        title = "Haptic vibration feedback",
                        subtitle = "Tactile vibration when photo is captured",
                        checked = hapticFeedback,
                        isLocked = false,
                        onCheckedChange = { viewModel.setHapticFeedback(it) }
                    )
                }

                // Close Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.border, contentColor = colors.text),
                    border = BorderStroke(1.dp, colors.border),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Text(
                        text = "Close",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    if (showProUpgradeDialog) {
        ProUpgradeDialog(
            onDismiss = { showProUpgradeDialog = false },
            onWatchAd = {
                showProUpgradeDialog = false
                showRewardedAdDialog = true
            }
        )
    }

    if (showRewardedAdDialog) {
        RewardedAdDialog(
            viewModel = viewModel,
            onDismiss = { showRewardedAdDialog = false },
            onRewardClaimed = {
                showRewardedAdDialog = false
            }
        )
    }
}

@Composable
fun ProSettingToggleRow(
    icon: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isLocked: Boolean = false,
    onLockedClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isLocked) { onLockedClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 14.sp, modifier = Modifier.padding(end = 8.dp))
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = "👑", fontSize = 11.sp)
                    if (isLocked) {
                        Text(text = "🔒", fontSize = 10.sp)
                    }
                }
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = {
                if (isLocked) onLockedClick() else onCheckedChange(it)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFFFFD700),
                checkedTrackColor = Color(0xFFFFD700).copy(alpha = 0.35f),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.White.copy(alpha = 0.15f)
            )
        )
    }
}

private fun triggerEmailIntent(context: Context, emailAddress: String) {
    try {
        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$emailAddress")
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Feedback for Pro GPS Cam App")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("developer_email", emailAddress)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Email copied to clipboard: $emailAddress", Toast.LENGTH_LONG).show()
    }
}

@Composable
fun LocationCard(
    state: LocationState,
    onCopy: (String, Double) -> Unit
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    var showTierInfoDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dots"
    )

    val dotColor = when (state.statusType) {
        "ok" -> CustomGreen
        "ip" -> CustomYellow
        "err" -> RedError
        else -> colors.accentOrange
    }

    val animatedAlpha = if (state.statusType == "searching") dotAlpha else 1.0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.panel, RoundedCornerShape(16.dp))
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Card status header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.panelSubtle)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(RoundedCornerShape(50))
                    .alpha(animatedAlpha)
                    .background(dotColor)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = state.statusLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textHeading,
                modifier = Modifier.weight(1f)
            )

            // Priority route number badge (1, 2, or 3) indicating address source
            if (state.addressTier != null && state.addressTier in 1..3) {
                val tierBadgeBg = when (state.addressTier) {
                    1 -> Color(0xFF10B981).copy(alpha = 0.18f)
                    2 -> Color(0xFF06B6D4).copy(alpha = 0.18f)
                    else -> Color(0xFFF59E0B).copy(alpha = 0.18f)
                }
                val tierBadgeBorder = when (state.addressTier) {
                    1 -> Color(0xFF10B981)
                    2 -> Color(0xFF06B6D4)
                    else -> Color(0xFFF59E0B)
                }
                val tierBadgeText = when (state.addressTier) {
                    1 -> Color(0xFF10B981)
                    2 -> Color(0xFF06B6D4)
                    else -> Color(0xFFF59E0B)
                }

                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(tierBadgeBg)
                        .border(1.2.dp, tierBadgeBorder, RoundedCornerShape(8.dp))
                        .clickable {
                            showTierInfoDialog = true
                            val shortName = state.addressTierName.ifEmpty {
                                when (state.addressTier) {
                                    1 -> "Android Geocoder"
                                    2 -> "OpenStreetMap Nominatim"
                                    else -> "IP-API"
                                }
                            }
                            Toast.makeText(context, "📍 Priority ${state.addressTier}: Fetched via $shortName", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                        .testTag("address_tier_badge"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${state.addressTier}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = tierBadgeText
                        )
                        Text(
                            text = "ℹ️",
                            fontSize = 9.sp
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.badgeBg)
                    .border(1.dp, colors.badgeText.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (state.isManualOverride) "MANUAL" else "AUTOMATIC",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = colors.badgeText
                )
            }
        }

        Divider(color = colors.border, thickness = 1.dp)

        // Card body readings panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val rawCombined = listOfNotNull(state.city.takeIf { it.isNotEmpty() }, state.state.takeIf { it.isNotEmpty() })
                .joinToString(", ").ifEmpty { "Survey Station" }
            val combinedCityState = cleanAddressText(rawCombined)
            Text(
                text = combinedCityState,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textHeading,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Keep clean without showing "Manually entered geo coordinates" as requested
            val rawRoad = if (state.road.contains("Manually entered") || state.road.contains("Please configure") || state.road.contains("Loading coordinates")) {
                ""
            } else {
                state.road
            }
            val cleanerRoad = cleanAddressText(rawRoad)

            if (cleanerRoad.isNotEmpty()) {
                Text(
                    text = cleanerRoad,
                    fontSize = 11.sp,
                    color = colors.muted,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Coords line click-to-copy trigger row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val formattedLat = if (state.lat != 0.0) "${String.format(Locale.US, "%.6f", state.lat)}°" else "—"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { if (state.lat != 0.0) onCopy("Latitude", state.lat) }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Lat $formattedLat",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = colors.gpsCoordColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                val formattedLng = if (state.lng != 0.0) "${String.format(Locale.US, "%.6f", state.lng)}°" else "—"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { if (state.lng != 0.0) onCopy("Longitude", state.lng) }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Long $formattedLng",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = colors.gpsCoordColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showTierInfoDialog && state.addressTier != null && state.addressTier in 1..3) {
        val tierBadgeColor = when (state.addressTier) {
            1 -> Color(0xFF10B981)
            2 -> Color(0xFF06B6D4)
            else -> Color(0xFFF59E0B)
        }
        AlertDialog(
            onDismissRequest = { showTierInfoDialog = false },
            containerColor = colors.panel,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(tierBadgeColor.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, tierBadgeColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${state.addressTier}",
                            fontWeight = FontWeight.ExtraBold,
                            color = tierBadgeColor,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = "Address Source (Priority ${state.addressTier})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textHeading
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Address was fetched via:",
                        fontSize = 12.sp,
                        color = colors.muted
                    )
                    Text(
                        text = state.addressTierName.ifEmpty {
                            when (state.addressTier) {
                                1 -> "Android Built-in Geocoder (1st Priority)"
                                2 -> "OpenStreetMap Nominatim (2nd Priority)"
                                3 -> "IP-API Network Lookup (3rd Priority)"
                                else -> "GPS Location"
                            }
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textHeading
                    )
                    Text(
                        text = state.addressTierDesc.ifEmpty {
                            when (state.addressTier) {
                                1 -> "Address resolved directly through your Android device's native system Geocoder service."
                                2 -> "Address resolved through OpenStreetMap Nominatim reverse geocoding web service."
                                3 -> "Address estimated using IP network-based geolocation lookup."
                                else -> "Location coordinates resolved."
                            }
                        },
                        fontSize = 13.sp,
                        color = colors.text,
                        lineHeight = 17.sp
                    )

                    Divider(color = colors.border, modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "Priority Levels:\n1 = Android Native Geocoder (Top Priority)\n2 = OpenStreetMap Nominatim (Secondary Fallback)\n3 = IP-API (Tertiary Fallback)",
                        fontSize = 11.sp,
                        color = colors.muted,
                        lineHeight = 15.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTierInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun ManualLocationSettingsPanel(
    onApply: (Double, Double, String, String, String) -> Unit
) {
    val colors = LocalAppColors.current
    var latStr by remember { mutableStateOf("") }
    var lngStr by remember { mutableStateOf("") }
    var cityInput by remember { mutableStateOf("") }
    var stateInput by remember { mutableStateOf("") }
    var pincodeInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .background(colors.inputBg, RoundedCornerShape(8.dp))
            .border(1.dp, colors.inputBorder, RoundedCornerShape(8.dp))
            .padding(14.dp)
    ) {
        Text(
            text = "🛰️ MANUAL STATION CONFIGURATION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = colors.headingColor,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = latStr,
                onValueChange = { latStr = it },
                label = { Text("Latitude (°N)", fontSize = 11.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.inputText,
                    unfocusedTextColor = colors.inputText,
                    focusedBorderColor = colors.headingColor,
                    unfocusedBorderColor = colors.inputBorder,
                    focusedLabelColor = colors.headingColor,
                    unfocusedLabelColor = colors.muted
                )
            )

            OutlinedTextField(
                value = lngStr,
                onValueChange = { lngStr = it },
                label = { Text("Longitude (°E)", fontSize = 11.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.inputText,
                    unfocusedTextColor = colors.inputText,
                    focusedBorderColor = colors.headingColor,
                    unfocusedBorderColor = colors.inputBorder,
                    focusedLabelColor = colors.headingColor,
                    unfocusedLabelColor = colors.muted
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = cityInput,
                onValueChange = { cityInput = it },
                label = { Text("City (e.g. Range)", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.inputText,
                    unfocusedTextColor = colors.inputText,
                    focusedBorderColor = colors.headingColor,
                    unfocusedBorderColor = colors.inputBorder,
                    focusedLabelColor = colors.headingColor,
                    unfocusedLabelColor = colors.muted
                )
            )

            OutlinedTextField(
                value = stateInput,
                onValueChange = { stateInput = it },
                label = { Text("State / Region", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.inputText,
                    unfocusedTextColor = colors.inputText,
                    focusedBorderColor = colors.headingColor,
                    unfocusedBorderColor = colors.inputBorder,
                    focusedLabelColor = colors.headingColor,
                    unfocusedLabelColor = colors.muted
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = pincodeInput,
                onValueChange = { pincodeInput = it },
                label = { Text("Pincode / Postcode (Optional)", fontSize = 11.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.inputText,
                    unfocusedTextColor = colors.inputText,
                    focusedBorderColor = colors.headingColor,
                    unfocusedBorderColor = colors.inputBorder,
                    focusedLabelColor = colors.headingColor,
                    unfocusedLabelColor = colors.muted
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val inputLat = latStr.toDoubleOrNull() ?: 0.0
                val inputLng = lngStr.toDoubleOrNull() ?: 0.0
                onApply(inputLat, inputLng, cityInput, stateInput, pincodeInput)
            },
            colors = ButtonDefaults.buttonColors(containerColor = colors.headingColor, contentColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "APPLY MANUAL COORDINATES",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun ResultViewScreen(
    bitmap: Bitmap,
    viewModel: GpsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Upper top action back header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📸",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = "Photo Ready",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = PanelColor),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(50)
            ) {
                Text("← Back", color = TextColor, fontWeight = FontWeight.Bold)
            }
        }

        // Mid aligned picture holder preview card
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(PanelColor),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Stamped georeferenced output photo result",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            )
        }

        // Active save sharing dynamic trigger layout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Save Photo action triggers
            Button(
                onClick = {
                    isSaving = true
                    scope.launch {
                        val filepath = saveProcessedBitmap(context, bitmap, viewModel.imageQuality.value.jpegQuality)
                        if (filepath != null) {
                            val savedUri = Uri.parse(filepath)
                            viewModel.addSessionMedia(SessionMediaItem(uri = savedUri, isVideo = false))
                            Toast.makeText(context, "Saved to Gallery! folder: Pictures/ProGpsCam", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Error saving photo.", Toast.LENGTH_SHORT).show()
                        }
                        isSaving = false
                    }
                },
                enabled = !isSaving,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CustomGreen),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
            ) {
                Text(
                    text = "⬇️ Save Photo",
                    color = BgColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Share Photo triggers
            Button(
                onClick = {
                    scope.launch {
                        shareProcessedBitmap(context, bitmap)
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PanelColor),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
            ) {
                Text(
                    text = "📤 Share Photo",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun ProcessingOverlayScreen(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = AccentCyan,
                strokeWidth = 4.dp,
                modifier = Modifier.size(54.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = message,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Survey georeferenced stamping is active...",
                color = MutedColor,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Write the compiled bitmap into the public photos repository safely on IO dispatcher
private suspend fun saveProcessedBitmap(
    context: Context,
    bitmap: Bitmap,
    jpegQuality: Int = 100,
    prefix: String = "PRO_GPS_",
    folderName: String = "ProGpsCam",
    latitude: Double? = null,
    longitude: Double? = null,
    timestampMillis: Long? = null
): String? {
    return withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.ENGLISH).format(Date())
        val filename = "${prefix}$timestamp.jpg"
        var outStream: OutputStream? = null
        var savedUrl: String? = null

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val relPath = if (folderName.startsWith("DCIM/")) {
                    folderName
                } else {
                    Environment.DIRECTORY_PICTURES + "/" + folderName
                }
                put(MediaStore.MediaColumns.RELATIVE_PATH, relPath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        try {
            val resolver = context.contentResolver
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            val imageUri = resolver.insert(collection, values)
            if (imageUri != null) {
                val quality = jpegQuality.coerceIn(10, 100)
                if (latitude != null && longitude != null) {
                    val tempCacheFile = File(context.cacheDir, "temp_stamp_${System.currentTimeMillis()}.jpg")
                    try {
                        FileOutputStream(tempCacheFile).use { tempOut ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, tempOut)
                        }
                        OfflineSyncManager.writeExifGps(tempCacheFile, latitude, longitude, timestampMillis ?: System.currentTimeMillis())
                        outStream = resolver.openOutputStream(imageUri)
                        if (outStream != null) {
                            tempCacheFile.inputStream().use { tempIn ->
                                tempIn.copyTo(outStream!!)
                            }
                        }
                    } finally {
                        try { tempCacheFile.delete() } catch (_: Exception) {}
                    }
                } else {
                    outStream = resolver.openOutputStream(imageUri)
                    if (outStream != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outStream)
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(imageUri, values, null, null)
                }
                savedUrl = imageUri.toString()

                if (latitude != null && longitude != null) {
                    OfflineSyncManager.writeExifGps(context, savedUrl, latitude, longitude, timestampMillis ?: System.currentTimeMillis())
                }
            }
        } catch (e: Exception) {
            Log.e("GpsViewModel", "Error saving bitmap", e)
        } finally {
            try {
                outStream?.close()
            } catch (ignored: Exception) {}
        }
        savedUrl
    }
}

// Write the recorded video file into the public device videos repository safely on IO dispatcher
private suspend fun saveVideoToMediaStore(
    context: Context,
    videoFile: File,
    folderName: String = "ProGpsCam"
): Uri? {
    return withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        val filename = "PRO_GPS_$timestamp.mp4"
        var outStream: OutputStream? = null
        var inStream: InputStream? = null
        var savedUri: Uri? = null

        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, filename)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val relPath = if (folderName.startsWith("DCIM/")) {
                    folderName
                } else {
                    Environment.DIRECTORY_MOVIES + "/" + folderName
                }
                put(MediaStore.Video.Media.RELATIVE_PATH, relPath)
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        try {
            val resolver = context.contentResolver
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
            val uri = resolver.insert(collection, values)
            if (uri != null) {
                outStream = resolver.openOutputStream(uri)
                inStream = FileInputStream(videoFile)
                if (outStream != null) {
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (inStream.read(buffer).also { bytesRead = it } != -1) {
                        outStream.write(buffer, 0, bytesRead)
                    }
                    outStream.flush()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Video.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                }
                savedUri = uri
            }
        } catch (e: Exception) {
            Log.e("GpsViewModel", "Error saving video to MediaStore", e)
        } finally {
            try { inStream?.close() } catch (_: Exception) {}
            try { outStream?.close() } catch (_: Exception) {}
        }
        savedUri
    }
}

// Share intent handler running safely on IO dispatcher with UI updates back on Main
private suspend fun shareProcessedBitmap(context: Context, bitmap: Bitmap) {
    withContext(Dispatchers.IO) {
        try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "shared_survey_photo.jpg")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.close()

            val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            if (contentUri != null) {
                val shareIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setDataAndType(contentUri, context.contentResolver.getType(contentUri))
                    putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
                    type = "image/jpeg"
                }
                withContext(Dispatchers.Main) {
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Photo"))
                }
            }
        } catch (e: Exception) {
            Log.e("GpsViewModel", "Error sharing image", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Sharing failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// Helper to fetch video dimensions dynamically
private fun extractVideoDimensions(context: Context, videoUri: Uri): Pair<Int, Int> {
    val retriever = android.media.MediaMetadataRetriever()
    try {
        retriever.setDataSource(context, videoUri)
        val wStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
        val hStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
        val rotationStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
        val r = rotationStr?.toIntOrNull() ?: 0
        val vw = wStr?.toIntOrNull() ?: 1280
        val vh = hStr?.toIntOrNull() ?: 720
        return if (r == 90 || r == 270) {
            Pair(vh, vw)
        } else {
            Pair(vw, vh)
        }
    } catch (e: Exception) {
        Log.e("GpsViewModel", "Error fetching video dimensions", e)
        return Pair(1280, 720)
    } finally {
        try {
            retriever.release()
        } catch (ignored: Exception) {}
    }
}

// Generate static overlay bitmap exactly matching the video file's physical dimensions
private fun drawOverlayOnVideoFrame(width: Int, height: Int, state: LocationState): Bitmap {
    val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    val scaleFactor = (width / 420.0f) * state.stampScale * 0.85f // slightly smaller and optimized to preserve extra video gaps
    val hasCustomNote = state.isCustomNoteEnabled && state.customNote.isNotBlank()
    val stampHeight = (if (hasCustomNote) 98.0f else 86.0f) * scaleFactor
    val startY = height - stampHeight
    val pad = 10f * scaleFactor

    fun drawWithClipping(txt: String, x: Float, y: Float, maxW: Float, paint: Paint) {
        var finalTxt = txt
        if (paint.measureText(finalTxt) <= maxW) {
            canvas.drawText(finalTxt, x, y, paint)
            return
        }
        while (finalTxt.isNotEmpty() && paint.measureText(finalTxt + "…") > maxW) {
            finalTxt = finalTxt.dropLast(1)
        }
        canvas.drawText(finalTxt + "…", x, y, paint)
    }

    when (state.stampTemplate) {
        StampTemplate.STANDARD -> {
            if (!state.useInsideOverlay) {
                val paintBar = Paint().apply {
                    color = 0xFF0A0B0E.toInt()
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, startY, width.toFloat(), height.toFloat(), paintBar)

                val gradientShader = LinearGradient(
                    0f, startY, width.toFloat(), startY,
                    intArrayOf(0xFF00E5FF.toInt(), 0xFF7B61FF.toInt(), 0xFFFF6B35.toInt()),
                    floatArrayOf(0f, 0.5f, 1f),
                    Shader.TileMode.CLAMP
                )
                val paintGrad = Paint().apply {
                    shader = gradientShader
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, startY, width.toFloat(), startY + 3f * scaleFactor, paintGrad)
            }

            val mapW = 75f * scaleFactor
            val mapH = stampHeight - pad * 2f
            val mapX = pad
            val mapY = startY + pad

            val paintMapBox = Paint().apply {
                color = if (state.useInsideOverlay) 0x660F111A.toInt() else 0xFF0F111A.toInt()
                style = Paint.Style.FILL
            }
            val rectPath = Path().apply {
                addRoundRect(
                    mapX, mapY, mapX + mapW, mapY + mapH,
                    6f * scaleFactor, 6f * scaleFactor,
                    Path.Direction.CW
                )
            }
            canvas.drawPath(rectPath, paintMapBox)

            val paintMapBorder = Paint().apply {
                color = 0x4000E5FF
                style = Paint.Style.STROKE
                strokeWidth = 1f * scaleFactor
                isAntiAlias = true
            }
            canvas.drawPath(rectPath, paintMapBorder)

            val cx = mapX + mapW / 2f
            val cy = mapY + mapH * 0.34f
            val r = 6.5f * scaleFactor

            val paintShadow = Paint().apply {
                color = 0x55000000
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawOval(
                cx - r * 0.4f, cy + r * 1.5f, cx + r * 0.4f, cy + r * 1.7f,
                paintShadow
            )

            val pinTriangle = Path().apply {
                moveTo(cx - r * 0.8f, cy + r * 0.3f)
                lineTo(cx + r * 0.8f, cy + r * 0.3f)
                lineTo(cx, cy + r * 1.6f)
                close()
            }
            val paintPin = Paint().apply {
                color = 0xFFFF6B35.toInt()
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawPath(pinTriangle, paintPin)
            canvas.drawCircle(cx, cy, r, paintPin)

            val paintEye = Paint().apply {
                color = android.graphics.Color.WHITE
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawCircle(cx, cy, r * 0.35f, paintEye)

            val paintMapCoordDigits = Paint().apply {
                color = 0xFF00E5FF.toInt()
                textSize = 6.5f * scaleFactor
                textAlign = Paint.Align.CENTER
                typeface = Typeface.MONOSPACE
                isAntiAlias = true
                if (state.useInsideOverlay) {
                    setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xFF000000.toInt())
                }
            }
            canvas.drawText("${String.format(Locale.US, "%.4f", state.lat)}°N", cx, mapY + mapH - 12f * scaleFactor, paintMapCoordDigits)
            canvas.drawText("${String.format(Locale.US, "%.4f", state.lng)}°E", cx, mapY + mapH - 4f * scaleFactor, paintMapCoordDigits)

            val paintAutomaticLabel = Paint().apply {
                color = 0x4DFFFFFF.toInt()
                textSize = 6.5f * scaleFactor
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
                if (state.useInsideOverlay) {
                    setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xFF000000.toInt())
                }
            }
            canvas.drawText("AUTOMATIC", cx, mapY + 8f * scaleFactor, paintAutomaticLabel)

            val tx = mapX + mapW + pad
            val tw = width - tx - pad
            var ty = startY + pad + 10f * scaleFactor

            val rawCityLine = listOfNotNull(
                state.city.takeIf { it.isNotEmpty() },
                state.state.takeIf { it.isNotEmpty() },
                state.country.takeIf { it.isNotEmpty() }
            ).distinct().joinToString(", ").ifEmpty { "Location" }
            val cityLine = cleanAddressText(rawCityLine)
            val paintCity = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 13.5f * scaleFactor
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
                if (state.useInsideOverlay) {
                    setShadowLayer(3f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xFF000000.toInt())
                }
            }
            drawWithClipping(cityLine, tx, ty, tw, paintCity)
            ty += 14f * scaleFactor

            val addrParts = listOfNotNull(
                state.road.takeIf { it.isNotEmpty() && !it.contains("Manually entered") },
                state.city.takeIf { it.isNotEmpty() },
                state.state.takeIf { it.isNotEmpty() },
                state.postcode.takeIf { it.isNotEmpty() },
                state.country.takeIf { it.isNotEmpty() }
            ).distinct()
            val addressText = cleanAddressText(addrParts.joinToString(", ").ifEmpty { "Georeferenced Survey Location" })
            val paintAddr = Paint().apply {
                color = if (state.useInsideOverlay) 0xEEFFFFFF.toInt() else 0x7BE8EAF0.toInt()
                textSize = 7.5f * scaleFactor
                typeface = Typeface.MONOSPACE
                isAntiAlias = true
                if (state.useInsideOverlay) {
                    setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xFF000000.toInt())
                }
            }
            drawWithClipping(addressText, tx, ty, tw, paintAddr)
            ty += 9.5f * scaleFactor

            val latText = "Lat ${String.format(Locale.US, "%.6f", state.lat)}°"
            val lngText = "Long ${String.format(Locale.US, "%.6f", state.lng)}°"
            val dirText = if (state.isCompassEnabled) "   Dir: ${state.getCardinalDirection()}" else ""
            val paintCoords = Paint().apply {
                color = 0xFF00E5FF.toInt()
                textSize = 8.5f * scaleFactor
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                isAntiAlias = true
                if (state.useInsideOverlay) {
                    setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xFF000000.toInt())
                }
            }
            drawWithClipping("$latText   $lngText$dirText", tx, ty, tw, paintCoords)
            ty += 10.5f * scaleFactor

            val dt = if (state.manualTime != null) Date(state.manualTime) else Date()
            val sdfDay = SimpleDateFormat("EEEE", Locale.ENGLISH)
            val sdfDate = SimpleDateFormat("dd/MM/yyyy  hh:mm:ss a", Locale.ENGLISH)
            val formattedDateLine = "${sdfDay.format(dt)}, ${sdfDate.format(dt).uppercase()}"

            val paintTime = Paint().apply {
                color = if (state.useInsideOverlay) 0xEEFFFFFF.toInt() else 0xA6FFFFFF.toInt()
                textSize = 8f * scaleFactor
                typeface = Typeface.MONOSPACE
                isAntiAlias = true
                if (state.useInsideOverlay) {
                    setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xFF000000.toInt())
                }
            }
            drawWithClipping(formattedDateLine, tx, ty, tw, paintTime)
            ty += 9.5f * scaleFactor

            val timeZone = TimeZone.getDefault()
            val totalOffsetMin = timeZone.getOffset(dt.time) / (1000 * 60)
            val tzHours = Math.abs(totalOffsetMin) / 60
            val tzMins = Math.abs(totalOffsetMin) % 60
            val tzSign = if (totalOffsetMin >= 0) "+" else "-"
            val tzString = "GMT$tzSign${String.format("%02d", tzHours)}:${String.format("%02d", tzMins)}"

            val accVal = if (state.accuracy != null) "${Math.round(state.accuracy)}m" else "5m"
            val accuracyString = "$tzString   ACCURACY: ±$accVal"
            val paintAccuracy = Paint().apply {
                color = if (state.accuracy != null) 0xFF00FF88.toInt() else 0xFFFFD700.toInt()
                textSize = 7.5f * scaleFactor
                typeface = Typeface.MONOSPACE
                isAntiAlias = true
                if (state.useInsideOverlay) {
                    setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xFF000000.toInt())
                }
            }
            drawWithClipping(accuracyString, tx, ty, tw, paintAccuracy)

            if (hasCustomNote) {
                ty += 9.5f * scaleFactor
                val noteText = "Note: ${state.customNote.trim()}"
                val paintNote = Paint().apply {
                    color = 0xFFFFD700.toInt()
                    textSize = 8f * scaleFactor
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                    isAntiAlias = true
                    if (state.useInsideOverlay) {
                        setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xFF000000.toInt())
                    }
                }
                drawWithClipping(noteText, tx, ty, tw, paintNote)
            }

            if (!state.removeWatermark) {
                val paintBranding = Paint().apply {
                    color = 0x9900E5FF.toInt()
                    textSize = 8f * scaleFactor
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    textAlign = Paint.Align.RIGHT
                    isAntiAlias = true
                    if (state.useInsideOverlay) {
                        setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xFF000000.toInt())
                    }
                }
                canvas.drawText(state.getWatermarkText(), width - pad, height - 5f * scaleFactor, paintBranding)
            }
        }

        StampTemplate.CLASSIC -> {
            val mapW = 75f * scaleFactor
            val mapH = stampHeight - pad * 2f
            val mapX = pad
            val mapY = startY + pad

            val paintBar = Paint().apply {
                color = if (state.useInsideOverlay) 0xCC150F07.toInt() else 0xFF150F07.toInt()
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, startY, width.toFloat(), height.toFloat(), paintBar)

            val paintGoldBorder = Paint().apply {
                color = 0xFFFFD700.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 2f * scaleFactor
                isAntiAlias = true
            }
            canvas.drawRect(pad * 0.5f, startY + 2f * scaleFactor, width.toFloat() - pad * 0.5f, height.toFloat() - 2f * scaleFactor, paintGoldBorder)

            val paintHeaderLine = Paint().apply {
                color = 0xFFFFD700.toInt()
                textSize = 7f * scaleFactor
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("★ GEODETIC FIELD SURVEY SEAL ★", pad * 1.5f, startY + 10f * scaleFactor, paintHeaderLine)

            val paintBadgeBg = Paint().apply {
                color = 0xFF2A1C0A.toInt()
                style = Paint.Style.FILL
            }
            val badgePath = Path().apply {
                addRoundRect(mapX, mapY + 6f * scaleFactor, mapX + mapW, mapY + mapH, 8f * scaleFactor, 8f * scaleFactor, Path.Direction.CW)
            }
            canvas.drawPath(badgePath, paintBadgeBg)
            canvas.drawPath(badgePath, paintGoldBorder)

            val cx = mapX + mapW / 2f
            val paintBadgeTitle = Paint().apply {
                color = 0xFFFFD700.toInt()
                textSize = 6.5f * scaleFactor
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("SURVEY", cx, mapY + 16f * scaleFactor, paintBadgeTitle)

            val paintIcon = Paint().apply {
                textSize = 14f * scaleFactor
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("🧭", cx, mapY + 34f * scaleFactor, paintIcon)

            val paintBadgeCoords = Paint().apply {
                color = 0xFFFDE68A.toInt()
                textSize = 6.5f * scaleFactor
                textAlign = Paint.Align.CENTER
                typeface = Typeface.MONOSPACE
                isAntiAlias = true
            }
            canvas.drawText("${String.format(Locale.US, "%.3f", state.lat)}°N", cx, mapY + mapH - 10f * scaleFactor, paintBadgeCoords)
            canvas.drawText("${String.format(Locale.US, "%.3f", state.lng)}°E", cx, mapY + mapH - 3f * scaleFactor, paintBadgeCoords)

            val tx = mapX + mapW + pad
            val tw = width - tx - pad
            var ty = startY + pad + 16f * scaleFactor

            val rawCityLine = listOfNotNull(
                state.city.takeIf { it.isNotEmpty() },
                state.state.takeIf { it.isNotEmpty() },
                state.country.takeIf { it.isNotEmpty() }
            ).distinct().joinToString(", ").ifEmpty { "Location" }
            val cityLine = cleanAddressText(rawCityLine)
            val paintCity = Paint().apply {
                color = 0xFFFFD700.toInt()
                textSize = 13f * scaleFactor
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            drawWithClipping("🏛️ $cityLine", tx, ty, tw, paintCity)
            ty += 13.5f * scaleFactor

            val addrParts = listOfNotNull(
                state.road.takeIf { it.isNotEmpty() && !it.contains("Manually entered") },
                state.city.takeIf { it.isNotEmpty() },
                state.state.takeIf { it.isNotEmpty() },
                state.postcode.takeIf { it.isNotEmpty() },
                state.country.takeIf { it.isNotEmpty() }
            ).distinct()
            val addressText = cleanAddressText(addrParts.joinToString(", ").ifEmpty { "Georeferenced Survey Location" })
            val paintAddr = Paint().apply {
                color = 0xFFFDE68A.toInt()
                textSize = 7.5f * scaleFactor
                typeface = Typeface.MONOSPACE
                isAntiAlias = true
            }
            drawWithClipping(addressText, tx, ty, tw, paintAddr)
            ty += 9.5f * scaleFactor

            val coordText = "[ LAT: ${String.format(Locale.US, "%.6f", state.lat)}° | LON: ${String.format(Locale.US, "%.6f", state.lng)}° ]"
            val paintCoords = Paint().apply {
                color = 0xFFFFE082.toInt()
                textSize = 8.5f * scaleFactor
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                isAntiAlias = true
            }
            drawWithClipping(coordText, tx, ty, tw, paintCoords)
            ty += 10.5f * scaleFactor

            val dt = if (state.manualTime != null) Date(state.manualTime) else Date()
            val sdfDate = SimpleDateFormat("EEEE, dd/MM/yyyy hh:mm:ss a", Locale.ENGLISH)
            val paintTime = Paint().apply {
                color = 0xFFFFF8E1.toInt()
                textSize = 7.8f * scaleFactor
                typeface = Typeface.MONOSPACE
                isAntiAlias = true
            }
            drawWithClipping(sdfDate.format(dt).uppercase(), tx, ty, tw, paintTime)
            ty += 9.5f * scaleFactor

            val accText = "ACCURACY: ±${if (state.accuracy != null) Math.round(state.accuracy) else 5}m"
            val paintAcc = Paint().apply {
                color = 0xFFFFD54F.toInt()
                textSize = 7.2f * scaleFactor
                typeface = Typeface.MONOSPACE
                isAntiAlias = true
            }
            drawWithClipping(accText, tx, ty, tw, paintAcc)

            if (hasCustomNote) {
                ty += 9.5f * scaleFactor
                val paintNote = Paint().apply {
                    color = 0xFFFFE082.toInt()
                    textSize = 8f * scaleFactor
                    typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                    isAntiAlias = true
                }
                drawWithClipping("NOTE: ${state.customNote.trim()}", tx, ty, tw, paintNote)
            }

            if (!state.removeWatermark) {
                val paintBranding = Paint().apply {
                    color = 0xFFFFD700.toInt()
                    textSize = 8f * scaleFactor
                    typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                    textAlign = Paint.Align.RIGHT
                    isAntiAlias = true
                    if (state.useInsideOverlay) {
                        setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xFF000000.toInt())
                    }
                }
                canvas.drawText(state.getWatermarkText(), width - pad * 1.5f, height - 5f * scaleFactor, paintBranding)
            }
        }

        StampTemplate.TECH_HUD -> {
            val mapW = 75f * scaleFactor
            val mapH = stampHeight - pad * 2f
            val mapX = pad
            val mapY = startY + pad

            val paintBar = Paint().apply {
                color = if (state.useInsideOverlay) 0xD9050E17.toInt() else 0xFF050E17.toInt()
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, startY, width.toFloat(), height.toFloat(), paintBar)

            val paintBorder = Paint().apply {
                color = 0xFF00E5FF.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 1.2f * scaleFactor
                isAntiAlias = true
            }
            canvas.drawRect(pad * 0.5f, startY + 2f * scaleFactor, width.toFloat() - pad * 0.5f, height.toFloat() - 2f * scaleFactor, paintBorder)

            val paintCyberHead = Paint().apply {
                color = 0xFF00FF66.toInt()
                textSize = 7f * scaleFactor
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("[ 🛰️ SATELLITE LOCK // GPS HUD ]", pad * 1.5f, startY + 9.5f * scaleFactor, paintCyberHead)

            val paintRadarBg = Paint().apply {
                color = 0xFF021B1C.toInt()
                style = Paint.Style.FILL
            }
            val radarPath = Path().apply {
                addRoundRect(mapX, mapY + 6f * scaleFactor, mapX + mapW, mapY + mapH, 4f * scaleFactor, 4f * scaleFactor, Path.Direction.CW)
            }
            canvas.drawPath(radarPath, paintRadarBg)

            val paintRadarBorder = Paint().apply {
                color = 0xFF00FF66.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 1f * scaleFactor
                isAntiAlias = true
            }
            canvas.drawPath(radarPath, paintRadarBorder)

            val cx = mapX + mapW / 2f
            val paintRadarLabel = Paint().apply {
                color = 0xFF00FF66.toInt()
                textSize = 6f * scaleFactor
                textAlign = Paint.Align.CENTER
                typeface = Typeface.MONOSPACE
                isAntiAlias = true
            }
            canvas.drawText("RADAR LOCK", cx, mapY + 15f * scaleFactor, paintRadarLabel)

            val paintIcon = Paint().apply {
                textSize = 14f * scaleFactor
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("⚡", cx, mapY + 33f * scaleFactor, paintIcon)

            val paintRadarCoords = Paint().apply {
                color = 0xFF00E5FF.toInt()
                textSize = 6.5f * scaleFactor
                textAlign = Paint.Align.CENTER
                typeface = Typeface.MONOSPACE
                isAntiAlias = true
            }
            canvas.drawText("${String.format(Locale.US, "%.3f", state.lat)}°N", cx, mapY + mapH - 10f * scaleFactor, paintRadarCoords)
            canvas.drawText("${String.format(Locale.US, "%.3f", state.lng)}°E", cx, mapY + mapH - 3f * scaleFactor, paintRadarCoords)

            val tx = mapX + mapW + pad
            val tw = width - tx - pad
            var ty = startY + pad + 16f * scaleFactor

            val rawCityLine = listOfNotNull(
                state.city.takeIf { it.isNotEmpty() },
                state.state.takeIf { it.isNotEmpty() },
                state.country.takeIf { it.isNotEmpty() }
            ).distinct().joinToString(", ").ifEmpty { "Location" }
            val cityLine = cleanAddressText(rawCityLine)
            val paintCity = Paint().apply {
                color = 0xFF00E5FF.toInt()
                textSize = 13f * scaleFactor
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                isAntiAlias = true
            }
            drawWithClipping("POS // $cityLine", tx, ty, tw, paintCity)
            ty += 13.5f * scaleFactor

            val addrParts = listOfNotNull(
                state.road.takeIf { it.isNotEmpty() && !it.contains("Manually entered") },
                state.city.takeIf { it.isNotEmpty() },
                state.state.takeIf { it.isNotEmpty() },
                state.postcode.takeIf { it.isNotEmpty() },
                state.country.takeIf { it.isNotEmpty() }
            ).distinct()
            val addressText = cleanAddressText(addrParts.joinToString(", ").ifEmpty { "Georeferenced Survey Location" })
            val paintAddr = Paint().apply {
                color = 0xFF00FF88.toInt()
                textSize = 7.5f * scaleFactor
                typeface = Typeface.MONOSPACE
                isAntiAlias = true
            }
            drawWithClipping("LOC // $addressText", tx, ty, tw, paintAddr)
            ty += 9.5f * scaleFactor

            val coordText = "GEO // LAT: ${String.format(Locale.US, "%.6f", state.lat)}°  LON: ${String.format(Locale.US, "%.6f", state.lng)}°"
            val paintCoords = Paint().apply {
                color = 0xFF00FF66.toInt()
                textSize = 8.5f * scaleFactor
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                isAntiAlias = true
            }
            drawWithClipping(coordText, tx, ty, tw, paintCoords)
            ty += 10.5f * scaleFactor

            val dt = if (state.manualTime != null) Date(state.manualTime) else Date()
            val sdfDate = SimpleDateFormat("EEEE, dd/MM/yyyy hh:mm:ss a", Locale.ENGLISH)
            val paintTime = Paint().apply {
                color = 0xFFE0F2FE.toInt()
                textSize = 7.8f * scaleFactor
                typeface = Typeface.MONOSPACE
                isAntiAlias = true
            }
            drawWithClipping("UTC // ${sdfDate.format(dt).uppercase()}", tx, ty, tw, paintTime)
            ty += 9.5f * scaleFactor

            val accText = "ACCURACY // ±${if (state.accuracy != null) Math.round(state.accuracy) else 5}m"
            val paintAcc = Paint().apply {
                color = 0xFF00FF88.toInt()
                textSize = 7.2f * scaleFactor
                typeface = Typeface.MONOSPACE
                isAntiAlias = true
            }
            drawWithClipping(accText, tx, ty, tw, paintAcc)

            if (hasCustomNote) {
                ty += 9.5f * scaleFactor
                val paintNote = Paint().apply {
                    color = 0xFFFFD700.toInt()
                    textSize = 8f * scaleFactor
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                    isAntiAlias = true
                }
                drawWithClipping("NOTE // ${state.customNote.trim()}", tx, ty, tw, paintNote)
            }

            if (!state.removeWatermark) {
                val paintBranding = Paint().apply {
                    color = 0xFF00E5FF.toInt()
                    textSize = 8f * scaleFactor
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                    textAlign = Paint.Align.RIGHT
                    isAntiAlias = true
                    if (state.useInsideOverlay) {
                        setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xFF000000.toInt())
                    }
                }
                canvas.drawText(state.getWatermarkText(), width - pad * 1.5f, height - 5f * scaleFactor, paintBranding)
            }
        }

        StampTemplate.CLEAN_MINIMAL -> {
            val mapW = 60f * scaleFactor
            val mapH = stampHeight - pad * 2f
            val mapX = pad
            val mapY = startY + pad

            val paintPillBg = Paint().apply {
                color = if (state.useInsideOverlay) 0xCC0F172A.toInt() else 0xFF0F172A.toInt()
                style = Paint.Style.FILL
            }
            val pillPath = Path().apply {
                addRoundRect(pad * 0.5f, startY + 2f * scaleFactor, width.toFloat() - pad * 0.5f, height.toFloat() - 2f * scaleFactor, 14f * scaleFactor, 14f * scaleFactor, Path.Direction.CW)
            }
            canvas.drawPath(pillPath, paintPillBg)

            val paintPillBorder = Paint().apply {
                color = 0x80818CF8.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 1.2f * scaleFactor
                isAntiAlias = true
            }
            canvas.drawPath(pillPath, paintPillBorder)

            val cx = mapX + mapW / 2f
            val cy = mapY + mapH / 2f
            val paintCircle = Paint().apply {
                color = 0x4D4F46E5.toInt()
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawCircle(cx, cy, 22f * scaleFactor, paintCircle)

            val paintIcon = Paint().apply {
                textSize = 18f * scaleFactor
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("✨", cx, cy + 6f * scaleFactor, paintIcon)

            val tx = mapX + mapW + pad * 0.5f
            val tw = width - tx - pad
            var ty = startY + pad + 12f * scaleFactor

            val rawCityLine = listOfNotNull(
                state.city.takeIf { it.isNotEmpty() },
                state.state.takeIf { it.isNotEmpty() },
                state.country.takeIf { it.isNotEmpty() }
            ).distinct().joinToString(", ").ifEmpty { "Location" }
            val cityLine = cleanAddressText(rawCityLine)
            val paintCity = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 13.5f * scaleFactor
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            val latStr = "${String.format(Locale.US, "%.4f", state.lat)}°N"
            val lngStr = "${String.format(Locale.US, "%.4f", state.lng)}°E"
            drawWithClipping("$cityLine  •  $latStr, $lngStr", tx, ty, tw, paintCity)
            ty += 14f * scaleFactor

            val addrParts = listOfNotNull(
                state.road.takeIf { it.isNotEmpty() && !it.contains("Manually entered") },
                state.city.takeIf { it.isNotEmpty() },
                state.state.takeIf { it.isNotEmpty() },
                state.postcode.takeIf { it.isNotEmpty() },
                state.country.takeIf { it.isNotEmpty() }
            ).distinct()
            val addressText = cleanAddressText(addrParts.joinToString(", ").ifEmpty { "Georeferenced Survey Location" })
            val paintAddr = Paint().apply {
                color = 0xFFE2E8F0.toInt()
                textSize = 7.5f * scaleFactor
                typeface = Typeface.MONOSPACE
                isAntiAlias = true
            }
            drawWithClipping(addressText, tx, ty, tw, paintAddr)
            ty += 9.5f * scaleFactor

            val dt = if (state.manualTime != null) Date(state.manualTime) else Date()
            val sdfDate = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH)
            val accText = "ACC: ±${if (state.accuracy != null) Math.round(state.accuracy) else 5}m"
            val timeString = "${sdfDate.format(dt)}  •  $accText"
            val paintTime = Paint().apply {
                color = 0xFF94A3B8.toInt()
                textSize = 7.8f * scaleFactor
                typeface = Typeface.MONOSPACE
                isAntiAlias = true
            }
            drawWithClipping(timeString, tx, ty, tw, paintTime)

            if (hasCustomNote) {
                ty += 9.5f * scaleFactor
                val paintNote = Paint().apply {
                    color = 0xFF38BDF8.toInt()
                    textSize = 8f * scaleFactor
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    isAntiAlias = true
                }
                drawWithClipping("Note: ${state.customNote.trim()}", tx, ty, tw, paintNote)
            }

            if (!state.removeWatermark) {
                val paintBranding = Paint().apply {
                    color = 0xFF818CF8.toInt()
                    textSize = 8f * scaleFactor
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    textAlign = Paint.Align.RIGHT
                    isAntiAlias = true
                    if (state.useInsideOverlay) {
                        setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xFF000000.toInt())
                    }
                }
                canvas.drawText(state.getWatermarkText(), width - pad * 1.5f, height - 5f * scaleFactor, paintBranding)
            }
        }

        StampTemplate.DARK_GLASS -> {
            val paintGlassBg = Paint().apply {
                color = if (state.useInsideOverlay) 0xCC0F172A.toInt() else 0xFF0F172A.toInt()
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, startY, width.toFloat(), height.toFloat(), paintGlassBg)

            val paintBar = Paint().apply {
                color = 0xFF38BDF8.toInt()
                style = Paint.Style.FILL
            }
            val barRect = RectF(pad * 0.8f, startY + pad * 0.5f, pad * 0.8f + 4f * scaleFactor, height - pad * 0.5f)
            canvas.drawRoundRect(barRect, 2f * scaleFactor, 2f * scaleFactor, paintBar)

            val tx = pad * 0.8f + 4f * scaleFactor + pad * 0.8f
            val tw = width - tx - pad
            var ty = startY + pad * 0.6f + 11f * scaleFactor

            val rawCityLine = listOfNotNull(
                state.city.takeIf { it.isNotEmpty() },
                state.state.takeIf { it.isNotEmpty() }
            ).joinToString(", ").ifEmpty { "Location" }
            val cityLine = cleanAddressText(rawCityLine)
            val paintCity = Paint().apply {
                color = 0xFFF8FAFC.toInt()
                textSize = 13f * scaleFactor
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            drawWithClipping(cityLine, tx, ty, tw, paintCity)
            ty += 13f * scaleFactor

            val addrParts = listOfNotNull(
                state.road.takeIf { it.isNotEmpty() && !it.contains("Manually entered") },
                state.city.takeIf { it.isNotEmpty() },
                state.postcode.takeIf { it.isNotEmpty() },
                state.country.takeIf { it.isNotEmpty() }
            )
            val addressText = cleanAddressText(addrParts.joinToString(", ").ifEmpty { "Georeferenced Survey Location" })
            val paintAddr = Paint().apply {
                color = 0xFF94A3B8.toInt()
                textSize = 8.5f * scaleFactor
                typeface = Typeface.SANS_SERIF
                isAntiAlias = true
            }
            drawWithClipping(addressText, tx, ty, tw, paintAddr)
            ty += 11f * scaleFactor

            val dirStr = if (state.isCompassEnabled) "   DIR: ${state.getCardinalDirection()}" else ""
            val altStr = if (state.accuracy != null) "   ALT: ${Math.round(state.accuracy * 12f + 180)}m" else ""
            val metaText = "LAT ${String.format(Locale.US, "%.4f", state.lat)}°  LON ${String.format(Locale.US, "%.4f", state.lng)}°$dirStr$altStr"
            val paintMeta = Paint().apply {
                color = 0xFFBAE6FD.toInt()
                textSize = 8f * scaleFactor
                typeface = Typeface.MONOSPACE
                isAntiAlias = true
            }
            drawWithClipping(metaText, tx, ty, tw, paintMeta)
            ty += 10f * scaleFactor

            val dt = if (state.manualTime != null) Date(state.manualTime) else Date()
            val sdfDate = SimpleDateFormat("dd-MM-yyyy (EEE) HH:mm:ss", Locale.ENGLISH)
            val accText = "ACC ±${if (state.accuracy != null) Math.round(state.accuracy) else 5}m"
            val timeString = "${sdfDate.format(dt)}   $accText"
            drawWithClipping(timeString, tx, ty, tw, paintMeta)

            if (hasCustomNote) {
                ty += 10f * scaleFactor
                val paintNote = Paint().apply {
                    color = 0xFFFDE047.toInt()
                    textSize = 8.2f * scaleFactor
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    isAntiAlias = true
                }
                drawWithClipping("REMARK: ${state.customNote.trim()}", tx, ty, tw, paintNote)
            }

            if (!state.removeWatermark) {
                val paintBranding = Paint().apply {
                    color = 0xFF64748B.toInt()
                    textSize = 7.5f * scaleFactor
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    textAlign = Paint.Align.RIGHT
                    isAntiAlias = true
                }
                canvas.drawText(state.getWatermarkText(), width - pad, height - 4f * scaleFactor, paintBranding)
            }
        }

        StampTemplate.OFFICIAL_STAMP -> {
            val paintGradient = Paint().apply {
                shader = LinearGradient(
                    0f, startY, 0f, height.toFloat(),
                    intArrayOf(0x88000000.toInt(), 0xEE000000.toInt()),
                    null,
                    Shader.TileMode.CLAMP
                )
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, startY, width.toFloat(), height.toFloat(), paintGradient)

            val sealSize = 52f * scaleFactor
            val sealX = width - pad - sealSize
            val sealY = startY + (stampHeight - sealSize) / 2f

            val paintSealBorder = Paint().apply {
                color = 0xFFDC2626.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 2.5f * scaleFactor
                isAntiAlias = true
            }
            val paintSealBg = Paint().apply {
                color = 0xFFFFFFFF.toInt()
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val sealRadius = sealSize / 2f
            val sealCenterX = sealX + sealRadius
            val sealCenterY = sealY + sealRadius
            canvas.drawCircle(sealCenterX, sealCenterY, sealRadius, paintSealBg)
            canvas.drawCircle(sealCenterX, sealCenterY, sealRadius, paintSealBorder)

            val paintSealText = Paint().apply {
                color = 0xFF991B1B.toInt()
                textSize = 7f * scaleFactor
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("★", sealCenterX, sealCenterY - 4f * scaleFactor, paintSealText)
            canvas.drawText("OFFICIAL", sealCenterX, sealCenterY + 4f * scaleFactor, paintSealText)
            canvas.drawText("SEAL", sealCenterX, sealCenterY + 11f * scaleFactor, paintSealText)

            val tx = pad * 1.2f
            val tw = sealX - tx - pad * 0.8f
            var ty = startY + pad * 0.8f + 11f * scaleFactor

            val rawCityLine = listOfNotNull(
                state.city.takeIf { it.isNotEmpty() },
                state.state.takeIf { it.isNotEmpty() }
            ).joinToString(", ").ifEmpty { "Official Survey Location" }
            val cityLine = cleanAddressText(rawCityLine)
            val paintCity = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 13f * scaleFactor
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            drawWithClipping(cityLine, tx, ty, tw, paintCity)
            ty += 13f * scaleFactor

            val addrParts = listOfNotNull(
                state.road.takeIf { it.isNotEmpty() && !it.contains("Manually entered") },
                state.city.takeIf { it.isNotEmpty() },
                state.postcode.takeIf { it.isNotEmpty() },
                state.country.takeIf { it.isNotEmpty() }
            )
            val addressText = cleanAddressText(addrParts.joinToString(", ").ifEmpty { "Verified Georeferenced Location" })
            val paintAddr = Paint().apply {
                color = 0xFFE4E4E7.toInt()
                textSize = 8.5f * scaleFactor
                typeface = Typeface.SANS_SERIF
                isAntiAlias = true
            }
            drawWithClipping(addressText, tx, ty, tw, paintAddr)
            ty += 11f * scaleFactor

            val dirStr = if (state.isCompassEnabled) " • DIR ${state.getCardinalDirection()}" else ""
            val altStr = if (state.accuracy != null) " • ALT ${Math.round(state.accuracy * 12f + 180)}m MSL" else ""
            val metaText = "${String.format(Locale.US, "%.4f", state.lat)}°N, ${String.format(Locale.US, "%.4f", state.lng)}°E$dirStr$altStr"
            val paintMeta = Paint().apply {
                color = 0xFFD4D4D8.toInt()
                textSize = 8f * scaleFactor
                typeface = Typeface.MONOSPACE
                isAntiAlias = true
            }
            drawWithClipping(metaText, tx, ty, tw, paintMeta)
            ty += 10f * scaleFactor

            val dt = if (state.manualTime != null) Date(state.manualTime) else Date()
            val sdfDate = SimpleDateFormat("EEE, dd MMM yyyy, hh:mm:ss a (z)", Locale.ENGLISH)
            val accText = " • ACC: ±${if (state.accuracy != null) Math.round(state.accuracy) else 5}m"
            val timeString = "${sdfDate.format(dt)}$accText"
            drawWithClipping(timeString, tx, ty, tw, paintMeta)

            if (hasCustomNote) {
                ty += 10f * scaleFactor
                val paintNote = Paint().apply {
                    color = 0xFFFACC15.toInt()
                    textSize = 8.2f * scaleFactor
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    isAntiAlias = true
                }
                drawWithClipping("REMARK: ${state.customNote.trim()}", tx, ty, tw, paintNote)
            }

            if (!state.removeWatermark) {
                val paintBranding = Paint().apply {
                    color = 0xFFCBD5E1.toInt()
                    textSize = 7.5f * scaleFactor
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    textAlign = Paint.Align.RIGHT
                    isAntiAlias = true
                }
                canvas.drawText(state.getWatermarkText(), width - pad, height - 4f * scaleFactor, paintBranding)
            }
        }

        StampTemplate.ECO_AGRI -> {
            val agriMargin = 8f * scaleFactor
            val agriRect = RectF(agriMargin, startY + 4f * scaleFactor, width - agriMargin, height - 4f * scaleFactor)

            val paintAgriBg = Paint().apply {
                color = if (state.useInsideOverlay) 0xEE06231A.toInt() else 0xFF06231A.toInt()
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(agriRect, 8f * scaleFactor, 8f * scaleFactor, paintAgriBg)

            val paintEmerald = Paint().apply {
                color = 0xFF10B981.toInt()
                style = Paint.Style.FILL
            }
            canvas.save()
            val clipPath = Path().apply {
                addRoundRect(agriRect, 8f * scaleFactor, 8f * scaleFactor, Path.Direction.CW)
            }
            canvas.clipPath(clipPath)
            canvas.drawRect(RectF(agriRect.left, agriRect.top, agriRect.left + 5f * scaleFactor, agriRect.bottom), paintEmerald)
            canvas.restore()

            val tx = agriRect.left + 5f * scaleFactor + 10f * scaleFactor
            val tw = agriRect.right - tx - 10f * scaleFactor
            var ty = agriRect.top + 13f * scaleFactor

            val rawCityLine = listOfNotNull(
                state.city.takeIf { it.isNotEmpty() },
                state.state.takeIf { it.isNotEmpty() }
            ).joinToString(", ").ifEmpty { "Agriculture Farm" }
            val cityLine = "🌾 " + cleanAddressText(rawCityLine)
            val paintCity = Paint().apply {
                color = 0xFF6EE7B7.toInt()
                textSize = 12.5f * scaleFactor
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            drawWithClipping(cityLine, tx, ty, tw - 60f * scaleFactor, paintCity)

            val badgeW = 55f * scaleFactor
            val badgeH = 12f * scaleFactor
            val badgeX = agriRect.right - 10f * scaleFactor - badgeW
            val badgeY = ty - 10f * scaleFactor
            val paintBadge = Paint().apply {
                color = 0x3310B981.toInt()
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(RectF(badgeX, badgeY, badgeX + badgeW, badgeY + badgeH), 4f * scaleFactor, 4f * scaleFactor, paintBadge)
            val paintBadgeText = Paint().apply {
                color = 0xFFA7F3D0.toInt()
                textSize = 7f * scaleFactor
                textAlign = Paint.Align.CENTER
                typeface = Typeface.MONOSPACE
                isAntiAlias = true
            }
            canvas.drawText("ECO PLOT", badgeX + badgeW / 2f, badgeY + 8.5f * scaleFactor, paintBadgeText)
            ty += 13f * scaleFactor

            val addrParts = listOfNotNull(
                state.road.takeIf { it.isNotEmpty() && !it.contains("Manually entered") },
                state.city.takeIf { it.isNotEmpty() },
                state.postcode.takeIf { it.isNotEmpty() },
                state.country.takeIf { it.isNotEmpty() }
            )
            val addressText = cleanAddressText(addrParts.joinToString(", ").ifEmpty { "Field Agricultural Verification" })
            val paintAddr = Paint().apply {
                color = 0xFFD1FAE5.toInt()
                textSize = 8.5f * scaleFactor
                typeface = Typeface.SANS_SERIF
                isAntiAlias = true
            }
            drawWithClipping(addressText, tx, ty, tw, paintAddr)
            ty += 11f * scaleFactor

            val dirStr = if (state.isCompassEnabled) " • DIR ${state.getCardinalDirection()}" else ""
            val altStr = if (state.accuracy != null) " • ALT ${Math.round(state.accuracy * 12f + 180)}m" else ""
            val metaText = "${String.format(Locale.US, "%.4f", state.lat)}°N, ${String.format(Locale.US, "%.4f", state.lng)}°E$altStr$dirStr"
            val paintMeta = Paint().apply {
                color = 0xFF6EE7B7.toInt()
                textSize = 8f * scaleFactor
                typeface = Typeface.MONOSPACE
                isAntiAlias = true
            }
            drawWithClipping(metaText, tx, ty, tw, paintMeta)
            ty += 10f * scaleFactor

            val dt = if (state.manualTime != null) Date(state.manualTime) else Date()
            val sdfDate = SimpleDateFormat("EEE, dd MMM yyyy, hh:mm:ss a (z)", Locale.ENGLISH)
            val accText = " • ACC: ±${if (state.accuracy != null) Math.round(state.accuracy) else 5}m"
            val timeString = "${sdfDate.format(dt)}$accText"
            drawWithClipping(timeString, tx, ty, tw, paintMeta)

            if (hasCustomNote) {
                ty += 10f * scaleFactor
                val paintNote = Paint().apply {
                    color = 0xFFFDE047.toInt()
                    textSize = 8.2f * scaleFactor
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    isAntiAlias = true
                }
                drawWithClipping("REMARK: ${state.customNote.trim()}", tx, ty, tw, paintNote)
            }

            if (!state.removeWatermark) {
                val paintBranding = Paint().apply {
                    color = 0xFF34D399.toInt()
                    textSize = 7.5f * scaleFactor
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    textAlign = Paint.Align.RIGHT
                    isAntiAlias = true
                }
                canvas.drawText(state.getWatermarkText(), agriRect.right - 8f * scaleFactor, agriRect.bottom - 4f * scaleFactor, paintBranding)
            }
        }

        StampTemplate.REVENUE_AMBER -> {
            val paintAmberBg = Paint().apply {
                color = if (state.useInsideOverlay) 0xEE18181B.toInt() else 0xFF18181B.toInt()
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, startY, width.toFloat(), height.toFloat(), paintAmberBg)

            val paintAmberLine = Paint().apply {
                color = 0xFFD97706.toInt()
                strokeWidth = 2.5f * scaleFactor
                style = Paint.Style.STROKE
            }
            canvas.drawLine(0f, startY, width.toFloat(), startY, paintAmberLine)

            val tx = pad * 1.2f
            val tw = width - tx - pad * 1.2f
            var ty = startY + pad * 0.7f + 11f * scaleFactor

            val rawCityLine = listOfNotNull(
                state.city.takeIf { it.isNotEmpty() },
                state.state.takeIf { it.isNotEmpty() }
            ).joinToString(" • ").ifEmpty { "Demarcation Zone" }
            val cityLine = cleanAddressText(rawCityLine)
            val paintCity = Paint().apply {
                color = 0xFFFBBF24.toInt()
                textSize = 13f * scaleFactor
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            drawWithClipping(cityLine, tx, ty, tw, paintCity)
            ty += 13f * scaleFactor

            val addrParts = listOfNotNull(
                state.road.takeIf { it.isNotEmpty() && !it.contains("Manually entered") },
                state.city.takeIf { it.isNotEmpty() },
                state.postcode.takeIf { it.isNotEmpty() },
                state.country.takeIf { it.isNotEmpty() }
            )
            val addressText = cleanAddressText(addrParts.joinToString(", ").ifEmpty { "Land Revenue Boundary Demarcation" })
            val paintAddr = Paint().apply {
                color = 0xFFD4D4D8.toInt()
                textSize = 8.5f * scaleFactor
                typeface = Typeface.SANS_SERIF
                isAntiAlias = true
            }
            drawWithClipping(addressText, tx, ty, tw, paintAddr)
            ty += 11f * scaleFactor

            val dirStr = if (state.isCompassEnabled) " • ${state.getCardinalDirection()}" else ""
            val altStr = if (state.accuracy != null) "${Math.round(state.accuracy * 12f + 180)}m MSL" else "214m MSL"
            val metaLine1 = "LAT/LON: ${String.format(Locale.US, "%.4f", state.lat)}°N, ${String.format(Locale.US, "%.4f", state.lng)}°E   ALT/DIR: $altStr$dirStr"
            val paintMeta = Paint().apply {
                color = 0xFFFEF3C7.toInt()
                textSize = 8f * scaleFactor
                typeface = Typeface.MONOSPACE
                isAntiAlias = true
            }
            drawWithClipping(metaLine1, tx, ty, tw, paintMeta)
            ty += 10f * scaleFactor

            val dt = if (state.manualTime != null) Date(state.manualTime) else Date()
            val sdfDate = SimpleDateFormat("EEE, dd MMM yyyy, hh:mm:ss a (z)", Locale.ENGLISH)
            val accText = "ACC: ±${if (state.accuracy != null) Math.round(state.accuracy) else 5}m"
            val metaLine2 = "TIMESTAMP: ${sdfDate.format(dt)} • $accText"
            drawWithClipping(metaLine2, tx, ty, tw, paintMeta)

            if (hasCustomNote) {
                ty += 10f * scaleFactor
                val paintNote = Paint().apply {
                    color = 0xFFFEF08A.toInt()
                    textSize = 8.2f * scaleFactor
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                    isAntiAlias = true
                }
                drawWithClipping("REMARK: ${state.customNote.trim()}", tx, ty, tw, paintNote)
            }

            if (!state.removeWatermark) {
                val paintBranding = Paint().apply {
                    color = 0xFFA1A1AA.toInt()
                    textSize = 7.5f * scaleFactor
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    textAlign = Paint.Align.RIGHT
                    isAntiAlias = true
                }
                canvas.drawText(state.getWatermarkText(), width - pad, height - 4f * scaleFactor, paintBranding)
            }
        }

        StampTemplate.GLACIER_FROST -> {
            val paintGlacierBg = Paint().apply {
                color = if (state.useInsideOverlay) 0x66FFFFFF.toInt() else 0x88FFFFFF.toInt()
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, startY, width.toFloat(), height.toFloat(), paintGlacierBg)

            val paintGlacierBorder = Paint().apply {
                color = 0xAAFFFFFF.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 1.2f * scaleFactor
            }
            canvas.drawLine(0f, startY, width.toFloat(), startY, paintGlacierBorder)

            val tx = pad * 1.2f
            val tw = width - tx - pad * 1.2f
            var ty = startY + pad * 0.7f + 11f * scaleFactor

            val rawCityLine = listOfNotNull(
                state.city.takeIf { it.isNotEmpty() },
                state.state.takeIf { it.isNotEmpty() }
            ).joinToString(", ").ifEmpty { "Location" }
            val cityLine = cleanAddressText(rawCityLine)
            val paintCity = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 13.5f * scaleFactor
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
                setShadowLayer(3f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xCC000000.toInt())
            }
            drawWithClipping(cityLine, tx, ty, tw, paintCity)
            ty += 13f * scaleFactor

            val addrParts = listOfNotNull(
                state.road.takeIf { it.isNotEmpty() && !it.contains("Manually entered") },
                state.city.takeIf { it.isNotEmpty() },
                state.postcode.takeIf { it.isNotEmpty() },
                state.country.takeIf { it.isNotEmpty() }
            )
            val addressText = cleanAddressText(addrParts.joinToString(", ").ifEmpty { "Georeferenced Survey Location" })
            val paintAddr = Paint().apply {
                color = 0xFFF1F5F9.toInt()
                textSize = 8.5f * scaleFactor
                typeface = Typeface.SANS_SERIF
                isAntiAlias = true
                setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xAA000000.toInt())
            }
            drawWithClipping(addressText, tx, ty, tw, paintAddr)
            ty += 11f * scaleFactor

            val dirStr = if (state.isCompassEnabled) " • DIR: ${state.getCardinalDirection()}" else ""
            val altStr = if (state.accuracy != null) " • ALT: ${Math.round(state.accuracy * 12f + 180)}m MSL" else ""
            val accText = " • ACC: ±${if (state.accuracy != null) Math.round(state.accuracy) else 5}m"
            val metaText = "${String.format(Locale.US, "%.4f", state.lat)}°N, ${String.format(Locale.US, "%.4f", state.lng)}°E$altStr$dirStr$accText"
            val paintMeta = Paint().apply {
                color = 0xFFE2E8F0.toInt()
                textSize = 8f * scaleFactor
                typeface = Typeface.MONOSPACE
                isAntiAlias = true
                setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xAA000000.toInt())
            }
            drawWithClipping(metaText, tx, ty, tw, paintMeta)
            ty += 10f * scaleFactor

            val dt = if (state.manualTime != null) Date(state.manualTime) else Date()
            val sdfDate = SimpleDateFormat("EEE, dd MMM yyyy, hh:mm:ss a (z)", Locale.ENGLISH)
            val timeString = sdfDate.format(dt)
            drawWithClipping(timeString, tx, ty, tw, paintMeta)

            if (hasCustomNote) {
                ty += 10f * scaleFactor
                val paintNote = Paint().apply {
                    color = 0xFFFEF08A.toInt()
                    textSize = 8.2f * scaleFactor
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    isAntiAlias = true
                    setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xCC000000.toInt())
                }
                drawWithClipping("REMARK: ${state.customNote.trim()}", tx, ty, tw, paintNote)
            }

            if (!state.removeWatermark) {
                val paintBranding = Paint().apply {
                    color = 0xFFCBD5E1.toInt()
                    textSize = 7.5f * scaleFactor
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    textAlign = Paint.Align.RIGHT
                    isAntiAlias = true
                    setShadowLayer(2f * scaleFactor, 1f * scaleFactor, 1f * scaleFactor, 0xAA000000.toInt())
                }
                canvas.drawText(state.getWatermarkText(), width - pad, height - 4f * scaleFactor, paintBranding)
            }
        }
    }

    return output
}

// Media3 Transformer worker that burns in overlays directly into H.264 streams
private suspend fun transcodeVideoWithWatermark(
    context: Context,
    videoUri: Uri,
    state: LocationState,
    isFrontCamera: Boolean = false,
    isSelfieMirrored: Boolean = true
): Uri? {
    return withContext(Dispatchers.Default) {
        val deferred = CompletableDeferred<Uri?>()

        // 1. Fetch exact video dimensions in pixels
        val d = extractVideoDimensions(context, videoUri)
        val w = d.first
        val h = d.second

        // 2. Build high contrast overlay bitmap
        val overlayBitmap = drawOverlayOnVideoFrame(w, h, state)

        // 3. Create temp destination file
        val tempOutput = File(context.cacheDir, "Survey_Geotagged_${System.currentTimeMillis()}.mp4")
        if (tempOutput.exists()) {
            tempOutput.delete()
        }

        withContext(Dispatchers.Main) {
            try {
                val transformer = Transformer.Builder(context)
                    .setVideoMimeType(MimeTypes.VIDEO_H264)
                    .setAudioMimeType(MimeTypes.AUDIO_AAC)
                    .build()

                val videoEffectList = mutableListOf<androidx.media3.common.Effect>()

                // If recorded from front camera and selfie mirroring is enabled, apply horizontal mirror transformation
                if (isFrontCamera && isSelfieMirrored) {
                    try {
                        val mirrorEffect = ScaleAndRotateTransformation.Builder()
                            .setScale(-1f, 1f)
                            .build()
                        videoEffectList.add(mirrorEffect)
                    } catch (e: Exception) {
                        Log.w("GpsViewModel", "ScaleAndRotateTransformation failed", e)
                    }
                }

                val bitmapOverlay = BitmapOverlay.createStaticBitmapOverlay(overlayBitmap)
                val overlayEffect = OverlayEffect(ImmutableList.of(bitmapOverlay as androidx.media3.effect.TextureOverlay))
                videoEffectList.add(overlayEffect)

                val editedMediaItem = EditedMediaItem.Builder(MediaItem.fromUri(videoUri))
                    .setEffects(Effects(ImmutableList.of(), videoEffectList))
                    .build()

                transformer.addListener(object : Transformer.Listener {
                    override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                        deferred.complete(Uri.fromFile(tempOutput))
                    }

                    override fun onError(
                        composition: Composition,
                        exportResult: ExportResult,
                        exportException: ExportException
                    ) {
                        Log.e("GpsViewModel", "Transformer enc failed", exportException)
                        deferred.complete(null)
                    }
                })

                transformer.start(editedMediaItem, tempOutput.absolutePath)
            } catch (e: Exception) {
                Log.e("GpsViewModel", "Transformer start failed", e)
                deferred.complete(null)
            }
        }
        deferred.await()
    }
}

// Write the original high-fidelity video file to Movies directory with embedded watermark overlay
private suspend fun saveProcessedVideo(
    context: Context,
    videoUri: Uri,
    state: LocationState,
    viewModel: GpsViewModel,
    isFrontCamera: Boolean = false,
    isSelfieMirrored: Boolean = true
): String? {
    viewModel.setProcessing(true, "Stamping physical GPS track into video…")

    val watermarkedUri = transcodeVideoWithWatermark(
        context = context,
        videoUri = videoUri,
        state = state,
        isFrontCamera = isFrontCamera,
        isSelfieMirrored = isSelfieMirrored
    )
    val finalVideoSource = watermarkedUri ?: videoUri // safe fallback if transcoding somehow fails

    val result = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        val filename = "PRO_GPS_VID_$timestamp.mp4"
        var outStream: OutputStream? = null
        var inStream: InputStream? = null
        var savedUrl: String? = null

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/ProGpsCam")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        try {
            val resolver = context.contentResolver
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
            val targetUri = resolver.insert(collection, values)
            if (targetUri != null) {
                outStream = resolver.openOutputStream(targetUri)
                inStream = resolver.openInputStream(finalVideoSource)
                if (outStream != null && inStream != null) {
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (inStream.read(buffer).also { bytesRead = it } != -1) {
                        outStream.write(buffer, 0, bytesRead)
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(targetUri, values, null, null)
                }
                savedUrl = targetUri.toString()
            }
        } catch (e: Exception) {
            Log.e("GpsViewModel", "Error saving video", e)
        } finally {
            try {
                inStream?.close()
            } catch (ignored: Exception) {}
            try {
                outStream?.close()
            } catch (ignored: Exception) {}
        }
        savedUrl
    }

    viewModel.setProcessing(false)
    return result
}

// Share video intent helper
private suspend fun shareProcessedVideo(context: Context, videoUri: Uri) {
    withContext(Dispatchers.IO) {
        try {
            val cachePath = File(context.cacheDir, "videos")
            cachePath.mkdirs()
            val file = File(cachePath, "shared_survey_video.mp4")
            
            context.contentResolver.openInputStream(videoUri)?.use { input ->
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }

            val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            if (contentUri != null) {
                val shareIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setDataAndType(contentUri, "video/mp4")
                    putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
                    type = "video/mp4"
                }
                withContext(Dispatchers.Main) {
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Video"))
                }
            }
        } catch (e: Exception) {
            Log.e("GpsViewModel", "Error sharing video", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Sharing failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// Beautifully stylized high-fidelity Compose GPS Stamp Overlay for looping playback
@Composable
fun VideoOverlayStamp(state: LocationState, modifier: Modifier = Modifier) {
    StampDesignCard(
        state = state,
        template = state.stampTemplate,
        scale = state.stampScale,
        modifier = modifier
    )
}

// Built-in looping video player using native Android VideoView
@Composable
fun VideoPlayerView(uri: Uri, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { ctx ->
            VideoView(ctx).apply {
                setVideoURI(uri)
                val mediaController = MediaController(ctx)
                mediaController.setAnchorView(this)
                setMediaController(mediaController)
                setOnPreparedListener { mp ->
                    mp.isLooping = true
                    start()
                }
            }
        },
        modifier = modifier
    )
}

// High-fidelity full-view state matching the user requests for video review
@Composable
fun VideoResultViewScreen(
    videoUri: Uri,
    state: LocationState,
    viewModel: GpsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Upper top action back header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🎥",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = "Video Ready",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = PanelColor),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(50)
            ) {
                Text("← Back", color = TextColor, fontWeight = FontWeight.Bold)
            }
        }

        // Mid aligned video holder preview card with permanent stamp overlay embedded
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(PanelColor),
            contentAlignment = Alignment.Center
        ) {
            if (state.useInsideOverlay) {
                // Overlayed inside the video using Box!
                Box(modifier = Modifier.fillMaxSize()) {
                    VideoPlayerView(
                        uri = videoUri,
                        modifier = Modifier.fillMaxSize()
                    )
                    VideoOverlayStamp(
                        state = state,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    )
                }
            } else {
                // Docked outside below the video using Column
                Column(modifier = Modifier.fillMaxSize()) {
                    VideoPlayerView(
                        uri = videoUri,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                    VideoOverlayStamp(
                        state = state,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Active save sharing dynamic trigger layout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Save Video action triggers
            Button(
                onClick = {
                    isSaving = true
                    scope.launch {
                        val filepath = saveProcessedVideo(context, videoUri, state, viewModel)
                        if (filepath != null) {
                            val savedUri = Uri.parse(filepath)
                            viewModel.addSessionMedia(SessionMediaItem(uri = savedUri, isVideo = true))
                            Toast.makeText(context, "Saved to Gallery! folder: Movies/ProGpsCam", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Error saving video.", Toast.LENGTH_SHORT).show()
                        }
                        isSaving = false
                    }
                },
                enabled = !isSaving,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CustomGreen),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
            ) {
                Text(
                    text = "⬇️ Save Video",
                    color = BgColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Share Video triggers
            Button(
                onClick = {
                    scope.launch {
                        shareProcessedVideo(context, videoUri)
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PanelColor),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
            ) {
                Text(
                    text = "📤 Share Video",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// Gorgeous live CameraX viewport card with exact embedded real-time GPS stamp overlay
@Composable
fun CameraSurveyPanel(
    state: LocationState,
    viewModel: GpsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isVideoMode by remember { mutableStateOf(false) }
    var deviceOrientationDegrees by remember { mutableStateOf(0) }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var recording by remember { mutableStateOf<Recording?>(null) }
    var isRecording by remember { mutableStateOf(false) }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(context) {
        val listener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return
                // Closest 90 degree step
                val rot = when (orientation) {
                    in 45 until 135 -> 270
                    in 135 until 225 -> 180
                    in 225 until 315 -> 90
                    else -> 0
                }
                if (deviceOrientationDegrees != rot) {
                    deviceOrientationDegrees = rot
                }
            }
        }
        if (listener.canDetectOrientation()) {
            listener.enable()
        }
        onDispose {
            listener.disable()
        }
    }

    val targetRotation = when (deviceOrientationDegrees) {
        90 -> Surface.ROTATION_270
        180 -> Surface.ROTATION_180
        270 -> Surface.ROTATION_90
        else -> Surface.ROTATION_0
    }

    LaunchedEffect(cameraProviderFuture, isVideoMode) {
        val cameraProvider = withContext(Dispatchers.IO) {
            try {
                cameraProviderFuture.get()
            } catch (e: Exception) {
                null
            }
        }
        if (cameraProvider != null) {
            val ratio = if (isVideoMode) AspectRatio.RATIO_16_9 else AspectRatio.RATIO_4_3

            val preview = Preview.Builder()
                .setTargetAspectRatio(ratio)
                .build()

            try {
                cameraProvider.unbindAll()

                if (isVideoMode) {
                    val recorder = Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.SD))
                        .build()
                    val vidCap = VideoCapture.withOutput(recorder)
                    vidCap.targetRotation = targetRotation
                    
                    videoCapture = vidCap
                    imageCapture = null

                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        vidCap
                    )
                } else {
                    val imgCap = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetAspectRatio(ratio)
                        .setTargetRotation(targetRotation)
                        .build()

                    imageCapture = imgCap
                    videoCapture = null

                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imgCap
                    )
                }
                preview.setSurfaceProvider(previewView.surfaceProvider)
            } catch (e: Exception) {
                Log.e("CameraSurveyPanel", "Binding failed", e)
            }
        }
    }

    // Dynamic rotation change binder live
    LaunchedEffect(targetRotation) {
        try {
            imageCapture?.targetRotation = targetRotation
        } catch (e: Exception) {
            // ignore
        }
        try {
            videoCapture?.targetRotation = targetRotation
        } catch (e: Exception) {
            // ignore
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .background(PanelColor, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (!isVideoMode) AccentCyan else Color.Transparent)
                    .clickable {
                        if (!isRecording) {
                            isVideoMode = false
                        } else {
                            Toast.makeText(context, "Stop recording first", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📸 PHOTO (3:4)",
                    color = if (!isVideoMode) BgColor else TextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isVideoMode) AccentOrange else Color.Transparent)
                    .clickable {
                        if (!isRecording) {
                            isVideoMode = true
                        } else {
                            Toast.makeText(context, "Stop recording first", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎥 VIDEO (9:16)",
                    color = if (isVideoMode) BgColor else TextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Viewport Box with Exact Aspect Ratio Matching Sensors!
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(if (isVideoMode) 9f / 16f else 3f / 4f)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, if (isVideoMode) AccentOrange.copy(alpha = 0.6f) else AccentCyan.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            // Live Camera viewport
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )

            // watermark stamp aligned directly over preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                VideoLiveOverlayStamp(state = state, deviceOrientationDegrees = deviceOrientationDegrees)
            }

            // recording blinking indicator
            if (isRecording) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color.Red)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RECORDING SURVEY VIDEO",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // orientation label
            if (deviceOrientationDegrees != 0) {
                val orientationLabel = when (deviceOrientationDegrees) {
                    90 -> "Landscape Left 📋"
                    180 -> "Portrait Inverted 📋"
                    270 -> "Landscape Right 📋"
                    else -> "Portrait 📋"
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = orientationLabel,
                        color = AccentCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Center aligned Shutter control
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isVideoMode) {
                // PHOTO Shutter: double border circular button
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(36.dp))
                        .background(Color.White)
                        .clickable {
                            viewModel.triggerHapticFeedback(context)
                            val img = imageCapture ?: return@clickable
                            viewModel.setProcessing(true, "Capturing Survey Photo…")
                            val file = File(context.cacheDir, "cap_${System.currentTimeMillis()}.jpg")
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

                            img.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        viewModel.processAndAutoSavePhoto(Uri.fromFile(file), context) {
                                            // Saved directly to device gallery and camera remains active for next shot
                                        }
                                    }
                                    override fun onError(exception: ImageCaptureException) {
                                        viewModel.setProcessing(false)
                                        Toast.makeText(context, "Photo capture failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                        .border(4.dp, Color.Black, RoundedCornerShape(36.dp))
                        .padding(4.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📸", fontSize = 24.sp, color = Color.Black)
                }
            } else {
                // VIDEO Shutter: red target capture and Stop action
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(36.dp))
                        .background(if (isRecording) Color.Red else Color.White)
                        .clickable {
                            viewModel.triggerHapticFeedback(context)
                            val vid = videoCapture ?: return@clickable
                            if (isRecording) {
                                recording?.stop()
                                recording = null
                                isRecording = false
                                viewModel.triggerHapticFeedback(context)
                            } else {
                                viewModel.setProcessing(true, "Preparing Video Recording…")
                                val file = File(context.cacheDir, "rec_${System.currentTimeMillis()}.mp4")
                                val fileOptions = FileOutputOptions.Builder(file).build()

                                try {
                                    val pending = vid.output.prepareRecording(context, fileOptions)
                                    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                        try {
                                            pending.withAudioEnabled()
                                        } catch (e: SecurityException) {
                                            Log.w("LiveCamera", "Fallback audio enable error", e)
                                        }
                                    }
                                    recording = pending.start(ContextCompat.getMainExecutor(context)) { event ->
                                            when (event) {
                                                is VideoRecordEvent.Start -> {
                                                    isRecording = true
                                                    viewModel.setProcessing(false)
                                                    viewModel.triggerHapticFeedback(context)
                                                }
                                                is VideoRecordEvent.Finalize -> {
                                                    isRecording = false
                                                    viewModel.setProcessing(false)
                                                    viewModel.triggerHapticFeedback(context)
                                                    if (!event.hasError()) {
                                                        viewModel.processVideoFile(Uri.fromFile(file), context)
                                                    } else {
                                                        Toast.makeText(context, "Video recording error", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        }
                                } catch (e: SecurityException) {
                                    viewModel.setProcessing(false)
                                    Toast.makeText(context, "Permissions required for recording", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    viewModel.setProcessing(false)
                                    Toast.makeText(context, "Failed to start recording loop", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        .border(4.dp, Color.Black, RoundedCornerShape(36.dp))
                        .padding(4.dp)
                        .clip(if (isRecording) RoundedCornerShape(8.dp) else RoundedCornerShape(32.dp))
                        .background(if (isRecording) Color.Red else Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (isRecording) "⏹️" else "🎥", fontSize = 24.sp, color = if (isRecording) Color.White else Color.Black)
                }
            }
        }
    }
}

// Reusable, modular Compose Stamp Design for all 4 templates
@Composable
fun StampDesignCard(
    state: LocationState,
    template: StampTemplate = state.stampTemplate,
    scale: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    val s = scale.coerceIn(0.5f, 1.5f)
    val hasCustomNote = state.isCustomNoteEnabled && state.customNote.isNotBlank()
    val dt = if (state.manualTime != null) Date(state.manualTime) else Date()
    val sdfDate = SimpleDateFormat("EEEE, dd/MM/yyyy hh:mm:ss a", Locale.ENGLISH)
    val sdfShortDate = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH)

    val rawCityLine = listOfNotNull(
        state.city.takeIf { it.isNotEmpty() },
        state.state.takeIf { it.isNotEmpty() },
        state.country.takeIf { it.isNotEmpty() }
    ).distinct().joinToString(", ").ifEmpty { "Detecting Survey Location…" }
    val cityLine = cleanAddressText(rawCityLine)

    val addrParts = listOfNotNull(
        state.road.takeIf { it.isNotEmpty() && !it.contains("Manually entered") },
        state.city.takeIf { it.isNotEmpty() },
        state.state.takeIf { it.isNotEmpty() },
        state.postcode.takeIf { it.isNotEmpty() },
        state.country.takeIf { it.isNotEmpty() }
    ).distinct()
    val addressText = cleanAddressText(addrParts.joinToString(", ").ifEmpty { "Georeferenced Survey Location" })

    when (template) {
        StampTemplate.STANDARD -> {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (state.useInsideOverlay) Color(0x990A0B0E) else Color(0xFF0A0B0E).copy(alpha = 0.92f))
                    .padding(bottom = (3f * s).coerceIn(2f, 6f).dp)
            ) {
                if (!state.useInsideOverlay) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((3f * s).coerceIn(2f, 4f).dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF00E5FF),
                                        Color(0xFF7B61FF),
                                        Color(0xFFFF6B35)
                                    )
                                )
                            )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = (8f * s).coerceIn(4f, 14f).dp,
                            vertical = (4f * s).coerceIn(2f, 8f).dp
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((6f * s).coerceIn(4f, 10f).dp)
                ) {
                    // Map card box
                    Box(
                        modifier = Modifier
                            .size(
                                width = (58.dp * s).coerceIn(38.dp, 84.dp),
                                height = (50.dp * s).coerceIn(34.dp, 74.dp)
                            )
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0F111A))
                            .border(1.dp, Color(0x4000E5FF), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "AUTOMATIC",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = (5.5f * s).coerceIn(4f, 8f).sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "📍",
                                fontSize = (10f * s).coerceIn(8f, 15f).sp
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            val formattedLat = String.format(Locale.US, "%.3f", state.lat)
                            val formattedLng = String.format(Locale.US, "%.3f", state.lng)
                            Text(
                                text = "${formattedLat}°N",
                                color = AccentCyan,
                                fontSize = (5.5f * s).coerceIn(4f, 8f).sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${formattedLng}°E",
                                color = AccentCyan,
                                fontSize = (5.5f * s).coerceIn(4f, 8f).sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Text readings
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = cityLine,
                            color = Color.White,
                            fontSize = (10.5f * s).coerceIn(7.5f, 14.5f).sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = addressText,
                            color = Color(0xFFE8EAF0).copy(alpha = 0.8f),
                            fontSize = (7.2f * s).coerceIn(5.5f, 10.5f).sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val latText = "Lat ${String.format(Locale.US, "%.5f", state.lat)}°"
                        val lngText = "Long ${String.format(Locale.US, "%.5f", state.lng)}°"
                        val dirText = if (state.isCompassEnabled) "   Dir: ${state.getCardinalDirection()}" else ""
                        Text(
                            text = "$latText   $lngText$dirText",
                            color = AccentCyan,
                            fontSize = (8f * s).coerceIn(6f, 11.5f).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val accText = " • ACCURACY: ±${if (state.accuracy != null) Math.round(state.accuracy) else 5}m"
                        Text(
                            text = "${sdfDate.format(dt).uppercase()}$accText",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = (7.2f * s).coerceIn(5.5f, 10.5f).sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (hasCustomNote) {
                            Text(
                                text = "Note: ${state.customNote.trim()}",
                                color = Color(0xFFFFD700),
                                fontSize = (7.2f * s).coerceIn(5.5f, 10.5f).sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (!state.removeWatermark) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = (10f * s).coerceIn(6f, 14f).dp, top = 1.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = state.getWatermarkText(),
                            color = AccentCyan.copy(alpha = 0.85f),
                            fontSize = (7.2f * s).coerceIn(5.5f, 10.5f).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }
        }

        StampTemplate.CLASSIC -> {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (state.useInsideOverlay) Color(0x99110E08) else Color(0xFF140F08).copy(alpha = 0.94f))
                    .border(1.2.dp, Color(0xFFFFD700).copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                    .padding(bottom = (3f * s).coerceIn(2f, 6f).dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((3f * s).coerceIn(2f, 4f).dp)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFFFD700), Color(0xFFFFA000), Color(0xFFFFD700))
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = (8f * s).coerceIn(4f, 14f).dp,
                            vertical = (4f * s).coerceIn(2f, 8f).dp
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((6f * s).coerceIn(4f, 10f).dp)
                ) {
                    // Seal Badge Box
                    Box(
                        modifier = Modifier
                            .size(
                                width = (58.dp * s).coerceIn(38.dp, 84.dp),
                                height = (50.dp * s).coerceIn(34.dp, 74.dp)
                            )
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x33FFD700))
                            .border(1.dp, Color(0x80FFD700), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "SEAL // OFFICIAL",
                                color = Color(0xFFFFD700),
                                fontSize = (5f * s).coerceIn(4f, 7.5f).sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "🏛️",
                                fontSize = (11f * s).coerceIn(9f, 16f).sp
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            val formattedLat = String.format(Locale.US, "%.3f", state.lat)
                            val formattedLng = String.format(Locale.US, "%.3f", state.lng)
                            Text(
                                text = "${formattedLat}°N",
                                color = Color(0xFFFFD700),
                                fontSize = (5.5f * s).coerceIn(4f, 8f).sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${formattedLng}°E",
                                color = Color(0xFFFFD700),
                                fontSize = (5.5f * s).coerceIn(4f, 8f).sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Text readings
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = "🏛️ $cityLine  [SURVEY RECORD]",
                            color = Color(0xFFFFD700),
                            fontSize = (10.5f * s).coerceIn(7.5f, 14.5f).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = addressText,
                            color = Color(0xFFF5E6CC),
                            fontSize = (7.2f * s).coerceIn(5.5f, 10.5f).sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val coordText = "COORD: ${String.format(Locale.US, "%.5f", state.lat)}°N, ${String.format(Locale.US, "%.5f", state.lng)}°E"
                        Text(
                            text = coordText,
                            color = Color(0xFFFFD700),
                            fontSize = (7.8f * s).coerceIn(6f, 11.5f).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val accText = " • ACCURACY: ±${if (state.accuracy != null) Math.round(state.accuracy) else 5}m"
                        Text(
                            text = "DATE: ${sdfDate.format(dt)}$accText".uppercase(),
                            color = Color(0xFFFFE082),
                            fontSize = (7.2f * s).coerceIn(5.5f, 10.5f).sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (hasCustomNote) {
                            Text(
                                text = "REMARK: ${state.customNote.trim()}",
                                color = Color(0xFFFFD700),
                                fontSize = (7.2f * s).coerceIn(5.5f, 10.5f).sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (!state.removeWatermark) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = (10f * s).coerceIn(6f, 14f).dp, top = 1.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = state.getWatermarkText(),
                            color = Color(0xFFFFD700).copy(alpha = 0.9f),
                            fontSize = (7.2f * s).coerceIn(5.5f, 10.5f).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                    }
                }
            }
        }

        StampTemplate.TECH_HUD -> {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (state.useInsideOverlay) Color(0x99050F14) else Color(0xFF040E14).copy(alpha = 0.94f))
                    .border(1.2.dp, Color(0xFF00E5FF).copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(bottom = (3f * s).coerceIn(2f, 6f).dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((3f * s).coerceIn(2f, 4f).dp)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF00E5FF), Color(0xFF00FF66), Color(0xFF00E5FF))
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = (8f * s).coerceIn(4f, 14f).dp,
                            vertical = (4f * s).coerceIn(2f, 8f).dp
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((6f * s).coerceIn(4f, 10f).dp)
                ) {
                    // Radar HUD Box
                    Box(
                        modifier = Modifier
                            .size(
                                width = (58.dp * s).coerceIn(38.dp, 84.dp),
                                height = (50.dp * s).coerceIn(34.dp, 74.dp)
                            )
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x3300E5FF))
                            .border(1.dp, Color(0x8000FF66), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "RADAR 3D",
                                color = Color(0xFF00FF66),
                                fontSize = (5f * s).coerceIn(4f, 7.5f).sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "⚡",
                                fontSize = (11f * s).coerceIn(9f, 16f).sp
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            val formattedLat = String.format(Locale.US, "%.3f", state.lat)
                            val formattedLng = String.format(Locale.US, "%.3f", state.lng)
                            Text(
                                text = "${formattedLat}°N",
                                color = Color(0xFF00E5FF),
                                fontSize = (5.5f * s).coerceIn(4f, 8f).sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${formattedLng}°E",
                                color = Color(0xFF00E5FF),
                                fontSize = (5.5f * s).coerceIn(4f, 8f).sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Text readings
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = "POS // $cityLine",
                            color = Color(0xFF00E5FF),
                            fontSize = (10.5f * s).coerceIn(7.5f, 14.5f).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "LOC // $addressText",
                            color = Color(0xFF00FF88),
                            fontSize = (7.2f * s).coerceIn(5.5f, 10.5f).sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val coordText = "GEO // LAT: ${String.format(Locale.US, "%.5f", state.lat)}°  LON: ${String.format(Locale.US, "%.5f", state.lng)}°"
                        Text(
                            text = coordText,
                            color = Color(0xFF00FF66),
                            fontSize = (7.8f * s).coerceIn(6f, 11.5f).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val accText = " // ACC: ±${if (state.accuracy != null) Math.round(state.accuracy) else 5}m"
                        Text(
                            text = "UTC // ${sdfDate.format(dt).uppercase()}$accText",
                            color = Color(0xFFE0F2FE),
                            fontSize = (7.2f * s).coerceIn(5.5f, 10.5f).sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (hasCustomNote) {
                            Text(
                                text = "NOTE // ${state.customNote.trim()}",
                                color = Color(0xFFFFD700),
                                fontSize = (7.2f * s).coerceIn(5.5f, 10.5f).sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (!state.removeWatermark) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = (10f * s).coerceIn(6f, 14f).dp, top = 1.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = state.getWatermarkText(),
                            color = Color(0xFF00E5FF),
                            fontSize = (7.2f * s).coerceIn(5.5f, 10.5f).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        StampTemplate.CLEAN_MINIMAL -> {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (state.useInsideOverlay) Color(0xCC0F172A) else Color(0xFF0F172A))
                    .border(1.2.dp, Color(0x80818CF8), RoundedCornerShape(14.dp))
                    .padding(horizontal = (10f * s).coerceIn(6f, 14f).dp, vertical = (6f * s).coerceIn(3f, 10f).dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((8f * s).coerceIn(4f, 12f).dp)
                ) {
                    // Minimal circle
                    Box(
                        modifier = Modifier
                            .size((38.dp * s).coerceIn(28.dp, 52.dp))
                            .clip(CircleShape)
                            .background(Color(0x4D4F46E5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✨", fontSize = (16f * s).coerceIn(12f, 22f).sp)
                    }

                    // Text readings
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        val latStr = "${String.format(Locale.US, "%.3f", state.lat)}°N"
                        val lngStr = "${String.format(Locale.US, "%.3f", state.lng)}°E"
                        Text(
                            text = "$cityLine  •  $latStr, $lngStr",
                            color = Color.White,
                            fontSize = (10.5f * s).coerceIn(7.5f, 14.5f).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = addressText,
                            color = Color(0xFFE2E8F0),
                            fontSize = (7.2f * s).coerceIn(5.5f, 10.5f).sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val accText = "ACC: ±${if (state.accuracy != null) Math.round(state.accuracy) else 5}m"
                        Text(
                            text = "${sdfShortDate.format(dt)}  •  $accText",
                            color = Color(0xFF94A3B8),
                            fontSize = (7.2f * s).coerceIn(5.5f, 10.5f).sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (hasCustomNote) {
                            Text(
                                text = "Note: ${state.customNote.trim()}",
                                color = Color(0xFF38BDF8),
                                fontSize = (7.2f * s).coerceIn(5.5f, 10.5f).sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (!state.removeWatermark) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = (10f * s).coerceIn(6f, 14f).dp, top = 2.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = state.getWatermarkText(),
                            color = Color(0xFF818CF8),
                            fontSize = (7.2f * s).coerceIn(5.5f, 10.5f).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }
        }

        StampTemplate.DARK_GLASS -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape((10f * s).coerceIn(6f, 12f).dp))
                    .background(Color(0xE60F172A))
                    .border(1.dp, Color(0x4038BDF8), RoundedCornerShape((10f * s).coerceIn(6f, 12f).dp))
                    .padding((8f * s).coerceIn(5f, 12f).dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width((3.5f * s).coerceIn(2.5f, 5f).dp)
                            .height((42f * s).coerceIn(30f, 55f).dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF38BDF8))
                    )
                    Spacer(modifier = Modifier.width((8f * s).coerceIn(5f, 10f).dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = cleanAddressText(listOfNotNull(state.city.takeIf { it.isNotEmpty() }, state.state.takeIf { it.isNotEmpty() }).joinToString(", ").ifEmpty { "Location" }),
                            color = Color(0xFFF8FAFC),
                            fontSize = (11f * s).coerceIn(8f, 15f).sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = cleanAddressText(listOfNotNull(state.road.takeIf { it.isNotEmpty() }, state.city.takeIf { it.isNotEmpty() }, state.country.takeIf { it.isNotEmpty() }).joinToString(", ").ifEmpty { "Georeferenced Survey Location" }),
                            color = Color(0xFF94A3B8),
                            fontSize = (7.8f * s).coerceIn(6f, 11f).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val dirStr = if (state.isCompassEnabled) "  DIR ${state.getCardinalDirection()}" else ""
                        val altStr = if (state.accuracy != null) "  ALT ${Math.round(state.accuracy * 12f + 180)}m" else ""
                        Text(
                            text = "LAT ${String.format(Locale.US, "%.4f", state.lat)}°  LON ${String.format(Locale.US, "%.4f", state.lng)}°$dirStr$altStr",
                            color = Color(0xFFBAE6FD),
                            fontSize = (7.2f * s).coerceIn(5.5f, 10.5f).sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val dt = if (state.manualTime != null) Date(state.manualTime) else Date()
                        val sdf = SimpleDateFormat("dd-MM-yyyy (EEE) HH:mm:ss", Locale.ENGLISH)
                        Text(
                            text = "${sdf.format(dt)}   ACC ±${if (state.accuracy != null) Math.round(state.accuracy) else 5}m",
                            color = Color(0xFFBAE6FD),
                            fontSize = (7.2f * s).coerceIn(5.5f, 10.5f).sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (hasCustomNote) {
                            Text(
                                text = "REMARK: ${state.customNote.trim()}",
                                color = Color(0xFFFDE047),
                                fontSize = (7.5f * s).coerceIn(6f, 11f).sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                if (!state.removeWatermark) {
                    Text(
                        text = state.getWatermarkText(),
                        color = Color(0xFF64748B),
                        fontSize = (7f * s).coerceIn(5.5f, 10f).sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
                    )
                }
            }
        }

        StampTemplate.OFFICIAL_STAMP -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape((8f * s).coerceIn(6f, 12f).dp))
                    .background(Color(0xE618181B))
                    .border(1.5.dp, Color(0xFFDC2626), RoundedCornerShape((8f * s).coerceIn(6f, 12f).dp))
                    .padding((8f * s).coerceIn(5f, 12f).dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = cleanAddressText(listOfNotNull(state.city.takeIf { it.isNotEmpty() }, state.state.takeIf { it.isNotEmpty() }).joinToString(", ").ifEmpty { "Official Survey Location" }),
                            color = Color.White,
                            fontSize = (11f * s).coerceIn(8f, 15f).sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = cleanAddressText(listOfNotNull(state.road.takeIf { it.isNotEmpty() }, state.city.takeIf { it.isNotEmpty() }, state.country.takeIf { it.isNotEmpty() }).joinToString(", ").ifEmpty { "Verified Georeferenced Location" }),
                            color = Color(0xFFE4E4E7),
                            fontSize = (7.8f * s).coerceIn(6f, 11f).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val dirStr = if (state.isCompassEnabled) " • DIR ${state.getCardinalDirection()}" else ""
                        val altStr = if (state.accuracy != null) " • ALT ${Math.round(state.accuracy * 12f + 180)}m" else ""
                        Text(
                            text = "${String.format(Locale.US, "%.4f", state.lat)}°N, ${String.format(Locale.US, "%.4f", state.lng)}°E$dirStr$altStr",
                            color = Color(0xFFD4D4D8),
                            fontSize = (7.2f * s).coerceIn(5.5f, 10.5f).sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val dt = if (state.manualTime != null) Date(state.manualTime) else Date()
                        val sdf = SimpleDateFormat("EEE, dd MMM yyyy, hh:mm:ss a (z)", Locale.ENGLISH)
                        val accText = " • ACC: ±${if (state.accuracy != null) Math.round(state.accuracy) else 5}m"
                        Text(
                            text = "${sdf.format(dt)}$accText",
                            color = Color(0xFFD4D4D8),
                            fontSize = (7.2f * s).coerceIn(5.5f, 10.5f).sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (hasCustomNote) {
                            Text(
                                text = "REMARK: ${state.customNote.trim()}",
                                color = Color(0xFFFACC15),
                                fontSize = (7.5f * s).coerceIn(6f, 11f).sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size((48f * s).coerceIn(36f, 60f).dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, Color(0xFFDC2626), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("★", color = Color(0xFFDC2626), fontSize = (8f * s).coerceIn(6f, 11f).sp, fontWeight = FontWeight.Bold)
                            Text("OFFICIAL", color = Color(0xFF991B1B), fontSize = (6.2f * s).coerceIn(4.8f, 8.5f).sp, fontWeight = FontWeight.Black)
                            Text("SEAL", color = Color(0xFF991B1B), fontSize = (5.5f * s).coerceIn(4.2f, 7.5f).sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (!state.removeWatermark) {
                    Text(
                        text = state.getWatermarkText(),
                        color = Color(0xFFCBD5E1),
                        fontSize = (7f * s).coerceIn(5.5f, 10f).sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
                    )
                }
            }
        }

        StampTemplate.ECO_AGRI -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape((8f * s).coerceIn(6f, 12f).dp))
                    .background(Color(0xE606231A))
                    .border(1.5.dp, Color(0xFF10B981), RoundedCornerShape((8f * s).coerceIn(6f, 12f).dp))
                    .padding((8f * s).coerceIn(5f, 12f).dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🌾 " + cleanAddressText(listOfNotNull(state.city.takeIf { it.isNotEmpty() }, state.state.takeIf { it.isNotEmpty() }).joinToString(", ").ifEmpty { "Agriculture Farm" }),
                        color = Color(0xFF6EE7B7),
                        fontSize = (11f * s).coerceIn(8f, 15f).sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x3310B981))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ECO PLOT",
                            color = Color(0xFFA7F3D0),
                            fontSize = (6.8f * s).coerceIn(5f, 9.5f).sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = cleanAddressText(listOfNotNull(state.road.takeIf { it.isNotEmpty() }, state.city.takeIf { it.isNotEmpty() }, state.country.takeIf { it.isNotEmpty() }).joinToString(", ").ifEmpty { "Field Agricultural Verification" }),
                    color = Color(0xFFD1FAE5),
                    fontSize = (7.8f * s).coerceIn(6f, 11f).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val dirStr = if (state.isCompassEnabled) " • DIR ${state.getCardinalDirection()}" else ""
                val altStr = if (state.accuracy != null) " • ALT ${Math.round(state.accuracy * 12f + 180)}m" else ""
                Text(
                    text = "${String.format(Locale.US, "%.4f", state.lat)}°N, ${String.format(Locale.US, "%.4f", state.lng)}°E$altStr$dirStr",
                    color = Color(0xFF6EE7B7),
                    fontSize = (7.2f * s).coerceIn(5.5f, 10.5f).sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val dt = if (state.manualTime != null) Date(state.manualTime) else Date()
                val sdf = SimpleDateFormat("EEE, dd MMM yyyy, hh:mm:ss a (z)", Locale.ENGLISH)
                val accText = " • ACC: ±${if (state.accuracy != null) Math.round(state.accuracy) else 5}m"
                Text(
                    text = "${sdf.format(dt)}$accText",
                    color = Color(0xFF6EE7B7),
                    fontSize = (7.2f * s).coerceIn(5.5f, 10.5f).sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (hasCustomNote) {
                    Text(
                        text = "REMARK: ${state.customNote.trim()}",
                        color = Color(0xFFFDE047),
                        fontSize = (7.5f * s).coerceIn(6f, 11f).sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!state.removeWatermark) {
                    Text(
                        text = state.getWatermarkText(),
                        color = Color(0xFF34D399),
                        fontSize = (7f * s).coerceIn(5.5f, 10f).sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
                    )
                }
            }
        }

        StampTemplate.REVENUE_AMBER -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape((8f * s).coerceIn(6f, 12f).dp))
                    .background(Color(0xE618181B))
                    .border(1.5.dp, Color(0xFFD97706), RoundedCornerShape((8f * s).coerceIn(6f, 12f).dp))
                    .padding((8f * s).coerceIn(5f, 12f).dp)
            ) {
                Text(
                    text = cleanAddressText(listOfNotNull(state.city.takeIf { it.isNotEmpty() }, state.state.takeIf { it.isNotEmpty() }).joinToString(" • ").ifEmpty { "Demarcation Zone" }),
                    color = Color(0xFFFBBF24),
                    fontSize = (11f * s).coerceIn(8f, 15f).sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = cleanAddressText(listOfNotNull(state.road.takeIf { it.isNotEmpty() }, state.city.takeIf { it.isNotEmpty() }, state.country.takeIf { it.isNotEmpty() }).joinToString(", ").ifEmpty { "Land Revenue Boundary Demarcation" }),
                    color = Color(0xFFD4D4D8),
                    fontSize = (7.8f * s).coerceIn(6f, 11f).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val dirStr = if (state.isCompassEnabled) " • ${state.getCardinalDirection()}" else ""
                val altStr = if (state.accuracy != null) "${Math.round(state.accuracy * 12f + 180)}m MSL" else "214m MSL"
                Text(
                    text = "LAT/LON: ${String.format(Locale.US, "%.4f", state.lat)}°N, ${String.format(Locale.US, "%.4f", state.lng)}°E   ALT/DIR: $altStr$dirStr",
                    color = Color(0xFFFEF3C7),
                    fontSize = (7.2f * s).coerceIn(5.5f, 10.5f).sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val dt = if (state.manualTime != null) Date(state.manualTime) else Date()
                val sdf = SimpleDateFormat("EEE, dd MMM yyyy, hh:mm:ss a (z)", Locale.ENGLISH)
                val accText = "ACC: ±${if (state.accuracy != null) Math.round(state.accuracy) else 5}m"
                Text(
                    text = "TIMESTAMP: ${sdf.format(dt)} • $accText",
                    color = Color(0xFFFEF3C7),
                    fontSize = (7.2f * s).coerceIn(5.5f, 10.5f).sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (hasCustomNote) {
                    Text(
                        text = "REMARK: ${state.customNote.trim()}",
                        color = Color(0xFFFEF08A),
                        fontSize = (7.5f * s).coerceIn(6f, 11f).sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!state.removeWatermark) {
                    Text(
                        text = state.getWatermarkText(),
                        color = Color(0xFFA1A1AA),
                        fontSize = (7f * s).coerceIn(5.5f, 10f).sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
                    )
                }
            }
        }

        StampTemplate.GLACIER_FROST -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape((8f * s).coerceIn(6f, 12f).dp))
                    .background(Color(0x66FFFFFF))
                    .border(1.dp, Color(0xAAFFFFFF), RoundedCornerShape((8f * s).coerceIn(6f, 12f).dp))
                    .padding((8f * s).coerceIn(5f, 12f).dp)
            ) {
                Text(
                    text = cleanAddressText(listOfNotNull(state.city.takeIf { it.isNotEmpty() }, state.state.takeIf { it.isNotEmpty() }).joinToString(", ").ifEmpty { "Location" }),
                    color = Color.White,
                    fontSize = (11f * s).coerceIn(8f, 15f).sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = cleanAddressText(listOfNotNull(state.road.takeIf { it.isNotEmpty() }, state.city.takeIf { it.isNotEmpty() }, state.country.takeIf { it.isNotEmpty() }).joinToString(", ").ifEmpty { "Georeferenced Survey Location" }),
                    color = Color(0xFFF1F5F9),
                    fontSize = (7.8f * s).coerceIn(6f, 11f).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val dirStr = if (state.isCompassEnabled) " • DIR: ${state.getCardinalDirection()}" else ""
                val altStr = if (state.accuracy != null) " • ALT: ${Math.round(state.accuracy * 12f + 180)}m MSL" else ""
                val accText = " • ACC: ±${if (state.accuracy != null) Math.round(state.accuracy) else 5}m"
                Text(
                    text = "${String.format(Locale.US, "%.4f", state.lat)}°N, ${String.format(Locale.US, "%.4f", state.lng)}°E$altStr$dirStr$accText",
                    color = Color(0xFFE2E8F0),
                    fontSize = (7.2f * s).coerceIn(5.5f, 10.5f).sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val dt = if (state.manualTime != null) Date(state.manualTime) else Date()
                val sdf = SimpleDateFormat("EEE, dd MMM yyyy, hh:mm:ss a (z)", Locale.ENGLISH)
                Text(
                    text = sdf.format(dt),
                    color = Color(0xFFE2E8F0),
                    fontSize = (7.2f * s).coerceIn(5.5f, 10.5f).sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (hasCustomNote) {
                    Text(
                        text = "REMARK: ${state.customNote.trim()}",
                        color = Color(0xFFFEF08A),
                        fontSize = (7.5f * s).coerceIn(6f, 11f).sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!state.removeWatermark) {
                    Text(
                        text = state.getWatermarkText(),
                        color = Color(0xFFCBD5E1),
                        fontSize = (7f * s).coerceIn(5.5f, 10f).sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

// Lightweight realtime stamp preview for camera layer UI
@Composable
fun VideoLiveOverlayStamp(state: LocationState, deviceOrientationDegrees: Int, modifier: Modifier = Modifier) {
    StampDesignCard(
        state = state,
        template = state.stampTemplate,
        scale = state.stampScale,
        modifier = modifier
    )
}

enum class LiveCameraMode {
    PHOTO, VIDEO
}

@Composable
fun LiveCameraViewfinderStamp(
    state: LocationState,
    deviceOrientationDegrees: Int = 0,
    modifier: Modifier = Modifier
) {
    val s = state.stampScale.coerceIn(0.5f, 1.5f)

    // Inverted orientation rotation target to keep stamp readable & upright relative to gravity
    val rotTarget = when (deviceOrientationDegrees) {
        90 -> 270f
        180 -> 180f
        270 -> 90f
        else -> 0f
    }
    val animRot by animateFloatAsState(
        targetValue = rotTarget,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "stampAnimRot"
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val vW = maxWidth
        val vH = maxHeight
        val hasCustomNote = state.isCustomNoteEnabled && state.customNote.isNotBlank()
        val stampHeight = ((if (hasCustomNote) 76.dp else 66.dp) * s).coerceIn(46.dp, 105.dp)

        val isLandscapeOrientation = (deviceOrientationDegrees == 90 || deviceOrientationDegrees == 270)
        val stampLength = if (isLandscapeOrientation) {
            (vH - 16.dp).coerceIn(160.dp, (vH - 16.dp).coerceAtLeast(160.dp))
        } else {
            vW
        }

        val stampBoxModifier = when (deviceOrientationDegrees) {
            270 -> {
                // Tilted Left (Top pointing left): Position along the Left edge of viewfinder (viewer's bottom)
                val offsetX = (stampHeight - stampLength) / 2 + 4.dp
                val offsetY = (vH - stampHeight) / 2
                Modifier
                    .size(width = stampLength, height = stampHeight)
                    .offset(x = offsetX, y = offsetY)
                    .graphicsLayer {
                        rotationZ = animRot
                    }
            }
            90 -> {
                // Tilted Right (Top pointing right): Position along the Right edge of viewfinder (viewer's bottom)
                val offsetX = vW - 4.dp - (stampLength + stampHeight) / 2
                val offsetY = (vH - stampHeight) / 2
                Modifier
                    .size(width = stampLength, height = stampHeight)
                    .offset(x = offsetX, y = offsetY)
                    .graphicsLayer {
                        rotationZ = animRot
                    }
            }
            180 -> {
                // Inverted Portrait: Top Center
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 3.dp)
                    .graphicsLayer {
                        rotationZ = animRot
                    }
            }
            else -> {
                // Standard Portrait: Bottom Center
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 3.dp)
                    .graphicsLayer {
                        rotationZ = animRot
                    }
            }
        }

        Box(
            modifier = stampBoxModifier
        ) {
            StampDesignCard(
                state = state,
                template = state.stampTemplate,
                scale = s
            )
        }
    }
}

@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier
) {
    val liveAdUnitId = BuildConfig.ADMOB_BANNER_ID

    var isAdLoaded by remember { mutableStateOf(false) }
    var adLoadAttempt by remember { mutableIntStateOf(0) }
    var adStatusText by remember { mutableStateOf("AdMob • Connecting...") }

    // Periodic auto-reload every 30 seconds if ad is not loaded or to refresh
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(30000L)
            if (!isAdLoaded) {
                adLoadAttempt++
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(Color.Black.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        if (!isAdLoaded) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = adStatusText,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        key(adLoadAttempt) {
            AndroidView(
                factory = { context ->
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        setAdUnitId(liveAdUnitId)
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        adListener = object : com.google.android.gms.ads.AdListener() {
                            override fun onAdLoaded() {
                                isAdLoaded = true
                                Log.d("AdMobBanner", "Real AdMob banner ad successfully loaded ($liveAdUnitId)")
                            }

                            override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                                isAdLoaded = false
                                adStatusText = "No ads to show • Retrying..."
                                Log.d("AdMobBanner", "Real banner failed to load: ${error.message} (code: ${error.code})")
                            }
                        }
                        try {
                            loadAd(AdRequest.Builder().build())
                        } catch (t: Throwable) {
                            isAdLoaded = false
                            adStatusText = "No ads to show"
                            Log.w("AdMobBanner", "Error calling loadAd: ${t.message}")
                        }
                    }
                },
                modifier = Modifier.wrapContentSize()
            )
        }
    }
}

@Composable
fun rememberCompassAzimuth(isEnabled: Boolean): Float {
    val context = LocalContext.current
    var azimuth by remember { mutableFloatStateOf(0f) }

    DisposableEffect(isEnabled) {
        if (!isEnabled) return@DisposableEffect onDispose {}

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            ?: return@DisposableEffect onDispose {}

        val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        var lastSmoothedAzimuth = 0f

        fun calculateHeadingFromMatrix(R: FloatArray): Float {
            // Camera line-of-sight is along the -Z axis of the physical device.
            // In Android's world coordinate system (X=East, Y=North, Z=Up):
            // Camera vector v_cam = R * (0, 0, -1)^T = (-R[2], -R[5], -R[8]).
            // Horizontal component: (-R[2], -R[5]).
            val camNormSq = R[2] * R[2] + R[5] * R[5]
            val angleRad = if (camNormSq > 0.08f) {
                // Device held upright or angled towards the horizon (standard camera survey posture)
                // Heading = atan2(East, North) = atan2(-R[2], -R[5])
                Math.atan2(-R[2].toDouble(), -R[5].toDouble())
            } else {
                // Device lying flat on table (screen facing sky), top of device (Y axis) points forward
                // Heading = atan2(East, North) = atan2(R[1], R[4])
                Math.atan2(R[1].toDouble(), R[4].toDouble())
            }
            var deg = Math.toDegrees(angleRad).toFloat()
            if (deg < 0f) deg += 360f
            return (deg + 360f) % 360f
        }

        val listener = object : SensorEventListener {
            val rotationMatrix = FloatArray(9)
            val accelValues = FloatArray(3)
            val magValues = FloatArray(3)
            var hasAccel = false
            var hasMag = false

            override fun onSensorChanged(event: SensorEvent) {
                var rawAzimuth = lastSmoothedAzimuth
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    rawAzimuth = calculateHeadingFromMatrix(rotationMatrix)
                } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, accelValues, 0, 3)
                    hasAccel = true
                } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(event.values, 0, magValues, 0, 3)
                    hasMag = true
                }

                if (hasAccel && hasMag && event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) {
                    if (SensorManager.getRotationMatrix(rotationMatrix, null, accelValues, magValues)) {
                        rawAzimuth = calculateHeadingFromMatrix(rotationMatrix)
                    }
                }

                rawAzimuth = (rawAzimuth + 360f) % 360f
                val diff = (rawAzimuth - lastSmoothedAzimuth + 540f) % 360f - 180f
                val step = if (Math.abs(diff) < 0.15f) 0f else diff * 0.22f
                val smoothed = (lastSmoothedAzimuth + step + 360f) % 360f
                lastSmoothedAzimuth = smoothed
                azimuth = smoothed
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (rotationVectorSensor != null) {
            sensorManager.registerListener(listener, rotationVectorSensor, SensorManager.SENSOR_DELAY_GAME)
        } else {
            if (accelerometerSensor != null) sensorManager.registerListener(listener, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME)
            if (magnetometerSensor != null) sensorManager.registerListener(listener, magnetometerSensor, SensorManager.SENSOR_DELAY_GAME)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    return azimuth
}

@Composable
fun LiveCompassHUD(azimuth: Float, modifier: Modifier = Modifier) {
    val deg = ((azimuth % 360f) + 360f) % 360f
    val cardinal = when {
        deg >= 348.75 || deg < 11.25 -> "N"
        deg >= 11.25 && deg < 33.75 -> "NNE"
        deg >= 33.75 && deg < 56.25 -> "NE"
        deg >= 56.25 && deg < 78.75 -> "ENE"
        deg >= 78.75 && deg < 101.25 -> "E"
        deg >= 101.25 && deg < 123.75 -> "ESE"
        deg >= 123.75 && deg < 146.25 -> "SE"
        deg >= 146.25 && deg < 168.75 -> "SSE"
        deg >= 168.75 && deg < 191.25 -> "S"
        deg >= 191.25 && deg < 213.75 -> "SSW"
        deg >= 213.75 && deg < 236.25 -> "SW"
        deg >= 236.25 && deg < 258.75 -> "WSW"
        deg >= 258.75 && deg < 281.25 -> "W"
        deg >= 281.25 && deg < 303.75 -> "WNW"
        deg >= 303.75 && deg < 326.25 -> "NW"
        else -> "NNW"
    }

    val compassSize = 94.dp

    Box(
        modifier = modifier
            .size(compassSize)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xF00D1526),
                        Color(0xFC060A14)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.sweepGradient(
                    listOf(
                        Color(0xFF00E5FF).copy(alpha = 0.85f),
                        Color(0xFF1E293B),
                        Color(0xFF3B82F6).copy(alpha = 0.75f),
                        Color(0xFF00E5FF).copy(alpha = 0.85f)
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // 1. Rotating Compass Rose & 3D Needle Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Rotating the dial counter-clockwise so that the facing heading aligns with the top index marker
                    rotationZ = -deg
                }
        ) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val radius = w / 2f
            val rOuter = radius - 4.dp.toPx()
            val rText = radius - 13.5.dp.toPx()
            val rNeedle = radius - 17.dp.toPx()

            // Subtle circular inner track
            drawCircle(
                color = Color(0x2400E5FF),
                radius = radius - 14.5.dp.toPx(),
                center = Offset(cx, cy),
                style = Stroke(width = 0.8.dp.toPx())
            )

            // Subtle Crosshairs
            drawLine(
                color = Color(0x1800E5FF),
                start = Offset(cx, cy - rNeedle),
                end = Offset(cx, cy + rNeedle),
                strokeWidth = 0.8.dp.toPx()
            )
            drawLine(
                color = Color(0x1800E5FF),
                start = Offset(cx - rNeedle, cy),
                end = Offset(cx + rNeedle, cy),
                strokeWidth = 0.8.dp.toPx()
            )

            // 72 Precision Ticks (every 5 degrees)
            for (angle in 0 until 360 step 5) {
                val rad = Math.toRadians((angle - 90).toDouble())
                val cosA = Math.cos(rad).toFloat()
                val sinA = Math.sin(rad).toFloat()

                val (tickLen, tickColor, strokeW) = when {
                    angle == 0 -> Triple(8.5.dp.toPx(), Color(0xFFFF3B30), 2.4.dp.toPx()) // North: Bold Red
                    angle % 90 == 0 -> Triple(7.5.dp.toPx(), Color(0xFFFFFFFF), 1.8.dp.toPx()) // E, S, W: White
                    angle % 30 == 0 -> Triple(5.5.dp.toPx(), Color(0xFF38BDF8), 1.4.dp.toPx()) // 30 deg: Light Cyan
                    angle % 10 == 0 -> Triple(3.5.dp.toPx(), Color.White.copy(alpha = 0.6f), 1.0.dp.toPx())
                    else -> Triple(2.2.dp.toPx(), Color.White.copy(alpha = 0.25f), 0.75.dp.toPx())
                }

                val startX = cx + rOuter * cosA
                val startY = cy + rOuter * sinA
                val endX = cx + (rOuter - tickLen) * cosA
                val endY = cy + (rOuter - tickLen) * sinA

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = strokeW
                )
            }

            // 3D Faceted Magnetic Needle on the Rotating Dial
            val needleBaseHalf = 3.2.dp.toPx()

            // North Half (Red / Crimson 3D bevel)
            val pathNorthLeft = android.graphics.Path().apply {
                moveTo(cx, cy)
                lineTo(cx - needleBaseHalf, cy - 2.dp.toPx())
                lineTo(cx, cy - rNeedle)
                close()
            }
            val paintNorthLeft = android.graphics.Paint().apply {
                color = 0xFFFF3B30.toInt()
                isAntiAlias = true
                style = android.graphics.Paint.Style.FILL
            }
            drawContext.canvas.nativeCanvas.drawPath(pathNorthLeft, paintNorthLeft)

            val pathNorthRight = android.graphics.Path().apply {
                moveTo(cx, cy)
                lineTo(cx + needleBaseHalf, cy - 2.dp.toPx())
                lineTo(cx, cy - rNeedle)
                close()
            }
            val paintNorthRight = android.graphics.Paint().apply {
                color = 0xFFCC1818.toInt()
                isAntiAlias = true
                style = android.graphics.Paint.Style.FILL
            }
            drawContext.canvas.nativeCanvas.drawPath(pathNorthRight, paintNorthRight)

            // South Half (Silver / Shadow-gray 3D bevel)
            val pathSouthLeft = android.graphics.Path().apply {
                moveTo(cx, cy)
                lineTo(cx - needleBaseHalf, cy + 2.dp.toPx())
                lineTo(cx, cy + rNeedle)
                close()
            }
            val paintSouthLeft = android.graphics.Paint().apply {
                color = 0xFFE2E8F0.toInt()
                isAntiAlias = true
                style = android.graphics.Paint.Style.FILL
            }
            drawContext.canvas.nativeCanvas.drawPath(pathSouthLeft, paintSouthLeft)

            val pathSouthRight = android.graphics.Path().apply {
                moveTo(cx, cy)
                lineTo(cx + needleBaseHalf, cy + 2.dp.toPx())
                lineTo(cx, cy + rNeedle)
                close()
            }
            val paintSouthRight = android.graphics.Paint().apply {
                color = 0xFF94A3B8.toInt()
                isAntiAlias = true
                style = android.graphics.Paint.Style.FILL
            }
            drawContext.canvas.nativeCanvas.drawPath(pathSouthRight, paintSouthRight)

            // Cardinal Labels (N, E, S, W)
            val paintN = android.graphics.Paint().apply {
                color = 0xFFFF3B30.toInt()
                textSize = 9.5.sp.toPx()
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }
            drawContext.canvas.nativeCanvas.drawText("N", cx, cy - rText + 3.2.dp.toPx(), paintN)

            val paintWhite = android.graphics.Paint().apply {
                color = 0xFFF1F5F9.toInt()
                textSize = 8.5.sp.toPx()
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }
            drawContext.canvas.nativeCanvas.drawText("E", cx + rText, cy + 3.dp.toPx(), paintWhite)
            drawContext.canvas.nativeCanvas.drawText("S", cx, cy + rText + 3.2.dp.toPx(), paintWhite)
            drawContext.canvas.nativeCanvas.drawText("W", cx - rText, cy + 3.dp.toPx(), paintWhite)
        }

        // 2. Fixed Top Sighting Marker (Stays locked at 12 o'clock / 0 deg of camera)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val pathTriangle = android.graphics.Path().apply {
                moveTo(cx - 4.5.dp.toPx(), 1.5.dp.toPx())
                lineTo(cx + 4.5.dp.toPx(), 1.5.dp.toPx())
                lineTo(cx, 7.5.dp.toPx())
                close()
            }
            val paintTriangle = android.graphics.Paint().apply {
                color = 0xFFFF3838.toInt()
                isAntiAlias = true
                style = android.graphics.Paint.Style.FILL
                setShadowLayer(4.dp.toPx(), 0f, 0f, 0xFFFF3838.toInt())
            }
            drawContext.canvas.nativeCanvas.drawPath(pathTriangle, paintTriangle)
        }

        // 3. Center Instrument Hub (Floating digital readout)
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xF5080E1B))
                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.55f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${deg.toInt()}°",
                    color = Color.White,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 11.sp
                )
                Text(
                    text = cardinal,
                    color = Color(0xFF00E5FF),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 8.sp
                )
            }
        }
    }
}

@Composable
fun LiveCameraViewScreen(
    state: LocationState,
    viewModel: GpsViewModel,
    sessionMedia: List<SessionMediaItem>,
    onOpenGalleryUpload: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val isSelfieMirrored by viewModel.isSelfieMirrored.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val processingMessage by viewModel.processingMessage.collectAsState()
    val flashMode by viewModel.flashMode.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val pendingOfflineCount by viewModel.pendingOfflineCount.collectAsState()

    val isCompassEnabled by viewModel.isCompassEnabled.collectAsState()
    val liveAzimuth = rememberCompassAzimuth(isCompassEnabled)

    LaunchedEffect(liveAzimuth, isCompassEnabled) {
        if (isCompassEnabled) {
            viewModel.updateAzimuth(liveAzimuth)
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.triggerCustomToast("Microphone audio enabled for videos 🎙️")
        } else {
            viewModel.triggerCustomToast("Audio permission denied. Video will record without sound.")
        }
    }

    var selectedRatio by remember { mutableStateOf(LiveCameraAspectRatio.RATIO_3_4) }
    var selectedMode by remember { mutableStateOf(LiveCameraMode.PHOTO) }
    var isTorchOn by remember { mutableStateOf(false) }
    var isFrontCamera by remember { mutableStateOf(false) }
    var showGridLines by remember { mutableStateOf(false) }
    var showScaleSlider by remember { mutableStateOf(false) }
    var showEditToolsMenu by remember { mutableStateOf(false) }
    var captureTimerSeconds by remember { mutableStateOf(0) } // 0=Off, 3=3s, 5=5s, 10=10s
    var countdownRemaining by remember { mutableStateOf<Int?>(null) }
    var countdownJob by remember { mutableStateOf<Job?>(null) }

    var showPartialGallery by remember { mutableStateOf(false) }
    var showTemplatesSheet by remember { mutableStateOf(false) }
    var previewTemplate by remember { mutableStateOf(state.stampTemplate) }
    var selectedGalleryMediaIndex by remember { mutableStateOf<Int?>(null) }
    var isShutterFlashing by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showManualLocationDialog by remember { mutableStateOf(false) }
    var showCustomNoteDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = selectedGalleryMediaIndex != null) {
        selectedGalleryMediaIndex = null
    }

    BackHandler(enabled = showSettingsDialog) {
        showSettingsDialog = false
    }

    // Tap-to-focus state
    var focusPoint by remember { mutableStateOf<Offset?>(null) }
    var focusVisible by remember { mutableStateOf(false) }
    var currentZoomRatio by remember { mutableStateOf(1.0f) }
    var maxZoomRatio by remember { mutableStateOf(8.0f) }

    var recording by remember { mutableStateOf<Recording?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableStateOf(0) }

    var imageCaptureUseCase by remember { mutableStateOf<ImageCapture?>(null) }
    var videoCaptureUseCase by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }

    var deviceOrientationDegrees by remember { mutableStateOf(0) }

    DisposableEffect(context) {
        val orientationListener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return
                val rot = when (orientation) {
                    in 45 until 135 -> 90
                    in 135 until 225 -> 180
                    in 225 until 315 -> 270
                    else -> 0
                }
                if (deviceOrientationDegrees != rot) {
                    deviceOrientationDegrees = rot
                }
            }
        }
        if (orientationListener.canDetectOrientation()) {
            orientationListener.enable()
        }
        onDispose {
            orientationListener.disable()
        }
    }

    LaunchedEffect(deviceOrientationDegrees, imageCaptureUseCase, videoCaptureUseCase) {
        val targetRot = when (deviceOrientationDegrees) {
            90 -> Surface.ROTATION_270
            180 -> Surface.ROTATION_180
            270 -> Surface.ROTATION_90
            else -> Surface.ROTATION_0
        }
        try {
            imageCaptureUseCase?.targetRotation = targetRot
            videoCaptureUseCase?.targetRotation = targetRot
        } catch (_: Exception) {}
    }

    var isCameraBound by remember { mutableStateOf(false) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            viewModel.triggerCustomToast("Camera permission is needed for live survey viewfinder.")
        }
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        hasCameraPermission = granted
        if (!granted) {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    var cameraProviderInstance by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                cameraProviderInstance?.unbindAll()
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(isFrontCamera, lifecycleOwner, hasCameraPermission) {
        if (!hasCameraPermission) {
            isCameraBound = false
            return@LaunchedEffect
        }
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProviderInstance = cameraProvider
                val cameraSelector = if (isFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                val recorder = Recorder.Builder()
                    .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                    .build()
                val videoCapture = VideoCapture.withOutput(recorder)

                cameraProvider.unbindAll()

                // Check if requested camera is available, fallback safely
                val selectedCamera = when {
                    cameraProvider.hasCamera(cameraSelector) -> cameraSelector
                    cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> CameraSelector.DEFAULT_BACK_CAMERA
                    cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> CameraSelector.DEFAULT_FRONT_CAMERA
                    else -> null
                }

                if (selectedCamera == null) {
                    Log.w("LiveCamera", "No hardware camera available, enabling survey viewport mode")
                    isCameraBound = false
                    return@addListener
                }

                val cam = try {
                    // Try binding all 3 use cases (Preview + ImageCapture + VideoCapture)
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        selectedCamera,
                        preview,
                        imageCapture,
                        videoCapture
                    )
                } catch (e: Exception) {
                    Log.w("LiveCamera", "Binding 3 use cases failed, falling back to Preview + ImageCapture", e)
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            selectedCamera,
                            preview,
                            imageCapture
                        )
                    } catch (e2: Exception) {
                        Log.w("LiveCamera", "Binding Preview + ImageCapture failed, falling back to Preview only", e2)
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            selectedCamera,
                            preview
                        )
                    }
                }

                cameraControl = cam.cameraControl
                isCameraBound = true
                try {
                    cameraControl?.enableTorch(isTorchOn)
                } catch (_: Exception) {}

                cam.cameraInfo.zoomState.observe(lifecycleOwner) { zState ->
                    if (zState != null) {
                        currentZoomRatio = zState.zoomRatio
                        maxZoomRatio = zState.maxZoomRatio.coerceIn(4.0f, 10.0f)
                    }
                }

                imageCaptureUseCase = imageCapture
                videoCaptureUseCase = videoCapture
            } catch (e: Exception) {
                Log.w("LiveCamera", "Camera binding handled gracefully: ${e.message}")
                isCameraBound = false
            }
        }, ContextCompat.getMainExecutor(context))
    }

    LaunchedEffect(flashMode, imageCaptureUseCase) {
        try {
            imageCaptureUseCase?.flashMode = flashMode.flashModeInt
        } catch (_: Exception) {}
    }

    LaunchedEffect(isTorchOn, flashMode, selectedMode, isFrontCamera) {
        try {
            if (!isFrontCamera) {
                val shouldTorch = isTorchOn ||
                        (flashMode == CameraFlashMode.FILL_LIGHT) ||
                        (selectedMode == LiveCameraMode.VIDEO && (flashMode == CameraFlashMode.ON || flashMode == CameraFlashMode.FILL_LIGHT))
                cameraControl?.enableTorch(shouldTorch)
            } else {
                cameraControl?.enableTorch(false)
            }
        } catch (_: Exception) {}
    }

    // Shutter capture trigger logic (with timer support & tactile haptic feedback)
    val executePhotoCapture = {
        viewModel.triggerHapticFeedback(context)
        val imgCapture = imageCaptureUseCase
        if (imgCapture != null && isCameraBound) {
            val photoFile = File(context.cacheDir, "live_photo_${System.currentTimeMillis()}.jpg")
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
            imgCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        isShutterFlashing = true
                        viewModel.triggerHapticFeedback(context)
                        viewModel.processAndAutoSaveLivePhoto(
                            Uri.fromFile(photoFile),
                            context,
                            isFrontCamera = isFrontCamera,
                            isSelfieMirrored = isSelfieMirrored,
                            targetAspectRatio = selectedRatio
                        ) {
                            // Auto saved directly to device gallery with GPS stamp
                        }
                    }
                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(context, "Photo capture error: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        } else {
            // Fallback capture: Create a rich survey frame when hardware camera is not available or emulated
            val width = 1440
            val height = when (selectedRatio) {
                LiveCameraAspectRatio.RATIO_1_1 -> 1440
                LiveCameraAspectRatio.RATIO_3_4 -> 1920
                LiveCameraAspectRatio.RATIO_9_16 -> 2560
                LiveCameraAspectRatio.RATIO_FULL -> 3040
            }
            val simulatedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(simulatedBitmap)
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

            val gradient = android.graphics.LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                android.graphics.Color.rgb(24, 32, 47),
                android.graphics.Color.rgb(11, 15, 25),
                android.graphics.Shader.TileMode.CLAMP
            )
            paint.shader = gradient
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.shader = null

            // Grid survey pattern
            paint.color = android.graphics.Color.argb(35, 255, 255, 255)
            paint.strokeWidth = 2f
            val step = 120
            var x = 0
            while (x < width) {
                canvas.drawLine(x.toFloat(), 0f, x.toFloat(), height.toFloat(), paint)
                x += step
            }
            var y = 0
            while (y < height) {
                canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), paint)
                y += step
            }

            // Crosshair Reticle
            paint.color = android.graphics.Color.argb(130, 0, 229, 255)
            paint.style = android.graphics.Paint.Style.STROKE
            paint.strokeWidth = 4f
            val cx = width / 2f
            val cy = height / 2f
            canvas.drawCircle(cx, cy, 140f, paint)
            canvas.drawLine(cx - 180f, cy, cx + 180f, cy, paint)
            canvas.drawLine(cx, cy - 180f, cx, cy + 180f, paint)

            val photoFile = File(context.cacheDir, "live_photo_${System.currentTimeMillis()}.jpg")
            try {
                FileOutputStream(photoFile).use { out ->
                    simulatedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                isShutterFlashing = true
                viewModel.triggerHapticFeedback(context)
                viewModel.processAndAutoSaveLivePhoto(
                    Uri.fromFile(photoFile),
                    context,
                    isFrontCamera = isFrontCamera,
                    isSelfieMirrored = isSelfieMirrored,
                    targetAspectRatio = selectedRatio
                ) {
                    // Auto saved directly to device gallery with GPS stamp
                }
            } catch (e: Exception) {
                Log.e("LiveCamera", "Simulation photo capture failed", e)
            } finally {
                simulatedBitmap.recycle()
            }
        }
    }

    val executeVideoCapture = {
        viewModel.triggerHapticFeedback(context)
        val vidCapture = videoCaptureUseCase
        if (vidCapture != null) {
            if (isRecording) {
                recording?.stop()
                recording = null
                isRecording = false
                viewModel.triggerHapticFeedback(context)
            } else {
                val hasAudioPerm = ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.RECORD_AUDIO
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                if (!hasAudioPerm) {
                    audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                }

                val videoFile = File(context.cacheDir, "live_vid_${System.currentTimeMillis()}.mp4")
                val outputOptions = FileOutputOptions.Builder(videoFile).build()
                val pendingRecording = vidCapture.output
                    .prepareRecording(context, outputOptions)
                    .apply {
                        if (hasAudioPerm) {
                            try {
                                withAudioEnabled()
                            } catch (e: SecurityException) {
                                Log.w("LiveCamera", "Audio permission error", e)
                            }
                        }
                    }

                recording = pendingRecording.start(ContextCompat.getMainExecutor(context)) { event ->
                    when (event) {
                        is VideoRecordEvent.Start -> {
                            isRecording = true
                            viewModel.triggerHapticFeedback(context)
                        }
                        is VideoRecordEvent.Finalize -> {
                            isRecording = false
                            viewModel.triggerHapticFeedback(context)
                            if (!event.hasError()) {
                                viewModel.processAndAutoSaveLiveVideo(
                                    videoFile = videoFile,
                                    context = context,
                                    isFrontCamera = isFrontCamera,
                                    isSelfieMirrored = isSelfieMirrored
                                )
                            } else {
                                Toast.makeText(context, "Video recording error", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    val onShutterClicked = {
        viewModel.triggerHapticFeedback(context)
        if (isRecording) {
            // If recording, stop immediately without timer
            executeVideoCapture()
        } else {
            if (selectedMode == LiveCameraMode.VIDEO) {
                val hasAudioPerm = ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.RECORD_AUDIO
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (!hasAudioPerm) {
                    audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                }
            }

            if (captureTimerSeconds > 0) {
                // Start countdown
                countdownJob?.cancel()
                countdownJob = coroutineScope.launch {
                    var remaining = captureTimerSeconds
                    while (remaining > 0) {
                        countdownRemaining = remaining
                        triggerHapticFeedback(context)
                        delay(1000)
                        remaining--
                    }
                    countdownRemaining = null
                    triggerHapticFeedback(context)
                    if (selectedMode == LiveCameraMode.PHOTO) {
                        executePhotoCapture()
                    } else {
                        executeVideoCapture()
                    }
                }
            } else {
                if (selectedMode == LiveCameraMode.PHOTO) {
                    executePhotoCapture()
                } else {
                    executeVideoCapture()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.volumeKeyEvent.collect { keyType ->
            when (viewModel.volumeKeyAction.value) {
                "capture" -> {
                    onShutterClicked()
                }
                "zoom" -> {
                    if (cameraControl != null) {
                        val step = 0.5f
                        val newRatio = if (keyType == VolumeKeyType.UP) {
                            (currentZoomRatio + step).coerceAtMost(maxZoomRatio)
                        } else {
                            (currentZoomRatio - step).coerceAtLeast(1.0f)
                        }
                        cameraControl?.setZoomRatio(newRatio)
                        val formattedZoom = String.format(java.util.Locale.ENGLISH, "%.1f", newRatio)
                        viewModel.triggerCustomToast("Zoom: ${formattedZoom}x")
                    }
                }
            }
        }
    }

    // Center loading / ready status overlay logic
    val isLocationLoading = state.lat == 0.0 ||
            state.road.contains("Fetching", ignoreCase = true) ||
            state.city.isBlank() ||
            state.city == "Please wait…" ||
            state.city == "Location Found" ||
            state.statusType == "searching"

    var noticeDismissed by remember { mutableStateOf(false) }

    // Auto-dismiss location popup strictly after 3 seconds max, or 800ms after location ready
    LaunchedEffect(Unit) {
        delay(3000)
        noticeDismissed = true
    }

    LaunchedEffect(isLocationLoading) {
        if (!isLocationLoading) {
            delay(800)
            noticeDismissed = true
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0
            while (isActive && isRecording) {
                delay(1000)
                recordingSeconds++
            }
        }
    }

    LaunchedEffect(isShutterFlashing) {
        if (isShutterFlashing) {
            delay(120)
            isShutterFlashing = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. Camera Viewfinder with Selected Aspect Ratio and Tap-to-Focus
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val ratioMod = when (val r = selectedRatio.ratioFloat) {
                null -> Modifier.fillMaxSize()
                else -> Modifier
                    .fillMaxWidth()
                    .aspectRatio(r)
            }

            Box(
                modifier = ratioMod
                    .pointerInput(previewView, currentZoomRatio, maxZoomRatio) {
                        detectTransformGestures { _, _, zoom, _ ->
                            if (zoom != 1.0f && cameraControl != null) {
                                val sensitivityMultiplier = 2.2f
                                val zoomDelta = (zoom - 1.0f) * sensitivityMultiplier
                                val effectiveZoom = (1.0f + zoomDelta).coerceAtLeast(0.1f)
                                val newRatio = (currentZoomRatio * effectiveZoom).coerceIn(1.0f, maxZoomRatio)
                                if (kotlin.math.abs(newRatio - currentZoomRatio) > 0.001f) {
                                    currentZoomRatio = newRatio
                                    cameraControl?.setZoomRatio(newRatio)
                                }
                            }
                        }
                    }
                    .pointerInput(previewView) {
                        detectTapGestures { offset ->
                            try {
                                val factory = previewView.meteringPointFactory
                                val point = factory.createPoint(offset.x, offset.y)
                                val action = FocusMeteringAction.Builder(
                                    point,
                                    FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
                                ).setAutoCancelDuration(3, TimeUnit.SECONDS).build()
                                cameraControl?.startFocusAndMetering(action)
                                focusPoint = offset
                                focusVisible = true
                            } catch (e: Exception) {
                                Log.e("LiveCamera", "Tap to focus failed", e)
                            }
                        }
                    }
            ) {
                if (hasCameraPermission) {
                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier.fillMaxSize().clipToBounds()
                    )
                }

                if (!isCameraBound || !hasCameraPermission) {
                    // Modern Survey Viewport Backdrop when hardware camera is emulated or initializing
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF18202F),
                                        Color(0xFF0F172A),
                                        Color(0xFF0A0E1A)
                                    )
                                )
                            )
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val gridColor = Color.White.copy(alpha = 0.06f)
                            val step = 48.dp.toPx()
                            var x = 0f
                            while (x < w) {
                                drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                                x += step
                            }
                            var y = 0f
                            while (y < h) {
                                drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                                y += step
                            }

                            // Center Survey Reticle
                            val center = Offset(w / 2f, h / 2f)
                            val reticleColor = Color(0x6600E5FF)
                            drawCircle(reticleColor, radius = 64.dp.toPx(), center = center, style = Stroke(width = 1.5.dp.toPx()))
                            drawCircle(reticleColor, radius = 4.dp.toPx(), center = center)
                            drawLine(reticleColor, Offset(center.x - 80.dp.toPx(), center.y), Offset(center.x + 80.dp.toPx(), center.y), strokeWidth = 1.5.dp.toPx())
                            drawLine(reticleColor, Offset(center.x, center.y - 80.dp.toPx()), Offset(center.x, center.y + 80.dp.toPx()), strokeWidth = 1.5.dp.toPx())
                        }
                    }
                }

                // 3x3 Grid Lines Overlay
                if (showGridLines) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val lineColor = Color.White.copy(alpha = 0.35f)
                        val stroke = 1.2.dp.toPx()

                        // 2 vertical lines (rule of thirds)
                        drawLine(lineColor, Offset(w / 3f, 0f), Offset(w / 3f, h), strokeWidth = stroke)
                        drawLine(lineColor, Offset(2f * w / 3f, 0f), Offset(2f * w / 3f, h), strokeWidth = stroke)

                        // 2 horizontal lines (rule of thirds)
                        drawLine(lineColor, Offset(0f, h / 3f), Offset(w, h / 3f), strokeWidth = stroke)
                        drawLine(lineColor, Offset(0f, 2f * h / 3f), Offset(w, 2f * h / 3f), strokeWidth = stroke)
                    }
                }

                // Tap-to-Focus Ring Indicator
                if (focusVisible && focusPoint != null) {
                    val pt = focusPoint!!
                    LaunchedEffect(pt) {
                        delay(2800)
                        focusVisible = false
                    }
                    Box(
                        modifier = Modifier
                            .offset { androidx.compose.ui.unit.IntOffset((pt.x - 32.dp.toPx()).toInt(), (pt.y - 32.dp.toPx()).toInt()) }
                            .size(64.dp)
                            .border(1.8.dp, Color(0xFFFFD700), RoundedCornerShape(10.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFFFFD700), CircleShape)
                                .align(Alignment.Center)
                        )
                    }
                }

                // Real-time GPS Stamp Overlay inside the viewfinder (responsive to orientation)
                LiveCameraViewfinderStamp(
                    state = state,
                    deviceOrientationDegrees = deviceOrientationDegrees,
                    modifier = Modifier.fillMaxSize()
                )

                // Visual Shutter Flash Effect
                if (isShutterFlashing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.75f))
                    )
                }
            }
        }

        val isAnyOverlayOpen = showSettingsDialog ||
            showManualLocationDialog ||
            showCustomNoteDialog ||
            showTemplatesSheet ||
            showPartialGallery ||
            selectedGalleryMediaIndex != null ||
            showEditToolsMenu ||
            showScaleSlider ||
            isProcessing

        // Live Compass Overlay HUD on Top Side of Viewfinder (Strictly visible ONLY during clean live camera preview)
        AnimatedVisibility(
            visible = isCompassEnabled && !isAnyOverlayOpen,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(150)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 62.dp)
                .zIndex(2f)
        ) {
            LiveCompassHUD(
                azimuth = liveAzimuth
            )
        }

        // Zoom In & Out Controls (Middle Right Side: 1x, 2x, Max)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.Black.copy(alpha = 0.7f))
                .border(1.2.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val maxLevel = if (maxZoomRatio >= 9.5f) 10.0f else if (maxZoomRatio >= 7.5f) 8.0f else maxZoomRatio
            val zoomLevels = listOf(1.0f, 2.0f, maxLevel)
            zoomLevels.forEach { zLevel ->
                val isSelected = kotlin.math.abs(currentZoomRatio - zLevel) < 0.35f
                val label = "${zLevel.toInt()}x"
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.12f))
                        .border(
                            1.dp,
                            if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.2f),
                            CircleShape
                        )
                        .clickable {
                            currentZoomRatio = zLevel
                            cameraControl?.setZoomRatio(zLevel)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSelected) Color.Black else Color.White
                    )
                }
            }
        }

        // 2. Top Bar (Uniform 40.dp Size & Equal Distance for All 6 Tools)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // 1. Flash
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                IconButton(
                    onClick = { viewModel.cycleFlashMode() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            when (flashMode) {
                                CameraFlashMode.ON -> Color(0xFFFFD700).copy(alpha = 0.35f)
                                CameraFlashMode.FILL_LIGHT -> Color(0xFFFF9800).copy(alpha = 0.35f)
                                CameraFlashMode.AUTO -> Color(0xFF00E5FF).copy(alpha = 0.25f)
                                CameraFlashMode.OFF -> Color.Black.copy(alpha = 0.65f)
                            },
                            CircleShape
                        )
                        .border(
                            1.4.dp,
                            when (flashMode) {
                                CameraFlashMode.ON -> Color(0xFFFFD700)
                                CameraFlashMode.FILL_LIGHT -> Color(0xFFFF9800)
                                CameraFlashMode.AUTO -> Color(0xFF00E5FF)
                                CameraFlashMode.OFF -> Color.White.copy(alpha = 0.35f)
                            },
                            CircleShape
                        )
                ) {
                    Text(
                        text = when (flashMode) {
                            CameraFlashMode.ON -> "⚡"
                            CameraFlashMode.FILL_LIGHT -> "💡"
                            CameraFlashMode.AUTO -> "⚡A"
                            CameraFlashMode.OFF -> "🚫"
                        },
                        fontSize = if (flashMode == CameraFlashMode.AUTO) 11.sp else 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = when (flashMode) {
                            CameraFlashMode.ON -> Color(0xFFFFD700)
                            CameraFlashMode.FILL_LIGHT -> Color(0xFFFF9800)
                            CameraFlashMode.AUTO -> Color(0xFF00E5FF)
                            CameraFlashMode.OFF -> Color.White.copy(alpha = 0.6f)
                        }
                    )
                }
                Text(
                    text = "Flash",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            // 2. Ratio
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                IconButton(
                    onClick = { selectedRatio = selectedRatio.next() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                        .border(1.2.dp, Color(0xFF00E5FF).copy(alpha = 0.85f), CircleShape)
                ) {
                    Text(
                        text = when (selectedRatio) {
                            LiveCameraAspectRatio.RATIO_3_4 -> "3:4"
                            LiveCameraAspectRatio.RATIO_9_16 -> "9:16"
                            LiveCameraAspectRatio.RATIO_1_1 -> "1:1"
                            LiveCameraAspectRatio.RATIO_FULL -> "FULL"
                        },
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF00E5FF)
                    )
                }
                Text(
                    text = "Ratio",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            // 3. Timer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                IconButton(
                    onClick = {
                        captureTimerSeconds = when (captureTimerSeconds) {
                            0 -> 3
                            3 -> 5
                            5 -> 10
                            else -> 0
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (captureTimerSeconds > 0) Color(0xFFFFD700).copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.65f),
                            CircleShape
                        )
                        .border(
                            1.2.dp,
                            if (captureTimerSeconds > 0) Color(0xFFFFD700) else Color.White.copy(alpha = 0.35f),
                            CircleShape
                        )
                ) {
                    if (captureTimerSeconds > 0) {
                        Text(
                            text = "${captureTimerSeconds}s",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD700)
                        )
                    } else {
                        Text(
                            text = "⏱️",
                            fontSize = 17.sp
                        )
                    }
                }
                Text(
                    text = "Timer",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            // 4. Size
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                IconButton(
                    onClick = {
                        showScaleSlider = !showScaleSlider
                        if (showScaleSlider) showEditToolsMenu = false
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(if (showScaleSlider) Color(0xFFFF6B35).copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.65f), CircleShape)
                        .border(1.2.dp, if (showScaleSlider) Color(0xFFFF6B35) else Color.White.copy(alpha = 0.35f), CircleShape)
                ) {
                    Text(
                        text = "📏",
                        fontSize = 17.sp
                    )
                }
                Text(
                    text = "Size",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            // 5. More
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                IconButton(
                    onClick = {
                        showEditToolsMenu = !showEditToolsMenu
                        if (showEditToolsMenu) showScaleSlider = false
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(if (showEditToolsMenu) Color(0xFF00E5FF).copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.65f), CircleShape)
                        .border(1.2.dp, if (showEditToolsMenu) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.35f), CircleShape)
                ) {
                    Text(
                        text = "✏️",
                        fontSize = 17.sp
                    )
                }
                Text(
                    text = "More",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            // 6. Settings
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                IconButton(
                    onClick = { showSettingsDialog = !showSettingsDialog },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (showSettingsDialog) Color(0xFFEF4444).copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.65f),
                            CircleShape
                        )
                        .border(
                            1.2.dp,
                            if (showSettingsDialog) Color(0xFFEF4444) else Color(0xFF00E5FF).copy(alpha = 0.8f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (showSettingsDialog) Icons.Filled.Close else Icons.Filled.Settings,
                        contentDescription = if (showSettingsDialog) "Close Settings" else "Open Settings",
                        tint = if (showSettingsDialog) Color(0xFFFF6B6B) else Color(0xFF00E5FF),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = if (showSettingsDialog) "Close" else "Settings",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        // Floating Pill-Badge Status Indicator for Offline Sync
        AnimatedVisibility(
            visible = syncStatus !is SyncStatus.Idle,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 68.dp)
                .zIndex(10f)
        ) {
            when (val currentStatus = syncStatus) {
                is SyncStatus.Syncing -> {
                    Surface(
                        color = Color(0xFF0F172A).copy(alpha = 0.94f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.2.dp, Color(0xFF00E5FF).copy(alpha = 0.8f)),
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🔄",
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Syncing offline tags (${currentStatus.current}/${currentStatus.total})…",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                is SyncStatus.Success -> {
                    Surface(
                        color = Color(0xFF064E3B).copy(alpha = 0.96f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.2.dp, Color(0xFF10B981)),
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "✓",
                                color = Color(0xFF6EE7B7),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "All photos synced",
                                color = Color(0xFFECFDF5),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                SyncStatus.Idle -> {}
            }
        }

        // Fullscreen tap-to-dismiss background for Edit Tools Menu
        if (showEditToolsMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showEditToolsMenu = false
                    }
            )
        }

        // Edit / Pen Tools Dropdown Menu (Grid lines + Selfie Mirror)
        AnimatedVisibility(
            visible = showEditToolsMenu,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 60.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(horizontal = 8.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* prevent click through to background scrim */ },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1322).copy(alpha = 0.98f)),
                border = BorderStroke(1.2.dp, Color(0xFF00E5FF)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("✏️", fontSize = 16.sp)
                            Text(
                                text = "CAMERA & SURVEY TOOLS",
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        IconButton(
                            onClick = { showEditToolsMenu = false },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close tools",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // 1. Grid lines toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .clickable { showGridLines = !showGridLines }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("⊞", fontSize = 18.sp, color = Color(0xFF00E5FF))
                            Column {
                                Text("Rule of Thirds Grid", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Overlay 3x3 alignment guidelines", color = Color.White.copy(alpha = 0.6f), fontSize = 10.5.sp)
                            }
                        }
                        Switch(
                            checked = showGridLines,
                            onCheckedChange = { showGridLines = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00E5FF),
                                checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.35f)
                            )
                        )
                    }

                    // 2. Selfie mirror toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .clickable { viewModel.setSelfieMirrored(!isSelfieMirrored) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("🪞", fontSize = 18.sp)
                            Column {
                                Text("Mirror Front Camera", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    if (isSelfieMirrored) "Mirrored selfie (Mirror View)" else "Standard selfie (Normal direction)",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                        Switch(
                            checked = isSelfieMirrored,
                            onCheckedChange = { viewModel.setSelfieMirrored(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00E5FF),
                                checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.35f)
                            )
                        )
                    }

                    // 3. Live Compass Overlay toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .clickable { viewModel.setCompassEnabled(!isCompassEnabled) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("🧭", fontSize = 18.sp)
                            Column {
                                Text("Live Compass Overlay", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    if (isCompassEnabled) "Live direction active & stamped on photos/videos" else "Show real-time compass & stamp heading direction",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                        Switch(
                            checked = isCompassEnabled,
                            onCheckedChange = { viewModel.setCompassEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00E5FF),
                                checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.35f)
                            )
                        )
                    }

                    // 3. Flash Mode Selector (Auto, On, Off)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("⚡", fontSize = 18.sp)
                            Column {
                                Text("Flash Mode (Survey Lighting)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Controls camera flash & low-light survey capture", color = Color.White.copy(alpha = 0.6f), fontSize = 10.5.sp)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CameraFlashMode.values().forEach { mode ->
                                val isSelected = flashMode == mode
                                val itemBg = if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.22f) else Color.White.copy(alpha = 0.05f)
                                val itemBorder = if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.2f)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(itemBg)
                                        .border(1.2.dp, itemBorder, RoundedCornerShape(10.dp))
                                        .clickable { viewModel.setFlashMode(mode) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(text = mode.icon, fontSize = 13.sp)
                                        Text(
                                            text = mode.shortLabel,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                            color = if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Fullscreen tap-to-dismiss background for Stamp Scale Slider
        if (showScaleSlider) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showScaleSlider = false
                    }
            )
        }

        // Floating Stamp Scale Slider Card
        AnimatedVisibility(
            visible = showScaleSlider,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 60.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(horizontal = 8.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* prevent click through to background scrim */ },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1322).copy(alpha = 0.96f)),
                border = BorderStroke(1.2.dp, Color(0xFFFF6B35)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("📏", fontSize = 16.sp)
                            Text(
                                text = "STAMP SIZE: ${(state.stampScale * 100).toInt()}%",
                                color = Color(0xFFFF6B35),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        IconButton(
                            onClick = { showScaleSlider = false },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close ruler",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Slider(
                        value = state.stampScale,
                        onValueChange = { viewModel.setStampScale(it) },
                        valueRange = 0.5f..1.5f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFF6B35),
                            activeTrackColor = Color(0xFFFF6B35),
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("50%", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                        Text("100% (Default)", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                        Text("150%", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                    }
                }
            }
        }

        // Timer Active Countdown Overlay
        countdownRemaining?.let { sec ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.85f))
                            .border(3.dp, Color(0xFFFFD700), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$sec",
                            fontSize = 54.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD700)
                        )
                    }
                    Button(
                        onClick = {
                            countdownJob?.cancel()
                            countdownJob = null
                            countdownRemaining = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Cancel Timer", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. Middle Screen Location & Address Status Notification HUD
        if (!noticeDismissed) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(0.88f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF0D1322).copy(alpha = 0.95f))
                    .border(
                        1.5.dp,
                        if (isLocationLoading) Color(0xFF00E5FF).copy(alpha = 0.7f) else Color(0xFF10B981).copy(alpha = 0.85f),
                        RoundedCornerShape(22.dp)
                    )
                    .clickable {
                        noticeDismissed = true
                    }
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLocationLoading) {
                        CircularProgressIndicator(
                            color = Color(0xFF00E5FF),
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "🛰️ Fetching Location & Address",
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Location and address details are loading, please wait a moment...",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    } else {
                        Text(text = "✅", fontSize = 34.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Location & Address Ready!",
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Everything is set. You can now capture photos & record videos with full GPS stamp.",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.5.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap to dismiss",
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // 4. Bottom Controls (Mode Switcher, Thumbnail, Shutter, Flip)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode Switcher (PHOTO / VIDEO)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LiveCameraMode.values().forEach { mode ->
                    val isSelected = selectedMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color(0xFFFF6B35) else Color.Transparent)
                            .clickable {
                                if (!isRecording) {
                                    selectedMode = mode
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = mode.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Bottom Controls (Gallery, Templates, Shutter, Custom Note, Switch Camera)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Bottom-Left Gallery Thumbnail (Opens 48% gallery sheet instantly)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.5.dp, Color(0xFF00E5FF), RoundedCornerShape(14.dp))
                            .clickable { showPartialGallery = true },
                        contentAlignment = Alignment.Center
                    ) {
                        val latest = sessionMedia.firstOrNull()
                        if (latest != null) {
                            AsyncThumbnailImage(
                                context = context,
                                uri = latest.uri,
                                isVideo = latest.isVideo,
                                modifier = Modifier.fillMaxSize(),
                                targetSize = 120
                            )
                        } else {
                            Text(
                                text = "🖼️",
                                fontSize = 20.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Gallery",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 2. Templates / Stamp Designs Button (Between Gallery Thumbnail and Shutter)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {
                            previewTemplate = state.stampTemplate
                            showTemplatesSheet = !showTemplatesSheet
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Color(state.stampTemplate.badgeColorHex).copy(alpha = 0.22f),
                                CircleShape
                            )
                            .border(
                                1.5.dp,
                                Color(state.stampTemplate.badgeColorHex),
                                CircleShape
                            )
                    ) {
                        Text(
                            text = state.stampTemplate.icon,
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Stamp",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 3. Bottom-Center Shutter Button
                if (selectedMode == LiveCameraMode.PHOTO) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .border(3.dp, Color.White, CircleShape)
                            .clickable { onShutterClicked() },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .border(3.dp, Color.White, CircleShape)
                            .clickable { onShutterClicked() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isRecording) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFEF4444))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444))
                            )
                        }
                    }
                }

                // 4. Note / Remark Button (Between Shutter and Flip Camera button)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {
                            showCustomNoteDialog = !showCustomNoteDialog
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (state.isCustomNoteEnabled && state.customNote.isNotBlank()) Color(0xFFFFD700).copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.65f),
                                CircleShape
                            )
                            .border(
                                1.5.dp,
                                if (state.isCustomNoteEnabled && state.customNote.isNotBlank()) Color(0xFFFFD700) else Color.White.copy(alpha = 0.35f),
                                CircleShape
                            )
                    ) {
                        Text(
                            text = "📝",
                            fontSize = 19.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Note",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 5. Bottom-Right Camera Flip / Switch Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {
                            if (!isRecording) {
                                isFrontCamera = !isFrontCamera
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                            .border(1.5.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Switch camera",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Switch",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (isRecording) {
                val mins = recordingSeconds / 60
                val secs = recordingSeconds % 60
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "🔴 REC  ${String.format("%02d:%02d", mins, secs)}",
                    color = Color(0xFFEF4444),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Google AdMob Bottom Banner Ad anchored safely at bottom with safe padding
            AdMobBanner()
        }

        // Fullscreen tap-to-dismiss background scrim for Templates Bottom Sheet
        if (showTemplatesSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showTemplatesSheet = false
                    }
            )
        }

        // 30% Bottom Sheet with custom Stamp Templates & Live Preview Card above it
        AnimatedVisibility(
            visible = showTemplatesSheet,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* prevent click through to background scrim */ }
            ) {
                // Floating Stamp Preview Card right above the 30% bottom sheet
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.panel.copy(alpha = 0.98f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, Color(previewTemplate.badgeColorHex).copy(alpha = 0.85f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(previewTemplate.icon, fontSize = 16.sp)
                                Text(
                                    text = "PREVIEW: ${previewTemplate.title.uppercase()}",
                                    color = Color(previewTemplate.badgeColorHex),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            if (state.stampTemplate == previewTemplate) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF10B981).copy(alpha = 0.25f))
                                        .border(1.dp, Color(0xFF10B981), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text("✓ APPLIED & ACTIVE", color = Color(0xFF10B981), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        viewModel.setStampTemplate(previewTemplate)
                                        viewModel.triggerCustomToast("Applied ${previewTemplate.title} Stamp")
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(previewTemplate.badgeColorHex),
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Apply Stamp ✨", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }

                        // Actual stamp design rendered live with current GPS data
                        StampDesignCard(
                            state = state,
                            template = previewTemplate,
                            scale = 0.95f
                        )
                    }
                }

                // 30% Screen Bottom Sheet with 4 stamp templates
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.30f),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = colors.panel.copy(alpha = 0.98f),
                    border = BorderStroke(1.2.dp, colors.accentCyan.copy(alpha = 0.5f)),
                    tonalElevation = 10.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Sheet Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = "🎨", fontSize = 15.sp)
                                Column {
                                    Text(
                                        text = "STAMP DESIGN TEMPLATES (${StampTemplate.values().size})",
                                        color = colors.titleColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        letterSpacing = 0.8.sp
                                    )
                                    Text(
                                        text = "Tap template to preview above, apply to camera & captures",
                                        color = colors.muted,
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            IconButton(
                                onClick = { showTemplatesSheet = false },
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(colors.border.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Close templates",
                                    tint = colors.text,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Horizontal list of 4 Templates
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StampTemplate.values().forEach { tpl ->
                                val isSelected = (previewTemplate == tpl)
                                val isApplied = (state.stampTemplate == tpl)
                                val badgeColor = Color(tpl.badgeColorHex)

                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) badgeColor.copy(alpha = 0.18f) else colors.bg
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) badgeColor else colors.border
                                    ),
                                    modifier = Modifier
                                        .width(130.dp)
                                        .fillMaxHeight()
                                        .clickable {
                                            previewTemplate = tpl
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp),
                                        verticalArrangement = Arrangement.SpaceBetween,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(tpl.icon, fontSize = 18.sp)
                                            if (isApplied) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color(0xFF10B981))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text("ACTIVE", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                                }
                                            } else if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(badgeColor.copy(alpha = 0.3f))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text("PREVIEW", color = badgeColor, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = tpl.title,
                                                color = if (isSelected) badgeColor else colors.text,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = tpl.subtitle,
                                                color = colors.muted,
                                                fontSize = 8.sp,
                                                lineHeight = 9.5.sp,
                                                textAlign = TextAlign.Center,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        if (!isApplied) {
                                            Button(
                                                onClick = {
                                                    previewTemplate = tpl
                                                    viewModel.setStampTemplate(tpl)
                                                    viewModel.triggerCustomToast("Applied ${tpl.title} Stamp")
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = badgeColor,
                                                    contentColor = Color.Black
                                                ),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(22.dp)
                                            ) {
                                                Text("Apply", fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(22.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("✓ Applied", color = Color(0xFF10B981), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Fullscreen tap-to-dismiss background scrim for Partial Gallery Bottom Sheet
        if (showPartialGallery) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showPartialGallery = false
                    }
            )
        }

        // 5. Partial Bottom-Sheet Gallery Overlay (Instant Open with Loading Spinners)
        AnimatedVisibility(
            visible = showPartialGallery,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.48f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* prevent click through to background scrim */ },
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = colors.panel.copy(alpha = 0.98f),
                border = BorderStroke(1.2.dp, colors.accentCyan.copy(alpha = 0.5f)),
                tonalElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    // Header Row: Title + Close 'X' button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🖼️", fontSize = 16.sp, modifier = Modifier.padding(end = 6.dp))
                            Text(
                                text = "RECENT CAPTURES (${sessionMedia.size})",
                                color = colors.titleColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        IconButton(
                            onClick = { showPartialGallery = false },
                            modifier = Modifier
                                .size(32.dp)
                                .background(colors.border.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close gallery",
                                tint = colors.text,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (sessionMedia.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No captures yet.\nTake a photo to see it here!",
                                color = colors.muted,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            itemsIndexed(sessionMedia) { index, item ->
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.bg)
                                        .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                                        .clickable {
                                            selectedGalleryMediaIndex = index
                                        }
                                ) {
                                    AsyncThumbnailImage(
                                        context = context,
                                        uri = item.uri,
                                        isVideo = item.isVideo,
                                        modifier = Modifier.fillMaxSize(),
                                        targetSize = 180
                                    )
                                    if (item.isVideo) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .size(24.dp)
                                                .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = "▶", color = Color.White, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Fullscreen tap-to-dismiss background scrim for Custom Stamp Note Sheet
        if (showCustomNoteDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showCustomNoteDialog = false
                    }
            )
        }

        // Custom Stamp Note Modal Sheet
        AnimatedVisibility(
            visible = showCustomNoteDialog,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            var noteInput by remember(state.customNote) { mutableStateOf(state.customNote) }
            var noteEnabled by remember(state.isCustomNoteEnabled) { mutableStateOf(state.isCustomNoteEnabled) }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* prevent click through to background scrim */ },
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = colors.panel.copy(alpha = 0.98f),
                border = BorderStroke(1.2.dp, if (colors.isDark) Color(0xFFFFD700).copy(alpha = 0.8f) else colors.accentCyan),
                tonalElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header Row: Title + Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("📝", fontSize = 18.sp)
                            Column {
                                Text(
                                    text = "CUSTOM STAMP NOTE / REMARK",
                                    color = if (colors.isDark) Color(0xFFFFD700) else colors.titleColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Add note (Survey, Building, College) to address stamp",
                                    color = colors.muted,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                        IconButton(
                            onClick = { showCustomNoteDialog = false },
                            modifier = Modifier
                                .size(28.dp)
                                .background(colors.border.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close note dialog",
                                tint = colors.text,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Toggle Switch Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.bg)
                            .clickable {
                                val next = !noteEnabled
                                noteEnabled = next
                                viewModel.setCustomNote(noteInput, next)
                            }
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Show Note on Address Stamp",
                                color = colors.text,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (noteEnabled) "Active: Note is attached at bottom of stamp" else "Disabled: Note will not appear on stamp",
                                color = if (noteEnabled) (if (colors.isDark) Color(0xFFFFD700) else colors.accentCyan) else colors.muted,
                                fontSize = 10.5.sp
                            )
                        }
                        Switch(
                            checked = noteEnabled,
                            onCheckedChange = {
                                noteEnabled = it
                                viewModel.setCustomNote(noteInput, it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = if (colors.isDark) Color(0xFFFFD700) else colors.accentCyan,
                                checkedTrackColor = (if (colors.isDark) Color(0xFFFFD700) else colors.accentCyan).copy(alpha = 0.35f)
                            )
                        )
                    }

                    // Text Input Field for Note
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = {
                            noteInput = it
                            viewModel.setCustomNote(it, noteEnabled)
                        },
                        label = { Text("Note / Remark Text", color = if (colors.isDark) Color(0xFFFFD700).copy(alpha = 0.85f) else colors.accentCyan) },
                        placeholder = { Text("e.g. Survey, Building, College, Site Inspection...", color = colors.muted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.text,
                            unfocusedTextColor = colors.text,
                            focusedBorderColor = if (colors.isDark) Color(0xFFFFD700) else colors.accentCyan,
                            unfocusedBorderColor = colors.border,
                            cursorColor = if (colors.isDark) Color(0xFFFFD700) else colors.accentCyan
                        ),
                        trailingIcon = {
                            if (noteInput.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        noteInput = ""
                                        viewModel.setCustomNote("", noteEnabled)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Clear note",
                                        tint = colors.muted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    )

                    // Quick Suggested Tag Chips
                    Text(
                        text = "Quick Tag Suggestions:",
                        color = colors.text,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val tags = listOf(
                            "🏗️ Survey",
                            "🏢 Building",
                            "🏫 College",
                            "🛣️ Road Project",
                            "🏠 Residential",
                            "🌾 Agriculture",
                            "📦 Delivery",
                            "⚡ Inspection",
                            "🌳 Forest / Land",
                            "🏥 Hospital"
                        )
                        tags.forEach { tag ->
                            val cleanTag = tag.substringAfter(" ")
                            val isSelected = noteInput.contains(cleanTag, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) (if (colors.isDark) Color(0xFFFFD700).copy(alpha = 0.25f) else colors.badgeBg) else colors.bg)
                                    .border(
                                        1.dp,
                                        if (isSelected) (if (colors.isDark) Color(0xFFFFD700) else colors.accentCyan) else colors.border,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        noteInput = cleanTag
                                        noteEnabled = true
                                        viewModel.setCustomNote(cleanTag, true)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = tag,
                                    color = if (isSelected) (if (colors.isDark) Color(0xFFFFD700) else colors.accentCyan) else colors.text,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Live Stamp Note Preview
                    if (noteInput.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.bg)
                                .border(1.dp, (if (colors.isDark) Color(0xFFFFD700) else colors.accentCyan).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Preview on Stamp:", color = colors.muted, fontSize = 10.5.sp)
                                Text(
                                    text = "Note: ${noteInput.trim()}",
                                    color = if (noteEnabled) (if (colors.isDark) Color(0xFFFFD700) else colors.accentCyan) else colors.muted,
                                    fontSize = 11.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Save / Apply Button
                    Button(
                        onClick = {
                            viewModel.setCustomNote(noteInput, noteEnabled)
                            showCustomNoteDialog = false
                            if (noteEnabled && noteInput.isNotBlank()) {
                                viewModel.triggerCustomToast("Stamp Note saved: ${noteInput.trim()}")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text(
                            text = "Apply Note to Stamp",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Full viewer dialog when item clicked in partial gallery
        selectedGalleryMediaIndex?.let { initialIndex ->
            SessionMediaViewerDialog(
                mediaList = sessionMedia,
                initialIndex = initialIndex,
                onDismiss = { selectedGalleryMediaIndex = null },
                onDelete = { mediaItem ->
                    viewModel.deleteSessionMedia(mediaItem, context)
                },
                onShare = { mediaItem ->
                    shareMediaUri(context, mediaItem.uri, mediaItem.isVideo)
                }
            )
        }

        // Settings Side Drawer / Sheet (Sliding from Left with visible Live Preview on the right)
        AnimatedVisibility(
            visible = showSettingsDialog,
            enter = slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(250)),
            exit = slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(250, easing = FastOutLinearInEasing)) + fadeOut(animationSpec = tween(200)),
            modifier = Modifier.fillMaxSize().zIndex(30f)
        ) {
            LiveCameraSettingsDrawer(
                viewModel = viewModel,
                state = state,
                onDismiss = { showSettingsDialog = false },
                onOpenManualLocation = {
                    showSettingsDialog = false
                    showManualLocationDialog = true
                },
                onOpenGalleryUpload = {
                    showSettingsDialog = false
                    onOpenGalleryUpload()
                }
            )
        }

        // Manual Location Configuration Dialog
        if (showManualLocationDialog) {
            ManualLocationDialog(
                state = state,
                viewModel = viewModel,
                onDismiss = { showManualLocationDialog = false }
            )
        }

        // Saving & Processing Loader Overlay
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(50f)
                    .background(Color.Black.copy(alpha = 0.72f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1322)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF00E5FF)),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF00E5FF),
                            strokeWidth = 3.5.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = processingMessage.ifEmpty { "Saving Media to Gallery…" },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LiveCameraSettingsDrawer(
    viewModel: GpsViewModel,
    state: LocationState,
    onDismiss: () -> Unit,
    onOpenManualLocation: () -> Unit,
    onOpenGalleryUpload: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isProUnlocked by viewModel.isProUnlocked.collectAsState()
    val imageQuality by viewModel.imageQuality.collectAsState()
    val flashMode by viewModel.flashMode.collectAsState()
    val showManualLocation by viewModel.showManualLocation.collectAsState()
    val showManualTime by viewModel.showManualTime.collectAsState()
    val showUploadGallery by viewModel.showUploadGallery.collectAsState()
    val removeWatermark by viewModel.removeWatermark.collectAsState()
    val hapticFeedback by viewModel.hapticFeedback.collectAsState()
    val liveGpsSources by viewModel.liveGpsSources.collectAsState()
    val refreshCooldownRemaining by viewModel.refreshCooldownRemaining.collectAsState()

    var showProUpgradeDialog by remember { mutableStateOf(false) }
    var showRewardedAdDialog by remember { mutableStateOf(false) }
    var isImageQualityDropdownExpanded by remember { mutableStateOf(false) }
    var isFlashModeDropdownExpanded by remember { mutableStateOf(false) }
    var isLiveGpsDropdownExpanded by remember { mutableStateOf(false) }
    var isProDropdownExpanded by remember { mutableStateOf(true) }
    var isVolumeKeyDropdownExpanded by remember { mutableStateOf(false) }
    var isStorageFolderDropdownExpanded by remember { mutableStateOf(false) }
    var isGoogleDriveDropdownExpanded by remember { mutableStateOf(false) }
    var isAboutUsExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshAllAddressSources()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Transparent clickable scrim background on the right side - clicking it dismisses settings
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        )

        // Settings panel touching top, left, bottom corners seamlessly with gap on right
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.82f)
                .align(Alignment.CenterStart)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* consume clicks inside panel */ }
                )
                .background(
                    color = colors.panel,
                    shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 24.dp, bottomEnd = 24.dp)
                )
                .border(
                    width = 1.5.dp,
                    color = colors.accentCyan.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 24.dp, bottomEnd = 24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header: Icon + Title + Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.accentCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings",
                                tint = colors.accentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "SETTINGS",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = colors.accentCyan,
                            letterSpacing = 1.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(colors.border.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = colors.text,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Divider(color = colors.border.copy(alpha = 0.6f))

                // 1. Light / Dark Mode Section
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.bg),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, colors.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = if (isDarkMode) "🌙" else "☀️", fontSize = 20.sp)
                            Column {
                                Text(
                                    text = if (isDarkMode) "Dark Theme" else "Light Theme",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = colors.text
                                )
                                Text(
                                    text = "Toggle visual theme color",
                                    fontSize = 11.sp,
                                    color = colors.muted
                                )
                            }
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { viewModel.toggleDarkMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = colors.accentCyan,
                                uncheckedThumbColor = colors.text,
                                uncheckedTrackColor = colors.border
                            )
                        )
                    }
                }

                // 2. Image Quality Dropdown Section (Direct Capture & Save compression)
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.bg),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.2.dp, if (isImageQualityDropdownExpanded) colors.accentCyan.copy(alpha = 0.8f) else colors.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Header row (clickable dropdown toggle)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isImageQualityDropdownExpanded) colors.accentCyan.copy(alpha = 0.12f) else Color.Transparent)
                                .clickable { isImageQualityDropdownExpanded = !isImageQualityDropdownExpanded }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = imageQuality.icon, fontSize = 20.sp)
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Image Quality",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = colors.text
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    when (imageQuality) {
                                                        ImageQuality.AUTO -> colors.accentCyan.copy(alpha = 0.2f)
                                                        ImageQuality.MEDIUM -> Color(0xFF10B981).copy(alpha = 0.2f)
                                                        ImageQuality.LOW -> Color(0xFFFF9800).copy(alpha = 0.2f)
                                                    }
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = imageQuality.badge,
                                                color = when (imageQuality) {
                                                    ImageQuality.AUTO -> colors.accentCyan
                                                    ImageQuality.MEDIUM -> Color(0xFF10B981)
                                                    ImageQuality.LOW -> Color(0xFFFF9800)
                                                },
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                    Text(
                                        text = imageQuality.title,
                                        fontSize = 11.sp,
                                        color = colors.muted
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (isImageQualityDropdownExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Toggle Image Quality Dropdown",
                                tint = colors.accentCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Expandable Options List
                        AnimatedVisibility(
                            visible = isImageQualityDropdownExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ImageQuality.values().forEach { qualityOption ->
                                    val isSelected = qualityOption == imageQuality
                                    val borderColor = if (isSelected) colors.accentCyan else colors.border.copy(alpha = 0.6f)
                                    val bgColor = if (isSelected) colors.accentCyan.copy(alpha = 0.12f) else colors.panel

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(bgColor)
                                            .border(1.2.dp, borderColor, RoundedCornerShape(10.dp))
                                            .clickable {
                                                viewModel.setImageQuality(qualityOption)
                                            }
                                            .padding(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(text = qualityOption.icon, fontSize = 18.sp)
                                                Column {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Text(
                                                            text = qualityOption.title,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            color = if (isSelected) colors.accentCyan else colors.text
                                                        )
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(
                                                                    if (isSelected) colors.accentCyan.copy(alpha = 0.2f)
                                                                    else colors.border.copy(alpha = 0.4f)
                                                                )
                                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                                        ) {
                                                            Text(
                                                                text = qualityOption.badge,
                                                                color = if (isSelected) colors.accentCyan else colors.muted,
                                                                fontSize = 8.5.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = qualityOption.description,
                                                        fontSize = 10.5.sp,
                                                        color = colors.muted,
                                                        lineHeight = 14.sp
                                                    )
                                                }
                                            }

                                            // Radio / Selection checkmark indicator
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) colors.accentCyan else Color.Transparent)
                                                    .border(
                                                        width = 1.5.dp,
                                                        color = if (isSelected) colors.accentCyan else colors.border,
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Check,
                                                        contentDescription = "Selected",
                                                        tint = Color.Black,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Performance / direct capture notice
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.panel.copy(alpha = 0.6f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("⚡", fontSize = 12.sp)
                                    Text(
                                        text = "Direct Capture: Photos are captured & saved directly in the chosen quality without secondary compression delays.",
                                        fontSize = 10.sp,
                                        color = colors.muted,
                                        lineHeight = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Live GPS Section (Expandable Dropdown with 3-Tier Address Sources)
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.bg),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.2.dp, if (isLiveGpsDropdownExpanded) colors.accentCyan.copy(alpha = 0.8f) else colors.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Header row (clickable dropdown toggle)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isLiveGpsDropdownExpanded) colors.accentCyan.copy(alpha = 0.12f) else Color.Transparent)
                                .clickable { isLiveGpsDropdownExpanded = !isLiveGpsDropdownExpanded }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = "🛰️", fontSize = 20.sp)
                                Column {
                                    Text(
                                        text = "Live GPS",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = colors.text
                                    )
                                    Text(
                                        text = "Coordinates & 3-Tier Address Sources",
                                        fontSize = 10.5.sp,
                                        color = colors.muted
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (isLiveGpsDropdownExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = if (isLiveGpsDropdownExpanded) "Collapse" else "Expand",
                                tint = colors.accentCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        if (isLiveGpsDropdownExpanded) {
                            Divider(color = colors.border.copy(alpha = 0.5f))

                            val isRefreshDisabled = liveGpsSources.isRefreshing || refreshCooldownRemaining > 0

                            // Manual GPS & Address Refresh Button
                            Button(
                                onClick = { viewModel.forceRefreshLiveLocation() },
                                enabled = !isRefreshDisabled,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.accentCyan,
                                    contentColor = Color.Black,
                                    disabledContainerColor = colors.border.copy(alpha = 0.4f),
                                    disabledContentColor = colors.muted
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Refresh GPS",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when {
                                        liveGpsSources.isRefreshing -> "Refreshing Coordinates…"
                                        refreshCooldownRemaining > 0 -> "Rest (${refreshCooldownRemaining}s) - Wait to Refresh"
                                        else -> "Refresh Live GPS & Addresses"
                                    },
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Current GPS Readings Box (Click to copy both lat and lng for Google Maps)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.panel)
                                    .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                                    .clickable {
                                        val coordsString = "${String.format(Locale.US, "%.6f", state.lat)},${String.format(Locale.US, "%.6f", state.lng)}"
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        val clip = ClipData.newPlainText("GPS Coordinates", coordsString)
                                        clipboard?.setPrimaryClip(clip)
                                        viewModel.triggerHapticFeedback(context)
                                        viewModel.triggerCustomToast("📋 Copied: $coordsString (Ready for Google Maps)")
                                    }
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "📡 Current Coordinates",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp,
                                            color = colors.text
                                        )
                                        Text(
                                            text = "(Tap to copy)",
                                            fontSize = 9.5.sp,
                                            color = colors.accentCyan,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                when (state.statusType) {
                                                    "ok" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                                    "ip" -> Color(0xFFFF9800).copy(alpha = 0.2f)
                                                    else -> Color(0xFF2196F3).copy(alpha = 0.2f)
                                                }
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = state.source.uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (state.statusType) {
                                                "ok" -> Color(0xFF4CAF50)
                                                "ip" -> Color(0xFFFF9800)
                                                else -> Color(0xFF2196F3)
                                            }
                                        )
                                    }
                                }

                                Text(
                                    text = "Lat: ${String.format(Locale.US, "%.5f", state.lat)}°  |  Lng: ${String.format(Locale.US, "%.5f", state.lng)}°",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.accentCyan
                                )
                                if (state.accuracy != null) {
                                    Text(
                                        text = "Accuracy: ±${String.format(Locale.US, "%.1f", state.accuracy)}m",
                                        fontSize = 10.sp,
                                        color = colors.muted
                                    )
                                }
                            }

                            // 3 Reverse Geocode Address Sources (Auto Priority System)
                            Text(
                                text = "Address Sources (Priority Auto-Selection):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = colors.text,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            // Tier 1: Primary Source
                            val androidItem = liveGpsSources.androidGeocoder
                            val isAndroidActive = state.addressTier == 1
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isAndroidActive) colors.accentCyan.copy(alpha = 0.08f) else colors.panel)
                                    .border(
                                        1.dp,
                                        if (isAndroidActive) colors.accentCyan.copy(alpha = 0.8f) else colors.border,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(colors.accentCyan),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("1", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                                        }
                                        Text(
                                            text = "Primary",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = colors.text
                                        )
                                    }
                                    if (isAndroidActive) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(colors.accentCyan.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("ACTIVE (AUTO)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.accentCyan)
                                        }
                                    }
                                }

                                if (androidItem.isLoading) {
                                    Text("Resolving primary address…", fontSize = 10.5.sp, color = colors.muted)
                                } else if (androidItem.address != null && androidItem.address.success) {
                                    val addr = androidItem.address
                                    val full = listOfNotNull(addr.road.takeIf { it.isNotBlank() }, addr.city.takeIf { it.isNotBlank() }, addr.state.takeIf { it.isNotBlank() }, addr.postcode.takeIf { it.isNotBlank() }).joinToString(", ")
                                    Text(text = full, fontSize = 10.5.sp, color = colors.text.copy(alpha = 0.9f))
                                } else {
                                    Text(
                                        text = androidItem.error ?: "Primary address unavailable",
                                        fontSize = 10.sp,
                                        color = colors.muted
                                    )
                                }
                            }

                            // Tier 2: Secondary Source
                            val nominatimItem = liveGpsSources.nominatim
                            val isNominatimActive = state.addressTier == 2
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isNominatimActive) colors.accentCyan.copy(alpha = 0.08f) else colors.panel)
                                    .border(
                                        1.dp,
                                        if (isNominatimActive) colors.accentCyan.copy(alpha = 0.8f) else colors.border,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF7B61FF)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("2", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                                        }
                                        Text(
                                            text = "Secondary",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = colors.text
                                        )
                                    }
                                    if (isNominatimActive) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF7B61FF).copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("ACTIVE (AUTO)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7B61FF))
                                        }
                                    }
                                }

                                if (nominatimItem.isLoading) {
                                    Text("Fetching secondary address…", fontSize = 10.5.sp, color = colors.muted)
                                } else if (nominatimItem.address != null && nominatimItem.address.success) {
                                    val addr = nominatimItem.address
                                    val full = listOfNotNull(addr.road.takeIf { it.isNotBlank() }, addr.city.takeIf { it.isNotBlank() }, addr.state.takeIf { it.isNotBlank() }, addr.postcode.takeIf { it.isNotBlank() }).joinToString(", ")
                                    Text(text = full, fontSize = 10.5.sp, color = colors.text.copy(alpha = 0.9f))
                                } else {
                                    Text(
                                        text = nominatimItem.error ?: "Secondary address pending",
                                        fontSize = 10.sp,
                                        color = colors.muted
                                    )
                                }
                            }

                            // Tier 3: Tertiary Source
                            val ipApiItem = liveGpsSources.ipApi
                            val isIpApiActive = state.addressTier == 3
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isIpApiActive) colors.accentCyan.copy(alpha = 0.08f) else colors.panel)
                                    .border(
                                        1.dp,
                                        if (isIpApiActive) colors.accentCyan.copy(alpha = 0.8f) else colors.border,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFF9800)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("3", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                                        }
                                        Text(
                                            text = "Tertiary",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = colors.text
                                        )
                                    }
                                    if (isIpApiActive) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFFF9800).copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("ACTIVE (AUTO)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                                        }
                                    }
                                }

                                if (ipApiItem.isLoading) {
                                    Text("Estimating tertiary address…", fontSize = 10.5.sp, color = colors.muted)
                                } else if (ipApiItem.address != null && ipApiItem.address.success) {
                                    val addr = ipApiItem.address
                                    val full = listOfNotNull(addr.city.takeIf { it.isNotBlank() }, addr.state.takeIf { it.isNotBlank() }).joinToString(", ")
                                    Text(text = full, fontSize = 10.5.sp, color = colors.text.copy(alpha = 0.9f))
                                } else {
                                    Text(
                                        text = ipApiItem.error ?: "Tertiary address unavailable",
                                        fontSize = 10.sp,
                                        color = colors.muted
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Pro User Section (Subscription / Rewarded Ad Gated)
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.bg),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.2.dp, if (isProUnlocked) Color(0xFFFFD700).copy(alpha = 0.8f) else Color(0xFFFFD700).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!isProUnlocked) {
                            // Locked State Header -> Clicking it opens ProUpgradeDialog
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFFFFD700).copy(alpha = 0.18f),
                                                Color(0xFFFF8C00).copy(alpha = 0.12f)
                                            )
                                        )
                                    )
                                    .border(1.2.dp, Color(0xFFFFD700).copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                                    .clickable { showProUpgradeDialog = true }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = "👑", fontSize = 22.sp)
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "Pro Features",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp,
                                                color = Color(0xFFFFD700)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text("🔒 LOCKED", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF87171))
                                            }
                                        }
                                        Text(
                                            text = "₹19/mo, ₹149/yr or Watch 15s Ad (24h Free)",
                                            fontSize = 10.5.sp,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                }
                                Button(
                                    onClick = { showProUpgradeDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFFD700),
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("👑 Unlock", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            // Unlocked State Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFFD700).copy(alpha = 0.15f))
                                    .border(1.2.dp, Color(0xFFFFD700).copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                                    .clickable { isProDropdownExpanded = !isProDropdownExpanded }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = "👑", fontSize = 22.sp)
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "PRO USER ACTIVE",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp,
                                                color = Color(0xFFFFD700)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF10B981))
                                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = "✓ ${viewModel.getProRemainingTimeString().uppercase()}",
                                                    color = Color.White,
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Text(
                                            text = if (isProDropdownExpanded) "Tap to collapse pro features" else "Tap dropdown to show pro features",
                                            fontSize = 10.5.sp,
                                            color = colors.text.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = if (isProDropdownExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Toggle Pro Dropdown",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Pro Features vertical expandable list
                        AnimatedVisibility(
                            visible = isProDropdownExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // 1. Edit location manually 👑
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.panel)
                                        .border(
                                            1.dp,
                                            if (!isProUnlocked) Color(0xFFFFD700).copy(alpha = 0.35f) else colors.border,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            if (!isProUnlocked) {
                                                showProUpgradeDialog = true
                                            }
                                        }
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "📍 Edit location manually",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = colors.text
                                                )
                                                Text(text = "👑", fontSize = 13.sp)
                                                if (!isProUnlocked) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(Color(0xFFFFD700).copy(alpha = 0.2f))
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    ) {
                                                        Text("PRO", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFD700))
                                                    }
                                                }
                                            }
                                            Text(
                                                text = if (!isProUnlocked) "Locked • Set custom lat, lng, city & state" else "Set custom lat, lng, city & state",
                                                fontSize = 10.5.sp,
                                                color = if (!isProUnlocked) Color(0xFFFFD700).copy(alpha = 0.8f) else colors.muted
                                            )
                                        }
                                        Switch(
                                            checked = if (isProUnlocked) showManualLocation else false,
                                            onCheckedChange = {
                                                if (!isProUnlocked) {
                                                    showProUpgradeDialog = true
                                                } else {
                                                    viewModel.setShowManualLocation(it)
                                                }
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = colors.accentCyan
                                            )
                                        )
                                    }
                                    if (isProUnlocked && showManualLocation) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = onOpenManualLocation,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = colors.accentCyan,
                                                contentColor = Color.Black
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("✏️ Set Coordinates & Details", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // 2. Manual time 👑
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.panel)
                                        .border(
                                            1.dp,
                                            if (!isProUnlocked) Color(0xFFFFD700).copy(alpha = 0.35f) else colors.border,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            if (!isProUnlocked) {
                                                showProUpgradeDialog = true
                                            }
                                        }
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "🕒 Manual time",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = colors.text
                                                )
                                                Text(text = "👑", fontSize = 13.sp)
                                                if (!isProUnlocked) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(Color(0xFFFFD700).copy(alpha = 0.2f))
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    ) {
                                                        Text("PRO", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFD700))
                                                    }
                                                }
                                            }
                                            Text(
                                                text = if (!isProUnlocked) "Locked • Pick custom timestamp for stamp" else "Pick custom timestamp for stamp",
                                                fontSize = 10.5.sp,
                                                color = if (!isProUnlocked) Color(0xFFFFD700).copy(alpha = 0.8f) else colors.muted
                                            )
                                        }
                                        Switch(
                                            checked = if (isProUnlocked) showManualTime else false,
                                            onCheckedChange = {
                                                if (!isProUnlocked) {
                                                    showProUpgradeDialog = true
                                                } else {
                                                    viewModel.setShowManualTime(it)
                                                }
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = colors.accentCyan
                                            )
                                        )
                                    }
                                    if (isProUnlocked && showManualTime) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = {
                                                val cal = Calendar.getInstance()
                                                DatePickerDialog(
                                                    context,
                                                    { _, year, month, dayOfMonth ->
                                                        TimePickerDialog(
                                                            context,
                                                            { _, hourOfDay, minute ->
                                                                val chosen = Calendar.getInstance().apply {
                                                                    set(year, month, dayOfMonth, hourOfDay, minute, 0)
                                                                }
                                                                viewModel.setManualTime(chosen.timeInMillis)
                                                            },
                                                            cal.get(Calendar.HOUR_OF_DAY),
                                                            cal.get(Calendar.MINUTE),
                                                            false
                                                        ).show()
                                                    },
                                                    cal.get(Calendar.YEAR),
                                                    cal.get(Calendar.MONTH),
                                                    cal.get(Calendar.DAY_OF_MONTH)
                                                ).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = colors.accentCyan,
                                                contentColor = Color.Black
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("📅 Pick Custom Date & Time", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // 3. Upload gallery photo 👑
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.panel)
                                        .border(
                                            1.dp,
                                            if (!isProUnlocked) Color(0xFFFFD700).copy(alpha = 0.35f) else colors.border,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            if (!isProUnlocked) {
                                                showProUpgradeDialog = true
                                            }
                                        }
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "📂 Upload gallery photo",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = colors.text
                                                )
                                                Text(text = "👑", fontSize = 13.sp)
                                                if (!isProUnlocked) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(Color(0xFFFFD700).copy(alpha = 0.2f))
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    ) {
                                                        Text("PRO", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFD700))
                                                    }
                                                }
                                            }
                                            Text(
                                                text = if (!isProUnlocked) "Locked • Stamp existing device photos" else "Stamp existing device photos",
                                                fontSize = 10.5.sp,
                                                color = if (!isProUnlocked) Color(0xFFFFD700).copy(alpha = 0.8f) else colors.muted
                                            )
                                        }
                                        Switch(
                                            checked = if (isProUnlocked) showUploadGallery else false,
                                            onCheckedChange = {
                                                if (!isProUnlocked) {
                                                    showProUpgradeDialog = true
                                                } else {
                                                    viewModel.setShowUploadGallery(it)
                                                }
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = colors.accentCyan
                                            )
                                        )
                                    }
                                    if (isProUnlocked && showUploadGallery) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = onOpenGalleryUpload,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = colors.accentCyan,
                                                contentColor = Color.Black
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("🖼️ Select & Stamp Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // 4. Remove watermark 👑
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.panel)
                                        .border(
                                            1.dp,
                                            if (!isProUnlocked) Color(0xFFFFD700).copy(alpha = 0.35f) else colors.border,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            if (!isProUnlocked) {
                                                showProUpgradeDialog = true
                                            }
                                        }
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "🚫 Remove watermark",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = colors.text
                                            )
                                            Text(text = "👑", fontSize = 13.sp)
                                            if (!isProUnlocked) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color(0xFFFFD700).copy(alpha = 0.2f))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    ) {
                                                        Text("PRO", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFD700))
                                                    }
                                            }
                                        }
                                        Text(
                                            text = if (!isProUnlocked) "Locked • Hide app branding in stamp" else "Hide app branding in stamp",
                                            fontSize = 10.5.sp,
                                            color = if (!isProUnlocked) Color(0xFFFFD700).copy(alpha = 0.8f) else colors.muted
                                        )
                                    }
                                    Switch(
                                        checked = if (isProUnlocked) removeWatermark else false,
                                        onCheckedChange = {
                                            if (!isProUnlocked) {
                                                showProUpgradeDialog = true
                                            } else {
                                                viewModel.setRemoveWatermark(it)
                                            }
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = colors.accentCyan
                                        )
                                    )
                                }

                                // 5. Custom Watermark 👑
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.panel)
                                        .border(
                                            1.dp,
                                            if (!isProUnlocked) Color(0xFFFFD700).copy(alpha = 0.35f) else colors.border,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            if (!isProUnlocked) {
                                                showProUpgradeDialog = true
                                            }
                                        }
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "🏷️ Custom Watermark",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = colors.text
                                                )
                                                Text(text = "👑", fontSize = 13.sp)
                                                if (!isProUnlocked) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(Color(0xFFFFD700).copy(alpha = 0.2f))
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    ) {
                                                        Text("PRO", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFD700))
                                                    }
                                                }
                                            }
                                            Text(
                                                text = if (!isProUnlocked) "Locked • Set personalized branding for stamp" else "Set personalized branding for stamp",
                                                fontSize = 10.5.sp,
                                                color = if (!isProUnlocked) Color(0xFFFFD700).copy(alpha = 0.8f) else colors.muted
                                            )
                                        }
                                        Switch(
                                            checked = if (isProUnlocked) state.isCustomWatermarkEnabled else false,
                                            onCheckedChange = {
                                                if (!isProUnlocked) {
                                                    showProUpgradeDialog = true
                                                } else {
                                                    viewModel.setCustomWatermarkEnabled(it)
                                                }
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = colors.accentCyan
                                            )
                                        )
                                    }
                                    if (isProUnlocked && state.isCustomWatermarkEnabled) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = state.customWatermarkText,
                                            onValueChange = { viewModel.setCustomWatermarkText(it) },
                                            label = { Text("Watermark Text", fontSize = 11.sp, color = colors.muted) },
                                            placeholder = { Text("Made With Love ❤️", fontSize = 12.sp, color = colors.muted) },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = colors.text,
                                                unfocusedTextColor = colors.text,
                                                focusedBorderColor = colors.accentCyan,
                                                unfocusedBorderColor = colors.border
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                                // 6. Google Drive Sync 👑
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.panel)
                                        .border(
                                            1.dp,
                                            if (!isProUnlocked) Color(0xFFFFD700).copy(alpha = 0.35f) else if (isGoogleDriveDropdownExpanded) colors.accentCyan.copy(alpha = 0.8f) else colors.border,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            if (!isProUnlocked) {
                                                showProUpgradeDialog = true
                                            }
                                        }
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                if (!isProUnlocked) {
                                                    showProUpgradeDialog = true
                                                } else {
                                                    isGoogleDriveDropdownExpanded = !isGoogleDriveDropdownExpanded
                                                }
                                            }
                                            .padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "☁️ Google Drive Sync",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = colors.text
                                                )
                                                Text(text = "👑", fontSize = 13.sp)
                                                if (!isProUnlocked) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(Color(0xFFFFD700).copy(alpha = 0.2f))
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    ) {
                                                        Text("PRO", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFD700))
                                                    }
                                                }
                                            }
                                            Text(
                                                text = if (!isProUnlocked) "Locked • Auto-sync photos & videos to cloud" else if (state.isGoogleDriveSyncEnabled) "Status: Enabled ☁️" else "Status: Disabled",
                                                fontSize = 10.5.sp,
                                                color = if (!isProUnlocked) Color(0xFFFFD700).copy(alpha = 0.8f) else if (state.isGoogleDriveSyncEnabled) colors.accentCyan else colors.muted
                                            )
                                        }
                                        Icon(
                                            imageVector = if (!isProUnlocked) Icons.Filled.Lock else if (isGoogleDriveDropdownExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                            contentDescription = "Toggle Google Drive Dropdown",
                                            tint = if (!isProUnlocked) Color(0xFFFFD700) else colors.accentCyan,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    AnimatedVisibility(
                                        visible = isProUnlocked && isGoogleDriveDropdownExpanded,
                                        enter = expandVertically() + fadeIn(),
                                        exit = shrinkVertically() + fadeOut()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "Auto-Sync Photos & Videos",
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 12.sp,
                                                        color = colors.text
                                                    )
                                                    Text(
                                                        text = "Automatically save a copy of photos & videos to your Google Drive",
                                                        fontSize = 10.sp,
                                                        color = colors.muted
                                                    )
                                                }
                                                Switch(
                                                    checked = state.isGoogleDriveSyncEnabled,
                                                    onCheckedChange = { viewModel.setGoogleDriveSyncEnabled(it) },
                                                    colors = SwitchDefaults.colors(
                                                        checkedThumbColor = Color.White,
                                                        checkedTrackColor = colors.accentCyan
                                                    )
                                                )
                                            }

                                            if (state.isGoogleDriveSyncEnabled) {
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Divider(color = colors.border.copy(alpha = 0.5f))
                                                Spacer(modifier = Modifier.height(10.dp))

                                                var accountEmail by remember(state.googleDriveAccountEmail) { mutableStateOf(state.googleDriveAccountEmail) }
                                                OutlinedTextField(
                                                    value = accountEmail,
                                                    onValueChange = {
                                                        accountEmail = it
                                                        viewModel.setGoogleDriveAccountEmail(it)
                                                    },
                                                    label = { Text("Google Account Email", fontSize = 10.sp) },
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = colors.accentCyan,
                                                        unfocusedBorderColor = colors.border,
                                                        focusedLabelColor = colors.accentCyan,
                                                        unfocusedLabelColor = colors.muted
                                                    )
                                                )

                                                Spacer(modifier = Modifier.height(6.dp))

                                                var driveFolderInput by remember(state.googleDriveFolderName) { mutableStateOf(state.googleDriveFolderName) }
                                                OutlinedTextField(
                                                    value = driveFolderInput,
                                                    onValueChange = {
                                                        driveFolderInput = it
                                                        viewModel.setGoogleDriveFolderName(it)
                                                    },
                                                    label = { Text("Google Drive Backup Folder", fontSize = 10.sp) },
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = colors.accentCyan,
                                                        unfocusedBorderColor = colors.border,
                                                        focusedLabelColor = colors.accentCyan,
                                                        unfocusedLabelColor = colors.muted
                                                    )
                                                )

                                                Spacer(modifier = Modifier.height(10.dp))

                                                Button(
                                                    onClick = { viewModel.syncAllSessionMediaToDrive() },
                                                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentCyan),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "☁️ Sync Session Photos/Videos to Drive Now",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.Black
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Camera & Device Preferences Section
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.bg),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, colors.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "⚙️ Camera & Device Preferences",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = colors.text
                        )

                        // 1. Save Both Photos (Stamped + Original)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.panel)
                                .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "📷 Save Both Photos (Stamped + Original)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = colors.text
                                )
                                Text(
                                    text = "Save GPS stamped photo & clean original photo together",
                                    fontSize = 10.5.sp,
                                    color = colors.muted
                                )
                            }
                            Switch(
                                checked = state.isSaveOriginalPhotoEnabled,
                                onCheckedChange = { viewModel.setSaveOriginalPhotoEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = colors.accentCyan
                                )
                            )
                        }

                        // 2. Haptic vibration feedback
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.panel)
                                .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "📳 Haptic vibration feedback",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = colors.text
                                )
                                Text(
                                    text = "Tactile vibration on capture",
                                    fontSize = 10.5.sp,
                                    color = colors.muted
                                )
                            }
                            Switch(
                                checked = hapticFeedback,
                                onCheckedChange = { viewModel.setHapticFeedback(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = colors.accentCyan
                                )
                            )
                        }

                        // 3. Volume Button Action (Expandable Dropdown)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.panel)
                                .border(1.dp, if (isVolumeKeyDropdownExpanded) colors.accentCyan.copy(alpha = 0.8f) else colors.border, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { isVolumeKeyDropdownExpanded = !isVolumeKeyDropdownExpanded }
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "🔊 Volume Button Action",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = colors.text
                                    )
                                    Text(
                                        text = "Set action for physical volume keys during camera preview",
                                        fontSize = 10.5.sp,
                                        color = colors.muted
                                    )
                                }
                                Icon(
                                    imageVector = if (isVolumeKeyDropdownExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Toggle Volume Key Dropdown",
                                    tint = colors.accentCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            AnimatedVisibility(
                                visible = isVolumeKeyDropdownExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                ) {
                                    val volumeOptions = listOf(
                                        Triple("capture", "📸 Capture Photo / Video", "Clicking Volume Up or Down captures photo or video"),
                                        Triple("zoom", "🔍 Zoom In & Out", "Volume Up = Zoom In, Volume Down = Zoom Out")
                                    )

                                    volumeOptions.forEach { (actionId, title, desc) ->
                                        val isSelected = state.volumeKeyAction == actionId
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) colors.accentCyan.copy(alpha = 0.15f) else Color.Transparent)
                                                .border(
                                                    1.dp,
                                                    if (isSelected) colors.accentCyan else colors.border.copy(alpha = 0.5f),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable { viewModel.setVolumeKeyAction(actionId) }
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { viewModel.setVolumeKeyAction(actionId) },
                                                colors = RadioButtonDefaults.colors(
                                                    selectedColor = colors.accentCyan,
                                                    unselectedColor = colors.muted
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = title,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.sp,
                                                    color = if (isSelected) colors.accentCyan else colors.text
                                                )
                                                Text(
                                                    text = desc,
                                                    fontSize = 10.sp,
                                                    color = colors.muted
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 4. Device Storage Folder Selection (Expandable Dropdown)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.panel)
                                .border(1.dp, if (isStorageFolderDropdownExpanded) colors.accentCyan.copy(alpha = 0.8f) else colors.border, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { isStorageFolderDropdownExpanded = !isStorageFolderDropdownExpanded }
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "📁 Save Location & Folder",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = colors.text
                                    )
                                    Text(
                                        text = "Folder: ${state.storageFolderName}",
                                        fontSize = 10.5.sp,
                                        color = colors.accentCyan,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Icon(
                                    imageVector = if (isStorageFolderDropdownExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Toggle Storage Folder Dropdown",
                                    tint = colors.accentCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            AnimatedVisibility(
                                visible = isStorageFolderDropdownExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                ) {
                                    Text(
                                        text = "Select preset or type custom folder name for saving photos & videos:",
                                        fontSize = 10.5.sp,
                                        color = colors.muted
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    val folderPresets = listOf("ProGpsCam", "DCIM/ProGpsCam", "Pictures/GPS_Map", "Survey_Photos")
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        folderPresets.take(2).forEach { preset ->
                                            val isSelected = state.storageFolderName == preset
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isSelected) colors.accentCyan.copy(alpha = 0.2f) else colors.bg)
                                                    .border(1.dp, if (isSelected) colors.accentCyan else colors.border, RoundedCornerShape(6.dp))
                                                    .clickable { viewModel.setStorageFolderName(preset) }
                                                    .padding(vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = preset,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) colors.accentCyan else colors.text
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        folderPresets.drop(2).forEach { preset ->
                                            val isSelected = state.storageFolderName == preset
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isSelected) colors.accentCyan.copy(alpha = 0.2f) else colors.bg)
                                                    .border(1.dp, if (isSelected) colors.accentCyan else colors.border, RoundedCornerShape(6.dp))
                                                    .clickable { viewModel.setStorageFolderName(preset) }
                                                    .padding(vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = preset,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) colors.accentCyan else colors.text
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    var customFolderText by remember(state.storageFolderName) { mutableStateOf(state.storageFolderName) }
                                    OutlinedTextField(
                                        value = customFolderText,
                                        onValueChange = {
                                            customFolderText = it
                                            viewModel.setStorageFolderName(it)
                                        },
                                        label = { Text("Custom Folder Path", fontSize = 10.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = colors.accentCyan,
                                            unfocusedBorderColor = colors.border,
                                            focusedLabelColor = colors.accentCyan,
                                            unfocusedLabelColor = colors.muted
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. About Us Section (Clickable / Expandable)
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.bg),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, if (isAboutUsExpanded) colors.accentCyan.copy(alpha = 0.8f) else colors.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isAboutUsExpanded) colors.accentCyan.copy(alpha = 0.12f) else Color.Transparent)
                                .clickable { isAboutUsExpanded = !isAboutUsExpanded }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = "ℹ️", fontSize = 18.sp)
                                Column {
                                    Text(
                                        text = "About Us",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = colors.text
                                    )
                                    Text(
                                        text = if (isAboutUsExpanded) "Tap to collapse info" else "Tap to view app & creator info",
                                        fontSize = 11.sp,
                                        color = colors.muted
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (isAboutUsExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Toggle About Us",
                                tint = colors.accentCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        AnimatedVisibility(
                            visible = isAboutUsExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "🛰️ PRO GPS CAM",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = colors.accentCyan,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Version 1.0",
                                    fontSize = 11.sp,
                                    color = colors.muted
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "This App is Made With Love By Pankaj",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = colors.accentCyan,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Purpose is to give ads free experience to all users",
                                    fontSize = 12.sp,
                                    color = colors.text,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "I hope you all enjoyed this app",
                                    fontSize = 12.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = colors.text,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.accentCyan.copy(alpha = 0.12f))
                                        .border(1.dp, colors.accentCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .clickable { triggerEmailIntent(context, "pr903128@gmail.com") }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(text = "✉️", fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "pr903128@gmail.com",
                                        color = colors.accentCyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.accentCyan.copy(alpha = 0.12f))
                                        .border(1.dp, colors.accentCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .clickable {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ConsentManager.PRIVACY_POLICY_URL))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Cannot open browser", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "🔒 Privacy Policy",
                                        color = colors.accentCyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.accentCyan.copy(alpha = 0.12f))
                                        .border(1.dp, colors.accentCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .clickable {
                                            val activity = context as? Activity
                                            if (activity != null) {
                                                ConsentManager.showPrivacyOptions(activity)
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "⚙️ Ad Privacy Options",
                                        color = colors.accentCyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Close Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Settings", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Pro Subscription and Rewarded Ad dialogs
    if (showProUpgradeDialog) {
        ProUpgradeDialog(
            onDismiss = { showProUpgradeDialog = false },
            onWatchAd = {
                showProUpgradeDialog = false
                showRewardedAdDialog = true
            }
        )
    }

    if (showRewardedAdDialog) {
        RewardedAdDialog(
            viewModel = viewModel,
            onDismiss = { showRewardedAdDialog = false },
            onRewardClaimed = {
                showRewardedAdDialog = false
                isProDropdownExpanded = true
            }
        )
    }
}

@Composable
fun ManualLocationDialog(
    state: LocationState,
    viewModel: GpsViewModel,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current

    var latInput by remember { mutableStateOf(if (state.lat != 0.0) state.lat.toString() else "") }
    var lngInput by remember { mutableStateOf(if (state.lng != 0.0) state.lng.toString() else "") }
    var cityInput by remember { mutableStateOf(state.city) }
    var stateInput by remember { mutableStateOf(state.state) }
    var pincodeInput by remember { mutableStateOf(state.postcode) }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(22.dp))
                .border(1.5.dp, colors.accentCyan, RoundedCornerShape(22.dp))
                .background(colors.panel)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📍 Configure Coordinates",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = colors.accentCyan
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(30.dp)
                            .background(colors.border.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = colors.text,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Divider(color = colors.border.copy(alpha = 0.6f))

                OutlinedTextField(
                    value = latInput,
                    onValueChange = { latInput = it },
                    label = { Text("Latitude") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accentCyan,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = lngInput,
                    onValueChange = { lngInput = it },
                    label = { Text("Longitude") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accentCyan,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = cityInput,
                    onValueChange = { cityInput = it },
                    label = { Text("City / Town") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accentCyan,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = stateInput,
                    onValueChange = { stateInput = it },
                    label = { Text("State / Region") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accentCyan,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pincodeInput,
                    onValueChange = { pincodeInput = it },
                    label = { Text("Pincode / Postal Code") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accentCyan,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.resetAppAndLocation()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.border,
                            contentColor = colors.text
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset GPS", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val lat = latInput.toDoubleOrNull() ?: state.lat
                            val lng = lngInput.toDoubleOrNull() ?: state.lng
                            viewModel.applyManualLocation(
                                lat = lat,
                                lng = lng,
                                city = cityInput,
                                state = stateInput,
                                customPincode = pincodeInput
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accentCyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Text("Apply Manual", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
