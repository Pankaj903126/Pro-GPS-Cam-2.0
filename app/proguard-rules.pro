# =========================================================================
# ProGuard / R8 Rules for Pro GPS Cam
# =========================================================================

# Keep line numbers and source attributes for useful crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# -------------------------------------------------------------------------
# Gson & Serialized Data Models
# -------------------------------------------------------------------------
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.** { *; }

# Keep data classes used with Gson and JSON serialization
-keep class com.example.PendingPhoto { *; }
-keep class com.example.AddressInfo { *; }
-keep class com.example.AddressSourceItem { *; }
-keep class com.example.LiveGpsSourcesState { *; }
-keep class com.example.IpLocationInfo { *; }
-keep class com.example.LocationState { *; }
-keep class com.example.SessionMediaItem { *; }
-keep class com.example.StampTemplate { *; }
-keep class com.example.CameraFlashMode { *; }
-keep class com.example.LiveCameraAspectRatio { *; }
-keep class com.example.LiveCameraMode { *; }

# -------------------------------------------------------------------------
# OkHttp & Okio
# -------------------------------------------------------------------------
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keepclassmembers class * extends okhttp3.ResponseBody { *; }

# -------------------------------------------------------------------------
# CameraX
# -------------------------------------------------------------------------
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# -------------------------------------------------------------------------
# Media3 (Transformer, Effect, Common)
# -------------------------------------------------------------------------
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# -------------------------------------------------------------------------
# Google Play Services Location
# -------------------------------------------------------------------------
-keep class com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.location.**

# -------------------------------------------------------------------------
# Google Mobile Ads (AdMob) & User Messaging Platform (UMP)
# -------------------------------------------------------------------------
-keep class com.google.android.gms.ads.** { *; }
-keep interface com.google.android.gms.ads.** { *; }
-keep class com.google.android.ump.** { *; }
-keep interface com.google.android.ump.** { *; }
-dontwarn com.google.android.gms.ads.**
-dontwarn com.google.android.ump.**

# -------------------------------------------------------------------------
# androidx ExifInterface
# -------------------------------------------------------------------------
-keep class androidx.exifinterface.media.ExifInterface { *; }

# -------------------------------------------------------------------------
# Kotlin Coroutines & Jetpack Compose
# -------------------------------------------------------------------------
-keep class kotlinx.coroutines.** { *; }
-keep class androidx.compose.** { *; }
