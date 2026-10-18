package cl.duoc.atenas.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.atenas.core.ModoTema
import cl.duoc.atenas.core.PreferenciasApp
import cl.duoc.atenas.core.SesionManager
import cl.duoc.atenas.di.contenedor
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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PerfilViewModel(
                    sesion = contenedor.sesion,
                    preferencias = contenedor.preferencias
                )
            }
        }
    }
}
