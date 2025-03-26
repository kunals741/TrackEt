package com.papaya.tracket.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.papaya.tracket.data.local.dao.TransactionDao
import com.papaya.tracket.data.local.entity.Transaction

@Database(
    entities = [Transaction::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao() : TransactionDao
}