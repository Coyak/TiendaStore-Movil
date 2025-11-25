package com.example.tiendastore.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.tiendastore.ui.view.AdminAddScreen
import com.example.tiendastore.ui.view.AdminEditScreen
import com.example.tiendastore.ui.view.AdminListScreen
import com.example.tiendastore.ui.view.HomeScreen
import com.example.tiendastore.ui.view.LoginScreen
import com.example.tiendastore.ui.view.RegisterScreen
import com.example.tiendastore.ui.view.CartCheckoutScreen
import com.example.tiendastore.viewmodel.AuthViewModel
import com.example.tiendastore.viewmodel.ProductViewModel
import com.example.tiendastore.viewmodel.CartViewModel

enum class Screen { LOGIN, REGISTER, HOME, PRODUCT_DETAIL, EDIT_PROFILE, CART_CHECKOUT, ADMIN_LIST, ADMIN_ADD, ADMIN_EDIT }

@Composable
fun AppNavigation(authVM: AuthViewModel, productVM: ProductViewModel, cartVM: CartViewModel) {
    var screen by remember { mutableStateOf(Screen.LOGIN) }
    var selectedId by remember { mutableStateOf<Int?>(null) }
    val currentUser by authVM.currentUser.collectAsState()
    val productUi = productVM.ui.collectAsState().value

    LaunchedEffect(currentUser) {
        screen = if (currentUser == null) Screen.LOGIN else Screen.HOME
    }

    when (screen) {
        Screen.LOGIN -> LoginScreen(
            uiState = authVM.ui.collectAsState().value,
            onLogin = { email, p -> authVM.login(email, p) },
            onGoRegister = { screen = Screen.REGISTER }
        )
        Screen.REGISTER -> RegisterScreen(
            uiState = authVM.ui.collectAsState().value,
            onRegister = { name, email, password, isAdmin -> authVM.register(name, email, password, isAdmin) },
            onBack = { screen = Screen.LOGIN }
        )
        Screen.HOME -> HomeScreen(
            products = productVM.products.collectAsState().value,
            externalProducts = productVM.external.collectAsState().value,
            loading = productUi.loading,
            errorMessage = productUi.error,
            isAdmin = currentUser?.isAdmin == true,
            displayName = authVM.profile.collectAsState().value?.name ?: "",
            onLogout = { authVM.logout() },
            onAdmin = { screen = Screen.ADMIN_LIST },
            onEditProfile = { screen = Screen.EDIT_PROFILE },
            onProductClick = { id ->
                selectedId = id
                screen = Screen.PRODUCT_DETAIL
            },
            onRefresh = { productVM.refreshProducts() },
            cartCount = cartVM.totalItems.collectAsState().value,
            cartItems = cartVM.items.collectAsState().value,
            cartTotal = cartVM.totalPrice.collectAsState().value,
            onCartChangeQty = { id, q -> cartVM.changeQty(id, q) },
            onCartRemove = { id -> cartVM.remove(id) },
            onGoCheckout = { screen = Screen.CART_CHECKOUT }
        )
        Screen.PRODUCT_DETAIL -> {
            val products = productVM.products.collectAsState().value
            val product = products.firstOrNull { it.id == selectedId }
            com.example.tiendastore.ui.view.ProductDetailScreen(
                product = product,
                onBack = { screen = Screen.HOME },
                onAddToCart = { p -> if (p != null) cartVM.add(p, 1) },
                cartCount = cartVM.totalItems.collectAsState().value,
                cartItems = cartVM.items.collectAsState().value,
                cartTotal = cartVM.totalPrice.collectAsState().value,
                onCartChangeQty = { id, q -> cartVM.changeQty(id, q) },
                onCartRemove = { id -> cartVM.remove(id) },
                onGoCheckout = { screen = Screen.CART_CHECKOUT }
            )
        }
        Screen.CART_CHECKOUT -> {
            val items = cartVM.items.collectAsState().value
            val total = cartVM.totalPrice.collectAsState().value
            CartCheckoutScreen(
                items = items,
                total = total,
                onChangeQty = { id, q -> cartVM.changeQty(id, q) },
                onRemove = { id -> cartVM.remove(id) },
                onPay = {
                    cartVM.clear()
                    screen = Screen.HOME
                },
                onBack = { screen = Screen.HOME }
            )
        }
        Screen.EDIT_PROFILE -> {
            val user = authVM.profile.collectAsState().value
            com.example.tiendastore.ui.view.EditProfileScreen(
                user = user,
                errors = authVM.ui.collectAsState().value.errors,
                onSave = { name, email, address, city -> authVM.updateProfile(name, email, address, city) },
                onBack = { screen = Screen.HOME }
            )
        }
        Screen.ADMIN_LIST -> AdminListScreen(
            products = productVM.products.collectAsState().value,
            onAdd = {
                productVM.clearForm()
                screen = Screen.ADMIN_ADD
            },
            onEdit = { id ->
                selectedId = id
                screen = Screen.ADMIN_EDIT
            },
            onDelete = { id -> productVM.delete(id) },
            onBack = { screen = Screen.HOME }
        )
        Screen.ADMIN_ADD -> AdminAddScreen(
            form = productVM.form.collectAsState().value,
            onChange = { f, v -> productVM.onFieldChange(f, v) },
            onSave = {
                productVM.addOrUpdate()
                screen = Screen.ADMIN_LIST
            },
            onCancel = {
                productVM.clearForm()
                screen = Screen.ADMIN_LIST
            },
            onBack = { screen = Screen.ADMIN_LIST }
        )
        Screen.ADMIN_EDIT -> AdminEditScreen(
            id = selectedId,
            form = productVM.form.collectAsState().value,
            onStart = { id -> if (id != null) productVM.edit(id) },
            onChange = { f, v -> productVM.onFieldChange(f, v) },
            onSave = {
                productVM.addOrUpdate()
                screen = Screen.ADMIN_LIST
            },
            onCancel = {
                productVM.clearForm()
                screen = Screen.ADMIN_LIST
            },
            onBack = { screen = Screen.ADMIN_LIST }
        )
    }

    BackHandler(enabled = screen != Screen.LOGIN && screen != Screen.HOME) {
        screen = when (screen) {
            Screen.REGISTER -> Screen.LOGIN
            Screen.PRODUCT_DETAIL -> Screen.HOME
            Screen.ADMIN_LIST -> Screen.HOME
            Screen.ADMIN_ADD, Screen.ADMIN_EDIT -> Screen.ADMIN_LIST
            else -> screen
        }
    }
}
