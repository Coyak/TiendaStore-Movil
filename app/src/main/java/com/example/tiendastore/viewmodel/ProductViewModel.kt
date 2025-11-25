package com.example.tiendastore.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiendastore.data.DataBaseHelper
import com.example.tiendastore.data.toDomain
import com.example.tiendastore.data.repository.ProductRepository
import com.example.tiendastore.data.repository.ExternalProductRepository
import com.example.tiendastore.domain.validation.ProductValidator
import com.example.tiendastore.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

data class ProductFormState(
    val id: Int? = null,
    val name: String = "",
    val price: String = "",
    val stock: String = "",
    val category: String = "Consolas",
    val description: String = "",
    val imagePath: String = "",
    val errors: Map<String, String> = emptyMap(),
    val isValid: Boolean = false
)

data class ProductUiState(
    val loading: Boolean = false,
    val error: String? = null
)

class ProductViewModel(
    application: Application,
    private val repository: ProductRepository = ProductRepository(productDao = DataBaseHelper.db(application).productDao()),
    private val externalRepo: ExternalProductRepository = ExternalProductRepository()
) : AndroidViewModel(application) {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _external = MutableStateFlow<List<Product>>(emptyList())
    val external: StateFlow<List<Product>> = _external.asStateFlow()

    private val _form = MutableStateFlow(ProductFormState())
    val form: StateFlow<ProductFormState> = _form.asStateFlow()

    private val _ui = MutableStateFlow(ProductUiState())
    val ui: StateFlow<ProductUiState> = _ui.asStateFlow()

    private val categories = listOf("Consolas", "Juegos", "Accesorios", "Otros")

    init {
        viewModelScope.launch {
            val localFlow = repository.localFlow()
            if (localFlow != null) {
                localFlow.collectLatest { entities -> _products.value = entities.map { it.toDomain() } }
            } else {
                repository.products.collectLatest { list -> _products.value = list }
            }
        }
        viewModelScope.launch { refreshProducts() }
        viewModelScope.launch {
            runCatching { externalRepo.fetch() }
        }
        viewModelScope.launch {
            externalRepo.external.collect { list -> _external.value = list }
        }
    }

    fun onFieldChange(field: String, value: String) {
        val current = _form.value
        val updated = when (field) {
            "name" -> current.copy(name = value)
            "price" -> current.copy(price = value)
            "stock" -> current.copy(stock = value)
            "category" -> current.copy(category = value)
            "description" -> current.copy(description = value)
            "imagePath" -> current.copy(imagePath = value)
            else -> current
        }
        _form.value = validate(updated)
    }

    fun refreshProducts(search: String? = null) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)
            val result = runCatching { repository.refresh(search) }
            _ui.value = _ui.value.copy(
                loading = false,
                error = result.exceptionOrNull()?.message ?: result.exceptionOrNull()?.localizedMessage
            )
        }
    }

    private fun validate(form: ProductFormState): ProductFormState {
        val errors = ProductValidator.validate(form.name, form.price, form.stock, form.category)
        return form.copy(errors = errors, isValid = errors.isEmpty())
    }

    fun addOrUpdate() {
        viewModelScope.launch {
            val f = _form.value
            val vf = validate(f)
            _form.value = vf
            if (!vf.isValid) return@launch

            val prod = Product(
                id = vf.id ?: 0,
                name = vf.name.trim(),
                price = vf.price.replace(",", ".").toDouble(),
                stock = vf.stock.toInt(),
                category = vf.category,
                description = vf.description.trim(),
                imagePath = vf.imagePath.takeIf { it.isNotBlank() }
            )
            runCatching {
                _ui.value = _ui.value.copy(loading = true, error = null)
                if (prod.id == 0) repository.create(prod) else repository.update(prod)
            }
                .onSuccess { clearForm() }
                .onFailure { err -> _ui.value = _ui.value.copy(error = err.message) }
            _ui.value = _ui.value.copy(loading = false)
        }
    }

    fun edit(id: Int) {
        viewModelScope.launch {
            val entity = _products.value.firstOrNull { it.id == id } ?: return@launch
            _form.value = ProductFormState(
                id = entity.id,
                name = entity.name,
                price = entity.price.toString(),
                stock = entity.stock.toString(),
                category = entity.category,
                description = entity.description,
                imagePath = entity.imagePath.orEmpty(),
                errors = emptyMap(),
                isValid = true
            )
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            runCatching {
                _ui.value = _ui.value.copy(loading = true, error = null)
                repository.delete(id)
            }.onFailure { err -> _ui.value = _ui.value.copy(error = err.message) }
            _ui.value = _ui.value.copy(loading = false)
        }
    }

    fun clearForm() {
        _form.value = ProductFormState()
    }
}
