package com.kgjr.safecircle.ui.layouts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.kgjr.safecircle.models.ArchiveLocationData
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.kgjr.safecircle.theme.primaryVariant
import java.util.Date
@Composable
fun BatterySparkline(
    data: List<ArchiveLocationData>
) {
    val batteryLevels = data.mapNotNull { it.battery }
    if (batteryLevels.isEmpty()) return

    val timeFormatter = remember {
        SimpleDateFormat("hh:mm a", Locale.getDefault())
    }

    val yLabelWidth = 50f

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(bottom = 32.dp, top = 8.dp, end = 8.dp)
    ) {
        val maxBattery = 100f
        val minBattery = 0f
        val graphHeight = size.height
        val graphWidth = size.width - yLabelWidth

        val validData = data.filter { it.battery != null }
        val pointCount = validData.size
        val widthPerPoint = graphWidth / (pointCount - 1).coerceAtLeast(1)

        val points = validData.mapIndexed { index, item ->
            val x = yLabelWidth + index * widthPerPoint
            val battery = item.battery!!.toFloat()
            val y = graphHeight - ((battery - minBattery) / (maxBattery - minBattery) * graphHeight)
            Offset(x, y)
        }

        val yLabelPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 22f
        }

        val ySteps = listOf(0, 25, 50, 75, 100)
        ySteps.forEach { value ->
            val y = graphHeight - (value / 100f * graphHeight)
            drawLine(
                color = Color.LightGray,
                start = Offset(yLabelWidth, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            drawContext.canvas.nativeCanvas.drawText(
                "$value%", yLabelWidth - 50f, y + 8f, yLabelPaint
            )
        }

        val xLabelPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 22f
            textAlign = android.graphics.Paint.Align.CENTER
        }

        val timestamps = validData.mapNotNull { it.timeStamp }
        if (timestamps.size >= 3) {
            val startTime = timeFormatter.format(Date(timestamps.first()))
            val midTime = timeFormatter.format(Date(timestamps[timestamps.size / 2]))
            val endTime = timeFormatter.format(Date(timestamps.last()))

            val yPos = graphHeight + 40f
            drawContext.canvas.nativeCanvas.apply {
                drawText(startTime, points.first().x, yPos, xLabelPaint)
                drawText(midTime, points[points.size / 2].x, yPos, xLabelPaint)
                drawText(endTime, points.last().x, yPos, xLabelPaint)
            }

            listOf(0, points.size / 2, points.lastIndex).forEach { i ->
                drawLine(
                    color = Color.LightGray,
                    start = Offset(points[i].x, 0f),
                    end = Offset(points[i].x, graphHeight),
                    strokeWidth = 1f
                )
            }
        }

        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }

        drawPath(
            path = path,
            color = primaryVariant,
            style = Stroke(width = 3f, cap = StrokeCap.Round)
        )

        points.forEach {
            drawCircle(
                color = primaryVariant,
                radius = 4f,
                center = it
            )
        }
    }
}
