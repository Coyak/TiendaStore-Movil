package com.example.tiendastore.data.repository

import com.example.tiendastore.data.ProductDao
import com.example.tiendastore.data.toDomain
import com.example.tiendastore.data.toEntity
import com.example.tiendastore.data.remote.RemoteProductDataSource
import com.example.tiendastore.data.remote.toDomain
import com.example.tiendastore.data.remote.toRemote
import com.example.tiendastore.model.Product
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class ProductRepository(
    private val remote: RemoteProductDataSource = RemoteProductDataSource(),
    private val productDao: ProductDao? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    fun localFlow() = productDao?.observeAll()

    suspend fun refresh(search: String? = null) = withContext(ioDispatcher) {
        val remoteItems = remote.fetchAll(search).map { it.toDomain() }
        syncLocal(remoteItems)
        _products.value = remoteItems
    }

    suspend fun create(product: Product): Product = withContext(ioDispatcher) {
        val created = remote.create(product.toRemote()).toDomain()
        productDao?.insert(created.toEntity())
        _products.value = _products.value + created
        created
    }

    suspend fun update(product: Product): Product = withContext(ioDispatcher) {
        val updated = remote.update(product.id, product.toRemote()).toDomain()
        val existingImage = productDao?.getByIdOnce(updated.id)?.imagePath
        productDao?.update(updated.toEntity(existingImagePath = existingImage))
        _products.value = _products.value.map { if (it.id == updated.id) updated else it }
        updated
    }

    suspend fun delete(id: Int) = withContext(ioDispatcher) {
        remote.delete(id)
        productDao?.deleteById(id)
        _products.value = _products.value.filterNot { it.id == id }
    }

    private suspend fun syncLocal(remoteItems: List<Product>) {
        val dao = productDao ?: return
        val local = dao.getAllOnce()
        val remoteIds = remoteItems.map { it.id }.toSet()
        // Remove items that no longer exist remotely
        local.filter { it.id !in remoteIds }.forEach { dao.delete(it) }

        val existingById = local.associateBy { it.id }
        remoteItems.forEach { product ->
            val existingImage = existingById[product.id]?.imagePath
            dao.insert(product.toEntity(existingImagePath = existingImage))
        }
    }
}
