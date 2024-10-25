package com.isis3510.spendiq.model.repository

import android.util.Log
import androidx.compose.ui.graphics.Color
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.spendiq.model.data.Account
import com.isis3510.spendiq.model.singleton.FirebaseManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class AccountRepository {
    private val firestore = FirebaseManager.getFirestore()
    private val auth = FirebaseManager.getAuth()

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

    // Update account balance
    suspend fun updateAccountBalance(accountId: String, amountDelta: Long) {
        val accountRef = firestore.collection("accounts").document(accountId)

        firestore.runTransaction { transactionObj ->
            val account = transactionObj.get(accountRef)
            val currentBalance = account.getLong("amount") ?: 0L
            transactionObj.update(accountRef, "amount", currentBalance + amountDelta)
        }.await()
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