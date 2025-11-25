package com.example.tiendastore.data.remote.dto

data class ExternalProductDto(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String
)
