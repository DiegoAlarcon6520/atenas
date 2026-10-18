package cl.duoc.atenas.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import cl.duoc.atenas.domain.model.RolUsuario

object Rutas {
    const val INICIO = "inicio"
    const val OPORTUNIDADES = "oportunidades"
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

/** La barra inferior se arma según el perfil que inició sesión. */
fun itemsDeNavegacion(rol: RolUsuario): List<ItemNavegacion> = when (rol) {
    RolUsuario.EJECUTIVO -> listOf(
        ItemNavegacion(Rutas.INICIO, "Mi tablero", Icons.Default.Dashboard),
        ItemNavegacion(Rutas.OPORTUNIDADES, "Oportunidades", Icons.Default.EventNote),
        ItemNavegacion(Rutas.ORGANIZACIONES, "Organizaciones", Icons.Default.Business),
        ItemNavegacion(Rutas.PERFIL, "Perfil", Icons.Default.Person)
    )

    RolUsuario.SUPERVISOR -> listOf(
        ItemNavegacion(Rutas.INICIO, "Equipo", Icons.Default.Dashboard),
        ItemNavegacion(Rutas.OPORTUNIDADES, "Cartera", Icons.Default.EventNote),
        ItemNavegacion(Rutas.ORGANIZACIONES, "Organizaciones", Icons.Default.Business),
        ItemNavegacion(Rutas.PERFIL, "Perfil", Icons.Default.Person)
    )
}

/** Título de la barra superior según la ruta activa. */
fun tituloDeRuta(ruta: String?, rol: RolUsuario): String = when (ruta) {
    Rutas.OPORTUNIDADES -> if (rol == RolUsuario.SUPERVISOR) "Cartera comercial" else "Mis oportunidades"
    Rutas.PERFIL -> "Mi perfil"
    Rutas.DETALLE_OPORTUNIDAD -> "Detalle de la oportunidad"
    Rutas.FORMULARIO_OPORTUNIDAD -> "Registrar oportunidad"
    Rutas.REGISTRAR_SEGUIMIENTO -> "Nueva gestión"
    Rutas.ORGANIZACIONES -> "Organizaciones"
    Rutas.DETALLE_ORGANIZACION -> "Ficha de la organización"
    Rutas.FORMULARIO_ORGANIZACION -> "Registrar organización"
    Rutas.FORMULARIO_CONTACTO -> "Registrar contacto"
    else -> "Atenas"
}
