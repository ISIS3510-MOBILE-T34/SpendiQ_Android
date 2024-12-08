package com.isis3510.spendiq.model.data

import androidx.compose.ui.graphics.Color

/**
 * Data class representing a financial account.
 *
 * This class contains information about a user's account, including its name,
 * type, balance, and color for UI representation.
 *
 * @property id Unique identifier for the account, typically assigned by the database.
 * @property name The name of the account (e.g., "Bank", "Cash", etc.).
 * @property type The type of the account (e.g., "Savings", "Checking", etc.).
 * @property amount The current balance of the account, represented as a long integer.
 * @property color A Color object representing the color associated with this account for UI display.
 */
data class Account(
    val id: String, // Unique identifier for the account
    val name: String, // Name of the account
    val type: String, // Type of the account
    val amount: Long, // Current balance of the account
    val color: Color, // Color representation for the account in the UI
    val lastUsed: Long
)
