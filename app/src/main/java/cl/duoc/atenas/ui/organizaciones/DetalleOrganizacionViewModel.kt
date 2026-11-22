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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DetalleOrganizacionUiState(
    val cargando: Boolean = true,
    val detalle: OrganizacionDetalle? = null,
    val contactos: List<Contacto> = emptyList(),
    val oportunidades: List<OportunidadResumen> = emptyList(),
    val gestiones: List<GestionDeOrganizacion> = emptyList()
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
    organizacionId: Long,
    sesion: SesionManager,
    organizaciones: OrganizacionRepository,
    contactos: ContactoRepository,
    oportunidades: OportunidadRepository,
    seguimientos: SeguimientoRepository
) : ViewModel() {

    /**
     * Mismo alcance que el resto de la aplicación: el supervisor ve todas las
     * oportunidades y gestiones de la organización; el ejecutivo, solo las
     * suyas. La organización y sus contactos son compartidos.
     */
    private val alcance: Long? = sesion.requerirUsuario().let { usuario ->
        if (Permisos.puedeVerTodasLasOportunidades(usuario.rol)) null else usuario.id
    }

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
            gestiones = listaGestiones
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DetalleOrganizacionUiState()
    )
}
