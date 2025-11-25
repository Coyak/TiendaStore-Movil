package com.example.tiendastore.data.remote.api

import com.example.tiendastore.data.remote.dto.ExternalProductDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ExternalApiService {
    @GET("products")
    suspend fun getProducts(@Query("limit") limit: Int = 4): List<ExternalProductDto>
}
