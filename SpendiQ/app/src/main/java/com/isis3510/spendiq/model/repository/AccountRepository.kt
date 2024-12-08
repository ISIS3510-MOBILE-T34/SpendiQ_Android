package com.isis3510.spendiq.model.repository

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.isis3510.spendiq.model.data.Account
import com.isis3510.spendiq.model.singleton.FirebaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

/**
 * Repository class for managing accounts in the application.
 * This class interacts with Firebase Firestore and handles offline data
 * storage using internal storage to maintain user accounts.
 */
class AccountRepository private constructor(private val context: Context) {
    private val firestore = FirebaseManager.firestore
    private val auth = FirebaseManager.auth
    private val fileName = "accounts_backup.json" // File for offline storage

    companion object {
        @Volatile
        private var INSTANCE: AccountRepository? = null

        /**
         * Initialize the repository instance with a context.
         */
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                synchronized(this) {
                    if (INSTANCE == null) {
                        INSTANCE = AccountRepository(context)
                    }
                }
            }
        }

        /**
         * Retrieve the singleton instance of the repository.
         */
        fun getInstance(): AccountRepository {
            return INSTANCE ?: throw IllegalStateException("AccountRepository must be initialized first.")
        }
    }

    /**
     * Retrieves all accounts associated with the currently authenticated user.
     * Tries to fetch from Firebase; if unavailable, loads accounts from internal storage.
     */
    fun getAccounts(): Flow<Result<List<Account>>> = flow {
        try {
            // Retrieve the current user's ID
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

            // Attempt to fetch accounts from Firebase
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

            if (accounts.isNotEmpty()) {
                // Save accounts to local storage for offline use
                saveAccountsToLocal(accounts)
                Log.d("AccountRepository", "Fetched accounts from Firebase: $accounts")
                emit(Result.success(accounts))
            } else {
                Log.w("AccountRepository", "No accounts found on Firebase for the user.")

                // Fallback: Load accounts from local storage
                val localAccounts = loadAccountsFromLocal()
                if (!localAccounts.isNullOrEmpty()) {
                    Log.d("AccountRepository", "Loaded accounts from local storage as a fallback: $localAccounts")
                    emit(Result.success(localAccounts))
                } else {
                    Log.e("AccountRepository", "No accounts found in local storage.")
                    emit(Result.failure(Exception("No accounts found in Firebase or local storage.")))
                }
            }
        } catch (firebaseException: Exception) {
            Log.e("AccountRepository", "Error fetching accounts from Firebase, falling back to local storage", firebaseException)

            // Fallback: Load accounts from local storage
            val localAccounts = loadAccountsFromLocal()
            if (!localAccounts.isNullOrEmpty()) {
                Log.d("AccountRepository", "Loaded accounts from local storage: $localAccounts")
                emit(Result.success(localAccounts))
            } else {
                Log.e("AccountRepository", "No accounts found in local storage.")
                emit(Result.failure(firebaseException))
            }
        }
    }



    fun getAccountsRealTime(): Flow<Result<List<Account>>> = callbackFlow {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

            val listenerRegistration = firestore.collection("accounts")
                .whereEqualTo("user_id", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("AccountRepository", "Error listening to accounts", error)
                        trySend(Result.failure(error)).isSuccess
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val accounts = snapshot.documents.mapNotNull { doc ->
                            Account(
                                id = doc.id,
                                name = doc.getString("name") ?: return@mapNotNull null,
                                type = doc.getString("type") ?: "Debit", // Asignar un valor por defecto
                                amount = doc.getLong("amount") ?: 0L,
                                color = getColorForAccount(doc.getString("name") ?: "")
                            )
                        }
                        saveAccountsToLocal(accounts) // Opcional: guardar localmente
                        Log.d("AccountRepository", "Fetched accounts: $accounts")
                        trySend(Result.success(accounts)).isSuccess
                    }
                }

            awaitClose { listenerRegistration.remove() }
        } catch (e: Exception) {
            Log.e("AccountRepository", "Exception in getAccountsRealTime", e)
            trySend(Result.failure(e)).isSuccess
            close(e)
        }
    }

    /**
     * Obtiene las 3 cuentas con transacciones más recientes como un Flow que emite actualizaciones en tiempo real.
     */
    fun getTop3RecentAccountsRealTime(): Flow<Result<List<Account>>> = callbackFlow {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

            // Listener para las cuentas
            val accountsListener = firestore.collection("accounts")
                .whereEqualTo("user_id", userId)
                .addSnapshotListener { accountsSnapshot, accountsError ->
                    if (accountsError != null) {
                        Log.e("AccountRepository", "Error listening to accounts for top3", accountsError)
                        trySend(Result.failure(accountsError)).isSuccess
                        return@addSnapshotListener
                    }

                    if (accountsSnapshot != null) {
                        val accounts = accountsSnapshot.documents.mapNotNull { doc ->
                            Account(
                                id = doc.id,
                                name = doc.getString("name") ?: return@mapNotNull null,
                                type = doc.getString("type") ?: "Debit",
                                amount = doc.getLong("amount") ?: 0L,
                                color = getColorForAccount(doc.getString("name") ?: "")
                            )
                        }

                        // Lanzar una corrutina dentro del callbackFlow
                        // Usa 'launch' en 'this' que es el CoroutineScope de callbackFlow
                        this.launch {
                            try {
                                val deferredAccountsWithLatestTransaction = accounts.map { account ->
                                    async(Dispatchers.IO) {
                                        val transactionsSnapshot = firestore.collection("accounts")
                                            .document(account.id)
                                            .collection("transactions")
                                            .orderBy("dateTime", Query.Direction.DESCENDING)
                                            .limit(1)
                                            .get()
                                            .await()

                                        val latestTransactionTime = transactionsSnapshot.documents.firstOrNull()
                                            ?.getTimestamp("dateTime")

                                        Pair(account, latestTransactionTime)
                                    }
                                }

                                val accountsWithLatestTransaction = deferredAccountsWithLatestTransaction.awaitAll()

                                val filteredAccounts = accountsWithLatestTransaction.filter { it.second != null }

                                val sortedAccounts = filteredAccounts.sortedByDescending { it.second }

                                val top3Accounts = sortedAccounts.take(3).map { it.first }

                                Log.d("AccountRepository", "Top 3 Accounts: $top3Accounts")
                                trySend(Result.success(top3Accounts)).isSuccess
                            } catch (e: Exception) {
                                Log.e("AccountRepository", "Error processing top 3 accounts", e)
                                trySend(Result.failure(e)).isSuccess
                            }
                        }
                    }
                }

            awaitClose { accountsListener.remove() }
        } catch (e: Exception) {
            Log.e("AccountRepository", "Exception in getTop3RecentAccountsRealTime", e)
            trySend(Result.failure(e)).isSuccess
            close(e)
        }
    }

    /**
     * Creates a new account for the currently authenticated user.
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
            emit(Result.success(Unit))
        } catch (e: Exception) {
            Log.e("AccountRepository", "Error creating account", e)
            emit(Result.failure(e))
        }
    }

    /**
     * Deletes an account and its associated transactions from Firestore.
     */
    fun deleteAccount(accountType: String): Flow<Result<Unit>> = flow {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

            // Retrieve accounts that match the accountType for the current user
            val accountsSnapshot = firestore.collection("accounts")
                .whereEqualTo("user_id", userId)
                .whereEqualTo("name", accountType)  // Match the account by its type (name)
                .get()
                .await()

            // If no account is found with the given accountType
            if (accountsSnapshot.isEmpty) {
                throw Exception("No account found with type: $accountType")
            }

            // Loop through all the accounts with the matching type
            for (doc in accountsSnapshot.documents) {
                val accountId = doc.id
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
            }

            emit(Result.success(Unit))
        } catch (e: Exception) {
            Log.e("AccountRepository", "Error deleting account", e)
            emit(Result.failure(e))
        }
    }

    /**
     * Updates the balance of a specified account.
     */
    suspend fun updateAccountBalance(accountId: String, amountDelta: Long) {
        val accountRef = firestore.collection("accounts").document(accountId)

        // Perform a Firestore transaction to update the account balance
        firestore.runTransaction { transactionObj ->
            val account = transactionObj.get(accountRef)
            val currentBalance = account.getLong("amount") ?: 0L
            transactionObj.update(accountRef, "amount", currentBalance + amountDelta)
        }.await()
    }

    /**
     * Saves the list of accounts to internal storage as a JSON file.
     * Ensures not to overwrite existing storage with empty data.
     */
    private fun saveAccountsToLocal(accounts: List<Account>) {
        try {
            if (accounts.isEmpty()) {
                Log.w("AccountRepository", "Empty accounts list, skipping save to local storage.")
                return
            }

            val file = File(context.filesDir, fileName)
            val json = Gson().toJson(accounts)
            file.writeText(json)
            Log.d("AccountRepository", "Accounts saved to local storage: $accounts")
        } catch (e: Exception) {
            Log.e("AccountRepository", "Error saving accounts locally", e)
        }
    }

    /**
     * Loads the list of accounts from internal storage if available.
     */
    private fun loadAccountsFromLocal(): List<Account>? {
        return try {
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                val json = file.readText()
                val accounts = Gson().fromJson<List<Account>>(json, object : TypeToken<List<Account>>() {}.type)
                Log.d("AccountRepository", "Loaded accounts from local storage: $accounts")
                accounts
            } else {
                Log.e("AccountRepository", "Local file not found: $fileName")
                null
            }
        } catch (e: Exception) {
            Log.e("AccountRepository", "Error loading accounts locally", e)
            null
        }
    }

    /**
     * Returns a Color associated with an account name.
     */
    private fun getColorForAccount(accountName: String): Color {
        return when (accountName) {
            "Nu" -> Color(0xFF820ad1)
            "Bancolombia" -> Color(0xFFFDDA24)
            "Nequi" -> Color(0xFFda0081)
            "Lulo" -> Color(0xFFe8ff00)
            "Davivienda" -> Color(0xFFed1c27)
            "Banco de Bogotá" -> Color(0xFF00317e)
            "Scotiabank" -> Color(0xFFED0722)
            else -> Color.Gray
        }
    }
}
