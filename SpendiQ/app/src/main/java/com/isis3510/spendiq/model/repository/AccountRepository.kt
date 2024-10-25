package com.isis3510.spendiq.model.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.spendiq.model.data.Account
import com.isis3510.spendiq.model.data.Transaction
import com.isis3510.spendiq.model.data.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp
import android.util.Log
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class AccountRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val anomalyRepository = AnomalyRepository()

    companion object {
        private val DEFAULT_LOCATION = Location(
            latitude = 4.6097100,  // Bogota's coordinates
            longitude = -74.0817500
        )
    }

    // Get all accounts for the current user
    fun getAccounts(): Flow<Result<List<Account>>> = flow {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val snapshot = firestore.collection("accounts")
                .whereEqualTo("user_id", userId)
                .get()
                .await()

            val accounts = snapshot.documents.mapNotNull { doc ->
                Account(
                    id = doc.id,
                    name = doc.getString("name") ?: return@mapNotNull null,
                    type = "Debit",
                    amount = doc.getLong("amount") ?: 0L,
                    color = getColorForAccount(doc.getString("name") ?: "")
                )
            }
            emit(Result.success(accounts))
        } catch (e: Exception) {
            Log.e("AccountRepository", "Error fetching accounts", e)
            emit(Result.failure(e))
        }
    }

    // Create a new account
    fun createAccount(accountType: String): Flow<Result<Unit>> = flow {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            firestore.collection("accounts").add(
                mapOf(
                    "name" to accountType,
                    "amount" to 0L,
                    "user_id" to userId
                )
            ).await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            Log.e("AccountRepository", "Error creating account", e)
            emit(Result.failure(e))
        }
    }

    // Delete an account
    fun deleteAccount(accountId: String): Flow<Result<Unit>> = flow {
        try {
            val accountRef = firestore.collection("accounts").document(accountId)

            // Fetch transactions outside of the transaction
            val transactionsSnapshot = accountRef.collection("transactions").get().await()

            firestore.runTransaction { transaction ->
                for (transactionDoc in transactionsSnapshot.documents) {
                    transaction.delete(transactionDoc.reference)
                }
                transaction.delete(accountRef)
            }.await()

            emit(Result.success(Unit))
        } catch (e: Exception) {
            Log.e("AccountRepository", "Error deleting account", e)
            emit(Result.failure(e))
        }
    }

    // Get specific transaction by accountId and transactionId
    fun getTransaction(accountId: String, transactionId: String): Flow<Result<Transaction>> = flow {
        try {
            val transactionDoc = firestore.collection("accounts")
                .document(accountId)
                .collection("transactions")
                .document(transactionId)
                .get()
                .await()

            if (!transactionDoc.exists()) {
                emit(Result.failure(Exception("Transaction not found")))
                return@flow
            }

            val transaction = Transaction(
                id = transactionDoc.id,
                accountId = accountId,
                transactionName = transactionDoc.getString("transactionName") ?: "",
                amount = transactionDoc.getLong("amount") ?: 0L,
                dateTime = transactionDoc.getTimestamp("dateTime") ?: Timestamp.now(),
                transactionType = transactionDoc.getString("transactionType") ?: "",
                location = transactionDoc.get("location")?.let { locationMap ->
                    if (locationMap is Map<*, *>) {
                        Location(
                            latitude = (locationMap["latitude"] as? Double) ?: DEFAULT_LOCATION.latitude,
                            longitude = (locationMap["longitude"] as? Double) ?: DEFAULT_LOCATION.longitude
                        )
                    } else null
                },
                amountAnomaly = transactionDoc.getBoolean("amountAnomaly") ?: false,
                locationAnomaly = transactionDoc.getBoolean("locationAnomaly") ?: false
            )
            emit(Result.success(transaction))
        } catch (e: Exception) {
            Log.e("AccountRepository", "Error fetching transaction", e)
            emit(Result.failure(e))
        }
    }

    // Get transactions for an account using accountId
    fun getTransactions(accountId: String): Flow<Result<List<Transaction>>> = flow {
        try {
            val transactionsSnapshot = firestore.collection("accounts")
                .document(accountId)
                .collection("transactions")
                .get()
                .await()

            val transactions = transactionsSnapshot.documents.mapNotNull { doc ->
                Transaction(
                    id = doc.id,
                    accountId = accountId,
                    transactionName = doc.getString("transactionName") ?: return@mapNotNull null,
                    amount = doc.getLong("amount") ?: return@mapNotNull null,
                    dateTime = doc.getTimestamp("dateTime") ?: return@mapNotNull null,
                    transactionType = doc.getString("transactionType") ?: return@mapNotNull null,
                    location = doc.get("location")?.let { locationMap ->
                        if (locationMap is Map<*, *>) {
                            val latitude = (locationMap["latitude"] as? Double) ?: return@mapNotNull null
                            val longitude = (locationMap["longitude"] as? Double) ?: return@mapNotNull null
                            Location(latitude, longitude)
                        } else null
                    },
                    amountAnomaly = doc.getBoolean("amountAnomaly") ?: false,
                    locationAnomaly = doc.getBoolean("locationAnomaly") ?: false
                )
            }

            emit(Result.success(transactions))
        } catch (e: Exception) {
            Log.e("AccountRepository", "Error fetching transactions", e)
            emit(Result.failure(e))
        }
    }
    // Add transaction with account check and ensure transaction ID is stored correctly
    fun addTransactionWithAccountCheck(transaction: Transaction): Flow<Result<Unit>> = flow {
        try {
            val accountRef = firestore.collection("accounts")
                .document(transaction.accountId)
                .collection("transactions")
                .document()

            val transactionWithId = transaction.copy(id = accountRef.id)

            val transactionMap = hashMapOf(
                "transactionId" to transactionWithId.id,
                "amount" to transactionWithId.amount,
                "dateTime" to transactionWithId.dateTime,
                "transactionName" to transactionWithId.transactionName,
                "transactionType" to transactionWithId.transactionType,
                "location" to transactionWithId.location?.let {
                    hashMapOf(
                        "latitude" to it.latitude,
                        "longitude" to it.longitude
                    )
                },
                "locationAnomaly" to transactionWithId.locationAnomaly,
                "amountAnomaly" to transactionWithId.amountAnomaly
            )

            // Save the transaction
            accountRef.set(transactionMap).await()
            updateAccountBalance(transaction.accountId, transactionWithId)

            // Make the anomaly analysis call
            coroutineScope {
                launch {
                    auth.currentUser?.uid?.let { userId ->
                        anomalyRepository.analyzeTransaction(userId, accountRef.id)
                    }
                }
            }

            emit(Result.success(Unit))
        } catch (e: Exception) {
            Log.e("AccountRepository", "Error adding transaction", e)
            emit(Result.failure(e))
        }
    }



    // Update account balance after transaction
    private suspend fun updateAccountBalance(accountId: String, transaction: Transaction) {
        val accountRef = firestore.collection("accounts").document(accountId)

        firestore.runTransaction { transactionObj ->
            val account = transactionObj.get(accountRef)
            val currentBalance = account.getLong("amount") ?: 0L
            val adjustment = if (transaction.transactionType == "Income") {
                transaction.amount
            } else {
                -transaction.amount
            }
            transactionObj.update(accountRef, "amount", currentBalance + adjustment)
        }.await()
    }

    fun updateTransaction(accountId: String, oldTransaction: Transaction, newTransaction: Transaction): Flow<Result<Unit>> = flow {
        try {
            val accountRef = firestore.collection("accounts").document(accountId)
            val transactionRef = accountRef.collection("transactions").document(oldTransaction.id)

            // Check if the transaction exists before updating
            val transactionDoc = transactionRef.get().await()
            if (!transactionDoc.exists()) {
                emit(Result.failure(Exception("Transaction does not exist for update.")))
                return@flow
            }

            firestore.runTransaction { transaction ->
                // Get the account document and current balance
                val account = transaction.get(accountRef)
                val currentBalance = account.getLong("amount") ?: 0L

                // Calculate the balance adjustment based on the old and new transaction amounts
                val oldAmount = if (oldTransaction.transactionType == "Income") oldTransaction.amount else -oldTransaction.amount
                val newAmount = if (newTransaction.transactionType == "Income") newTransaction.amount else -newTransaction.amount
                val balanceAdjustment = newAmount - oldAmount

                // Update the transaction details
                transaction.set(transactionRef, mapOf(
                    "transactionName" to newTransaction.transactionName,
                    "amount" to newTransaction.amount,
                    "dateTime" to newTransaction.dateTime,
                    "transactionType" to newTransaction.transactionType,
                    "location" to newTransaction.location?.let {
                        mapOf("latitude" to it.latitude, "longitude" to it.longitude)
                    }
                ))

                // Update the account's balance
                transaction.update(accountRef, "amount", currentBalance + balanceAdjustment)
            }.await()

            // Re-analyze for anomalies after the transaction update
            coroutineScope {
                launch {
                    auth.currentUser?.uid?.let { userId ->
                        anomalyRepository.analyzeTransaction(userId, newTransaction.id)
                    }
                }
            }

            emit(Result.success(Unit))
        } catch (e: Exception) {
            Log.e("AccountRepository", "Error updating transaction", e)
            emit(Result.failure(e))
        }
    }


    fun deleteTransaction(accountId: String, transaction: Transaction): Flow<Result<Unit>> = flow {
        try {
            val accountRef = firestore.collection("accounts").document(accountId)
            val transactionRef = accountRef.collection("transactions").document(transaction.id)

            firestore.runTransaction { trans ->
                // Get the account document and current balance
                val account = trans.get(accountRef)
                val currentBalance = account.getLong("amount") ?: 0L

                // Calculate the amount to remove based on transaction type
                val amountToRemove = if (transaction.transactionType == "Income") transaction.amount else -transaction.amount

                // Delete the transaction document
                trans.delete(transactionRef)

                // Update the account's balance after deleting the transaction
                trans.update(accountRef, "amount", currentBalance - amountToRemove)
            }.await()

            emit(Result.success(Unit))
        } catch (e: Exception) {
            Log.e("AccountRepository", "Error during transaction deletion", e)
            emit(Result.failure(e))
        }
    }

    // Get color for accounts
    private fun getColorForAccount(accountName: String): Color {
        return when (accountName) {
            "Nu" -> Color(0xFF9747FF)
            "Bancolombia" -> Color(0xFFFFCC00)
            "Nequi" -> Color(0xFF8B2F87)
            else -> Color.Gray
        }
    }
}
