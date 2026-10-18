package cl.duoc.atenas.ui.demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.atenas.AtenasApp
import cl.duoc.atenas.data.local.dao.OportunidadResumen
import cl.duoc.atenas.data.local.entity.Usuario
import cl.duoc.atenas.data.repository.OportunidadRepository
import cl.duoc.atenas.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Estado de la pantalla de verificación de la Fase 0.
 * Esta pantalla es temporal: se reemplaza por el login en la Fase 1.
 */
data class PanelDemoUiState(
    val cargando: Boolean = true,
    val usuarios: List<Usuario> = emptyList(),
    val oportunidades: List<OportunidadResumen> = emptyList()
)

class PanelDemoViewModel(
    usuarioRepository: UsuarioRepository,
    oportunidadRepository: OportunidadRepository
) : ViewModel() {

    val estado: StateFlow<PanelDemoUiState> = combine(
        usuarioRepository.observarTodos(),
        oportunidadRepository.observarResumen(responsableId = null)
    ) { usuarios, oportunidades ->
        PanelDemoUiState(
            cargando = false,
            usuarios = usuarios,
            oportunidades = oportunidades
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PanelDemoUiState()
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AtenasApp
                PanelDemoViewModel(
                    usuarioRepository = app.contenedor.usuarios,
                    oportunidadRepository = app.contenedor.oportunidades
                )
            }
        }
    }
}
