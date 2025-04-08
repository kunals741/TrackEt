package com.papaya.tracket.data.remote

import com.papaya.tracket.data.local.entity.Transaction
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseApi {
    @GET("rest/v1/transactions")
    suspend fun getTransactions(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String
    ): Response<List<Transaction>>

    @POST("rest/v1/transactions")
    suspend fun createTransaction(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Body transaction: Transaction
    ): Response<Transaction>

    @PATCH("rest/v1/transactions")
    suspend fun updateTransaction(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("id") id: String,
        @Body transaction: Transaction
    ): Response<Transaction>

    @DELETE("rest/v1/transactions")
    suspend fun deleteTransaction(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("id") id: String
    ): Response<Void>
}