package com.isis3510.spendiq.model.data

import androidx.compose.ui.graphics.Color

data class Account(
    val id: String,
    val name: String,
    val type: String,
    val amount: Long,
    val color: Color
)