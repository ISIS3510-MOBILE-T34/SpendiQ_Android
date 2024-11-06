// model/FinancialData.kt
package com.isis3510.spendiq.model


data class Account(
    val id: String = "",
    val name: String = "",
    val userId: String = "",
    val amount: Long = 0L // Si este campo es necesario
)

data class Transaction(
    val id: String = "",
    val accountId: String = "",
    val amount: Long = 0L, // Usar Long si es consistente con el resto del código
    val type: String = "" // Asegúrate de que este sea el nombre correcto
)

