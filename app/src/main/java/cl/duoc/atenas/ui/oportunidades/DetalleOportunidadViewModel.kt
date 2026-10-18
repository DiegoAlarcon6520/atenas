package cl.duoc.atenas.ui.oportunidades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.atenas.core.SesionManager
import cl.duoc.atenas.data.local.dao.OportunidadDetalle
import cl.duoc.atenas.data.local.entity.Seguimiento
import cl.duoc.atenas.data.repository.OportunidadRepository
import cl.duoc.atenas.data.repository.SeguimientoRepository
import cl.duoc.atenas.di.contenedor
import cl.duoc.atenas.domain.model.RolUsuario
import kotlinx.coroutines.flow.SharingStarted
import cl.duoc.atenas.domain.rules.Permisos
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DetalleOportunidadUiState(
    val cargando: Boolean = true,
    val detalle: OportunidadDetalle? = null,
    val historial: List<Seguimiento> = emptyList(),
    val rol: RolUsuario = RolUsuario.EJECUTIVO,
    val puedeGestionarEstado: Boolean = false,
    val sinAcceso: Boolean = false
)

class DetalleOportunidadViewModel(
    oportunidadId: Long,
    sesion: SesionManager,
    oportunidades: OportunidadRepository,
    seguimientos: SeguimientoRepository
) : ViewModel() {

    private val usuario = sesion.requerirUsuario()

    val estado: StateFlow<DetalleOportunidadUiState> = combine(
        oportunidades.observarDetalle(oportunidadId),
        seguimientos.observarHistorial(oportunidadId)
    ) { detalle, historial ->
        // Un ejecutivo solo puede abrir oportunidades asignadas a él.
        val autorizado = detalle != null && (
            Permisos.puedeVerTodasLasOportunidades(usuario.rol) ||
                detalle.oportunidad.responsableId == usuario.id
            )
        DetalleOportunidadUiState(
            cargando = false,
            detalle = if (autorizado) detalle else null,
            historial = if (autorizado) historial else emptyList(),
            rol = usuario.rol,
            puedeGestionarEstado = Permisos.puedeConfirmarODescartar(usuario.rol),
            sinAcceso = !autorizado
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DetalleOportunidadUiState()
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val argumentos = createSavedStateHandle()
                val id: Long = argumentos.get<Long>("oportunidadId")
                    ?: error("Falta el identificador de la oportunidad.")
                DetalleOportunidadViewModel(
                    oportunidadId = id,
                    sesion = contenedor.sesion,
                    oportunidades = contenedor.oportunidades,
                    seguimientos = contenedor.seguimientos
                )
            }
        }
    }
}
