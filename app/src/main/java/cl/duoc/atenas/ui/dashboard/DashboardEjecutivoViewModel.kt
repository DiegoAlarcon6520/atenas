package cl.duoc.atenas.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.atenas.core.SesionManager
import cl.duoc.atenas.data.local.dao.ConteoPorEstado
import cl.duoc.atenas.data.local.dao.GestionPendiente
import cl.duoc.atenas.data.local.dao.OportunidadResumen
import cl.duoc.atenas.data.repository.OportunidadRepository
import cl.duoc.atenas.data.repository.SeguimientoRepository
import cl.duoc.atenas.di.contenedor
import cl.duoc.atenas.domain.model.EstadoOportunidad
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/*
 * VIEWMODEL DEL TABLERO DEL EJECUTIVO
 *
 * combine() junta varios Flow de la base de datos en un solo estado: cada vez
 * que cualquiera de las consultas cambia, se recalcula el UiState completo y
 * la pantalla se redibuja sola.
 *
 * stateIn() convierte ese Flow en un StateFlow con valor inicial.
 * WhileSubscribed(5000) mantiene viva la suscripción cinco segundos después de
 * que la pantalla desaparece, para no reconsultar la base al rotar el teléfono.
 */
data class DashboardEjecutivoUiState(
    val cargando: Boolean = true,
    val nombreUsuario: String = "",
    val zona: String = "",
    val enGestion: Int = 0,
    val proximas: List<OportunidadResumen> = emptyList(),
    val atrasadas: List<GestionPendiente> = emptyList(),
    val conteos: List<ConteoPorEstado> = emptyList()
)

/** Tablero del ejecutivo: solo muestra las oportunidades asignadas a él. */
class DashboardEjecutivoViewModel(
    sesion: SesionManager,
    oportunidades: OportunidadRepository,
    seguimientos: SeguimientoRepository
) : ViewModel() {

    private val usuario = sesion.requerirUsuario()

    val estado: StateFlow<DashboardEjecutivoUiState> = combine(
        oportunidades.observarResumen(usuario.id),
        oportunidades.observarProximas(dias = 30, responsableId = usuario.id),
        seguimientos.observarGestionesAtrasadas(usuario.id),
        oportunidades.observarConteoPorEstado(usuario.id)
    ) { asignadas, proximas, atrasadas, conteos ->
        val cerradas = setOf(EstadoOportunidad.DESCARTADA, EstadoOportunidad.CERRADA)
        DashboardEjecutivoUiState(
            cargando = false,
            nombreUsuario = usuario.nombreCompleto,
            zona = usuario.zona,
            enGestion = asignadas.count { it.estado !in cerradas },
            proximas = proximas,
            atrasadas = atrasadas,
            conteos = conteos.sortedBy { it.estado.orden }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardEjecutivoUiState()
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DashboardEjecutivoViewModel(
                    sesion = contenedor.sesion,
                    oportunidades = contenedor.oportunidades,
                    seguimientos = contenedor.seguimientos
                )
            }
        }
    }
}
