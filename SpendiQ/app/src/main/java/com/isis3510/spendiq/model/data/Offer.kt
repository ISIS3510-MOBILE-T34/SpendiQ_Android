package com.isis3510.spendiq.model.data

/**
 * Data class representing a special offer available at a specific place.
 *
 * This class contains information about an offer, including its location,
 * description, and other related details.
 *
 * @property id Unique identifier for the offer, typically assigned by the database.
 * @property placeName The name of the place where the offer is available.
 * @property offerDescription A detailed description of the offer being made.
 * @property shopImage URL or identifier for an image representing the shop.
 * @property recommendationReason Reason why this offer is recommended to the user.
 * @property latitude Latitude coordinate of the offer's location.
 * @property longitude Longitude coordinate of the offer's location.
 * @property distance The distance from the user's current location to the offer's location, in meters.
 */
data class Offer(
    val id: String? = null, // Unique identifier for the offer, default is null
    val placeName: String? = null, // Name of the place where the offer is available, default is null
    val offerDescription: String? = null, // Description of the offer, default is null
    val shopImage: String? = null, // Image URL for the shop, default is null
    val recommendationReason: String? = null, // Reason for recommending this offer, default is null
    val latitude: Double? = null, // Latitude of the offer's location, default is null
    val longitude: Double? = null, // Longitude of the offer's location, default is null
    val distance: Int? = null, // Distance to the offer's location from the user, default is null
    val featured: Boolean = false // Indicates if the offer is featured, default is false
)
