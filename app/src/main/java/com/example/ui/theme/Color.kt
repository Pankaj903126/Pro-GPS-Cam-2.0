package com.example.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// 5 Specific Accent Colors from HTML/CSS Prototype Dropdown:
// Gold (#D4AF6A), Burnt Orange (#D85A30), Emerald Teal (#1D9E75), Electric Blue (#378ADD), Royal Purple (#7F77DD)
enum class AppAccent(
    val id: String,
    val displayName: String,
    val color: Color,
    val inkColor: Color
) {
    GOLD("gold", "Gold", Color(0xFFD4AF6A), Color(0xFF14120E)),
    BURNT_ORANGE("burnt_orange", "Burnt Orange", Color(0xFFD85A30), Color(0xFFFFFFFF)),
    EMERALD_TEAL("emerald_teal", "Emerald Teal", Color(0xFF1D9E75), Color(0xFFFFFFFF)),
    ELECTRIC_BLUE("electric_blue", "Electric Blue", Color(0xFF378ADD), Color(0xFFFFFFFF)),
    ROYAL_PURPLE("royal_purple", "Royal Purple", Color(0xFF7F77DD), Color(0xFFFFFFFF));

    companion object {
        fun fromId(id: String): AppAccent =
            entries.find { it.id.equals(id, ignoreCase = true) } ?: GOLD
    }
}

// Global & Legacy Theme Colors for full backward-compatibility across all views
val BgColor = Color(0xFF000000)
val PanelColor = Color(0xFF0F111A)
val LightPanelColor = Color(0xFFFFFFFF)
val BorderColor = Color(0xFF1E2235)
val TextColor = Color(0xFFE8EAF0)
val MutedColor = Color(0xFF7E8494)

val AccentCyan = Color(0xFF00E5FF)
val AccentCyanLight = Color(0xFF1E3A8A)
val AccentOrange = Color(0xFFD85A30)
val PurpleGradientStart = Color(0xFF7B61FF)

// Viewfinder Background Gradient (#c1707a to #a85862) to simulate camera preview
val ViewfinderGradientStart = Color(0xFFC1707A)
val ViewfinderGradientEnd = Color(0xFFA85862)
val RedError = Color(0xFFFF4D4D)
val CustomGreen = Color(0xFF10B981)
val CustomYellow = Color(0xFFFFD700)

data class AppThemeColors(
    val bg: Color,
    val panel: Color,
    val panelSubtle: Color,
    val border: Color,
    val text: Color,
    val textHeading: Color,
    val titleColor: Color,
    val gpsCoordColor: Color,
    val headingColor: Color,
    val badgeBg: Color,
    val badgeText: Color,
    val inputBg: Color,
    val inputBorder: Color,
    val inputText: Color,
    val sliderAccent: Color,
    val toggleActive: Color,
    val muted: Color,
    val accentCyan: Color,
    val accentOrange: Color,
    val customGreen: Color,
    val customYellow: Color,
    val redError: Color,
    val photoBtnGradient: List<Color>,
    val videoBtnGradient: List<Color>,
    val galleryBtnGradient: List<Color>,
    val thumbnailOverlayBg: Color,
    val isDark: Boolean,
    // HTML/CSS Prototype Step 1 variables:
    val accent: Color,
    val accentInk: Color,
    val accentSubtle: Color,
    val surfaceCard: Color,
    val surfaceCardSubtle: Color,
    val surfaceCardStrong: Color,
    val borderCard: Color,
    val borderSubtle: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val statusBg: Color
)

