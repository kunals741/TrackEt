package com.papaya.tracket.di

import com.papaya.tracket.domain.MainRepository
import com.papaya.tracket.domain.MainRepositoryImpl
import com.papaya.tracket.data.local.dao.TransactionDao
import com.papaya.tracket.data.remote.SupabaseApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMainRepository(
        transactionDao: TransactionDao,
        supabaseApi: SupabaseApi
    ): MainRepository {
        return MainRepositoryImpl(transactionDao, supabaseApi)
    }
}