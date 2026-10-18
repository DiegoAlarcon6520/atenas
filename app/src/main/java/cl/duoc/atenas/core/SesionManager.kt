package cl.duoc.atenas.core

import cl.duoc.atenas.data.local.entity.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Sesión activa de la aplicación.
 *
 * Se mantiene en memoria a propósito: al cerrar la aplicación la sesión
 * termina y se vuelve a solicitar autenticación, que es el comportamiento
 * esperado en una herramienta comercial usada en terreno.
 */
class SesionManager {

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    fun iniciarSesion(usuario: Usuario) {
        _usuarioActual.value = usuario
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
    }

    fun requerirUsuario(): Usuario =
        _usuarioActual.value ?: error("No hay una sesión activa.")
}
