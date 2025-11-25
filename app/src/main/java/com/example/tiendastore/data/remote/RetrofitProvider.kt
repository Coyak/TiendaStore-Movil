package com.example.tiendastore.data.remote

import com.example.tiendastore.BuildConfig
import com.example.tiendastore.data.remote.api.ProductApiService
import com.example.tiendastore.data.remote.api.AuthApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitProvider {
    // Interceptor para adjuntar token JWT si existe
    private val authInterceptor = Interceptor { chain ->
        val token = AuthTokenStore.get()
        val builder = chain.request().newBuilder()
        if (!token.isNullOrBlank()) {
            builder.addHeader("Authorization", "Bearer $token")
        }
        chain.proceed(builder.build())
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor(authInterceptor)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val productApi: ProductApiService by lazy { retrofit.create(ProductApiService::class.java) }
    val authApi: AuthApiService by lazy { retrofit.create(AuthApiService::class.java) }
}
