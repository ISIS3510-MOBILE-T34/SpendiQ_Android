package com.isis3510.spendiq.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.spendiq.R
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

/**
 * NotificationListener is a service that listens for notifications from other apps.
 * It processes notifications related to transactions and updates the user's account accordingly.
 */
class NotificationListener : NotificationListenerService() {

    private val firestore = FirebaseFirestore.getInstance() // Firestore instance for database operations
    private lateinit var locationService: LocationService // Service to handle location-related tasks
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()) // Coroutine scope for background tasks

    override fun onCreate() {
        super.onCreate()
        locationService = LocationService(this) // Initialize location service
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel() // Cancel all coroutines when the service is destroyed
    }

    /**
     * This method is called when a notification is posted.
     * It checks the notification's title and text, and processes transactions accordingly.
     */
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        sbn?.let {
            val notification = sbn.notification
            val extras = notification.extras

            val title = extras.getString("android.title", "No title") ?: "No title"
            val text = extras.getCharSequence("android.text", "No text").toString()

            // Skip processing if the notification content is hidden
            if (text.contains("content hidden", ignoreCase = true)) return

            // Process the notification based on its title
            when {
                title.startsWith("Compra aprobada por") -> {
                    coroutineScope.launch { processExpenseTransaction(text) } // Process as an expense transaction
                }
                title == "Nu" -> {
                    coroutineScope.launch { processIncomeTransaction(text) } // Process as an income transaction
                }

                else -> {}
            }
        }
    }

    /**
     * Processes incoming transaction notifications related to income.
     * Extracts relevant details and adds the transaction to the user's account.
     */
    private suspend fun processIncomeTransaction(text: String) {
        val userId = getCurrentUserId() ?: return // Get the current user's ID

        val nuAccount = getNuAccount(userId) // Get or create Nu account
        if (nuAccount == null) createNuAccount(userId)

        // Regex to extract company name and amount from notification text
        val regex = Regex("([\\w\\s]+) te envio \\$([\\d,.]+) con motivo de ([\\w\\s]+)")
        val matchResult = regex.find(text)

        matchResult?.let {
            val company = matchResult.groupValues[1] // Extract company name
            var amountString = matchResult.groupValues[2].replace(".", "") // Clean amount string

            // Handle comma in amount string
            if (amountString.contains(",")) {
                amountString = amountString.split(",")[0]
            }

            val amount = amountString.toLong() // Parse amount to Long
            val currentTimestamp = Timestamp.now() // Get current timestamp

            // Retrieve location before proceeding with the transaction
            val location = locationService.getCurrentLocation()

            // Check if the transaction already exists
            if (!transactionExists(userId, company, amount, currentTimestamp, "Income")) {
                addTransaction(userId, amount, company, "Income", location, automatic = true) // Add transaction
                updateNuAccountBalance(userId, amount) // Update account balance
            }
        }
    }

    /**
     * Processes outgoing transaction notifications related to expenses.
     * Extracts relevant details and adds the transaction to the user's account.
     */
    private suspend fun processExpenseTransaction(text: String) {
        val userId = getCurrentUserId() ?: return // Get the current user's ID

        val nuAccount = getNuAccount(userId) // Get or create Nu account
        if (nuAccount == null) createNuAccount(userId)

        // Regex to extract company name and amount from notification text
        val regex = Regex("Tu compra en ([\\w\\s]+) por \\$([\\d,.]+) con tu tarjeta terminada en ([\\d]+)")
        val matchResult = regex.find(text)

        matchResult?.let {
            val company = matchResult.groupValues[1] // Extract company name
            var amountString = matchResult.groupValues[2].replace(".", "") // Clean amount string

            // Handle comma in amount string
            if (amountString.contains(",")) {
                amountString = amountString.split(",")[0]
            }

            val amount = amountString.toLong() // Parse amount to Long
            val currentTimestamp = Timestamp.now() // Get current timestamp

            // Retrieve location before proceeding with the transaction
            val location = locationService.getCurrentLocation()

            // Check if the transaction already exists
            if (!transactionExists(userId, company, amount, currentTimestamp, "Expense")) {
                addTransaction(userId, amount, company, "Expense", location, automatic = true) // Add transaction
                updateNuAccountBalance(userId, -amount) // Update account balance
            }
        }
    }

    /**
     * Checks if a transaction already exists for the given parameters to avoid duplicates.
     *
     * @return True if the transaction exists, otherwise false.
     */
    private suspend fun transactionExists(userId: String, transactionName: String, amount: Long, timestamp: Timestamp, transactionType: String): Boolean {
        return try {
            val startTime = Timestamp(timestamp.seconds - 60, timestamp.nanoseconds) // Set a time window of 60 seconds before
            val endTime = Timestamp(timestamp.seconds + 60, timestamp.nanoseconds) // Set a time window of 60 seconds after

            val snapshot = firestore.collection("accounts")
                .whereEqualTo("name", "Nu")
                .whereEqualTo("user_id", userId)
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                val accountId = snapshot.documents[0].id // Get account ID
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

                transactionSnapshot.documents.isNotEmpty() // Return true if a transaction exists
            } else {
                false // No account found
            }
        } catch (e: Exception) {
            false // Return false in case of error
        }
    }

    /**
     * Retrieves the Nu account for the current user.
     *
     * @return A map containing the account data, or null if the account does not exist.
     */
    private suspend fun getNuAccount(userId: String): Map<String, Any>? {
        return try {
            val snapshot = firestore.collection("accounts")
                .whereEqualTo("name", "Nu")
                .whereEqualTo("user_id", userId)
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                snapshot.documents[0].data // Return account data
            } else {
                null // No account found
            }
        } catch (e: Exception) {
            null // Return null in case of error
        }
    }

    /**
     * Creates a new Nu account for the user.
     */
    private suspend fun createNuAccount(userId: String) {
        try {
            firestore.collection("accounts").add(
                mapOf(
                    "amount" to 0L, // Initial amount
                    "name" to "Nu", // Account name
                    "user_id" to userId // User ID
                )
            ).await() // Await completion
        } catch (e: Exception) {
            // Handle error
        }
    }

    /**
     * Adds a transaction to the user's account.
     */
    private suspend fun addTransaction(userId: String, amount: Long, transactionName: String, transactionType: String, location: android.location.Location?, automatic: Boolean) {
        try {
            val accountSnapshot = firestore.collection("accounts")
                .whereEqualTo("name", "Nu")
                .whereEqualTo("user_id", userId)
                .get()
                .await()

            if (accountSnapshot.documents.isNotEmpty()) {
                val accountId = accountSnapshot.documents[0].id // Get account ID
                val transaction = hashMapOf(
                    "amount" to amount, // Transaction amount
                    "dateTime" to Timestamp.now(), // Current timestamp
                    "accountID" to accountId, // Associated account ID
                    "transactionName" to transactionName, // Name of the transaction
                    "transactionType" to transactionType, // Type of transaction
                    "automatic" to automatic, // Flag indicating if the transaction is automatic
                    "location" to if (location != null) {
                        hashMapOf(
                            "latitude" to location.latitude,
                            "longitude" to location.longitude
                        ) // Location coordinates
                    } else null
                )

                firestore.collection("accounts")
                    .document(accountId)
                    .collection("transactions")
                    .add(transaction) // Add transaction to Firestore
                    .await()
            } else {
                // Handle error
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    /**
     * Updates the Nu account balance after a transaction.
     */
    private suspend fun updateNuAccountBalance(userId: String, amountDelta: Long) {
        try {
            val accountSnapshot = firestore.collection("accounts")
                .whereEqualTo("name", "Nu")
                .whereEqualTo("user_id", userId)
                .get()
                .await()

            if (accountSnapshot.documents.isNotEmpty()) {
                val accountId = accountSnapshot.documents[0].id
                val currentAmount = accountSnapshot.documents[0].getLong("amount") ?: 0L // Get current amount
                val newAmount = currentAmount + amountDelta // Update balance

                firestore.collection("accounts")
                    .document(accountId)
                    .update("amount", newAmount) // Update the account amount
                    .await()
            } else {
                // Handle error
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    /**
     * Retrieves the current user's ID.
     *
     * @return The user's ID as a String, or null if not authenticated.
     */
    private fun getCurrentUserId(): String? {
        val user = FirebaseAuth.getInstance().currentUser
        return user?.uid // Return user ID or null
    }
}
