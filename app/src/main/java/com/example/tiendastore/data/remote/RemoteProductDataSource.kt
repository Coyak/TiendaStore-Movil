package com.example.tiendastore.data.remote

import com.example.tiendastore.data.remote.api.ProductApiService
import com.example.tiendastore.data.remote.dto.ProductRemoteDto

class RemoteProductDataSource(
    private val api: ProductApiService = RetrofitProvider.productApi
    ) {
    suspend fun fetchAll(search: String? = null): List<ProductRemoteDto> = api.getAll(search)
    suspend fun fetchById(id: Int): ProductRemoteDto = api.getById(id)
    suspend fun create(dto: ProductRemoteDto): ProductRemoteDto = api.create(dto)
    suspend fun update(id: Int, dto: ProductRemoteDto): ProductRemoteDto = api.update(id, dto)
    suspend fun delete(id: Int) = api.delete(id)
}
