package com.isis3510.spendiq.model.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.spendiq.model.data.Location
import com.isis3510.spendiq.model.data.Transaction
import com.isis3510.spendiq.model.singleton.FirebaseManager
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TransactionRepository {
    private val firestore = FirebaseManager.getFirestore()
    private val auth = FirebaseManager.getAuth()
    private val anomalyRepository = AnomalyRepository()
    private val accountRepository = AccountRepository()

    companion object {
        private val DEFAULT_LOCATION = Location(
            latitude = 4.6097100,  // Bogota's coordinates
            longitude = -74.0817500
        )
    }

    // Get transactions by account name
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

            val transactions = transactionsSnapshot.documents.mapNotNull { doc ->
                parseTransactionDocument(doc, accountId)
            }.sortedByDescending { it.dateTime.toDate() }

            emit(Result.success(transactions))
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error fetching transactions", e)
            emit(Result.failure(e))
        }
    }

    // Get specific transaction
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
                emit(Result.success(it))
            } ?: emit(Result.failure(Exception("Failed to parse transaction")))
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error fetching transaction", e)
            emit(Result.failure(e))
        }
    }

    // Add transaction
    fun addTransaction(transaction: Transaction): Flow<Result<Unit>> = flow {
        try {
            val accountRef = firestore.collection("accounts")
                .document(transaction.accountId)
                .collection("transactions")
                .document()

            val transactionWithId = transaction.copy(id = accountRef.id)
            val transactionMap = createTransactionMap(transactionWithId)

            accountRef.set(transactionMap).await()

            val amountDelta = if (transaction.transactionType == "Income") transaction.amount else -transaction.amount
            accountRepository.updateAccountBalance(transaction.accountId, amountDelta)

            coroutineScope {
                launch {
                    auth.currentUser?.uid?.let { userId ->
                        anomalyRepository.analyzeTransaction(userId, accountRef.id)
                    }
                }
            }

            emit(Result.success(Unit))
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error adding transaction", e)
            emit(Result.failure(e))
        }
    }

    // Update transaction
    fun updateTransaction(accountId: String, oldTransaction: Transaction, newTransaction: Transaction): Flow<Result<Unit>> = flow {
        try {
            val transactionRef = firestore.collection("accounts")
                .document(accountId)
                .collection("transactions")
                .document(oldTransaction.id)

            if (!(transactionRef.get().await().exists())) {
                emit(Result.failure(Exception("Transaction does not exist")))
                return@flow
            }

            val oldAmount = if (oldTransaction.transactionType == "Income") oldTransaction.amount else -oldTransaction.amount
            val newAmount = if (newTransaction.transactionType == "Income") newTransaction.amount else -newTransaction.amount
            val balanceAdjustment = newAmount - oldAmount

            transactionRef.set(createTransactionMap(newTransaction)).await()
            accountRepository.updateAccountBalance(accountId, balanceAdjustment)

            coroutineScope {
                launch {
                    auth.currentUser?.uid?.let { userId ->
                        anomalyRepository.analyzeTransaction(userId, newTransaction.id)
                    }
                }
            }

            emit(Result.success(Unit))
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error updating transaction", e)
            emit(Result.failure(e))
        }
    }

    // Delete transaction
    fun deleteTransaction(accountId: String, transaction: Transaction): Flow<Result<Unit>> = flow {
        try {
            val transactionRef = firestore.collection("accounts")
                .document(accountId)
                .collection("transactions")
                .document(transaction.id)

            transactionRef.delete().await()

            val amountToRemove = if (transaction.transactionType == "Income") -transaction.amount else transaction.amount
            accountRepository.updateAccountBalance(accountId, amountToRemove)

            emit(Result.success(Unit))
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error deleting transaction", e)
            emit(Result.failure(e))
        }
    }

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
                locationAnomaly = doc.getBoolean("locationAnomaly") ?: false
            )
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error parsing transaction document", e)
            null
        }
    }

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
            "amountAnomaly" to transaction.amountAnomaly
        )
    }
}