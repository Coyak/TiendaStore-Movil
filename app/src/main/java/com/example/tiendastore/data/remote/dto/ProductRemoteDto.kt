package com.example.tiendastore.data.remote.dto

data class ProductRemoteDto(
    val id: Int? = null,
    val name: String,
    val description: String? = null,
    val price: Double,
    val stock: Int,
    val category: String,
    val imageUrl: String? = null
)
