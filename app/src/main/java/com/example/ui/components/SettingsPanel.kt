package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppAccent
import com.example.ui.theme.LocalAppColors

/**
 * Step 5: Full Settings Panel
 * Full-screen sliding panel for Settings:
 * - "Dark Theme" toggle switch
 * - "App Theme" dropdown to select the 5 accent colors
 * - Pro Features Box: crown icon, "LOCKED" pill, subscription prices
 * - Disabled toggle rows for PRO features with lock icons
 * - General preference toggles at the bottom
 */
@Composable
fun SettingsPanel(
    onClose: () -> Unit,
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    currentAccent: AppAccent,
    onAccentSelect: (AppAccent) -> Unit,
    saveBothPhotos: Boolean,
    onSaveBothPhotosToggle: (Boolean) -> Unit,
    hapticVibration: Boolean,
    onHapticVibrationToggle: (Boolean) -> Unit,
    onUnlockProClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    var isThemeDropdownExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("full_settings_panel")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "⚙️",
                        fontSize = 22.sp
                    )
                    Column {
                        Text(
                            text = "SETTINGS & PREFERENCES",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Pro GPS Survey Camera v1.1",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (colors.isDark) Color(0x22FFFFFF) else Color(0x14000000))
                        .testTag("btn_close_settings")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close Settings",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.borderCard)
            )

            // Settings Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(6.dp)) }

                // 1. Appearance & Theme Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (colors.isDark) Color(0x14FFFFFF) else Color(0xFFFFFFFF))
                            .border(1.dp, colors.borderCard, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "APPEARANCE & THEME",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            letterSpacing = 0.8.sp
                        )

                        // Dark Theme Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = if (isDarkMode) "🌙" else "☀️",
                                    fontSize = 18.sp
                                )
                                Column {
                                    Text(
                                        text = "Dark Theme",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = if (isDarkMode) "Pure black background (#000000)" else "Clean soft light mode (#f5f4f0)",
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            CustomSwitch(
                                checked = isDarkMode,
                                onCheckedChange = onDarkModeToggle,
                                testTag = "toggle_dark_theme"
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(colors.borderCard)
                        )

                        // App Theme (Accent Color Dropdown)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(text = "🎨", fontSize = 18.sp)
                                Column {
                                    Text(
                                        text = "App Theme (Accent Color)",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Select from 5 dynamic prototype color palettes",
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            // Interactive Dropdown Trigger Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (colors.isDark) Color(0x22FFFFFF) else Color(0x0A000000))
                                    .border(1.2.dp, colors.accent, RoundedCornerShape(12.dp))
                                    .clickable { isThemeDropdownExpanded = true }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                                    .testTag("app_theme_dropdown_trigger")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(currentAccent.color)
                                                .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                                        )
                                        Text(
                                            text = currentAccent.displayName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Filled.ArrowDropDown,
                                        contentDescription = "Select Accent",
                                        tint = colors.textPrimary
                                    )
                                }

                                DropdownMenu(
                                    expanded = isThemeDropdownExpanded,
                                    onDismissRequest = { isThemeDropdownExpanded = false },
                                    modifier = Modifier
                                        .background(if (colors.isDark) Color(0xFF1E222D) else Color(0xFFFFFFFF))
                                        .border(1.dp, colors.borderCard, RoundedCornerShape(8.dp))
                                ) {
                                    AppAccent.entries.forEach { accent ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(18.dp)
                                                            .clip(CircleShape)
                                                            .background(accent.color)
                                                            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                                    )
                                                    Text(
                                                        text = accent.displayName,
                                                        fontSize = 13.sp,
                                                        fontWeight = if (accent == currentAccent) FontWeight.ExtraBold else FontWeight.Medium,
                                                        color = if (colors.isDark) Color.White else Color.Black
                                                    )
                                                }
                                            },
                                            trailingIcon = {
                                                if (accent == currentAccent) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Check,
                                                        contentDescription = "Selected",
                                                        tint = accent.color,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            },
                                            onClick = {
                                                onAccentSelect(accent)
                                                isThemeDropdownExpanded = false
                                            },
                                            modifier = Modifier.testTag("accent_option_${accent.id}")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Pro Features Box (Highlighted Section)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (colors.isDark) Color(0xFF181512) else Color(0xFFFDFBF7)
                            )
                            .border(1.5.dp, Color(0xFFD4AF6A), RoundedCornerShape(18.dp))
                            .padding(16.dp)
                            .testTag("pro_features_box"),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Pro Header with Crown and LOCKED Pill
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = "👑", fontSize = 22.sp)
                                Column {
                                    Text(
                                        text = "PRO FEATURES",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFD4AF6A),
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "Unlock survey tools & manual edits",
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            // "LOCKED" Pill Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE11D48).copy(alpha = 0.18f))
                                    .border(1.dp, Color(0xFFE11D48), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = "Locked",
                                        tint = Color(0xFFE11D48),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "LOCKED",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFE11D48),
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }

                        // Subscription Pricing Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFD4AF6A).copy(alpha = 0.12f))
                                .border(1.dp, Color(0xFFD4AF6A).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "$2.99 / Month",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "or $19.99 / Year (Save 45%)",
                                        fontSize = 11.sp,
                                        color = Color(0xFFD4AF6A),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Button(
                                    onClick = onUnlockProClick,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFD4AF6A),
                                        contentColor = Color(0xFF14120E)
                                    ),
                                    modifier = Modifier.testTag("btn_unlock_pro")
                                ) {
                                    Text(
                                        text = "Upgrade",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(colors.borderCard)
                        )

                        // Disabled PRO Feature Rows with Lock Icons
                        val proFeatures = listOf(
                            Pair("Edit location manually", "Override GPS coordinates directly"),
                            Pair("Manual time", "Set custom survey date and time"),
                            Pair("Upload gallery photo", "Apply georeferenced stamps to any existing photo"),
                            Pair("Remove watermark", "Remove the default app watermark brand"),
                            Pair("Custom Watermark", "Upload custom surveyor logo & badge watermark"),
                            Pair("Google Drive Sync", "Automatic instant cloud synchronization")
                        )

                        proFeatures.forEach { (title, subtitle) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = "Locked feature",
                                        tint = colors.textTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column {
                                        Text(
                                            text = title,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.textSecondary
                                        )
                                        Text(
                                            text = subtitle,
                                            fontSize = 10.sp,
                                            color = colors.textTertiary
                                        )
                                    }
                                }

                                CustomSwitch(
                                    checked = false,
                                    enabled = false,
                                    onCheckedChange = {},
                                    testTag = "pro_disabled_switch"
                                )
                            }
                        }
                    }
                }

                // 3. General Preferences Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (colors.isDark) Color(0x14FFFFFF) else Color(0xFFFFFFFF))
                            .border(1.dp, colors.borderCard, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "GENERAL PREFERENCES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            letterSpacing = 0.8.sp
                        )

                        // Save Both Photos
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(text = "📸", fontSize = 18.sp)
                                Column {
                                    Text(
                                        text = "Save Both Photos",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Keep original unstamped copy + stamped copy",
                                        fontSize = 10.5.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            CustomSwitch(
                                checked = saveBothPhotos,
                                onCheckedChange = onSaveBothPhotosToggle,
                                testTag = "toggle_save_both_photos"
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(colors.borderCard)
                        )

                        // Haptic Vibration
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(text = "📳", fontSize = 18.sp)
                                Column {
                                    Text(
                                        text = "Haptic Vibration",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Tactile feedback on shutter tap & survey lock",
                                        fontSize = 10.5.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            CustomSwitch(
                                checked = hapticVibration,
                                onCheckedChange = onHapticVibrationToggle,
                                testTag = "toggle_haptic_vibration"
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}
