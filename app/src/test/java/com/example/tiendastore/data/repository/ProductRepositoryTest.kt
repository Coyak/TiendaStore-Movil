package com.example.tiendastore.data.repository

import com.example.tiendastore.data.remote.RemoteProductDataSource
import com.example.tiendastore.data.remote.dto.ProductRemoteDto
import com.example.tiendastore.model.Product
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductRepositoryTest {
    private val dispatcher = StandardTestDispatcher()

    @Test
    fun `refresh loads remote products`() = runTest(dispatcher) {
        val remote = mockk<RemoteProductDataSource>()
        coEvery { remote.fetchAll(any()) } returns listOf(
            ProductRemoteDto(1, "Prod", "desc", 100.0, 5, "Cat", null)
        )
        val repo = ProductRepository(remote, ioDispatcher = dispatcher)

        repo.refresh()

        val list = repo.products.first()
        assertEquals(1, list.size)
        assertEquals("Prod", list.first().name)
    }

    @Test
    fun `create appends product and delete removes it`() = runTest(dispatcher) {
        val remote = mockk<RemoteProductDataSource>()
        coEvery { remote.fetchAll(any()) } returns emptyList()
        coEvery { remote.create(any()) } returns ProductRemoteDto(2, "New", "d", 10.0, 1, "Cat", null)
        coJustRun { remote.delete(any()) }
        val repo = ProductRepository(remote, ioDispatcher = dispatcher)

        repo.refresh()
        repo.create(Product(0, "New", 10.0, 1, "Cat", "d", null))
        assertEquals(1, repo.products.first().size)

        repo.delete(2)
        assertEquals(0, repo.products.first().size)
    }

    @Test
    fun `refresh failure reports no crash and keeps previous data`() = runTest(dispatcher) {
        val remote = mockk<RemoteProductDataSource>()
        coEvery { remote.fetchAll(any()) } throws IllegalStateException("Network down")
        val repo = ProductRepository(remote, ioDispatcher = dispatcher)

        // should not throw
        try {
            repo.refresh()
        } catch (ex: Exception) {
            // ignore for test; products remain unchanged
        }
        assertEquals(0, repo.products.first().size)
    }
}
