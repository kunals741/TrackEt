package com.papaya.tracket.domain

import com.papaya.tracket.constants.NetworkConstants
import com.papaya.tracket.data.local.dao.TransactionDao
import com.papaya.tracket.data.local.entity.Transaction
import com.papaya.tracket.data.remote.SupabaseApi
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface TransactionRepository {
    // Local data operations
    suspend fun insertTransaction(transaction: Transaction): Long
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transactionId: Long)
    fun getAllTransactions(): Flow<List<Transaction>>

    // Remote data operations
    suspend fun syncTransactions()
    suspend fun fetchTransactionsFromRemote()
}

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val supabaseApi: SupabaseApi
) : TransactionRepository {
    override suspend fun insertTransaction(transaction: Transaction): Long {
        // Insert locally first
        val localId = transactionDao.insertTransaction(transaction)

        // Then try to sync with remote
        try {
            val authHeader = "Bearer ${NetworkConstants.SUPABASE_API_KEY}"
            val response = supabaseApi.createTransaction(
                apiKey = NetworkConstants.SUPABASE_API_KEY,
                authorization = authHeader,
                transaction = transaction.copy(transactionId = localId)
            )

            if (response.isSuccessful && response.body() != null) {
                // Update local with remote ID
                val remoteId = response.body()?.remoteId
                if (remoteId != null) {
                    transactionDao.updateTransaction(
                        transaction.copy(
                            transactionId = localId,
                            remoteId = remoteId,
                            synced = true
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Handle exception, but still return localId
            e.printStackTrace()
        }

        return localId
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        // Update locally
        transactionDao.updateTransaction(transaction)

        // Try to sync with remote if we have a remote ID
        if (transaction.remoteId != null) {
            try {
                val authHeader = "Bearer ${NetworkConstants.SUPABASE_API_KEY}"
                supabaseApi.updateTransaction(
                    apiKey = NetworkConstants.SUPABASE_API_KEY,
                    authorization = authHeader,
                    id = transaction.remoteId,
                    transaction = transaction
                )

                // Mark as synced if successful
                transactionDao.updateTransaction(
                    transaction.copy(synced = true)
                )
            } catch (e: Exception) {
                // Handle exception
                e.printStackTrace()
            }
        }
    }

    override suspend fun deleteTransaction(transactionId: Long) {
        val transaction = transactionDao.getTransactionById(transactionId)

        // Delete locally
        transactionDao.deleteTransaction(transactionId)

        // Try to delete from remote if we have a remote ID
        if (transaction?.remoteId != null) {
            try {
                val authHeader = "Bearer ${NetworkConstants.SUPABASE_API_KEY}"
                supabaseApi.deleteTransaction(
                    apiKey = NetworkConstants.SUPABASE_API_KEY,
                    authorization = authHeader,
                    id = transaction.remoteId
                )
            } catch (e: Exception) {
                // Handle exception
                e.printStackTrace()
            }
        }
    }

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions()
    }

    override suspend fun syncTransactions() {
        // Get all unsynced local transactions
        val unsyncedTransactions = transactionDao.getUnsyncedTransactions()

        // Try to sync each one
        unsyncedTransactions.forEach { transaction ->
            try {
                val authHeader = "Bearer ${NetworkConstants.SUPABASE_API_KEY}"

                if (transaction.remoteId == null) {
                    // Create new remote transaction
                    val response = supabaseApi.createTransaction(
                        apiKey = NetworkConstants.SUPABASE_API_KEY,
                        authorization = authHeader,
                        transaction = transaction
                    )

                    if (response.isSuccessful && response.body() != null) {
                        val remoteId = response.body()?.remoteId
                        if (remoteId != null) {
                            transactionDao.updateTransaction(
                                transaction.copy(
                                    remoteId = remoteId,
                                    synced = true
                                )
                            )
                        }
                    }
                } else {
                    // Update existing remote transaction
                    val response = supabaseApi.updateTransaction(
                        apiKey = NetworkConstants.SUPABASE_API_KEY,
                        authorization = authHeader,
                        id = transaction.remoteId,
                        transaction = transaction
                    )

                    if (response.isSuccessful) {
                        transactionDao.updateTransaction(
                            transaction.copy(synced = true)
                        )
                    }
                }
            } catch (e: Exception) {
                // Handle exception
                e.printStackTrace()
            }
        }
    }

    override suspend fun fetchTransactionsFromRemote() {
        try {
            val authHeader = "Bearer ${NetworkConstants.SUPABASE_API_KEY}"
            val response = supabaseApi.getTransactions(
                apiKey = NetworkConstants.SUPABASE_API_KEY,
                authorization = authHeader
            )

            if (response.isSuccessful && response.body() != null) {
                val remoteTransactions = response.body()!!

                // Get all local transactions
                val localTransactions = transactionDao.getAllTransactionsAsList()

                // Map remote IDs to local transactions
                val remoteIdToLocalMap = localTransactions.filter { it.remoteId != null }
                    .associateBy { it.remoteId!! }

                // Process each remote transaction
                remoteTransactions.forEach { remoteTransaction ->
                    val localTransaction = remoteIdToLocalMap[remoteTransaction.remoteId]

                    if (localTransaction == null) {
                        // This is a new transaction, insert locally
                        transactionDao.insertTransaction(
                            remoteTransaction.copy(synced = true)
                        )
                    } else if (localTransaction.synced) {
                        // This transaction is already synced, update locally
                        transactionDao.updateTransaction(
                            remoteTransaction.copy(
                                transactionId = localTransaction.transactionId,
                                synced = true
                            )
                        )
                    }
                    // If not synced, local changes take precedence
                }
            }
        } catch (e: Exception) {
            // Handle exception
            e.printStackTrace()
        }
    }
}