package cl.duoc.atenas.ui.organizaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.atenas.core.SesionManager
import cl.duoc.atenas.data.local.dao.GestionDeOrganizacion
import cl.duoc.atenas.data.local.dao.OportunidadResumen
import cl.duoc.atenas.data.local.dao.OrganizacionDetalle
import cl.duoc.atenas.data.local.entity.Contacto
import cl.duoc.atenas.data.repository.ContactoRepository
import cl.duoc.atenas.data.repository.OportunidadRepository
import cl.duoc.atenas.data.repository.OrganizacionRepository
import cl.duoc.atenas.data.repository.SeguimientoRepository
import cl.duoc.atenas.domain.rules.Permisos
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetalleOrganizacionUiState(
    val cargando: Boolean = true,
    val detalle: OrganizacionDetalle? = null,
    val contactos: List<Contacto> = emptyList(),
    val oportunidades: List<OportunidadResumen> = emptyList(),
    val gestiones: List<GestionDeOrganizacion> = emptyList(),
    /** Solo el supervisor (que ve todas sus oportunidades), y solo si no tiene ninguna. */
    val puedeEliminar: Boolean = false
)

/**
 * VIEWMODEL DE LA FICHA DE ORGANIZACIÓN
 *
 * Junta tres fuentes en un solo estado: los datos de la organización, sus
 * contactos y las oportunidades asociadas a ella. Las tres se consultan por
 * separado en la base y se combinan acá, porque cada una cambia por su cuenta
 * (puedes agregar un contacto sin tocar la organización).
 */
class DetalleOrganizacionViewModel(
    private val organizacionId: Long,
    sesion: SesionManager,
    private val organizaciones: OrganizacionRepository,
    contactos: ContactoRepository,
    oportunidades: OportunidadRepository,
    seguimientos: SeguimientoRepository
) : ViewModel() {

    /**
     * Mismo alcance que el resto de la aplicación: el supervisor ve todas las
     * oportunidades y gestiones de la organización; el ejecutivo, solo las
     * suyas. La organización y sus contactos son compartidos.
     */
    private val usuario = sesion.requerirUsuario()

    private val alcance: Long? =
        if (Permisos.puedeVerTodasLasOportunidades(usuario.rol)) null else usuario.id

    val estado: StateFlow<DetalleOrganizacionUiState> = combine(
        organizaciones.observarDetalle(organizacionId),
        contactos.observarPorOrganizacion(organizacionId),
        oportunidades.observarPorOrganizacion(organizacionId, alcance),
        seguimientos.observarPorOrganizacion(organizacionId, alcance)
    ) { detalle, listaContactos, listaOportunidades, listaGestiones ->
        DetalleOrganizacionUiState(
            cargando = false,
            detalle = detalle,
            contactos = listaContactos,
            oportunidades = listaOportunidades,
            gestiones = listaGestiones,
            puedeEliminar = detalle != null && Permisos.puedeEliminar(usuario.rol) &&
                listaOportunidades.isEmpty()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DetalleOrganizacionUiState()
    )

    private val _mensajes = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val mensajes: SharedFlow<String> = _mensajes.asSharedFlow()

    private val _eliminada = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val eliminada: SharedFlow<Unit> = _eliminada.asSharedFlow()

    /**
     * Elimina la organización junto con sus contactos. El servidor la
     * rechaza (409) si tiene oportunidades, aunque la pantalla no las vea.
     */
    fun eliminar() {
        if (!estado.value.puedeEliminar) return
        viewModelScope.launch {
            try {
                organizaciones.eliminar(organizacionId)
                _mensajes.tryEmit("Organización eliminada")
                _eliminada.tryEmit(Unit)
            } catch (excepcion: Exception) {
                _mensajes.tryEmit("No se pudo eliminar: ${excepcion.message}")
            }
        }
    }
}
