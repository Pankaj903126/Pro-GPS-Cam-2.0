package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppColors

enum class FlashChipOption(val id: String, val label: String, val icon: String) {
    AUTO("auto", "Auto", "⚡A"),
    ON("on", "On", "⚡"),
    FILL("fill", "Fill", "💡"),
    OFF("off", "Off", "🚫")
}

/**
 * Step 4: "CAMERA & SURVEY TOOLS" Overlay Panel (The 'More' Menu)
 * Matches the HTML .modal and .switch design:
 * - Rule of thirds grid toggle
 * - Mirror front camera toggle (checked by default)
 * - Live compass overlay toggle
 * - Flash mode grid with chips: Auto, On, Fill, and Off
 */
@Composable
fun SurveyToolsModal(
    onDismiss: () -> Unit,
    showGrid: Boolean,
    onGridChange: (Boolean) -> Unit,
    mirrorFrontCamera: Boolean,
    onMirrorFrontCameraChange: (Boolean) -> Unit,
    liveCompass: Boolean,
    onLiveCompassChange: (Boolean) -> Unit,
    selectedFlash: FlashChipOption,
    onFlashSelect: (FlashChipOption) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    // Scrim background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() }
            .testTag("survey_tools_modal_scrim"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* stop propagation */ }
                .testTag("survey_tools_modal_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (colors.isDark) Color(0xFF11141E) else Color(0xFFFFFFFF)
            ),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, colors.borderCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header: Title and Close button
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
                            text = "📐",
                            fontSize = 20.sp
                        )
                        Column {
                            Text(
                                text = "CAMERA & SURVEY TOOLS",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.accent,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Pro survey overlay & hardware toggles",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (colors.isDark) Color(0x22FFFFFF) else Color(0x14000000))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close Tools",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.borderCard)
                )

                // 1. Rule of thirds grid toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (colors.isDark) Color(0x1AFFFFFF) else Color(0x0A000000))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("⊞", fontSize = 20.sp, color = colors.accent)
                        Column {
                            Text(
                                text = "Rule of thirds grid",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Overlay 3x3 alignment guidelines",
                                fontSize = 10.5.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    CustomSwitch(
                        checked = showGrid,
                        onCheckedChange = onGridChange,
                        testTag = "toggle_rule_of_thirds"
                    )
                }

                // 2. Mirror front camera (checked by default)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (colors.isDark) Color(0x1AFFFFFF) else Color(0x0A000000))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("🪞", fontSize = 18.sp)
                        Column {
                            Text(
                                text = "Mirror front camera",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = if (mirrorFrontCamera) "Checked by default (Mirror View)" else "Standard selfie orientation",
                                fontSize = 10.5.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    CustomSwitch(
                        checked = mirrorFrontCamera,
                        onCheckedChange = onMirrorFrontCameraChange,
                        testTag = "toggle_mirror_front_camera"
                    )
                }

                // 3. Live compass overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (colors.isDark) Color(0x1AFFFFFF) else Color(0x0A000000))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("🧭", fontSize = 18.sp)
                        Column {
                            Text(
                                text = "Live compass overlay",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Real-time heading & survey azimuth HUD",
                                fontSize = 10.5.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    CustomSwitch(
                        checked = liveCompass,
                        onCheckedChange = onLiveCompassChange,
                        testTag = "toggle_live_compass"
                    )
                }

                // 4. Flash Mode Grid with Chips: Auto, On, Fill, Off
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (colors.isDark) Color(0x1AFFFFFF) else Color(0x0A000000))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("⚡", fontSize = 16.sp)
                        Text(
                            text = "Flash Mode",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FlashChipOption.entries.forEach { option ->
                            val isSelected = selectedFlash == option
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) colors.accent else (if (colors.isDark) Color(0x22FFFFFF) else Color(0x14000000))
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) colors.accent else colors.borderCard,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onFlashSelect(option) }
                                    .padding(vertical = 8.dp)
                                    .testTag("flash_chip_${option.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = option.icon,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = option.label,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = if (isSelected) colors.accentInk else colors.textPrimary
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
