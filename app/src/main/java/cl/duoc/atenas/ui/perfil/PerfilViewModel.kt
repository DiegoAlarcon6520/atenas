package cl.duoc.atenas.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.atenas.core.SesionManager
import cl.duoc.atenas.di.contenedor

class PerfilViewModel(private val sesion: SesionManager) : ViewModel() {

    fun cerrarSesion() = sesion.cerrarSesion()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { PerfilViewModel(sesion = contenedor.sesion) }
        }
    }
}
