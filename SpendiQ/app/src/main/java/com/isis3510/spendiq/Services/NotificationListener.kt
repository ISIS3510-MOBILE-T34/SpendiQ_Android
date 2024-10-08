package com.isis3510.spendiq.Services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class NotificationListener : NotificationListenerService() {

    private val firestore = FirebaseFirestore.getInstance()

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        sbn?.let {
            val notification = sbn.notification
            val extras = notification.extras

            Log.d("NotificationListener1", "Notification received from: ${sbn.packageName}")

            val title = extras.getString("android.title", "No title") ?: "No title"
            val text = extras.getCharSequence("android.text", "No text").toString()

            Log.d("NotificationListener3", "Notification Title: $title")
            Log.d("NotificationListener4", "Notification Text: $text")

            if (text.contains("content hidden", ignoreCase = true)) {
                Log.d("NotificationListener2", "Sensitive notification content is hidden. Unable to process.")
                return
            }

            if (title.startsWith("Compra aprobada por")) {
                GlobalScope.launch {
                    processExpenseTransaction(text)
                }
            } else if (title == "Nu") {
                GlobalScope.launch {
                    processIncomeTransaction(text)
                }
            } else {
                Log.d("NotificationListener5", "Notification does not match the required title for processing.")
            }
        }
    }

    private suspend fun processIncomeTransaction(text: String) {
        val userId = getCurrentUserId() ?: return

        val nuAccount = getNuAccount(userId)
        if (nuAccount == null) {
            Log.d("NotificationListener6", "Nu account not found, creating new account.")
            createNuAccount(userId)
        }

        val regex = Regex("([\\w\\s]+) te envio \\$([\\d,.]+) con motivo de ([\\w\\s]+)")
        val matchResult = regex.find(text)

        matchResult?.let {
            val company = matchResult.groupValues[1]
            var amountString = matchResult.groupValues[2].replace(".", "")

            if (amountString.contains(",")) {
                amountString = amountString.split(",")[0]
            }

            val amount = amountString.toLong()
            val currentTime = System.currentTimeMillis()

            if (!transactionExists(userId, company, amount, currentTime, "Income")) {
                Log.d("NotificationListener9", "Processing income from $company, amount: $amount")
                addTransaction(userId, amount, company, "Income")
                updateNuAccountBalance(userId, amount) // Add to account
            } else {
                Log.d("NotificationListener", "Duplicate income transaction detected. Skipping creation.")
            }
        } ?: run {
            Log.d("NotificationListener10", "Income transaction format not matched.")
        }
    }

    private suspend fun processExpenseTransaction(text: String) {
        val userId = getCurrentUserId() ?: return

        val nuAccount = getNuAccount(userId)
        if (nuAccount == null) {
            Log.d("NotificationListener6", "Nu account not found, creating new account.")
            createNuAccount(userId)
        }

        val regex = Regex("Tu compra en ([\\w\\*\\s]+) por \\$([\\d,.]+) con tu tarjeta terminada en ([\\d]+)")
        val matchResult = regex.find(text)

        matchResult?.let {
            val company = matchResult.groupValues[1]
            var amountString = matchResult.groupValues[2].replace(".", "")

            if (amountString.contains(",")) {
                amountString = amountString.split(",")[0]
            }

            val amount = amountString.toLong()
            val currentTime = System.currentTimeMillis()

            if (!transactionExists(userId, company, amount, currentTime, "Expense")) {
                Log.d("NotificationListener7", "Processing expense for $company, amount: $amount")
                addTransaction(userId, amount, company, "Expense")
                updateNuAccountBalance(userId, -amount)
            } else {
                Log.d("NotificationListener", "Duplicate expense transaction detected. Skipping creation.")
            }
        } ?: run {
            Log.d("NotificationListener8", "Expense transaction format not matched.")
        }
    }

    private suspend fun transactionExists(userId: String, transactionName: String, amount: Long, dateTime: Long, transactionType: String): Boolean {
        return try {
            val snapshot = firestore.collection("accounts")
                .whereEqualTo("name", "Nu")
                .whereEqualTo("user_id", userId)
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                val accountId = snapshot.documents[0].id
                val transactionSnapshot = firestore.collection("accounts")
                    .document(accountId)
                    .collection("transactions")
                    .whereEqualTo("transactionName", transactionName)
                    .whereEqualTo("amount", amount)
                    .whereEqualTo("transactionType", transactionType)
                    .whereGreaterThan("dateTime", dateTime - 60000)
                    .whereLessThan("dateTime", dateTime + 60000)
                    .get()
                    .await()

                return transactionSnapshot.documents.isNotEmpty()
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("NotificationListener", "Error checking transaction existence: ${e.message}")
            false
        }
    }

    private suspend fun getNuAccount(userId: String): Map<String, Any>? {
        return try {
            val snapshot = firestore.collection("accounts")
                .whereEqualTo("name", "Nu")
                .whereEqualTo("user_id", userId)
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                snapshot.documents[0].data
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("NotificationListener", "Error fetching Nu account: ${e.message}")
            null
        }
    }

    private suspend fun createNuAccount(userId: String) {
        try {
            firestore.collection("accounts").add(
                mapOf(
                    "amount" to 0L,
                    "name" to "Nu",
                    "user_id" to userId
                )
            ).await()
            Log.d("NotificationListener", "Nu account created successfully.")
        } catch (e: Exception) {
            Log.e("NotificationListener", "Error creating Nu account: ${e.message}")
        }
    }

    private suspend fun addTransaction(userId: String, amount: Long, transactionName: String, transactionType: String) {
        val currentTime = System.currentTimeMillis()

        try {
            firestore.collection("accounts")
                .whereEqualTo("name", "Nu")
                .whereEqualTo("user_id", userId)
                .get()
                .await()
                .documents.firstOrNull()?.let { account ->
                    firestore.collection("accounts")
                        .document(account.id)
                        .collection("transactions")
                        .add(
                            mapOf(
                                "amount" to amount,
                                "dateTime" to currentTime,
                                "accountID" to account.id,
                                "transactionName" to transactionName,
                                "transactionType" to transactionType
                            )
                        ).await()
                    Log.d("NotificationListener", "Transaction added: $transactionName, Amount: $amount")
                }
        } catch (e: Exception) {
            Log.e("NotificationListener", "Error adding transaction: ${e.message}")
        }
    }

    private suspend fun updateNuAccountBalance(userId: String, amountDelta: Long) {
        try {
            firestore.collection("accounts")
                .whereEqualTo("name", "Nu")
                .whereEqualTo("user_id", userId)
                .get()
                .await()
                .documents.firstOrNull()?.let { account ->
                    val currentAmount = account.getLong("amount") ?: 0L
                    val newAmount = currentAmount + amountDelta
                    firestore.collection("accounts")
                        .document(account.id)
                        .update("amount", newAmount)
                        .await()
                    Log.d("NotificationListener", "Nu account balance updated by $amountDelta. New balance: $newAmount")
                }
        } catch (e: Exception) {
            Log.e("NotificationListener", "Error updating account balance: ${e.message}")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        Log.d("NotificationListener", "Notification removed: ${sbn?.packageName}")
    }

    private fun getCurrentUserId(): String? {
        val user = FirebaseAuth.getInstance().currentUser
        return user?.uid
    }
}
