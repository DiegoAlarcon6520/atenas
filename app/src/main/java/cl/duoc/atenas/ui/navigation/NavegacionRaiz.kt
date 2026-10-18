package cl.duoc.atenas.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import cl.duoc.atenas.ui.oportunidades.FormularioOportunidadScreen
import cl.duoc.atenas.ui.oportunidades.ListaOportunidadesScreen
import cl.duoc.atenas.ui.organizaciones.DetalleOrganizacionScreen
import cl.duoc.atenas.ui.organizaciones.FormularioContactoScreen
import cl.duoc.atenas.ui.organizaciones.FormularioOrganizacionScreen
import cl.duoc.atenas.ui.organizaciones.ListaOrganizacionesScreen
import cl.duoc.atenas.ui.perfil.PerfilScreen
import cl.duoc.atenas.ui.seguimiento.RegistrarSeguimientoScreen
import cl.duoc.atenas.ui.supervisor.PanelSupervisorScreen
import kotlinx.coroutines.launch

/**
 * NAVEGACIÓN RAÍZ
 *
 * Decide qué se muestra según haya o no sesión activa:
 *  - sin sesión -> solo existe el login,
 *  - con sesión -> se construye el contenedor principal con sus pestañas.
 *
 * Al no declarar el login como un destino más del grafo, es imposible llegar
 * a una pantalla interna sin estar autenticado: simplemente no existe ninguna
 * ruta hacia ella.
 */
