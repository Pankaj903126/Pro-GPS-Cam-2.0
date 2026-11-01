package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppColors

/**
 * Step 2: Custom status bar layout matching the HTML prototype.
 * Displays dummy Time (3:12), Speed (12 kB/s), Network (LTE+), and Battery (82%).
 */
@Composable
fun CustomStatusBar(
    modifier: Modifier = Modifier,
    timeText: String = "3:12",
    speedText: String = "12 kB/s",
    networkText: String = "LTE+",
    batteryText: String = "82%"
) {
    val colors = LocalAppColors.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .testTag("custom_status_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Time (3:12)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = timeText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier.testTag("status_bar_time")
                )
            }

            // Right: Speed, Network, Battery
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Speed: 12 kB/s
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "⚡",
                        fontSize = 10.sp
                    )
                    Text(
                        text = speedText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary,
                        modifier = Modifier.testTag("status_bar_speed")
                    )
                }

                // Network: LTE+
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "📶",
                        fontSize = 10.sp
                    )
                    Text(
                        text = networkText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                        modifier = Modifier.testTag("status_bar_network")
                    )
                }

                // Battery: 82%
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = batteryText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                        modifier = Modifier.testTag("status_bar_battery")
                    )
                    // Custom micro battery icon
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(9.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .border(1.dp, colors.textPrimary.copy(alpha = 0.8f), RoundedCornerShape(2.dp))
                            .padding(1.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.82f)
                                .height(7.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(colors.accent)
                        )
                    }
                }
            }
        }
    }
}
