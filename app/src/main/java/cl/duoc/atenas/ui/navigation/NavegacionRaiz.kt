package cl.duoc.atenas.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.atenas.AtenasApp
import cl.duoc.atenas.data.local.entity.Usuario
import cl.duoc.atenas.domain.rules.Permisos
import cl.duoc.atenas.ui.dashboard.DashboardEjecutivoScreen
import cl.duoc.atenas.ui.login.LoginScreen
import cl.duoc.atenas.ui.oportunidades.DetalleOportunidadScreen
import cl.duoc.atenas.ui.oportunidades.ListaOportunidadesScreen
import cl.duoc.atenas.ui.perfil.PerfilScreen
import cl.duoc.atenas.ui.supervisor.PanelSupervisorScreen

/**
 * Punto de entrada de la navegación.
 *
 * Sin sesión activa solo existe el login; con sesión activa se construye el
 * contenedor principal, cuyo contenido depende del rol del usuario.
 */
@Composable
fun NavegacionRaiz(modifier: Modifier = Modifier) {
    val aplicacion = LocalContext.current.applicationContext as AtenasApp
    val usuario by aplicacion.contenedor.sesion.usuarioActual.collectAsStateWithLifecycle()

    Crossfade(
        targetState = usuario,
        animationSpec = tween(durationMillis = 350),
        label = "sesion",
        modifier = modifier
    ) { sesionActual ->
        if (sesionActual == null) {
            LoginScreen()
        } else {
            ContenedorPrincipal(sesionActual)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContenedorPrincipal(usuario: Usuario) {
    val navController = rememberNavController()
    val items = itemsDeNavegacion(usuario.rol)
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route ?: Rutas.INICIO
    val esRutaPrincipal = items.any { it.ruta == rutaActual }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(tituloDeRuta(rutaActual, usuario.rol)) },
                navigationIcon = {
                    if (!esRutaPrincipal) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = rutaActual == item.ruta,
                        onClick = {
                            if (rutaActual != item.ruta) {
                                navController.navigate(item.ruta) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(item.icono, contentDescription = item.etiqueta) },
                        label = { Text(item.etiqueta) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Rutas.INICIO,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Rutas.INICIO) {
                // El contenido del inicio depende del perfil autenticado.
                if (Permisos.puedeVerPanelDeEquipo(usuario.rol)) {
                    PanelSupervisorScreen()
                } else {
                    DashboardEjecutivoScreen()
                }
            }

            composable(Rutas.OPORTUNIDADES) {
                ListaOportunidadesScreen(
                    alAbrirDetalle = { id ->
                        navController.navigate(Rutas.detalleOportunidad(id))
                    }
                )
            }

            composable(
                route = Rutas.DETALLE_OPORTUNIDAD,
                arguments = listOf(
                    navArgument(Rutas.ARG_OPORTUNIDAD) { type = NavType.LongType }
                )
            ) {
                DetalleOportunidadScreen()
            }

            composable(Rutas.PERFIL) {
                PerfilScreen(usuario = usuario)
            }
        }
    }
}