@Composable
fun NavegacionRaiz(modifier: Modifier = Modifier) {
    // La sesión vive en el contenedor de dependencias de la Application, así
    // que sobrevive a cambios de pantalla y de configuración.
    val aplicacion = LocalContext.current.applicationContext as AtenasApp
    val usuario by aplicacion.contenedor.sesion.usuarioActual.collectAsStateWithLifecycle()

    // Crossfade: primera mecánica de animación. Funde login <-> aplicación en
    // lugar de cortar bruscamente al iniciar o cerrar sesión.
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

/**
 * Estructura común de la aplicación autenticada: barra superior, barra
 * inferior, botón de acción y el NavHost con todas las pantallas internas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContenedorPrincipal(usuario: Usuario) {
    val navController = rememberNavController()

    // Las pestañas de abajo dependen del rol: ver Rutas.kt.
    val items = itemsDeNavegacion(usuario.rol)

    // Ruta activa: sirve para marcar la pestaña, elegir el título y decidir
    // si corresponde mostrar la flecha de volver o el botón flotante.
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route ?: Rutas.INICIO
    val esRutaPrincipal = items.any { it.ruta == rutaActual }

    // Snackbar compartido: cualquier pantalla puede pedir mostrar un mensaje
    // de confirmación sin tener que montar su propio Scaffold.
    val estadoSnackbar = remember { SnackbarHostState() }
    val alcanceCorrutina = rememberCoroutineScope()
    val mostrarMensaje: (String) -> Unit = { mensaje ->
        alcanceCorrutina.launch { estadoSnackbar.showSnackbar(mensaje) }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(estadoSnackbar) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(tituloDeRuta(rutaActual, usuario.rol)) },
                navigationIcon = {
                    // La flecha de volver solo aparece en pantallas que se
                    // abrieron encima de una pestaña (detalle, formulario).
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
        floatingActionButton = {
            // El botón de crear cambia de destino según la pestaña activa,
            // y no aparece en las pantallas donde no tendría sentido.
            when (rutaActual) {
                Rutas.OPORTUNIDADES -> if (Permisos.puedeCrearOportunidad(usuario.rol)) {
                    ExtendedFloatingActionButton(
                        onClick = { navController.navigate(Rutas.formularioOportunidad()) },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Nueva") }
                    )
                }

                Rutas.ORGANIZACIONES -> ExtendedFloatingActionButton(
                    onClick = { navController.navigate(Rutas.formularioOrganizacion()) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Nueva") }
                )
            }
        },
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = rutaActual == item.ruta,
                        onClick = {
                            if (rutaActual != item.ruta) {
                                navController.navigate(item.ruta) {
                                    // popUpTo + saveState: al cambiar de
                                    // pestaña no se apilan copias y cada una
                                    // recuerda dónde iba el scroll.
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

            // Inicio: el contenido depende del perfil autenticado.
            composable(Rutas.INICIO) {
                if (Permisos.puedeVerPanelDeEquipo(usuario.rol)) {
                    PanelSupervisorScreen()
                } else {
                    DashboardEjecutivoScreen()
                }
            }

            // Listado con búsqueda y filtros.
            composable(Rutas.OPORTUNIDADES) {
                ListaOportunidadesScreen(
                    alAbrirDetalle = { id -> navController.navigate(Rutas.detalleOportunidad(id)) }
                )
            }

            // Detalle: recibe el id por la ruta.
            composable(
                route = Rutas.DETALLE_OPORTUNIDAD,
                arguments = listOf(
                    navArgument(Rutas.ARG_OPORTUNIDAD) { type = NavType.LongType }
                )
            ) {
                DetalleOportunidadScreen(
                    alEditar = { id -> navController.navigate(Rutas.formularioOportunidad(id)) },
                    alRegistrarGestion = { id ->
                        navController.navigate(Rutas.registrarSeguimiento(id))
                    }
                )
            }

            // Formulario: mismo destino para crear y editar. El valor por
            // defecto -1 indica "nueva oportunidad".
            composable(
                route = Rutas.FORMULARIO_OPORTUNIDAD,
                arguments = listOf(
                    navArgument(Rutas.ARG_OPORTUNIDAD) {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) {
                FormularioOportunidadScreen(
                    alTerminar = { navController.popBackStack() },
                    mostrarMensaje = mostrarMensaje
                )
            }

            // Registro de gestiones sobre una oportunidad.
            composable(
                route = Rutas.REGISTRAR_SEGUIMIENTO,
                arguments = listOf(
                    navArgument(Rutas.ARG_OPORTUNIDAD) { type = NavType.LongType }
                )
            ) {
                RegistrarSeguimientoScreen(
                    alTerminar = { navController.popBackStack() },
                    mostrarMensaje = mostrarMensaje
                )
            }

            // ----- Organizaciones y contactos -----

            composable(Rutas.ORGANIZACIONES) {
                ListaOrganizacionesScreen(
                    alAbrirDetalle = { id -> navController.navigate(Rutas.detalleOrganizacion(id)) }
                )
            }

            composable(
                route = Rutas.DETALLE_ORGANIZACION,
                arguments = listOf(
                    navArgument(Rutas.ARG_ORGANIZACION) { type = NavType.LongType }
                )
            ) {
                DetalleOrganizacionScreen(
                    alEditarOrganizacion = { id ->
                        navController.navigate(Rutas.formularioOrganizacion(id))
                    },
                    alAgregarContacto = { organizacionId ->
                        navController.navigate(Rutas.formularioContacto(organizacionId))
                    },
                    alEditarContacto = { organizacionId, contactoId ->
                        navController.navigate(Rutas.formularioContacto(organizacionId, contactoId))
                    },
                    alAbrirOportunidad = { id ->
                        navController.navigate(Rutas.detalleOportunidad(id))
                    }
                )
            }

            composable(
                route = Rutas.FORMULARIO_ORGANIZACION,
                arguments = listOf(
                    navArgument(Rutas.ARG_ORGANIZACION) {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) {
                FormularioOrganizacionScreen(
                    alTerminar = { navController.popBackStack() },
                    mostrarMensaje = mostrarMensaje
                )
            }

            composable(
                route = Rutas.FORMULARIO_CONTACTO,
                arguments = listOf(
                    navArgument(Rutas.ARG_ORGANIZACION) { type = NavType.LongType },
                    navArgument(Rutas.ARG_CONTACTO) {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) {
                FormularioContactoScreen(
                    alTerminar = { navController.popBackStack() },
                    mostrarMensaje = mostrarMensaje
                )
            }

            composable(Rutas.PERFIL) {
                PerfilScreen(usuario = usuario)
            }
        }
    }
}
