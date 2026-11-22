package cl.duoc.atenas.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector
import cl.duoc.atenas.domain.model.RolUsuario

object Rutas {
    const val INICIO = "inicio"
    const val OPORTUNIDADES = "oportunidades"
    const val AGENDA = "agenda"
    const val PERFIL = "perfil"

    const val ARG_OPORTUNIDAD = "oportunidadId"
    const val DETALLE_OPORTUNIDAD = "oportunidad/{$ARG_OPORTUNIDAD}"

    /**
     * Ruta del formulario. Usa un argumento opcional: cuando llega -1 el
     * formulario se abre en blanco (crear); con un id real, precargado (editar).
     */
    const val FORMULARIO_OPORTUNIDAD = "formulario?$ARG_OPORTUNIDAD={$ARG_OPORTUNIDAD}"

    fun detalleOportunidad(id: Long): String = "oportunidad/$id"

    fun formularioOportunidad(id: Long? = null): String =
        "formulario?$ARG_OPORTUNIDAD=${id ?: -1L}"

    // ----- Seguimiento -----
    const val REGISTRAR_SEGUIMIENTO = "seguimiento/{$ARG_OPORTUNIDAD}"

    fun registrarSeguimiento(oportunidadId: Long): String = "seguimiento/$oportunidadId"

    // ----- Evidencias fotográficas -----
    const val EVIDENCIAS = "evidencias/{$ARG_OPORTUNIDAD}"

    fun evidencias(oportunidadId: Long): String = "evidencias/$oportunidadId"

    // ----- Propuestas enviadas (HU-16) -----
    const val PROPUESTAS = "propuestas"

    // ----- Organizaciones y contactos -----
    const val ORGANIZACIONES = "organizaciones"
    const val ARG_ORGANIZACION = "organizacionId"
    const val ARG_CONTACTO = "contactoId"

    const val DETALLE_ORGANIZACION = "organizacion/{$ARG_ORGANIZACION}"
    const val FORMULARIO_ORGANIZACION = "formulario-organizacion?$ARG_ORGANIZACION={$ARG_ORGANIZACION}"
    const val FORMULARIO_CONTACTO = "formulario-contacto/{$ARG_ORGANIZACION}?$ARG_CONTACTO={$ARG_CONTACTO}"

    fun detalleOrganizacion(id: Long): String = "organizacion/$id"

    fun formularioOrganizacion(id: Long? = null): String =
        "formulario-organizacion?$ARG_ORGANIZACION=${id ?: -1L}"

    /** [contactoId] nulo abre el formulario en blanco para un contacto nuevo. */
    fun formularioContacto(organizacionId: Long, contactoId: Long? = null): String =
        "formulario-contacto/$organizacionId?$ARG_CONTACTO=${contactoId ?: -1L}"
}

data class ItemNavegacion(
    val ruta: String,
    val etiqueta: String,
    val icono: ImageVector
)

/**
 * Pestañas de la barra inferior. El perfil no es una pestaña: se abre desde
 * el avatar del encabezado, y el botón "+" central (fuera de esta lista)
 * registra una oportunidad nueva.
 */
fun itemsDeNavegacion(rol: RolUsuario): List<ItemNavegacion> = listOf(
    ItemNavegacion(
        Rutas.INICIO,
        if (rol == RolUsuario.SUPERVISOR) "Equipo" else "Inicio",
        Icons.Default.Home
    ),
    ItemNavegacion(Rutas.AGENDA, "Agenda", Icons.Default.CalendarMonth),
    ItemNavegacion(Rutas.OPORTUNIDADES, "Cartera", Icons.Default.Work),
    ItemNavegacion(Rutas.ORGANIZACIONES, "Empresas", Icons.Default.Business)
)

/** Rutas de flujos de captura: ocupan toda la pantalla, sin barra inferior. */
val rutasDeFlujo = setOf(
    Rutas.FORMULARIO_OPORTUNIDAD,
    Rutas.REGISTRAR_SEGUIMIENTO,
    Rutas.FORMULARIO_ORGANIZACION,
    Rutas.FORMULARIO_CONTACTO
)

/** Pantallas que dibujan su propio encabezado y no llevan barra superior. */
val rutasSinBarraSuperior = setOf(Rutas.INICIO, Rutas.AGENDA, Rutas.OPORTUNIDADES)

/** Título de la barra superior según la ruta activa. */
fun tituloDeRuta(ruta: String?, rol: RolUsuario): String = when (ruta) {
    Rutas.OPORTUNIDADES -> if (rol == RolUsuario.SUPERVISOR) "Cartera comercial" else "Mi cartera"
    Rutas.AGENDA -> "Agenda"
    Rutas.PERFIL -> "Mi perfil"
    Rutas.DETALLE_OPORTUNIDAD -> "Oportunidad"
    Rutas.FORMULARIO_OPORTUNIDAD -> "Nueva oportunidad"
    Rutas.REGISTRAR_SEGUIMIENTO -> "Nueva gestión"
    Rutas.EVIDENCIAS -> "Evidencias"
    Rutas.ORGANIZACIONES -> "Empresas e instituciones"
    Rutas.DETALLE_ORGANIZACION -> "Ficha de la organización"
    Rutas.FORMULARIO_ORGANIZACION -> "Registrar organización"
    Rutas.FORMULARIO_CONTACTO -> "Registrar contacto"
    Rutas.PROPUESTAS -> "Propuestas enviadas"
    else -> "Atenas"
}
