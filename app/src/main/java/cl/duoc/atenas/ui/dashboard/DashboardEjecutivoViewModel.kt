package cl.duoc.atenas.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.core.SesionManager
import cl.duoc.atenas.data.local.dao.ConteoPorEstado
import cl.duoc.atenas.data.local.dao.GestionPendiente
import cl.duoc.atenas.data.local.dao.OportunidadResumen
import cl.duoc.atenas.data.repository.OportunidadRepository
import cl.duoc.atenas.data.repository.SeguimientoRepository
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.rules.GuiaGestion
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Qué tipo de pendiente es una tarea; define su ícono, color y botón. */
enum class TipoTarea { ATRASADA, HOY, PROPUESTA, SIN_CONTACTO }

/** Una fila de "Qué hacer hoy": algo concreto que el ejecutivo debe resolver. */
data class Tarea(
    val clave: String,
    val oportunidadId: Long,
    val tipo: TipoTarea,
    val titulo: String,
    val detalle: String,
    val meta: String
)

/** Un tramo del embudo resumido en el inicio. */
data class TramoEmbudo(val etiqueta: String, val total: Int, val estados: Set<EstadoOportunidad>)

data class DashboardEjecutivoUiState(
    val cargando: Boolean = true,
    val nombreUsuario: String = "",
    val primerNombre: String = "",
    val tareas: List<Tarea> = emptyList(),
    val atrasadas: Int = 0,
    val propuestasPorInsistir: Int = 0,
    val embudo: List<TramoEmbudo> = emptyList()
) {
    val totalActivas: Int get() = embudo.sumOf { it.total }
}

/*
 * VIEWMODEL DEL INICIO DEL EJECUTIVO: "QUÉ HACER HOY"
 *
 * En vez de un tablero de números, el inicio arma una lista de tareas
 * concretas a partir de cuatro fuentes, en orden de urgencia:
 *  1. gestiones atrasadas,
 *  2. gestiones que vencen hoy,
 *  3. propuestas que esperan respuesta (insistir),
 *  4. oportunidades activas sin primer contacto.
 * Una oportunidad aparece una sola vez, en su motivo más urgente.
 */
class DashboardEjecutivoViewModel(
    sesion: SesionManager,
    oportunidades: OportunidadRepository,
    seguimientos: SeguimientoRepository
) : ViewModel() {

    private val usuario = sesion.requerirUsuario()

    private val alertas = combine(
        oportunidades.observarSinContacto(usuario.id),
        oportunidades.observarPropuestasSinRespuesta(usuario.id)
    ) { sinContacto, propuestas -> sinContacto to propuestas }

    val estado: StateFlow<DashboardEjecutivoUiState> = combine(
        seguimientos.observarGestionesAtrasadas(usuario.id),
        seguimientos.observarGestionesDeHoy(usuario.id),
        oportunidades.observarConteoPorEstado(usuario.id),
        alertas
    ) { atrasadas, deHoy, conteos, (sinContacto, propuestas) ->
        DashboardEjecutivoUiState(
            cargando = false,
            nombreUsuario = usuario.nombreCompleto,
            primerNombre = usuario.nombreCompleto.substringBefore(" "),
            tareas = armarTareas(atrasadas, deHoy, propuestas, sinContacto),
            atrasadas = atrasadas.size,
            propuestasPorInsistir = propuestas.size,
            embudo = armarEmbudo(conteos)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardEjecutivoUiState()
    )

    companion object {
        /** Junta los pendientes en una sola lista, sin repetir oportunidades. */
        fun armarTareas(
            atrasadas: List<GestionPendiente>,
            deHoy: List<GestionPendiente>,
            propuestas: List<OportunidadResumen>,
            sinContacto: List<OportunidadResumen>
        ): List<Tarea> {
            val hoy = Fechas.hoy()
            val tareas = buildList {
                atrasadas.forEach { g ->
                    add(
                        Tarea(
                            clave = "atr-${g.id}",
                            oportunidadId = g.oportunidadId,
                            tipo = TipoTarea.ATRASADA,
                            titulo = g.oportunidadNombre,
                            detalle = g.proximaAccion ?: "Retomar la gestión",
                            meta = GuiaGestion.textoVencimiento(Fechas.diasEntre(hoy, g.fechaProximoSeguimiento))
                        )
                    )
                }
                deHoy.forEach { g ->
                    add(
                        Tarea(
                            clave = "hoy-${g.id}",
                            oportunidadId = g.oportunidadId,
                            tipo = TipoTarea.HOY,
                            titulo = g.oportunidadNombre,
                            detalle = g.proximaAccion ?: "Gestión comprometida para hoy",
                            meta = "Vence hoy"
                        )
                    )
                }
                propuestas.forEach { o ->
                    add(
                        Tarea(
                            clave = "pro-${o.id}",
                            oportunidadId = o.id,
                            tipo = TipoTarea.PROPUESTA,
                            titulo = o.nombre,
                            detalle = o.organizacionNombre,
                            meta = o.estado.etiqueta
                        )
                    )
                }
                sinContacto.forEach { o ->
                    add(
                        Tarea(
                            clave = "sin-${o.id}",
                            oportunidadId = o.id,
                            tipo = TipoTarea.SIN_CONTACTO,
                            titulo = o.nombre,
                            detalle = "Evento el ${Fechas.aTexto(o.fechaInicioEvento)} · ${o.organizacionNombre}",
                            meta = "Sin primer contacto"
                        )
                    )
                }
            }
            return tareas.distinctBy { it.oportunidadId }
        }

        /** Resume los estados activos en cuatro tramos fáciles de leer. */
        fun armarEmbudo(conteos: List<ConteoPorEstado>): List<TramoEmbudo> {
            val porEstado = conteos.associate { it.estado to it.total }
            return listOf(
                "Identificadas" to setOf(EstadoOportunidad.IDENTIFICADA),
                "Contacto" to setOf(EstadoOportunidad.CONTACTO_PENDIENTE, EstadoOportunidad.CONTACTO_REALIZADO),
                "Propuesta" to setOf(EstadoOportunidad.PROPUESTA_ENVIADA, EstadoOportunidad.EN_ESPERA_RESPUESTA),
                "Confirmadas" to setOf(EstadoOportunidad.CONFIRMADA)
            ).map { (etiqueta, estados) ->
                TramoEmbudo(etiqueta, estados.sumOf { porEstado[it] ?: 0 }, estados)
            }
        }
    }
}
