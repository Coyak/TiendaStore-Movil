package com.example.tiendastore.viewmodel

import android.app.Application
import com.example.tiendastore.data.repository.ProductRepository
import com.example.tiendastore.model.Product
import com.example.tiendastore.data.repository.ExternalProductRepository
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProductViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val productFlow = MutableStateFlow<List<Product>>(emptyList())
    private val app: Application = mockk(relaxed = true)
    private lateinit var repo: ProductRepository
    private lateinit var externalRepo: ExternalProductRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = mockk(relaxed = true)
        every { repo.localFlow() } returns null
        every { repo.products } returns productFlow
        externalRepo = mockk(relaxed = true)
        coJustRun { externalRepo.fetch(any()) }
        every { externalRepo.external } returns MutableStateFlow(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `refresh success updates products state`() = runTest(dispatcher) {
        val remoteList = listOf(Product(1, "Test", 10.0, 1, "Cat", "desc", null))
        coEvery { repo.refresh(any()) } coAnswers { productFlow.value = remoteList }

        val vm = ProductViewModel(app, repository = repo, externalRepo = externalRepo)

        advanceUntilIdle()

        assertEquals(1, vm.products.value.size)
        assertEquals("Test", vm.products.value.first().name)
        assertTrue { vm.ui.value.error == null }
    }

    @Test
    fun `refresh failure exposes error state`() = runTest(dispatcher) {
        coEvery { repo.refresh(any()) } throws IllegalStateException("Network down")

        val vm = ProductViewModel(app, repository = repo, externalRepo = externalRepo)

        advanceUntilIdle()

        assertEquals("Network down", vm.ui.value.error)
        assertTrue { vm.products.value.isEmpty() }
    }
}
