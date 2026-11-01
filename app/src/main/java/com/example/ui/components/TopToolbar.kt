package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppColors

/**
 * Step 2: Uniform Top Toolbar with 6 circular buttons matching HTML/CSS prototype:
 * 1. Flash
 * 2. Ratio (3:4 text)
 * 3. Timer
 * 4. Size
 * 5. More
 * 6. Settings
 */
@Composable
fun TopToolbar(
    modifier: Modifier = Modifier,
    flashLabel: String = "⚡",
    isFlashActive: Boolean = false,
    onFlashClick: () -> Unit,
    ratioText: String = "3:4",
    onRatioClick: () -> Unit,
    timerSeconds: Int = 0,
    onTimerClick: () -> Unit,
    isSizeActive: Boolean = false,
    onSizeClick: () -> Unit,
    isMoreActive: Boolean = false,
    onMoreClick: () -> Unit,
    isSettingsOpen: Boolean = false,
    onSettingsClick: () -> Unit
) {
    val colors = LocalAppColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("camera_top_toolbar"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Flash
        ToolbarCircularButton(
            icon = {
                Text(
                    text = flashLabel,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isFlashActive) colors.accent else colors.textPrimary
                )
            },
            label = "Flash",
            isActive = isFlashActive,
            onClick = onFlashClick,
            testTag = "toolbar_btn_flash"
        )

        // 2. Ratio (3:4 text)
        ToolbarCircularButton(
            icon = {
                Text(
                    text = ratioText,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.accent
                )
            },
            label = "Ratio",
            isActive = true,
            onClick = onRatioClick,
            testTag = "toolbar_btn_ratio"
        )

        // 3. Timer
        ToolbarCircularButton(
            icon = {
                if (timerSeconds > 0) {
                    Text(
                        text = "${timerSeconds}s",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.accent
                    )
                } else {
                    Text(
                        text = "⏱️",
                        fontSize = 15.sp
                    )
                }
            },
            label = if (timerSeconds > 0) "${timerSeconds}s" else "Timer",
            isActive = timerSeconds > 0,
            onClick = onTimerClick,
            testTag = "toolbar_btn_timer"
        )

        // 4. Size (Stamp scaling)
        ToolbarCircularButton(
            icon = {
                Text(
                    text = "📐",
                    fontSize = 15.sp
                )
            },
            label = "Size",
            isActive = isSizeActive,
            onClick = onSizeClick,
            testTag = "toolbar_btn_size"
        )

        // 5. More (Survey & Camera Tools)
        ToolbarCircularButton(
            icon = {
                Text(
                    text = "⋯",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isMoreActive) colors.accent else colors.textPrimary
                )
            },
            label = "More",
            isActive = isMoreActive,
            onClick = onMoreClick,
            testTag = "toolbar_btn_more"
        )

        // 6. Settings
        ToolbarCircularButton(
            icon = {
                Icon(
                    imageVector = if (isSettingsOpen) Icons.Filled.Close else Icons.Filled.Settings,
                    contentDescription = if (isSettingsOpen) "Close Settings" else "Settings",
                    tint = if (isSettingsOpen) Color(0xFFFF5252) else colors.textPrimary,
                    modifier = Modifier.size(19.dp)
                )
            },
            label = if (isSettingsOpen) "Close" else "Settings",
            isActive = isSettingsOpen,
            activeBorderColor = Color(0xFFFF5252),
            onClick = onSettingsClick,
            testTag = "toolbar_btn_settings"
        )
    }
}

@Composable
fun ToolbarCircularButton(
    icon: @Composable () -> Unit,
    label: String,
    isActive: Boolean = false,
    activeBorderColor: Color? = null,
    onClick: () -> Unit,
    testTag: String = "toolbar_btn"
) {
    val colors = LocalAppColors.current
    val effectiveActiveBorder = activeBorderColor ?: colors.accent

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .testTag(testTag)
                .size(42.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) effectiveActiveBorder.copy(alpha = 0.22f)
                    else if (colors.isDark) Color(0x33000000) else Color(0x14000000)
                )
                .border(
                    1.2.dp,
                    if (isActive) effectiveActiveBorder else colors.borderCard,
                    CircleShape
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Text(
            text = label,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Medium,
            color = if (isActive) effectiveActiveBorder else colors.textSecondary
        )
    }
}
