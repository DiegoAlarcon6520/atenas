package cl.duoc.atenas.ui.propuestas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.atenas.core.SesionManager
import cl.duoc.atenas.data.local.dao.PropuestaEnviada
import cl.duoc.atenas.data.repository.SeguimientoRepository
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.rules.Permisos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class PropuestasUiState(
    val cargando: Boolean = true,
    val esSupervisor: Boolean = false,
    val soloSinRespuesta: Boolean = false,
    val propuestas: List<PropuestaEnviada> = emptyList(),
    val totalSinRespuesta: Int = 0
)

/**
 * VIEWMODEL DE PROPUESTAS ENVIADAS (HU-16)
 *
 * Una propuesta es una gestión de tipo "Envío de propuesta". Esta pantalla
 * las reúne todas para saber cuáles se enviaron y cuáles siguen esperando la
 * respuesta de la organización. El supervisor ve las del equipo completo.
 */
class PropuestasViewModel(
    sesion: SesionManager,
    seguimientos: SeguimientoRepository
) : ViewModel() {

    private val usuario = sesion.requerirUsuario()
    private val esSupervisor = Permisos.puedeVerTodasLasOportunidades(usuario.rol)
    private val _soloSinRespuesta = MutableStateFlow(false)

    val estado: StateFlow<PropuestasUiState> = combine(
        seguimientos.observarPropuestas(if (esSupervisor) null else usuario.id),
        _soloSinRespuesta
    ) { propuestas, soloSinRespuesta ->
        PropuestasUiState(
            cargando = false,
            esSupervisor = esSupervisor,
            soloSinRespuesta = soloSinRespuesta,
            propuestas = if (soloSinRespuesta) propuestas.filter { esperaRespuesta(it) } else propuestas,
            totalSinRespuesta = propuestas.count { esperaRespuesta(it) }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PropuestasUiState()
    )

    fun alternarSoloSinRespuesta() = _soloSinRespuesta.update { !it }

    companion object {
        /** La organización todavía no responde mientras la oportunidad siga en estos estados. */
        fun esperaRespuesta(propuesta: PropuestaEnviada): Boolean =
            propuesta.estadoActual == EstadoOportunidad.PROPUESTA_ENVIADA ||
                propuesta.estadoActual == EstadoOportunidad.EN_ESPERA_RESPUESTA
    }
}
