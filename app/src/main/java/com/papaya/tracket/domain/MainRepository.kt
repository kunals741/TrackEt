package com.papaya.tracket.domain

import com.papaya.tracket.data.local.dao.TransactionDao
import com.papaya.tracket.data.remote.SupabaseApi
import javax.inject.Inject

interface MainRepository {
    //todo
}

class MainRepositoryImpl @Inject constructor(
    transactionDao: TransactionDao,
    supabaseApi: SupabaseApi
) : MainRepository {
    //todo
}