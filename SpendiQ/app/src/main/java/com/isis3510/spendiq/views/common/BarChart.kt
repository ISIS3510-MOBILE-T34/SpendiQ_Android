package com.isis3510.spendiq.views.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

@Composable
fun BarChart(
    modifier: Modifier = Modifier,
    data: Map<String, Long>,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val maxAmount = data.values.maxOrNull() ?: 0L
    val maxBarHeight = if (maxAmount != 0L) maxAmount else 1L
    val barColors = if (isDarkTheme) {
        listOf(Color.Cyan, Color.Magenta)
    } else {
        listOf(Color(0xFFB3CB54), Color(0xFFE57373))
    }

    val sortedData = data.toList().sortedBy { it.first }

    Canvas(modifier = modifier) {
        val barWidth = size.width / (data.size * 2)
        var xPosition = barWidth / 2

        sortedData.forEach { (accountName, amount) ->
            val barHeightRatio = amount.toFloat() / maxBarHeight.toFloat()
            val barHeight = barHeightRatio * size.height
            drawRect(
                color = if (amount >= 0L) barColors[0] else barColors[1],
                topLeft = Offset(xPosition, size.height - barHeight),
                size = Size(barWidth, barHeight)
            )
            xPosition += barWidth * 2
        }
    }
}
