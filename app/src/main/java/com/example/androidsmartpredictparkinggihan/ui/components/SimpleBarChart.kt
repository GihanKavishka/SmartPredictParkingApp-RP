package com.example.androidsmartpredictparkinggihan.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.androidsmartpredictparkinggihan.ui.theme.ParkingChart1
import com.example.androidsmartpredictparkinggihan.ui.theme.ParkingChart2
import com.example.androidsmartpredictparkinggihan.ui.theme.ParkingChart3
import com.example.androidsmartpredictparkinggihan.ui.theme.ParkingChart4

@Composable
fun SimpleBarChart(
    title: String,
    data: List<Pair<String, Float>>,
    barColor: Color = ParkingChart1,
    modifier: Modifier = Modifier,
) {
    if (data.isEmpty()) {
        Text(text = "$title — no data yet", style = MaterialTheme.typography.bodyMedium)
        return
    }
    Text(text = title, style = MaterialTheme.typography.titleMedium)
    val max = data.maxOf { it.second }.coerceAtLeast(0.01f)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
    ) {
        val barWidth = size.width / (data.size * 1.6f)
        val gap = barWidth * 0.6f
        data.forEachIndexed { index, (_, value) ->
            val barHeight = (value / max) * (size.height - 24f)
            val left = index * (barWidth + gap) + gap
            drawRoundRect(
                color = barColor,
                topLeft = Offset(left, size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(8f, 8f),
            )
        }
    }
}

@Composable
fun MultiColorBarChart(
    title: String,
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
) {
    val colors = listOf(ParkingChart1, ParkingChart2, ParkingChart3, ParkingChart4)
    if (data.isEmpty()) {
        Text(text = "$title — no data yet", style = MaterialTheme.typography.bodyMedium)
        return
    }
    Text(text = title, style = MaterialTheme.typography.titleMedium)
    val max = data.maxOf { it.second }.coerceAtLeast(0.01f)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
    ) {
        val barWidth = size.width / (data.size * 1.6f)
        val gap = barWidth * 0.6f
        data.forEachIndexed { index, (_, value) ->
            val barHeight = (value / max) * (size.height - 24f)
            val left = index * (barWidth + gap) + gap
            drawRoundRect(
                color = colors[index % colors.size],
                topLeft = Offset(left, size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(8f, 8f),
            )
        }
    }
}
