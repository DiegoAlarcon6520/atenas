package cl.duoc.atenas.ui.perfil

import androidx.lifecycle.ViewModel
import cl.duoc.atenas.core.ModoTema
import cl.duoc.atenas.core.PreferenciasApp
import cl.duoc.atenas.core.SesionManager
import kotlinx.coroutines.flow.StateFlow

/**
 * VIEWMODEL DEL PERFIL
 *
 * Maneja las dos acciones de la pantalla: cambiar el tema y cerrar sesión.
 * El modo de tema se expone directamente desde las preferencias, porque es
 * el mismo StateFlow que observa MainActivity para repintar la aplicación.
 */
class PerfilViewModel(
    private val sesion: SesionManager,
    private val preferencias: PreferenciasApp
) : ViewModel() {

    val modoTema: StateFlow<ModoTema> = preferencias.modoTema

    fun cambiarModoTema(modo: ModoTema) = preferencias.cambiarModoTema(modo)

    fun cerrarSesion() = sesion.cerrarSesion()
}
