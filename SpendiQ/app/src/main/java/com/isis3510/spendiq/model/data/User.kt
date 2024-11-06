package com.isis3510.spendiq.model.data

/**
 * Data class representing a user in the application.
 *
 * This class holds information about the user, including their unique identifier,
 * email address, personal details, and profile image URL.
 *
 * @property id Unique identifier for the user, typically their Firebase UID.
 * @property email The email address of the user.
 * @property fullName The full name of the user. Optional property.
 * @property phoneNumber The phone number of the user. Optional property.
 * @property birthDate The birth date of the user in a String format (e.g., "YYYY-MM-DD"). Optional property.
 * @property profileImageUrl URL to the user's profile image. Optional property.
 */
data class User(
    val id: String, // Unique identifier for the user
    val email: String, // The user's email address
    val fullName: String? = null, // The user's full name, optional
    val phoneNumber: String? = null, // The user's phone number, optional
    val birthDate: String? = null, // The user's birth date, optional
    val profileImageUrl: String? = null // URL to the user's profile image, optional
)
