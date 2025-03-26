package com.papaya.tracket.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val transactionId: Long = 0,
    val transactionName : String?,
    val amount: Double,
    val description: String?,
    val categoryId: Long?,
    val date: Long?,
    val isDebit: Boolean = true,
    //todo: will use this for offline and online sync
    val synced: Boolean = false,
    val remoteId: String? = null
)
