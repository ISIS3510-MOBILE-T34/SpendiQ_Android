package com.isis3510.spendiq.utils

import com.isis3510.spendiq.model.data.Offer
import com.isis3510.spendiq.model.local.database.DatabaseOffer

// Convert DatabaseOffer to Offer
fun DatabaseOffer.toDomainModel(): Offer {
    return Offer(
        id = this.id,
        placeName = this.placeName,
        offerDescription = this.offerDescription,
        shopImage = this.shopImage, // Already nullable
        recommendationReason = this.recommendationReason, // Already nullable
        latitude = this.latitude, // Non-nullable, directly assigned
        longitude = this.longitude, // Non-nullable, directly assigned
        distance = this.distance.toInt() // Convert Float to Int
    )
}


// Convert Offer to DatabaseOffer
fun Offer.toDatabaseModel(): DatabaseOffer {
    return DatabaseOffer(
        id = this.id ?: "", // Provide default value if null
        placeName = this.placeName ?: "Unknown Place", // Default if null
        offerDescription = this.offerDescription ?: "No description available", // Default if null
        shopImage = this.shopImage, // Already nullable
        recommendationReason = this.recommendationReason, // Already nullable
        latitude = this.latitude ?: 0.0, // Default to 0.0 if null
        longitude = this.longitude ?: 0.0, // Default to 0.0 if null
        distance = this.distance?.toFloat() ?: 0.0f // Convert Int to Float, default to 0.0f if null
    )
}

