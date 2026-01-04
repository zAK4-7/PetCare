package com.petcare.app.data.api

import android.content.Context
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL = "http://10.0.2.2:3000/"

    private lateinit var tokenStore: TokenStore

    fun init(context: Context) {
        tokenStore = TokenStore(context.applicationContext)
    }

    private val authInterceptor = Interceptor { chain ->
        val original: Request = chain.request()

        val token: String? = runBlocking {
            // prend le token stocké (ou null)
            tokenStore.tokenFlow.first()
        }

        val newReq = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        chain.proceed(newReq)
    }

    private val okHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
