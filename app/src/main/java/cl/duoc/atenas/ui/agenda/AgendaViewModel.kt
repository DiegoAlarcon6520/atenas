package cl.duoc.atenas.ui.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.core.SesionManager
import cl.duoc.atenas.data.local.dao.OportunidadResumen
import cl.duoc.atenas.data.repository.OportunidadRepository
import cl.duoc.atenas.di.contenedor
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.rules.Permisos
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/** Un día de la franja semanal. */
data class DiaSemana(val fecha: Long, val inicial: String, val numero: Int, val esHoy: Boolean, val tieneEventos: Boolean)

/** Un evento de la agenda con los días que faltan para él. */
data class EventoAgenda(val oportunidad: OportunidadResumen, val diasRestantes: Int)

/** Grupo de eventos bajo un título ("Esta semana", "Próximas semanas"…). */
data class GrupoAgenda(val titulo: String, val eventos: List<EventoAgenda>)

data class AgendaUiState(
    val cargando: Boolean = true,
    val semana: List<DiaSemana> = emptyList(),
    val diaElegido: Long? = null,
    val grupos: List<GrupoAgenda> = emptyList(),
    /** Eventos en menos de [DIAS_AVISO] días que aún no tienen participación confirmada. */
    val sinConfirmarPronto: Int = 0
)

/** Margen bajo el cual un evento sin confirmar se considera en riesgo. */
const val DIAS_AVISO = 15

/**
 * VIEWMODEL DE LA AGENDA
 *
 * Ordena los eventos por fecha para responder "¿qué se viene y a tiempo de
 * qué estoy?". Es la vista que ataca el problema central del caso: detectar
 * las oportunidades con anticipación.
 */
class AgendaViewModel(
    sesion: SesionManager,
    oportunidades: OportunidadRepository
) : ViewModel() {

    private val usuario = sesion.requerirUsuario()
    private val alcance = if (Permisos.puedeVerTodasLasOportunidades(usuario.rol)) null else usuario.id
    private val _diaElegido = MutableStateFlow<Long?>(null)

    private val terminadas = setOf(EstadoOportunidad.DESCARTADA, EstadoOportunidad.CERRADA)
    private val comprometidas = setOf(EstadoOportunidad.CONFIRMADA, EstadoOportunidad.ACTIVACION_REALIZADA)

    val estado: StateFlow<AgendaUiState> = combine(
        oportunidades.observarResumen(alcance),
        _diaElegido
    ) { todas, diaElegido ->
        val hoy = Fechas.hoy()
        val proximos = todas
            .filter { it.fechaInicioEvento >= hoy && it.estado !in terminadas }
            .sortedBy { it.fechaInicioEvento }
            .map { EventoAgenda(it, Fechas.diasEntre(hoy, it.fechaInicioEvento)) }

        val semana = semanaActual(hoy).map { dia ->
            dia.copy(tieneEventos = proximos.any { mismoDia(it.oportunidad.fechaInicioEvento, dia.fecha) })
        }
        val visibles = if (diaElegido == null) {
            proximos
        } else {
            proximos.filter { mismoDia(it.oportunidad.fechaInicioEvento, diaElegido) }
        }
        val finDeSemana = semana.last().fecha

        AgendaUiState(
            cargando = false,
            semana = semana,
            diaElegido = diaElegido,
            grupos = listOf(
                GrupoAgenda("Esta semana", visibles.filter { it.oportunidad.fechaInicioEvento <= finDeSemana }),
                GrupoAgenda(
                    "Próximas semanas",
                    visibles.filter { it.oportunidad.fechaInicioEvento > finDeSemana && it.diasRestantes <= 31 }
                ),
                GrupoAgenda("Más adelante", visibles.filter { it.oportunidad.fechaInicioEvento > finDeSemana && it.diasRestantes > 31 })
            ).filter { it.eventos.isNotEmpty() },
            sinConfirmarPronto = proximos.count { it.diasRestantes < DIAS_AVISO && it.oportunidad.estado !in comprometidas }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AgendaUiState()
    )

    /** Tocar un día filtra la lista; tocarlo de nuevo vuelve a mostrar todo. */
    fun alElegirDia(fecha: Long) = _diaElegido.update { if (it == fecha) null else fecha }

    private fun mismoDia(a: Long, b: Long): Boolean {
        val ca = Calendar.getInstance().apply { timeInMillis = a }
        val cb = Calendar.getInstance().apply { timeInMillis = b }
        return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) &&
            ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR)
    }

    /** Los siete días de la semana en curso, de lunes a domingo. */
    private fun semanaActual(hoy: Long): List<DiaSemana> {
        val calendario = Calendar.getInstance().apply {
            timeInMillis = hoy
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
        val iniciales = listOf("L", "M", "M", "J", "V", "S", "D")
        return (0 until 7).map { i ->
            val fecha = calendario.timeInMillis
            val dia = DiaSemana(
                fecha = fecha,
                inicial = iniciales[i],
                numero = calendario.get(Calendar.DAY_OF_MONTH),
                esHoy = mismoDia(fecha, hoy),
                tieneEventos = false
            )
            calendario.add(Calendar.DAY_OF_YEAR, 1)
            dia
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AgendaViewModel(
                    sesion = contenedor.sesion,
                    oportunidades = contenedor.oportunidades
                )
            }
        }
    }
}
