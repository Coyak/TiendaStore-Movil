package com.example.tiendastore.data.remote.api

import com.example.tiendastore.data.remote.dto.ProductRemoteDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApiService {
    @GET("/api/products")
    suspend fun getAll(@Query("search") search: String? = null): List<ProductRemoteDto>

    @GET("/api/products/{id}")
    suspend fun getById(@Path("id") id: Int): ProductRemoteDto

    @POST("/api/products")
    suspend fun create(@Body dto: ProductRemoteDto): ProductRemoteDto

    @PUT("/api/products/{id}")
    suspend fun update(@Path("id") id: Int, @Body dto: ProductRemoteDto): ProductRemoteDto

    @DELETE("/api/products/{id}")
    suspend fun delete(@Path("id") id: Int)
}
