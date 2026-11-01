package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalAppColors

/**
 * Custom switch component matching the HTML/CSS prototype's .switch design.
 * Provides a smooth pill track and circular knob with dynamic accent tinting.
 */
@Composable
fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    width: Dp = 46.dp,
    height: Dp = 26.dp,
    thumbSize: Dp = 20.dp,
    testTag: String = "custom_switch"
) {
    val colors = LocalAppColors.current

    val trackBgColor by animateColorAsState(
        targetValue = when {
            !enabled -> if (colors.isDark) Color(0x33FFFFFF) else Color(0x26000000)
            checked -> colors.accent
            else -> if (colors.isDark) Color(0x33FFFFFF) else Color(0x26000000)
        },
        animationSpec = tween(180),
        label = "switchTrackColor"
    )

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) (width - thumbSize - 3.dp) else 3.dp,
        animationSpec = tween(180),
        label = "switchThumbOffset"
    )

    val thumbColor by animateColorAsState(
        targetValue = when {
            !enabled -> if (colors.isDark) Color(0x66FFFFFF) else Color(0x66000000)
            checked -> colors.accentInk
            else -> Color.White
        },
        animationSpec = tween(180),
        label = "switchThumbColor"
    )

    Box(
        modifier = modifier
            .testTag(testTag)
            .size(width, height)
            .clip(RoundedCornerShape(height / 2))
            .background(trackBgColor)
            .border(
                1.dp,
                if (checked) colors.accent.copy(alpha = 0.5f) else colors.borderCard,
                RoundedCornerShape(height / 2)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled
            ) {
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}
