package cl.duoc.atenas.ui.supervisor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.core.SesionManager
import cl.duoc.atenas.data.local.dao.GestionPendiente
import cl.duoc.atenas.data.local.dao.OportunidadResumen
import cl.duoc.atenas.data.local.entity.Seguimiento
import cl.duoc.atenas.data.repository.OportunidadRepository
import cl.duoc.atenas.data.repository.SeguimientoRepository
import cl.duoc.atenas.data.repository.UsuarioRepository
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.TipoAccion
import cl.duoc.atenas.domain.rules.GuiaGestion
import cl.duoc.atenas.domain.rules.TransicionesEstado
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Carga de un ejecutivo: oportunidades activas y cuántas tiene atrasadas. */
data class CargaEjecutivo(val nombre: String, val enGestion: Int, val atrasadas: Int)

data class PanelSupervisorUiState(
    val cargando: Boolean = true,
    val nombreUsuario: String = "",
    val enGestion: Int = 0,
    val confirmadas: Int = 0,
    /** Porcentaje de oportunidades ganadas entre las que ya tienen desenlace; null si aún no hay. */
    val conversion: Int? = null,
    /** Oportunidades con respuesta pendiente: el supervisor decide si confirmar o descartar. */
    val decisiones: List<OportunidadResumen> = emptyList(),
    val sinAsignar: List<OportunidadResumen> = emptyList(),
    val atrasadas: List<GestionPendiente> = emptyList(),
    val carga: List<CargaEjecutivo> = emptyList()
) {
    val totalAlertas: Int get() = decisiones.size + sinAsignar.size + atrasadas.size
}

/**
 * VIEWMODEL DEL PANEL DEL SUPERVISOR
 *
 * El panel pone primero lo que solo el supervisor puede resolver:
 * confirmar o descartar oportunidades que esperan respuesta y asignar las
 * que no tienen responsable. Después, cómo va el equipo.
 */
class PanelSupervisorViewModel(
    sesion: SesionManager,
    private val oportunidades: OportunidadRepository,
    private val seguimientos: SeguimientoRepository,
    usuarios: UsuarioRepository
) : ViewModel() {

    private val usuario = sesion.requerirUsuario()

    /** Mensajes de una sola vez para el snackbar ("Participación confirmada"). */
    private val _mensajes = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val mensajes: SharedFlow<String> = _mensajes.asSharedFlow()

    private val equipo = combine(
        usuarios.observarEjecutivos(),
        seguimientos.observarGestionesAtrasadas(responsableId = null)
    ) { ejecutivos, atrasadas -> ejecutivos to atrasadas }

    val estado: StateFlow<PanelSupervisorUiState> = combine(
        oportunidades.observarResumen(responsableId = null),
        oportunidades.observarSinResponsable(),
        oportunidades.observarConteoPorEstado(responsableId = null),
        equipo
    ) { todas, sinAsignar, conteos, (ejecutivos, atrasadas) ->
        val terminadas = setOf(EstadoOportunidad.DESCARTADA, EstadoOportunidad.CERRADA)
        val activas = todas.filter { it.estado !in terminadas }
        PanelSupervisorUiState(
            cargando = false,
            nombreUsuario = usuario.nombreCompleto,
            enGestion = activas.size,
            confirmadas = todas.count {
                it.estado == EstadoOportunidad.CONFIRMADA || it.estado == EstadoOportunidad.ACTIVACION_REALIZADA
            },
            conversion = GuiaGestion.tasaConversion(conteos.associate { it.estado to it.total }),
            decisiones = todas.filter { it.estado == EstadoOportunidad.EN_ESPERA_RESPUESTA },
            sinAsignar = sinAsignar,
            atrasadas = atrasadas,
            carga = ejecutivos.map { ejecutivo ->
                CargaEjecutivo(
                    nombre = ejecutivo.nombreCompleto,
                    enGestion = activas.count { it.responsableNombre == ejecutivo.nombreCompleto },
                    atrasadas = atrasadas.count { it.responsableNombre == ejecutivo.nombreCompleto }
                )
            }.sortedByDescending { it.enGestion }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PanelSupervisorUiState()
    )

    /**
     * Confirma o descarta una oportunidad desde el panel. Se registra como
     * una gestión más, así el historial muestra quién decidió y cuándo.
     */
    fun decidir(oportunidadId: Long, confirmar: Boolean) {
        viewModelScope.launch {
            val oportunidad = oportunidades.porId(oportunidadId) ?: return@launch
            val destino = if (confirmar) EstadoOportunidad.CONFIRMADA else EstadoOportunidad.DESCARTADA
            // Misma regla que en el registro de gestiones: si la transición no
            // es válida para el perfil, no se aplica.
            if (!TransicionesEstado.esValida(oportunidad.estado, destino, usuario.rol)) {
                _mensajes.tryEmit("No se puede aplicar ese cambio desde el estado actual.")
                return@launch
            }
            try {
                seguimientos.registrar(
                    Seguimiento(
                        oportunidadId = oportunidadId,
                        usuarioId = usuario.id,
                        tipoAccion = if (confirmar) TipoAccion.CONFIRMACION else TipoAccion.CIERRE,
                        fechaAccion = Fechas.hoy(),
                        resultado = if (confirmar) {
                            "Participación confirmada por el supervisor desde el panel."
                        } else {
                            "Oportunidad descartada por el supervisor desde el panel."
                        },
                        proximaAccion = null,
                        fechaProximoSeguimiento = null,
                        estadoAnterior = oportunidad.estado,
                        estadoNuevo = destino,
                        observaciones = null
                    ),
                    contactoId = null
                )
                _mensajes.tryEmit(if (confirmar) "Participación confirmada" else "Oportunidad descartada")
            } catch (excepcion: Exception) {
                _mensajes.tryEmit("No se pudo registrar la decisión: ${excepcion.message}")
            }
        }
    }
}
