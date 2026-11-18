package com.gswift.relay.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gswift.relay.data.util.BankDraftEngine

@Composable
fun NativeReceiptPreview(
    html: String,
    subject: String = "",
    compactMobileMode: Boolean = true,
    modifier: Modifier = Modifier
) {
    val receipt = remember(html, subject) {
        BankDraftEngine.parseReceiptPreview(html, subject)
    }
    val uriHandler = LocalUriHandler.current
    val bankColor = Color(receipt.bank.colorHex)
    val btnColor = Color(receipt.bank.buttonColorHex)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(if (compactMobileMode) Color(0xFFE2E8F0).copy(alpha = 0.45f) else Color(0xFFF8FAFC))
            .padding(if (compactMobileMode) 10.dp else 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(if (compactMobileMode) 0.94f else 1f)
        ) {
            if (compactMobileMode) {
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PhoneAndroid,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Gmail / Mail App",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                        }
                        Text(
                            text = "100% Fluid",
                            fontSize = 9.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Surface(
                color = Color.White,
                shape = if (compactMobileMode) {
                    RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                } else {
                    RoundedCornerShape(16.dp)
                },
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Bank Header Banner with Official White Logo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bankColor)
                            .padding(vertical = 18.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = receipt.bank.whiteLogoRes),
                            contentDescription = "Logo ${receipt.bank.label}",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .height(36.dp)
                                .fillMaxWidth(0.55f)
                        )
                    }

                    // Body Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Status Badge
                        Surface(
                            color = Color(0xFFECFDF5),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                        ) {
                            Text(
                                text = receipt.statusBadge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF047857),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        // Heading
                        Text(
                            text = receipt.heading,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 22.sp,
                            color = Color(0xFF0F172A)
                        )

                        // Intro Paragraph
                        if (receipt.introText.isNotBlank()) {
                            Text(
                                text = receipt.introText,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        // Key-Value Receipt Table or Plain Fallback Text
                        if (receipt.rows.isNotEmpty()) {
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    receipt.rows.forEachIndexed { idx, (label, value) ->
                                        val isStatus = label.contains("Status", ignoreCase = true)
                                        val isRef = label.contains("Referensi", ignoreCase = true) ||
                                            label.contains("Ref", ignoreCase = true)
                                        val isAmount = label.contains("Nominal", ignoreCase = true) ||
                                            label.contains("Total", ignoreCase = true) ||
                                            value.startsWith("Rp", ignoreCase = true)

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 11.5.sp,
                                                color = Color(0xFF64748B),
                                                modifier = Modifier.weight(0.44f),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = value,
                                                fontSize = if (isAmount) 14.sp else 12.sp,
                                                fontWeight = when {
                                                    isAmount || isStatus -> FontWeight.ExtraBold
                                                    isRef -> FontWeight.Bold
                                                    else -> FontWeight.SemiBold
                                                },
                                                fontFamily = if (isRef) FontFamily.Monospace else FontFamily.Default,
                                                color = when {
                                                    isStatus -> Color(0xFF059669)
                                                    isRef -> bankColor
                                                    isAmount -> Color(0xFF0F172A)
                                                    else -> Color(0xFF1E293B)
                                                },
                                                textAlign = TextAlign.End,
                                                modifier = Modifier.weight(0.56f),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        if (idx < receipt.rows.lastIndex) {
                                            HorizontalDivider(color = Color(0xFFE2E8F0).copy(alpha = 0.6f))
                                        }
                                    }
                                }
                            }
                        } else if (receipt.fallbackText.isNotBlank()) {
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = receipt.fallbackText.take(1500),
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = Color(0xFF334155),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        // Primary CTA Button
                        if (!receipt.ctaLabel.isNullOrBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Button(
                                onClick = {
                                    val url = receipt.ctaUrl
                                    if (!url.isNullOrBlank()) {
                                        runCatching { uriHandler.openUri(url) }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            ) {
                                Text(
                                    text = receipt.ctaLabel,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (!receipt.ctaUrl.isNullOrBlank()) {
                                    Spacer(Modifier.size(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.9f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Footer Copyright
                    val footer = receipt.footerText ?: "© 2026 ${receipt.bank.fullName}. Seluruh hak cipta dilindungi."
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9))
                            .border(0.5.dp, Color(0xFFE2E8F0))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = footer,
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp,
    strokeWidth: Dp,
    dashLength: Float,
    gapLength: Float
): Modifier = drawBehind {
    val stroke = Stroke(
        width = strokeWidth.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength, gapLength), 0f)
    )
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
        style = stroke
    )
}
