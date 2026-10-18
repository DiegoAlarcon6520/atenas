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
import cl.duoc.atenas.data.local.entity.Usuario
import cl.duoc.atenas.data.repository.OportunidadRepository
import cl.duoc.atenas.data.repository.SeguimientoRepository
import cl.duoc.atenas.data.repository.UsuarioRepository
import cl.duoc.atenas.di.contenedor
import cl.duoc.atenas.domain.model.RolUsuario
import cl.duoc.atenas.domain.rules.Permisos
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetalleOportunidadUiState(
    val cargando: Boolean = true,
    val detalle: OportunidadDetalle? = null,
    val historial: List<Seguimiento> = emptyList(),
    val rol: RolUsuario = RolUsuario.EJECUTIVO,
    /** Ejecutivos disponibles para asignar; solo se llena para el supervisor. */
    val ejecutivos: List<Usuario> = emptyList(),
    val puedeAsignar: Boolean = false,
    val sinAcceso: Boolean = false
)

/**
 * VIEWMODEL DEL DETALLE DE OPORTUNIDAD
 *
 * Además de mostrar los datos, maneja la asignación de responsable, que es
 * una acción exclusiva del supervisor.
 *
 * Control de acceso: un ejecutivo solo puede abrir oportunidades asignadas a
 * él. La verificación está acá y no en la pantalla, así que no basta con
 * esconder un botón: aunque alguien llegue a la ruta, el estado viene vacío.
 */
class DetalleOportunidadViewModel(
    private val oportunidadId: Long,
    sesion: SesionManager,
    private val oportunidades: OportunidadRepository,
    seguimientos: SeguimientoRepository,
    usuarios: UsuarioRepository
) : ViewModel() {

    private val usuario = sesion.requerirUsuario()

    val estado: StateFlow<DetalleOportunidadUiState> = combine(
        oportunidades.observarDetalle(oportunidadId),
        seguimientos.observarHistorial(oportunidadId),
        usuarios.observarEjecutivos()
    ) { detalle, historial, ejecutivos ->
        val autorizado = detalle != null && (
            Permisos.puedeVerTodasLasOportunidades(usuario.rol) ||
                detalle.oportunidad.responsableId == usuario.id
            )
        DetalleOportunidadUiState(
            cargando = false,
            detalle = if (autorizado) detalle else null,
            historial = if (autorizado) historial else emptyList(),
            rol = usuario.rol,
            ejecutivos = if (Permisos.puedeAsignarResponsable(usuario.rol)) ejecutivos else emptyList(),
            puedeAsignar = Permisos.puedeAsignarResponsable(usuario.rol),
            sinAcceso = !autorizado
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DetalleOportunidadUiState()
    )

    /**
     * Asigna o reasigna el responsable de la oportunidad.
     *
     * Vuelve a comprobar el permiso aunque la pantalla solo muestre el
     * control a los supervisores: la regla no puede depender de la UI.
     * Pasar null deja la oportunidad sin responsable.
     */
    fun asignarResponsable(responsableId: Long?) {
        if (!Permisos.puedeAsignarResponsable(usuario.rol)) return
        viewModelScope.launch {
            oportunidades.asignarResponsable(oportunidadId, responsableId)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val argumentos = createSavedStateHandle()
                DetalleOportunidadViewModel(
                    oportunidadId = argumentos.get<Long>("oportunidadId")
                        ?: error("Falta el identificador de la oportunidad."),
                    sesion = contenedor.sesion,
                    oportunidades = contenedor.oportunidades,
                    seguimientos = contenedor.seguimientos,
                    usuarios = contenedor.usuarios
                )
            }
        }
    }
}
