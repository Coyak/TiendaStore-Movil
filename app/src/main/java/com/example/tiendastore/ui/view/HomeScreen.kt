package com.example.tiendastore.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Arrangement as ColArrangement
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tiendastore.model.Product
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.ShoppingCart
import com.example.tiendastore.model.CartItem
import com.example.tiendastore.ui.view.components.CartQuickSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    products: List<Product>,
    externalProducts: List<Product>,
    loading: Boolean,
    errorMessage: String?,
    isAdmin: Boolean,
    displayName: String,
    onLogout: () -> Unit,
    onAdmin: () -> Unit,
    onEditProfile: () -> Unit,
    onProductClick: (Int) -> Unit,
    onRefresh: () -> Unit,
    cartCount: Int,
    cartItems: List<CartItem>,
    cartTotal: Double,
    onCartChangeQty: (Int, Int) -> Unit,
    onCartRemove: (Int) -> Unit,
    onGoCheckout: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showCart by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = if (displayName.isNotBlank()) "Hola, $displayName" else "Hola",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                Divider()
                NavigationDrawerItem(
                    label = { Text("Editar perfil") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onEditProfile()
                    },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors()
                )
                if (isAdmin) {
                    NavigationDrawerItem(
                        label = { Text("Panel admin") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onAdmin()
                        },
                        icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) }
                    )
                }
                NavigationDrawerItem(
                    label = { Text("Cerrar sesión") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) }
                )
            }
        }
    ) {
        Scaffold(topBar = {
            TopAppBar(
                title = { Text("TiendaStore") },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                },
                actions = {
                    IconButton(onClick = { showCart = true }) {
                        BadgedBox(badge = {
                            if (cartCount > 0) Badge { Text(cartCount.toString()) }
                        }) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                        }
                    }
                }
            )
        }) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                }
                if (!errorMessage.isNullOrBlank()) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = onRefresh) { Text("Reintentar") }
                    Spacer(Modifier.height(8.dp))
                }
                if (externalProducts.isNotEmpty()) {
                    Text("Novedades (API externa)", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = ColArrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.height(240.dp)
                    ) {
                        items(externalProducts, key = { it.id }) { p ->
                            androidx.compose.material3.Card(onClick = { }) {
                                com.example.tiendastore.ui.view.components.ImageFromPath(
                                    p.imagePath,
                                    Modifier.fillMaxWidth().aspectRatio(1f)
                                )
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(p.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                                    Text("${formatPriceCLP(p.price)}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Divider()
                    Spacer(Modifier.height(16.dp))
                }

                Text("Productos", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Divider()
                Spacer(Modifier.height(8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = ColArrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products, key = { it.id }) { p ->
                        androidx.compose.material3.Card(
                            onClick = { onProductClick(p.id) }
                        ) {
                            com.example.tiendastore.ui.view.components.ImageFromPath(
                                p.imagePath,
                                Modifier.fillMaxWidth().aspectRatio(1f)
                            )
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(p.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                                Text("${formatPriceCLP(p.price)}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCart) {
        CartQuickSheet(
            items = cartItems,
            total = cartTotal,
            onChangeQty = onCartChangeQty,
            onRemove = onCartRemove,
            onGoCheckout = onGoCheckout,
            onDismiss = { showCart = false }
        )
    }
}

private fun formatPriceCLP(price: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale("es", "CL"))
    nf.maximumFractionDigits = 0
    nf.minimumFractionDigits = 0
    return "$" + nf.format(price)
}
