package com.isis3510.spendiq.model.data

import java.util.Date

data class Promo(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val discountCode: String,
    val restaurantName: String,
    val expirationDate: Date
)