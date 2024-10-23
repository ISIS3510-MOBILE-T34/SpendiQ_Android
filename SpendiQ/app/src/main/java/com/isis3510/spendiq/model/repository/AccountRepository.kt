package com.isis3510.spendiq.model.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.spendiq.model.data.Account
import com.isis3510.spendiq.model.data.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class AccountRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Method to get all accounts for the current user
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
            emit(Result.failure(e))
        }
    }

    // Method to create a new account for the user
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
            emit(Result.failure(e))
        }
    }

    // Method to delete an account
    fun deleteAccount(accountType: String): Flow<Result<Unit>> = flow {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val documents = firestore.collection("accounts")
                .whereEqualTo("name", accountType)
                .whereEqualTo("user_id", userId)
                .get()
                .await()

            for (document in documents) {
                document.reference.delete().await()
            }
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // Method to get transactions for a specific account
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
                            com.isis3510.spendiq.model.data.Location(latitude, longitude)
                        } else {
                            null
                        }
                    }
                )
            }
            emit(Result.success(transactions))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // Method to add a transaction and create the account if it doesn't exist
    fun addTransactionWithAccountCheck(transaction: Transaction): Flow<Result<Unit>> = flow {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

            // Check if account exists
            val accountSnapshot = firestore.collection("accounts")
                .whereEqualTo("user_id", userId)
                .whereEqualTo("name", transaction.accountId)
                .get()
                .await()

            val accountId = if (accountSnapshot.documents.isEmpty()) {
                // If the account does not exist, create it
                val newAccountRef = firestore.collection("accounts").document()
                val newAccountData = mapOf(
                    "name" to transaction.accountId,
                    "amount" to 0L,
                    "user_id" to userId
                )
                newAccountRef.set(newAccountData).await()
                newAccountRef.id
            } else {
                // Use existing account ID
                accountSnapshot.documents[0].id
            }

            // Add the transaction to the account
            val transactionMap = hashMapOf(
                "amount" to transaction.amount,
                "dateTime" to transaction.dateTime,
                "transactionName" to transaction.transactionName,
                "transactionType" to transaction.transactionType,
                "location" to transaction.location?.let {
                    hashMapOf(
                        "latitude" to it.latitude,
                        "longitude" to it.longitude
                    )
                }
            )

            firestore.collection("accounts")
                .document(accountId)
                .collection("transactions")
                .add(transactionMap)
                .await()

            // Update account balance
            val accountRef = firestore.collection("accounts").document(accountId)
            firestore.runTransaction { transactionObj ->
                val account = transactionObj.get(accountRef)
                val currentBalance = account.getLong("amount") ?: 0L
                val newBalance = if (transaction.transactionType == "Income") {
                    currentBalance + transaction.amount
                } else {
                    currentBalance - transaction.amount
                }
                transactionObj.update(accountRef, "amount", newBalance)
            }.await()

            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // Utility method to get color for an account
    private fun getColorForAccount(accountName: String): androidx.compose.ui.graphics.Color {
        return when (accountName) {
            "Nu" -> androidx.compose.ui.graphics.Color(0xFF9747FF)
            "Bancolombia" -> androidx.compose.ui.graphics.Color(0xFFFFCC00)
            "Nequi" -> androidx.compose.ui.graphics.Color(0xFF8B2F87)
            else -> androidx.compose.ui.graphics.Color.Gray
        }
    }
}
