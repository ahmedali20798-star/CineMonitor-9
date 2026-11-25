package com.canon.multicampro.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canon.multicampro.ui.theme.CanonCrimson
import com.canon.multicampro.ui.theme.StudioBorder
import com.canon.multicampro.ui.theme.StudioIvory
import com.canon.multicampro.ui.theme.StudioObsidian
import com.canon.multicampro.ui.theme.StudioSlate
import com.canon.multicampro.ui.theme.StudioSurfaceElevated
import com.canon.multicampro.ui.theme.TelemetryCyan

@Composable
fun StudioBrandBadge(
    isAnyRecording: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StudioViewfinderMark(
            isAnyRecording = isAnyRecording,
            modifier = Modifier.size(34.dp)
        )
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "CANON MULTICAM",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = StudioIvory
                )
                Box(
                    modifier = Modifier
                        .background(StudioSurfaceElevated, RoundedCornerShape(4.dp))
                        .border(1.dp, StudioBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "PRO v3.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = TelemetryCyan
                    )
                }
            }
            Text(
                text = "DARK STUDIO PRO • DIRECTOR CONSOLE",
                style = MaterialTheme.typography.labelSmall,
                color = StudioSlate
            )
        }
    }
}

@Composable
fun StudioViewfinderMark(
    isAnyRecording: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val pad = w * 0.08f
        val bracket = w * 0.22f
        val strokePx = w * 0.065f

        drawRoundRect(
            color = StudioObsidian,
            cornerRadius = CornerRadius(w * 0.18f, h * 0.18f)
        )

        val corners = listOf(
            Pair(Offset(pad, pad + bracket), Offset(pad, pad)) to Offset(pad + bracket, pad),
            Pair(Offset(w - pad - bracket, pad), Offset(w - pad, pad)) to Offset(w - pad, pad + bracket),
            Pair(Offset(w - pad, h - pad - bracket), Offset(w - pad, h - pad)) to Offset(w - pad - bracket, h - pad),
            Pair(Offset(pad + bracket, h - pad), Offset(pad, h - pad)) to Offset(pad, h - pad - bracket)
        )
        corners.forEach { (seg1, end2) ->
            drawLine(StudioIvory, seg1.first, seg1.second, strokeWidth = strokePx, cap = StrokeCap.Round)
            drawLine(StudioIvory, seg1.second, end2, strokeWidth = strokePx, cap = StrokeCap.Round)
        }

        val gap = w * 0.06f
        val innerStart = w * 0.23f
        val cell = (w - innerStart * 2f - gap) / 2f
        val positions = listOf(
            Offset(innerStart, innerStart),
            Offset(innerStart + cell + gap, innerStart),
            Offset(innerStart, innerStart + cell + gap),
            Offset(innerStart + cell + gap, innerStart + cell + gap)
        )
        positions.forEachIndexed { index, topLeft ->
            drawRoundRect(
                color = if (index == 0) TelemetryCyan else StudioBorder,
                topLeft = topLeft,
                size = Size(cell, cell),
                cornerRadius = CornerRadius(3f, 3f),
                style = Stroke(width = w * 0.035f)
            )
        }

        drawCircle(
            color = if (isAnyRecording) CanonCrimson else TelemetryCyan,
            radius = w * 0.085f,
            center = Offset(w * 0.5f, h * 0.5f)
        )
    }
}
