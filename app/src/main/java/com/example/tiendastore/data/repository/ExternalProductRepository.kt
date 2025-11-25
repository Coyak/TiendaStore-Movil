package com.example.tiendastore.data.repository

import com.example.tiendastore.data.remote.ExternalRetrofitProvider
import com.example.tiendastore.data.remote.dto.ExternalProductDto
import com.example.tiendastore.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExternalProductRepository {
    private val _external = MutableStateFlow<List<Product>>(emptyList())
    val external: StateFlow<List<Product>> = _external.asStateFlow()

    private val api = ExternalRetrofitProvider.api

    suspend fun fetch(limit: Int = 4) {
        val list = api.getProducts(limit).map { it.toDomain() }
        _external.value = list
    }
}

private fun ExternalProductDto.toDomain(): Product = Product(
    id = id,
    name = title,
    price = price,
    stock = 0,
    category = category,
    description = description,
    imagePath = image
)
