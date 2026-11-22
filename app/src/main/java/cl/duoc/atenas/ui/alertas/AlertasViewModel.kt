package cl.duoc.atenas.ui.alertas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.atenas.core.SesionManager
import cl.duoc.atenas.data.repository.OportunidadRepository
import cl.duoc.atenas.data.repository.SeguimientoRepository
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.rules.Permisos
import cl.duoc.atenas.ui.dashboard.DashboardEjecutivoViewModel
import cl.duoc.atenas.ui.dashboard.TipoTarea
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Por qué algo aparece en el panel de alertas; define su grupo, ícono y color. */
enum class MotivoAlerta(val titulo: String) {
    DECISION("Esperan tu decisión"),
    SIN_RESPONSABLE("Sin responsable"),
    ATRASADA("Atrasadas"),
    HOY("Vencen hoy"),
    PROPUESTA("Propuestas sin respuesta"),
    SIN_CONTACTO("Sin primer contacto")
}

/** Un pendiente del panel de alertas, sin nada de la interfaz. */
data class AlertaPendiente(
    val clave: String,
    val oportunidadId: Long,
    val motivo: MotivoAlerta,
    val titulo: String,
    val detalle: String
)

/**
 * VIEWMODEL DE ALERTAS
 *
 * Alimenta la campana del encabezado común, que se ve en todas las pestañas.
 * Cada perfil tiene sus propios pendientes:
 *  - ejecutivo: atrasadas, que vencen hoy, propuestas sin respuesta y
 *    oportunidades sin primer contacto (las mismas tareas del inicio),
 *  - supervisor: decisiones pendientes, oportunidades sin responsable y
 *    gestiones atrasadas del equipo.
 */
class AlertasViewModel(
    sesion: SesionManager,
    oportunidades: OportunidadRepository,
    seguimientos: SeguimientoRepository
) : ViewModel() {

    private val usuario = sesion.requerirUsuario()

    private val fuente: Flow<List<AlertaPendiente>> =
        if (Permisos.puedeVerPanelDeEquipo(usuario.rol)) {
            combine(
                oportunidades.observarResumen(responsableId = null),
                oportunidades.observarSinResponsable(),
                seguimientos.observarGestionesAtrasadas(responsableId = null)
            ) { todas, sinAsignar, atrasadas ->
                todas.filter { it.estado == EstadoOportunidad.EN_ESPERA_RESPUESTA }.map {
                    AlertaPendiente(
                        "d-${it.id}", it.id, MotivoAlerta.DECISION, it.nombre,
                        "${it.responsableNombre ?: "Sin responsable"} · ${it.organizacionNombre}"
                    )
                } + sinAsignar.map {
                    AlertaPendiente("s-${it.id}", it.id, MotivoAlerta.SIN_RESPONSABLE, it.nombre, it.organizacionNombre)
                } + atrasadas.map {
                    AlertaPendiente(
                        "a-${it.id}", it.oportunidadId, MotivoAlerta.ATRASADA, it.oportunidadNombre,
                        "${it.responsableNombre ?: "Sin responsable"} · ${it.proximaAccion ?: "Retomar la gestión"}"
                    )
                }
            }
        } else {
            combine(
                seguimientos.observarGestionesAtrasadas(usuario.id),
                seguimientos.observarGestionesDeHoy(usuario.id),
                oportunidades.observarPropuestasSinRespuesta(usuario.id),
                oportunidades.observarSinContacto(usuario.id)
            ) { atrasadas, deHoy, propuestas, sinContacto ->
                // Mismas tareas que "Qué hacer hoy", para que la campana y el
                // inicio nunca digan cosas distintas.
                DashboardEjecutivoViewModel.armarTareas(atrasadas, deHoy, propuestas, sinContacto).map { t ->
                    val motivo = when (t.tipo) {
                        TipoTarea.ATRASADA -> MotivoAlerta.ATRASADA
                        TipoTarea.HOY -> MotivoAlerta.HOY
                        TipoTarea.PROPUESTA -> MotivoAlerta.PROPUESTA
                        TipoTarea.SIN_CONTACTO -> MotivoAlerta.SIN_CONTACTO
                    }
                    AlertaPendiente(t.clave, t.oportunidadId, motivo, t.titulo, "${t.detalle} · ${t.meta}")
                }
            }
        }

    val alertas: StateFlow<List<AlertaPendiente>> = fuente.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )
}