fun createAppThemeColors(isDark: Boolean, accent: AppAccent = AppAccent.GOLD): AppThemeColors {
    val accColor = accent.color
    val accInk = accent.inkColor
    val accSubtle = accColor.copy(alpha = 0.18f)

    return if (isDark) {
        // Dark Mode: background #000000, semi-transparent white overlays
        AppThemeColors(
            bg = Color(0xFF000000),
            panel = Color(0x1FFFFFFF),
            panelSubtle = Color(0x0FFFFFFF),
            border = Color(0x1FFFFFFF),
            text = Color(0xFFFFFFFF),
            textHeading = Color(0xFFFFFFFF),
            titleColor = accColor,
            gpsCoordColor = accColor,
            headingColor = accColor,
            badgeBg = accSubtle,
            badgeText = accColor,
            inputBg = Color(0xFF111111),
            inputBorder = Color(0x26FFFFFF),
            inputText = Color(0xFFFFFFFF),
            sliderAccent = accColor,
            toggleActive = accColor,
            muted = Color(0x80FFFFFF),
            accentCyan = accColor,
            accentOrange = Color(0xFFD85A30),
            customGreen = Color(0xFF10B981),
            customYellow = Color(0xFFFFD700),
            redError = Color(0xFFFF4D4D),
            photoBtnGradient = listOf(accColor, accColor.copy(alpha = 0.75f)),
            videoBtnGradient = listOf(Color(0xFFEF4444), Color(0xFFDC2626)),
            galleryBtnGradient = listOf(Color(0xFF3B82F6), accColor),
            thumbnailOverlayBg = Color(0xB3000000),
            isDark = true,
            accent = accColor,
            accentInk = accInk,
            accentSubtle = accSubtle,
            surfaceCard = Color(0x1CFFFFFF),
            surfaceCardSubtle = Color(0x0DFFFFFF),
            surfaceCardStrong = Color(0x2EFFFFFF),
            borderCard = Color(0x2BFFFFFF),
            borderSubtle = Color(0x17FFFFFF),
            textPrimary = Color(0xFFFFFFFF),
            textSecondary = Color(0xB3FFFFFF),
            textTertiary = Color(0x66FFFFFF),
            statusBg = Color(0xCC000000)
        )
    } else {
        // Light Mode: background #f5f4f0 (matching CSS .light-theme)
        AppThemeColors(
            bg = Color(0xFFF5F4F0),
            panel = Color(0xFFFFFFFF),
            panelSubtle = Color(0xFFEBEAE5),
            border = Color(0x1A000000),
            text = Color(0xFF1A1A1A),
            textHeading = Color(0xFF111111),
            titleColor = accColor,
            gpsCoordColor = accColor,
            headingColor = accColor,
            badgeBg = accSubtle,
            badgeText = accColor,
            inputBg = Color(0xFFFFFFFF),
            inputBorder = Color(0x26000000),
            inputText = Color(0xFF1A1A1A),
            sliderAccent = accColor,
            toggleActive = accColor,
            muted = Color(0x80000000),
            accentCyan = accColor,
            accentOrange = Color(0xFFD85A30),
            customGreen = Color(0xFF059669),
            customYellow = Color(0xFFD97706),
            redError = Color(0xFFDC2626),
            photoBtnGradient = listOf(accColor, accColor.copy(alpha = 0.85f)),
            videoBtnGradient = listOf(Color(0xFFF87171), Color(0xFFEF4444)),
            galleryBtnGradient = listOf(Color(0xFF2563EB), accColor),
            thumbnailOverlayBg = Color(0x80F5F4F0),
            isDark = false,
            accent = accColor,
            accentInk = accInk,
            accentSubtle = accSubtle,
            surfaceCard = Color(0xFFFFFFFF),
            surfaceCardSubtle = Color(0x08000000),
            surfaceCardStrong = Color(0x14000000),
            borderCard = Color(0x1A000000),
            borderSubtle = Color(0x0F000000),
            textPrimary = Color(0xFF1A1A1A),
            textSecondary = Color(0xA6000000),
            textTertiary = Color(0x66000000),
            statusBg = Color(0xCCF5F4F0)
        )
    }
}

val DarkAppThemeColors = createAppThemeColors(isDark = true, accent = AppAccent.GOLD)
val LightAppThemeColors = createAppThemeColors(isDark = false, accent = AppAccent.GOLD)

val LocalAppColors = staticCompositionLocalOf { DarkAppThemeColors }
