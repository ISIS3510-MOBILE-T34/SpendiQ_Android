package com.isis3510.spendiq.model.data

data class User(
    val id: String,
    val email: String,
    val fullName: String? = null,
    val phoneNumber: String? = null,
    val birthDate: String? = null,
    val profileImageUrl: String? = null
)