package com.isis3510.spendiq.model.iterator

import android.os.Build
import androidx.annotation.RequiresApi
import com.isis3510.spendiq.model.data.DailyTransaction
import com.isis3510.spendiq.model.data.Transaction
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Locale

class TransactionIterator(private val transactions: List<Transaction>) : Iterator<DailyTransaction> {
    private var currentIndex = 0
    private val dailyTransactions = mutableMapOf<String, Double>()
    @RequiresApi(Build.VERSION_CODES.O)
    private val thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS).toEpochMilli()

    override fun hasNext(): Boolean {
        return currentIndex < transactions.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun next(): DailyTransaction {
        val transaction = transactions[currentIndex++]
        val transactionDate = transaction.dateTime.toDate()
        val dateString = SimpleDateFormat("dd-MM", Locale.getDefault()).format(transactionDate)
        if (transaction.dateTime.toDate().time >= thirtyDaysAgo) {

            // Acumular el monto en el mapa
            val amount = if (transaction.transactionType == "Income") {
                transaction.amount.toDouble() // Positivo para ingresos
            } else {
                -transaction.amount.toDouble() // Negativo para gastos
            }

            // Sumar el monto al día correspondiente
            dailyTransactions[dateString] = dailyTransactions.getOrDefault(dateString, 0.0) + amount
        }
        return DailyTransaction(dateString, dailyTransactions[dateString] ?: 0.0)
    }

    fun getDailyTransactions(): List<DailyTransaction> {
        return dailyTransactions.map { DailyTransaction(it.key, it.value) } // Convertir el mapa a una lista
    }
}