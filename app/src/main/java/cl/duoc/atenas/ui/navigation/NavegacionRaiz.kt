package cl.duoc.atenas.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.atenas.AtenasApp
import cl.duoc.atenas.data.local.entity.Usuario
import cl.duoc.atenas.di.FabricasViewModel
import cl.duoc.atenas.domain.model.RolUsuario
import cl.duoc.atenas.domain.rules.Permisos
import cl.duoc.atenas.ui.agenda.AgendaScreen
import cl.duoc.atenas.ui.alertas.AlertasViewModel
import cl.duoc.atenas.ui.components.EncabezadoSaludo
import cl.duoc.atenas.ui.components.HojaAlertas
import cl.duoc.atenas.ui.components.gruposDeAlertas
import cl.duoc.atenas.ui.dashboard.DashboardEjecutivoScreen
import cl.duoc.atenas.ui.evidencias.EvidenciasScreen
import cl.duoc.atenas.ui.login.LoginScreen
import cl.duoc.atenas.ui.oportunidades.DetalleOportunidadScreen
import cl.duoc.atenas.ui.oportunidades.FormularioOportunidadScreen
import cl.duoc.atenas.ui.oportunidades.ListaOportunidadesScreen
import cl.duoc.atenas.ui.organizaciones.DetalleOrganizacionScreen
import cl.duoc.atenas.ui.organizaciones.FormularioContactoScreen
import cl.duoc.atenas.ui.organizaciones.FormularioOrganizacionScreen
import cl.duoc.atenas.ui.organizaciones.ListaOrganizacionesScreen
import cl.duoc.atenas.ui.perfil.PerfilScreen
import cl.duoc.atenas.ui.propuestas.PropuestasScreen
import cl.duoc.atenas.ui.seguimiento.RegistrarSeguimientoScreen
import cl.duoc.atenas.ui.supervisor.PanelSupervisorScreen
import cl.duoc.atenas.ui.theme.Espaciado
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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

    // Pestaña a la que pertenece la pantalla visible. No basta con mirar la
    // ruta actual: en un detalle o formulario la ruta ya no es la de la
    // pestaña, pero la pestaña sigue en la pila debajo. Inicio es el destino
    // de partida y siempre está en la pila, así que se revisan primero las
    // demás; si ninguna está, la pantalla cuelga de Inicio.
    val pestanaActual = remember(entradaActual) {
        items.map { it.ruta }
            .filter { it != Rutas.INICIO }
            .firstOrNull { ruta -> runCatching { navController.getBackStackEntry(ruta) }.isSuccess }
            ?: Rutas.INICIO
    }

    // Snackbar compartido: cualquier pantalla puede pedir mostrar un mensaje
    // de confirmación sin tener que montar su propio Scaffold.
    val estadoSnackbar = remember { SnackbarHostState() }
    val alcanceCorrutina = rememberCoroutineScope()
    val mostrarMensaje: (String) -> Unit = { mensaje ->
        alcanceCorrutina.launch { estadoSnackbar.showSnackbar(mensaje) }
    }

    // Abre una pestaña sin apilar copias: cada pestaña recuerda su propia
    // pila (popUpTo + saveState + restoreState). Tocar la pestaña en la que
    // ya se está vuelve a su pantalla raíz.
    val irAPestana: (String) -> Unit = { ruta ->
        if (pestanaActual == ruta) {
            navController.popBackStack(ruta, inclusive = false)
        } else {
            navController.navigate(ruta) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    // Alertas de la campana. La clave por usuario evita que, al cerrar sesión
    // y entrar con otra cuenta, se reutilice el ViewModel de la anterior.
    val alertasViewModel: AlertasViewModel = viewModel(
        key = "alertas-${usuario.id}",
        factory = FabricasViewModel.alertas
    )
    val alertas by alertasViewModel.alertas.collectAsStateWithLifecycle()
    var mostrarAlertas by rememberSaveable { mutableStateOf(false) }

    // El panel siempre muestra algo (los pendientes o "Estás al día"), así el
    // toque en la campana nunca parece no haber hecho nada.
    if (mostrarAlertas) {
        HojaAlertas(
            grupos = gruposDeAlertas(alertas),
            alCerrar = { mostrarAlertas = false },
            alAbrirOportunidad = { id -> navController.navigate(Rutas.detalleOportunidad(id)) }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(estadoSnackbar) },
        topBar = {
            // Las cuatro pestañas comparten un encabezado con el avatar (abre
            // el perfil), el título de la sección y la campana de alertas: así
            // el perfil y las alertas están a mano en toda la aplicación.
            // Las pantallas internas muestran un título centrado y la flecha
            // para volver.
            if (esRutaPrincipal) {
                val (subtitulo, titulo) = encabezadoDe(rutaActual, usuario)
                EncabezadoSaludo(
                    saludo = subtitulo,
                    nombre = titulo,
                    nombreAvatar = usuario.nombreCompleto,
                    alertas = alertas.size,
                    alTocarAvatar = { navController.navigate(Rutas.PERFIL) },
                    alTocarAlertas = { mostrarAlertas = true },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .statusBarsPadding()
                        .padding(horizontal = Espaciado.pantalla, vertical = 12.dp)
                )
            } else {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = tituloDeRuta(rutaActual, usuario.rol),
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    navigationIcon = {
                        if (!esRutaPrincipal) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                                    contentDescription = "Volver",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    // La barra se funde con el fondo: sin bloque de color, el
                    // acento queda reservado para lo importante.
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        },
        floatingActionButton = {
            // El "+" central de la barra crea oportunidades; en Empresas el
            // botón flotante crea organizaciones.
            if (rutaActual == Rutas.ORGANIZACIONES) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(Rutas.formularioOrganizacion()) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Nueva empresa") }
                )
            }
        },
        bottomBar = {
            // Los flujos de captura (formularios, registrar gestión) ocupan
            // toda la pantalla para que el usuario se concentre en completarlos.
            if (rutaActual !in rutasDeFlujo) {
                BarraInferior(
                    items = items,
                    pestanaActual = pestanaActual,
                    alSeleccionar = { item -> irAPestana(item.ruta) },
                    alCrear = { navController.navigate(Rutas.formularioOportunidad()) }
                )
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
                val alAbrirOportunidad: (Long) -> Unit = { id ->
                    navController.navigate(Rutas.detalleOportunidad(id))
                }
                val alRegistrarGestion: (Long) -> Unit = { id ->
                    navController.navigate(Rutas.registrarSeguimiento(id))
                }
                val alVerPropuestas = { navController.navigate(Rutas.PROPUESTAS) }
                if (Permisos.puedeVerPanelDeEquipo(usuario.rol)) {
                    PanelSupervisorScreen(
                        alAbrirOportunidad = alAbrirOportunidad,
                        alVerPropuestas = alVerPropuestas,
                        mostrarMensaje = mostrarMensaje
                    )
                } else {
                    DashboardEjecutivoScreen(
                        alAbrirOportunidad = alAbrirOportunidad,
                        alRegistrarGestion = alRegistrarGestion,
                        alVerCartera = { irAPestana(Rutas.OPORTUNIDADES) }
                    )
                }
            }

            // Agenda: eventos por fecha, para anticiparse.
            composable(Rutas.AGENDA) {
                AgendaScreen(
                    alAbrirOportunidad = { id -> navController.navigate(Rutas.detalleOportunidad(id)) }
                )
            }

            // Propuestas enviadas (HU-16), abierta desde los tableros.
            composable(Rutas.PROPUESTAS) {
                PropuestasScreen(
                    alAbrirOportunidad = { id -> navController.navigate(Rutas.detalleOportunidad(id)) }
                )
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
                    alAbrirOrganizacion = { id -> navController.navigate(Rutas.detalleOrganizacion(id)) },
                    alRegistrarGestion = { id ->
                        navController.navigate(Rutas.registrarSeguimiento(id))
                    },
                    alVerEvidencias = { id -> navController.navigate(Rutas.evidencias(id)) },
                    mostrarMensaje = mostrarMensaje
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
                    // Al guardar, esos formularios vuelven atrás: de regreso al paso 3.
                    alCrearOrganizacion = { navController.navigate(Rutas.formularioOrganizacion()) },
                    alCrearContacto = { organizacionId -> navController.navigate(Rutas.formularioContacto(organizacionId)) },
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

            // Evidencias fotográficas de una oportunidad.
            composable(
                route = Rutas.EVIDENCIAS,
                arguments = listOf(
                    navArgument(Rutas.ARG_OPORTUNIDAD) { type = NavType.LongType }
                )
            ) {
                EvidenciasScreen(mostrarMensaje = mostrarMensaje)
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

/** Subtítulo y título del encabezado común de cada pestaña. */
private fun encabezadoDe(ruta: String, usuario: Usuario): Pair<String, String> {
    val locale = Locale.forLanguageTag("es-CL")
    val supervisor = usuario.rol == RolUsuario.SUPERVISOR
    return when (ruta) {
        Rutas.AGENDA -> SimpleDateFormat("MMMM yyyy", locale).format(Date())
            .replaceFirstChar { it.uppercase() } to "Agenda"
        Rutas.OPORTUNIDADES -> (if (supervisor) "Todo el equipo" else "Asignadas a ti") to
            (if (supervisor) "Cartera comercial" else "Mi cartera")
        Rutas.ORGANIZACIONES -> "Organizaciones y contactos" to "Empresas"
        else -> SimpleDateFormat("EEEE d 'de' MMMM", locale).format(Date())
            .replaceFirstChar { it.uppercase() } to
            (if (supervisor) "Panel del equipo" else "Hola, ${usuario.nombreCompleto.substringBefore(" ")}")
    }
}
