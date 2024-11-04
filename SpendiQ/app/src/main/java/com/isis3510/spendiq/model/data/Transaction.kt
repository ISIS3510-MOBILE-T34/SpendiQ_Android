package com.isis3510.spendiq.model.data

import com.google.firebase.Timestamp

data class Transaction(
    val id: String,
    val accountId: String,
    val transactionName: String,
    val amount: Long,
    val dateTime: Timestamp,
    val transactionType: String,
    val location: Location?,
    val amountAnomaly: Boolean = false,
    val locationAnomaly: Boolean = false,
    val automatic: Boolean = false
)

data class Location(
    val latitude: Double,
    val longitude: Double
)
