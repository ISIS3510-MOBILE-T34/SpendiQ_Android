package com.isis3510.spendiq.model.data

import com.google.firebase.Timestamp

/**
 * Data class representing a financial transaction in the application.
 *
 * This class holds information about a transaction, including its unique identifier,
 * associated account, amount, type, and other relevant details.
 *
 * @property id Unique identifier for the transaction.
 * @property accountId The ID of the account associated with the transaction.
 * @property transactionName The name or description of the transaction.
 * @property amount The amount of money involved in the transaction.
 * @property dateTime The timestamp of when the transaction occurred.
 * @property transactionType The type of transaction (e.g., "Income", "Expense").
 * @property location The geographical location associated with the transaction, if available.
 * @property amountAnomaly Flag indicating if there is an anomaly with the amount (true if there's an anomaly).
 * @property locationAnomaly Flag indicating if there is an anomaly with the location (true if there's an anomaly).
 * @property automatic Flag indicating if the transaction was created automatically (true if automatic).
 */
data class Transaction(
    val id: String, // Unique identifier for the transaction
    val accountId: String, // ID of the account associated with the transaction
    val transactionName: String, // Name or description of the transaction
    val amount: Long, // Amount of money involved in the transaction
    val dateTime: Timestamp, // Timestamp of when the transaction occurred
    val transactionType: String, // Type of transaction (e.g., "Income", "Expense")
    val location: Location?, // Geographical location associated with the transaction, if available
    val amountAnomaly: Boolean = false, // Indicates if there is an anomaly with the amount
    val locationAnomaly: Boolean = false, // Indicates if there is an anomaly with the location
    val automatic: Boolean = false // Indicates if the transaction was created automatically
)

/**
 * Data class representing a geographical location.
 *
 * This class holds latitude and longitude coordinates for a specific location.
 *
 * @property latitude The latitude of the location.
 * @property longitude The longitude of the location.
 */
data class Location(
    val latitude: Double, // Latitude of the location
    val longitude: Double // Longitude of the location
)
