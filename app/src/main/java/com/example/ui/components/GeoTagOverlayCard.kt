package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppColors

/**
 * Step 3: The Geo-Tag Overlay Card
 * Floating card over the viewfinder positioned at the bottom.
 * - Header: "AUTO" badge with icon, Location Name and full Address
 * - Details: Latitude and Longitude prominently in accent color
 * - Metadata: Date, Time, Accuracy (±11m), custom Note chip ("College")
 * - Footer: "Made with love"
 */
@Composable
fun GeoTagOverlayCard(
    modifier: Modifier = Modifier,
    locationName: String = "Central Park",
    address: String = "Near 5th Ave, Manhattan, New York, NY 10022",
    latitude: Double = 40.785091,
    longitude: Double = -73.968285,
    dateText: String = "28 Sep 2026",
    timeText: String = "03:12:08 PM",
    accuracyText: String = "±11m",
    noteText: String = "College",
    sourceBadgeText: String = "AUTO",
    onCardClick: () -> Unit = {}
) {
    val colors = LocalAppColors.current

    val latFormatted = if (latitude >= 0) {
        String.format("%.5f° N", latitude)
    } else {
        String.format("%.5f° S", -latitude)
    }

    val lngFormatted = if (longitude >= 0) {
        String.format("%.5f° E", longitude)
    } else {
        String.format("%.5f° W", -longitude)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (colors.isDark) Color(0xCC0D1117) else Color(0xF2FFFFFF)
            )
            .border(
                1.2.dp,
                colors.borderCard,
                RoundedCornerShape(18.dp)
            )
            .clickable { onCardClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("geotag_overlay_card")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: AUTO badge + Location Name & Address
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // "AUTO" Badge with GPS satellite icon
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.accentSubtle)
                        .border(1.dp, colors.accent.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "🛰️",
                            fontSize = 9.sp
                        )
                        Text(
                            text = sourceBadgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.accent,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.testTag("badge_auto")
                        )
                    }
                }

                // Location Name & Address
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = locationName.ifEmpty { "Detected Location" },
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("geotag_location_name")
                    )

                    Text(
                        text = address.ifEmpty { "Resolving survey coordinates…" },
                        fontSize = 11.sp,
                        color = colors.textSecondary,
                        lineHeight = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("geotag_address")
                    )
                }
            }

            // Details: Latitude & Longitude prominently displayed in the accent color
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.accentSubtle.copy(alpha = 0.12f))
                    .border(1.dp, colors.accent.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
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
                            text = "LAT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textTertiary
                        )
                        Text(
                            text = latFormatted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = colors.accent,
                            modifier = Modifier.testTag("geotag_lat")
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(14.dp)
                            .width(1.dp)
                            .background(colors.borderCard)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "LONG",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textTertiary
                        )
                        Text(
                            text = lngFormatted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = colors.accent,
                            modifier = Modifier.testTag("geotag_lng")
                        )
                    }
                }
            }

            // Metadata: Date, Time, Accuracy (±11m), Note chip ("College")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date & Time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "📅 $dateText",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = colors.textTertiary
                    )
                    Text(
                        text = timeText,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary
                    )
                }

                // Accuracy and Custom Note chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Accuracy (±11m)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (colors.isDark) Color(0x24FFFFFF) else Color(0x14000000))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "🎯 $accuracyText",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary,
                            modifier = Modifier.testTag("geotag_accuracy")
                        )
                    }

                    // Custom Note Chip (e.g. "College")
                    if (noteText.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.accent.copy(alpha = 0.22f))
                                .border(0.8.dp, colors.accent, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "📝 $noteText",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.accent,
                                modifier = Modifier.testTag("geotag_note")
                            )
                        }
                    }
                }
            }

            // Footer: "Made with love"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Made with ❤️ • Pro GPS Survey Camera",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Normal,
                    color = colors.textTertiary
                )
            }
        }
    }
}
