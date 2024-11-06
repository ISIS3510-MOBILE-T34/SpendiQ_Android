package com.isis3510.spendiq.model.repository

import android.util.Log
import androidx.compose.ui.graphics.Color
import com.isis3510.spendiq.model.data.Account
import com.isis3510.spendiq.model.singleton.FirebaseManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

/**
 * Repository class for managing accounts in the application.
 * This class interacts with Firebase Firestore to perform CRUD operations
 * on user accounts and provides access to account data through coroutines.
 */
class AccountRepository {
    // Firebase Firestore and Authentication instances
    private val firestore = FirebaseManager.firestore
    private val auth = FirebaseManager.auth

    /**
     * Retrieves all accounts associated with the currently authenticated user.
     * Emits a Result containing a list of Account objects.
     */
    fun getAccounts(): Flow<Result<List<Account>>> = flow {
        try {
            // Retrieve the current user's ID
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            // Fetch account documents from Firestore
            val snapshot = firestore.collection("accounts")
                .whereEqualTo("user_id", userId)
                .get()
                .await()

            // Map Firestore documents to Account objects
            val accounts = snapshot.documents.mapNotNull { doc ->
                Account(
                    id = doc.id,
                    name = doc.getString("name") ?: return@mapNotNull null,
                    type = "Debit",
                    amount = doc.getLong("amount") ?: 0L,
                    color = getColorForAccount(doc.getString("name") ?: "")
                )
            }
            // Emit the successful result
            emit(Result.success(accounts))
        } catch (e: Exception) {
            Log.e("AccountRepository", "Error fetching accounts", e)
            // Emit failure result on exception
            emit(Result.failure(e))
        }
    }

    /**
     * Creates a new account for the currently authenticated user.
     * Emits a Result indicating the success or failure of the operation.
     *
     * @param accountType The type/name of the account to create.
     */
    fun createAccount(accountType: String): Flow<Result<Unit>> = flow {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            // Add a new account document to Firestore
            firestore.collection("accounts").add(
                mapOf(
                    "name" to accountType,
                    "amount" to 0L,
                    "user_id" to userId
                )
            ).await()
            // Emit success result
            emit(Result.success(Unit))
        } catch (e: Exception) {
            Log.e("AccountRepository", "Error creating account", e)
            // Emit failure result on exception
            emit(Result.failure(e))
        }
    }

    /**
     * Deletes an account and its associated transactions from Firestore.
     * Emits a Result indicating the success or failure of the operation.
     *
     * @param accountId The ID of the account to delete.
     */
    fun deleteAccount(accountId: String): Flow<Result<Unit>> = flow {
        try {
            // Reference to the account document
            val accountRef = firestore.collection("accounts").document(accountId)
            // Fetch transactions associated with the account
            val transactionsSnapshot = accountRef.collection("transactions").get().await()

            // Perform a Firestore transaction to delete the account and its transactions
            firestore.runTransaction { transaction ->
                for (transactionDoc in transactionsSnapshot.documents) {
                    transaction.delete(transactionDoc.reference)
                }
                transaction.delete(accountRef)
            }.await()

            // Emit success result
            emit(Result.success(Unit))
        } catch (e: Exception) {
            Log.e("AccountRepository", "Error deleting account", e)
            // Emit failure result on exception
            emit(Result.failure(e))
        }
    }

    /**
     * Updates the balance of a specified account.
     *
     * @param accountId The ID of the account to update.
     * @param amountDelta The change in amount (can be positive or negative).
     */
    suspend fun updateAccountBalance(accountId: String, amountDelta: Long) {
        // Reference to the account document
        val accountRef = firestore.collection("accounts").document(accountId)

        // Perform a Firestore transaction to update the account balance
        firestore.runTransaction { transactionObj ->
            val account = transactionObj.get(accountRef)
            val currentBalance = account.getLong("amount") ?: 0L
            transactionObj.update(accountRef, "amount", currentBalance + amountDelta)
        }.await()
    }

    /**
     * Returns a Color associated with an account name.
     *
     * @param accountName The name of the account.
     * @return The corresponding Color for the account.
     */
    private fun getColorForAccount(accountName: String): Color {
        return when (accountName) {
            "Nu" -> Color(0xFF9747FF)
            "Bancolombia" -> Color(0xFFFFCC00)
            "Nequi" -> Color(0xFF8B2F87)
            "Lulo" -> Color(0xFFE8FF00)
            "Davivienda" -> Color(0xFFed1c27)
            "BBVA" -> Color(0xFF072146)
            else -> Color.Gray // Default color for unrecognized accounts
        }
    }
}
