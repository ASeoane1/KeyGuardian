package com.example.keyguardian

import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    //private const val BASE_URL = "https://proyecto-cumn-back-66f34t4snq-no.a.run.app/"
    private const val BASE_URL = "http://100.83.62.114:8080/"

    private val clientWithoutToken = OkHttpClient.Builder().build()

    private val retrofitWithoutToken = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(clientWithoutToken)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private fun getRetrofitClientWithToken(token: String): Retrofit {
        val clientWithToken = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder: Request.Builder = original.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .header("Content-Type", "application/json")
                val request: Request = requestBuilder.build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(clientWithToken)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getApiService(): ApiService {
        return retrofitWithoutToken.create(ApiService::class.java)
    }

    fun getApiServiceWithToken(token: String): ApiService {
        return getRetrofitClientWithToken(token).create(ApiService::class.java)
    }
}
