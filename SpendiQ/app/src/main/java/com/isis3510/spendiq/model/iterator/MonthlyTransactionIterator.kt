package com.isis3510.spendiq.model.iterator

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.isis3510.spendiq.model.data.Transaction
import java.time.LocalDate
import java.time.ZoneId

class MonthlyTransactionIterator(private val transactions: List<Transaction>) : Iterator<Pair<Long, Long>> {
    private var currentIndex = 0
    private var currentMonthTotal = 0L
    private var previousMonthTotal = 0L

    override fun hasNext(): Boolean {
        return currentIndex < transactions.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun next(): Pair<Long, Long> {
        val transaction = transactions[currentIndex++]
        val transactionDate = transaction.dateTime.toDate().toInstant().atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        val currentDate = LocalDate.now()
        val currentMonth = currentDate.monthValue // Obtiene el mes actual (1-12)
        val previousMonth = if (currentMonth == 1) 12 else currentMonth - 1 // Calcular el mes anterior

        Log.d("MonthlyTransactionIterator", "current month: $currentMonth")
        Log.d("MonthlyTransactionIterator", "previous month: $previousMonth")
        Log.d("MonthlyTransactionIterator", "transaction month: $transactionDate.monthValue")


        Log.d("MonthlyTransactionIterator", "Entro2")
        if (transactionDate.monthValue == currentMonth && transaction.transactionType == "Expense") {
            Log.d("MonthlyTransactionIterator", "Entro3")
            currentMonthTotal += transaction.amount
        } else if (transactionDate.monthValue == previousMonth && transaction.transactionType == "Expense") {
            Log.d("MonthlyTransactionIterator", "Entro4")
            previousMonthTotal += transaction.amount
        }

        Log.d("MonthlyTransactionIterator", "currentMonthExpenses: $currentMonthTotal")
        Log.d("MonthlyTransactionIterator", "previousMonthExpenses: $previousMonthTotal")
        return Pair(currentMonthTotal, previousMonthTotal)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMonthlyExpenses(): Pair<Long, Long> {
        Log.d("MonthlyTransactionIterator", "Entro")
        while (hasNext()) {
            Log.d("MonthlyTransactionIterator", "Hello")
            next()
        }
        return Pair(currentMonthTotal, previousMonthTotal)
    }
}