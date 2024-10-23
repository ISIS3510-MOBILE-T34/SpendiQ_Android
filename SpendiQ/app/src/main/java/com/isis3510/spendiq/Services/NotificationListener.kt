package com.isis3510.spendiq.Services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.spendiq.R
import com.isis3510.spendiq.services.LocationService
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class NotificationListener : NotificationListenerService() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var locationService: LocationService
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        locationService = LocationService(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        sbn?.let {
            val notification = sbn.notification
            val extras = notification.extras

            Log.d("NotificationListener", "Notification received from: ${sbn.packageName}")

            val title = extras.getString("android.title", "No title") ?: "No title"
            val text = extras.getCharSequence("android.text", "No text").toString()

            Log.d("NotificationListener", "Notification Title: $title")
            Log.d("NotificationListener", "Notification Text: $text")

            if (text.contains("content hidden", ignoreCase = true)) {
                Log.d("NotificationListener", "Sensitive notification content is hidden. Unable to process.")
                return
            }

            when {
                title.startsWith("Compra aprobada por") -> {
                    coroutineScope.launch {
                        processExpenseTransaction(text)
                    }
                }
                title == "Nu" -> {
                    coroutineScope.launch {
                        processIncomeTransaction(text)
                    }
                }
                else -> {
                    Log.d("NotificationListener", "Notification does not match the required title for processing.")
                }
            }
        }
    }

    private suspend fun processIncomeTransaction(text: String) {
        val userId = getCurrentUserId() ?: return

        val nuAccount = getNuAccount(userId)
        if (nuAccount == null) {
            Log.d("NotificationListener", "Nu account not found, creating new account.")
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
            val currentTimestamp = Timestamp.now()

            // Retrieve location before proceeding with transaction
            val location = locationService.getCurrentLocation()

            if (!transactionExists(userId, company, amount, currentTimestamp, "Income")) {
                Log.d("NotificationListener", "Processing income from $company, amount: $amount")
                addTransaction(userId, amount, company, "Income", location)
                updateNuAccountBalance(userId, amount)
                showNotification("Income Recorded", "Income of $$amount from $company has been recorded.")
            } else {
                Log.d("NotificationListener", "Duplicate income transaction detected. Skipping creation.")
            }
        } ?: run {
            Log.d("NotificationListener", "Income transaction format not matched.")
        }
    }

    private suspend fun processExpenseTransaction(text: String) {
        val userId = getCurrentUserId() ?: return

        val nuAccount = getNuAccount(userId)
        if (nuAccount == null) {
            Log.d("NotificationListener", "Nu account not found, creating new account.")
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
            val currentTimestamp = Timestamp.now()

            // Retrieve location before proceeding with transaction
            val location = locationService.getCurrentLocation()

            if (!transactionExists(userId, company, amount, currentTimestamp, "Expense")) {
                Log.d("NotificationListener", "Processing expense for $company, amount: $amount")
                addTransaction(userId, amount, company, "Expense", location)
                updateNuAccountBalance(userId, -amount)
                showNotification("Expense Recorded", "Expense of $$amount to $company has been recorded.")
            } else {
                Log.d("NotificationListener", "Duplicate expense transaction detected. Skipping creation.")
            }
        } ?: run {
            Log.d("NotificationListener", "Expense transaction format not matched.")
        }
    }

    private suspend fun transactionExists(userId: String, transactionName: String, amount: Long, timestamp: Timestamp, transactionType: String): Boolean {
        return try {
            val startTime = Timestamp(timestamp.seconds - 60, timestamp.nanoseconds)
            val endTime = Timestamp(timestamp.seconds + 60, timestamp.nanoseconds)

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
                    .whereGreaterThan("dateTime", startTime)
                    .whereLessThan("dateTime", endTime)
                    .get()
                    .await()

                transactionSnapshot.documents.isNotEmpty()
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

    private suspend fun addTransaction(userId: String, amount: Long, transactionName: String, transactionType: String, location: android.location.Location?) {
        try {
            val accountSnapshot = firestore.collection("accounts")
                .whereEqualTo("name", "Nu")
                .whereEqualTo("user_id", userId)
                .get()
                .await()

            if (accountSnapshot.documents.isNotEmpty()) {
                val accountId = accountSnapshot.documents[0].id
                val transaction = hashMapOf(
                    "amount" to amount,
                    "dateTime" to Timestamp.now(),
                    "accountID" to accountId,
                    "transactionName" to transactionName,
                    "transactionType" to transactionType,
                    "location" to if (location != null) {
                        hashMapOf(
                            "latitude" to location.latitude,
                            "longitude" to location.longitude
                        )
                    } else null
                )

                firestore.collection("accounts")
                    .document(accountId)
                    .collection("transactions")
                    .add(transaction)
                    .await()
                Log.d("NotificationListener", "Transaction added: $transactionName, Amount: $amount")
            } else {
                Log.e("NotificationListener", "No Nu account found for user: $userId")
            }
        } catch (e: Exception) {
            Log.e("NotificationListener", "Error adding transaction: ${e.message}")
        }
    }

    private suspend fun updateNuAccountBalance(userId: String, amountDelta: Long) {
        try {
            val accountSnapshot = firestore.collection("accounts")
                .whereEqualTo("name", "Nu")
                .whereEqualTo("user_id", userId)
                .get()
                .await()

            if (accountSnapshot.documents.isNotEmpty()) {
                val accountId = accountSnapshot.documents[0].id
                val currentAmount = accountSnapshot.documents[0].getLong("amount") ?: 0L
                val newAmount = currentAmount + amountDelta

                firestore.collection("accounts")
                    .document(accountId)
                    .update("amount", newAmount)
                    .await()
                Log.d("NotificationListener", "Nu account balance updated by $amountDelta. New balance: $newAmount")
            } else {
                Log.e("NotificationListener", "No Nu account found for user: $userId")
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

    private fun showNotification(title: String, content: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "SpendiQ_Channel"
        val channelName = "SpendiQ Notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.notification)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}