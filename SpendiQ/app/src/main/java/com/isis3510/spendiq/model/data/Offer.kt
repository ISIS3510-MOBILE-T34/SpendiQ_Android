package com.isis3510.spendiq.model.data

data class Offer(
    val id: String? = null,
    val placeName: String? = null,
    val offerDescription: String? = null,
    val shopImage: String? = null,
    val recommendationReason: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)