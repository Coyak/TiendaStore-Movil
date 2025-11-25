package com.example.tiendastore.data.remote

import com.example.tiendastore.data.remote.dto.ProductRemoteDto
import com.example.tiendastore.model.Product

fun ProductRemoteDto.toDomain(): Product = Product(
    id = id ?: 0,
    name = name,
    price = price,
    stock = stock,
    category = category,
    description = description.orEmpty(),
    imagePath = imageUrl
)

fun Product.toRemote(): ProductRemoteDto = ProductRemoteDto(
    id = id.takeIf { it != 0 },
    name = name,
    description = description,
    price = price,
    stock = stock,
    category = category,
    imageUrl = imagePath
)
