package com.isis3510.spendiq.utils

import androidx.compose.ui.graphics.Color

/**
 * Determina si un color es claro.
 * @param color El color a evaluar.
 * @return `true` si el color es claro, `false` si es oscuro.
 */

fun isColorLight(color: Color): Boolean {
    val luminance = (0.299f * color.red + 0.587f * color.green + 0.114f * color.blue)
    return luminance > 0.5f
}
