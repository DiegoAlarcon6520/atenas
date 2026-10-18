package cl.duoc.atenas.ui.supervisor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.atenas.core.SesionManager
import cl.duoc.atenas.data.local.dao.ConteoPorEstado
import cl.duoc.atenas.data.local.dao.GestionPendiente
import cl.duoc.atenas.data.local.dao.OportunidadResumen
import cl.duoc.atenas.data.local.entity.Usuario
import cl.duoc.atenas.data.repository.OportunidadRepository
import cl.duoc.atenas.data.repository.SeguimientoRepository
import cl.duoc.atenas.data.repository.UsuarioRepository
import cl.duoc.atenas.di.contenedor
import cl.duoc.atenas.domain.model.EstadoOportunidad
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class PanelSupervisorUiState(
    val cargando: Boolean = true,
    val nombreUsuario: String = "",
    val total: Int = 0,
    val enGestion: Int = 0,
    val confirmadas: Int = 0,
    val sinAsignar: List<OportunidadResumen> = emptyList(),
    val atrasadas: List<GestionPendiente> = emptyList(),
    val conteos: List<ConteoPorEstado> = emptyList(),
    val ejecutivos: List<Usuario> = emptyList(),
    val cargaPorEjecutivo: Map<String, Int> = emptyMap()
)

/** Tablero del supervisor: consulta global de toda la cartera. */
class PanelSupervisorViewModel(
    sesion: SesionManager,
    oportunidades: OportunidadRepository,
    seguimientos: SeguimientoRepository,
    usuarios: UsuarioRepository
) : ViewModel() {

    private val usuario = sesion.requerirUsuario()

    val estado: StateFlow<PanelSupervisorUiState> = combine(
        oportunidades.observarResumen(responsableId = null),
        oportunidades.observarSinResponsable(),
        oportunidades.observarConteoPorEstado(responsableId = null),
        seguimientos.observarGestionesAtrasadas(responsableId = null),
        usuarios.observarEjecutivos()
    ) { todas, sinAsignar, conteos, atrasadas, ejecutivos ->
        val cerradas = setOf(EstadoOportunidad.DESCARTADA, EstadoOportunidad.CERRADA)
        PanelSupervisorUiState(
            cargando = false,
            nombreUsuario = usuario.nombreCompleto,
            total = todas.size,
            enGestion = todas.count { it.estado !in cerradas },
            confirmadas = todas.count {
                it.estado == EstadoOportunidad.CONFIRMADA ||
                    it.estado == EstadoOportunidad.ACTIVACION_REALIZADA
            },
            sinAsignar = sinAsignar,
            atrasadas = atrasadas,
            conteos = conteos.sortedBy { it.estado.orden },
            ejecutivos = ejecutivos,
            cargaPorEjecutivo = ejecutivos.associate { ejecutivo ->
                ejecutivo.nombreCompleto to todas.count {
                    it.responsableNombre == ejecutivo.nombreCompleto && it.estado !in cerradas
                }
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PanelSupervisorUiState()
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PanelSupervisorViewModel(
                    sesion = contenedor.sesion,
                    oportunidades = contenedor.oportunidades,
                    seguimientos = contenedor.seguimientos,
                    usuarios = contenedor.usuarios
                )
            }
        }
    }
}
