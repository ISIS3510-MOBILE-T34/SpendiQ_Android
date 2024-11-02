package com.isis3510.spendiq.views.common

import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie


@Composable
fun CreatePieChart(data: List<Pair<String, Long>>) {
    Log.d("PieChart", "$data")

    // Convertir los datos a la estructura requerida por ComposeChart
    var pieData = data.map { (label, value) ->
        Pie(label = label,
            data = value.toDouble(),
            color = getColorForLabel(label),
            selectedColor = Color.Red) // Cambiar el color seleccionado según sea necesario
    }

    PieChart(
        modifier = Modifier.size(170.dp),
        data = pieData,
        onPieClick = {
            val pieIndex = pieData.indexOf(it)
            pieData = pieData.mapIndexed { mapIndex, pie -> pie.copy(selected = pieIndex == mapIndex) }
        },
        selectedScale = 1.2f,
        spaceDegree = 4f,
        selectedPaddingDegree = 2f,
        style = Pie.Style.Stroke(width = 42.dp)
    )
}

private fun getColorForLabel(label: String): Color {
    return when (label) {
        "Income" -> Color(0xffb3cb54)
        "Expenses" -> Color(0xffc33ba5)
        else -> Color.Gray
    }
}