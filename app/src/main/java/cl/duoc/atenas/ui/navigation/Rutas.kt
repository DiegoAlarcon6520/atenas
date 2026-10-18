package cl.duoc.atenas.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import cl.duoc.atenas.domain.model.RolUsuario

object Rutas {
    const val INICIO = "inicio"
    const val PERFIL = "perfil"
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
        ItemNavegacion(Rutas.PERFIL, "Perfil", Icons.Default.Person)
    )

    RolUsuario.SUPERVISOR -> listOf(
        ItemNavegacion(Rutas.INICIO, "Equipo", Icons.Default.Dashboard),
        ItemNavegacion(Rutas.PERFIL, "Perfil", Icons.Default.Person)
    )
}
