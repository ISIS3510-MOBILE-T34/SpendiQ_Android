package com.isis3510.spendiq.model.repository

import android.util.Log
import com.isis3510.spendiq.model.data.Location
import com.isis3510.spendiq.model.data.Transaction
import com.isis3510.spendiq.model.singleton.FirebaseManager
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Repository class for managing transactions related to user accounts.
 *
 * This class provides methods to fetch, add, update, and delete transactions
 * stored in Firestore, along with analyzing anomalies in transactions.
 */
class TransactionRepository {
    // Firebase instances for authentication and Firestore
    private val auth = FirebaseManager.auth
    private val firestore = FirebaseManager.firestore
    private val anomalyRepository = AnomalyRepository() // For anomaly analysis
    private val accountRepository = AccountRepository() // For account balance updates

    companion object {
        // Default location used when no location is available
        private val DEFAULT_LOCATION = Location(
            latitude = 4.6097100,  // Coordinates for Bogota
            longitude = -74.0817500
        )
    }

    /**
     * Retrieves transactions for a specified account name.
     *
     * @param accountName The name of the account whose transactions are to be fetched.
     * @return A Flow emitting the result containing a list of transactions on success,
     * or an error if the operation fails.
     */
    fun getTransactions(accountName: String): Flow<Result<List<Transaction>>> = flow {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val accountSnapshot = firestore.collection("accounts")
                .whereEqualTo("user_id", userId)
                .whereEqualTo("name", accountName)
                .get()
                .await()

            if (accountSnapshot.documents.isEmpty()) {
                emit(Result.failure(Exception("Account not found")))
                return@flow
            }

            val accountId = accountSnapshot.documents[0].id
            val transactionsSnapshot = firestore.collection("accounts")
                .document(accountId)
                .collection("transactions")
                .get()
                .await()

            // Map documents to Transaction objects and sort them by date
            val transactions = transactionsSnapshot.documents.mapNotNull { doc ->
                parseTransactionDocument(doc, accountId)
            }.sortedByDescending { it.dateTime.toDate() }

            emit(Result.success(transactions)) // Emit the result
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error fetching transactions", e) // Log error
            emit(Result.failure(e))
        }
    }

    /**
     * Retrieves a specific transaction by its ID.
     *
     * @param accountId The ID of the account containing the transaction.
     * @param transactionId The ID of the transaction to retrieve.
     * @return A Flow emitting the result containing the transaction on success,
     * or an error if the operation fails.
     */
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

            val transaction = parseTransactionDocument(transactionDoc, accountId)
            transaction?.let {
                emit(Result.success(it)) // Emit the retrieved transaction
            } ?: emit(Result.failure(Exception("Failed to parse transaction")))
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error fetching transaction", e) // Log error
            emit(Result.failure(e))
        }
    }

    /**
     * Retrieves all transactions for the current user across all accounts.
     *
     * @return A Flow emitting the result containing a list of all transactions on success,
     * or an error if the operation fails.
     */
    fun getAllTransactions(): Flow<Result<List<Transaction>>> = flow {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val accountsSnapshot = firestore.collection("accounts")
                .whereEqualTo("user_id", userId)
                .get()
                .await()

            val transactions = mutableListOf<Transaction>()

            // Iterate over each account to fetch its transactions
            for (accountDoc in accountsSnapshot.documents) {
                val accountId = accountDoc.id
                val transactionsSnapshot = firestore.collection("accounts")
                    .document(accountId)
                    .collection("transactions")
                    .get()
                    .await()

                // Add each transaction to the list
                transactions.addAll(transactionsSnapshot.documents.mapNotNull { doc ->
                    parseTransactionDocument(doc, accountId)
                })
            }

            emit(Result.success(transactions)) // Emit the list of transactions
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error fetching all transactions", e) // Log error
            emit(Result.failure(e))
        }
    }

    /**
     * Adds a new transaction to the specified account.
     *
     * @param transaction The transaction to add.
     * @return A Flow emitting the result of the operation.
     */
    fun addTransaction(transaction: Transaction): Flow<Result<Unit>> = flow {
        try {
            // Create a reference for the new transaction document
            val accountRef = firestore.collection("accounts")
                .document(transaction.accountId)
                .collection("transactions")
                .document()

            val transactionWithId = transaction.copy(id = accountRef.id) // Include the document ID
            val transactionMap = createTransactionMap(transactionWithId) // Create a map for Firestore

            // Store the transaction in Firestore
            accountRef.set(transactionMap).await()

            // Update the account balance based on transaction type
            val amountDelta = if (transaction.transactionType == "Income") transaction.amount else -transaction.amount
            accountRepository.updateAccountBalance(transaction.accountId, amountDelta)

            // Launch a coroutine to analyze the transaction for anomalies
            coroutineScope {
                launch {
                    auth.currentUser?.uid?.let { userId ->
                        anomalyRepository.analyzeTransaction(userId, accountRef.id)
                    }
                }
            }

            emit(Result.success(Unit)) // Emit success
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error adding transaction", e) // Log error
            emit(Result.failure(e))
        }
    }

    /**
     * Updates an existing transaction.
     *
     * @param accountId The ID of the account containing the transaction.
     * @param oldTransaction The transaction to be updated.
     * @param newTransaction The updated transaction details.
     * @return A Flow emitting the result of the operation.
     */
    fun updateTransaction(accountId: String, oldTransaction: Transaction, newTransaction: Transaction): Flow<Result<Unit>> = flow {
        try {
            val transactionRef = firestore.collection("accounts")
                .document(accountId)
                .collection("transactions")
                .document(oldTransaction.id)

            // Check if the transaction exists before updating
            if (!(transactionRef.get().await().exists())) {
                emit(Result.failure(Exception("Transaction does not exist")))
                return@flow
            }

            // Calculate the balance adjustment based on the old and new amounts
            val oldAmount = if (oldTransaction.transactionType == "Income") oldTransaction.amount else -oldTransaction.amount
            val newAmount = if (newTransaction.transactionType == "Income") newTransaction.amount else -newTransaction.amount
            val balanceAdjustment = newAmount - oldAmount

            // Update the transaction in Firestore
            transactionRef.set(createTransactionMap(newTransaction)).await()
            // Update the account balance
            accountRepository.updateAccountBalance(accountId, balanceAdjustment)

            // Launch a coroutine to analyze the updated transaction for anomalies
            coroutineScope {
                launch {
                    auth.currentUser?.uid?.let { userId ->
                        anomalyRepository.analyzeTransaction(userId, newTransaction.id)
                    }
                }
            }

            emit(Result.success(Unit)) // Emit success
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error updating transaction", e) // Log error
            emit(Result.failure(e))
        }
    }

    /**
     * Deletes a transaction from the specified account.
     *
     * @param accountId The ID of the account containing the transaction.
     * @param transaction The transaction to delete.
     * @return A Flow emitting the result of the operation.
     */
    fun deleteTransaction(accountId: String, transaction: Transaction): Flow<Result<Unit>> = flow {
        try {
            val transactionRef = firestore.collection("accounts")
                .document(accountId)
                .collection("transactions")
                .document(transaction.id)

            // Delete the transaction document
            transactionRef.delete().await()

            // Update the account balance
            val amountToRemove = if (transaction.transactionType == "Income") -transaction.amount else transaction.amount
            accountRepository.updateAccountBalance(accountId, amountToRemove)

            emit(Result.success(Unit)) // Emit success
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error deleting transaction", e) // Log error
            emit(Result.failure(e))
        }
    }

    /**
     * Parses a Firestore document into a Transaction object.
     *
     * @param doc The Firestore document to parse.
     * @param accountId The ID of the account associated with the transaction.
     * @return The Transaction object or null if parsing fails.
     */
    private fun parseTransactionDocument(doc: com.google.firebase.firestore.DocumentSnapshot, accountId: String): Transaction? {
        return try {
            Transaction(
                id = doc.id,
                accountId = accountId,
                transactionName = doc.getString("transactionName") ?: return null,
                amount = doc.getLong("amount") ?: return null,
                dateTime = doc.getTimestamp("dateTime") ?: return null,
                transactionType = doc.getString("transactionType") ?: return null,
                location = doc.get("location")?.let { locationMap ->
                    if (locationMap is Map<*, *>) {
                        Location(
                            latitude = (locationMap["latitude"] as? Double) ?: DEFAULT_LOCATION.latitude,
                            longitude = (locationMap["longitude"] as? Double) ?: DEFAULT_LOCATION.longitude
                        )
                    } else null
                },
                amountAnomaly = doc.getBoolean("amountAnomaly") ?: false,
                locationAnomaly = doc.getBoolean("locationAnomaly") ?: false,
                automatic = doc.getBoolean("automatic") ?: false // Track if the transaction was created automatically
            )
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error parsing transaction document", e) // Log error
            null
        }
    }

    /**
     * Creates a map representation of a Transaction object for Firestore storage.
     *
     * @param transaction The Transaction object to convert.
     * @return A map containing the transaction's data.
     */
    private fun createTransactionMap(transaction: Transaction): Map<String, Any?> {
        return hashMapOf(
            "transactionId" to transaction.id,
            "amount" to transaction.amount,
            "dateTime" to transaction.dateTime,
            "transactionName" to transaction.transactionName,
            "transactionType" to transaction.transactionType,
            "location" to transaction.location?.let {
                hashMapOf(
                    "latitude" to it.latitude,
                    "longitude" to it.longitude
                )
            },
            "locationAnomaly" to transaction.locationAnomaly,
            "amountAnomaly" to transaction.amountAnomaly,
            "automatic" to transaction.automatic // Track if the transaction was created automatically
        )
    }
}
