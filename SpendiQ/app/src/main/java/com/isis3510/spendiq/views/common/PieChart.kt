package com.isis3510.spendiq.views.common

import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie

/**
 * CreatePieChart composable function
 *
 * Displays a pie chart visualization using provided data. This component leverages a third-party
 * library, `ComposeChart`, to render a customizable pie chart with selectable slices.
 *
 * Features:
 * - Converts input data into a pie chart format suitable for ComposeChart.
 * - Allows individual slices of the chart to be highlighted when clicked.
 * - Customizable colors for each data label, with specific colors for "Income" and "Expenses".
 *
 * Functionality:
 * - `CreatePieChart`: Takes a list of data pairs (`List<Pair<String, Long>>`) representing labels
 *   and values, converts them to `Pie` objects, and renders them in a pie chart.
 * - Slices in the chart are selectable, with the clicked slice visually enlarged.
 *
 * Supporting Function:
 * - `getColorForLabel`: Maps each label to a specific color, providing consistent color coding
 *   for "Income" and "Expenses" labels.
 *
 * @param data A list of pairs with each containing a label (`String`) and its associated value (`Long`).
 */

@Composable
fun CreatePieChart(data: List<Pair<String, Long>>) {
    Log.d("PieChart", "$data") // Log the data for debugging purposes

    // Convert the input data to a format compatible with ComposeChart's PieChart model
    var pieData = data.map { (label, value) ->
        Pie(
            label = label,
            data = value.toDouble(),
            color = getColorForLabel(label), // Color based on label
            selectedColor = Color.Red       // Highlight color for selected slice
        )
    }

    // Render the PieChart with customization for size, spacing, and styling
    PieChart(
        modifier = Modifier.size(170.dp), // Chart size
        data = pieData,                   // Pie chart data
        onPieClick = {                    // Handles pie slice selection
            val pieIndex = pieData.indexOf(it) // Get index of clicked slice
            // Update pieData to set the clicked slice as selected
            pieData = pieData.mapIndexed { mapIndex, pie ->
                pie.copy(selected = pieIndex == mapIndex)
            }
        },
        selectedScale = 1.2f,             // Scale of selected slice
        spaceDegree = 4f,                 // Space between slices in degrees
        selectedPaddingDegree = 2f,       // Padding around selected slice
        style = Pie.Style.Stroke(width = 42.dp) // Stroke style with 42dp width
    )
}

/**
 * Maps specific labels to predefined colors for the pie chart slices.
 * - "Income" -> Green color
 * - "Expenses" -> Pink color
 * - Default color is Gray for other labels
 *
 * @param label A label for the pie slice.
 * @return A [Color] corresponding to the label.
 */
private fun getColorForLabel(label: String): Color {
    return when (label) {
        "Income" -> Color(0xffb3cb54)    // Green color for income
        "Expenses" -> Color(0xffc33ba5)  // Pink color for expenses
        else -> Color.Gray               // Default gray color
    }
}
