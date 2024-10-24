package com.isis3510.spendiq.model.data

data class Offer(
    val id: String? = null, // Added ID for navigation purposes
    val distance: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val offerDescription: String? = null,
    val placeName: String? = null,
    val recommendationReason: String? = null,
    val shopImage: String? = null
)
