package cl.duoc.atenas.ui.organizaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.atenas.data.local.dao.OportunidadResumen
import cl.duoc.atenas.data.local.dao.OrganizacionDetalle
import cl.duoc.atenas.data.local.entity.Contacto
import cl.duoc.atenas.data.repository.ContactoRepository
import cl.duoc.atenas.data.repository.OportunidadRepository
import cl.duoc.atenas.data.repository.OrganizacionRepository
import cl.duoc.atenas.di.contenedor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DetalleOrganizacionUiState(
    val cargando: Boolean = true,
    val detalle: OrganizacionDetalle? = null,
    val contactos: List<Contacto> = emptyList(),
    val oportunidades: List<OportunidadResumen> = emptyList()
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
    organizaciones: OrganizacionRepository,
    contactos: ContactoRepository,
    oportunidades: OportunidadRepository
) : ViewModel() {

    val estado: StateFlow<DetalleOrganizacionUiState> = combine(
        organizaciones.observarDetalle(organizacionId),
        contactos.observarPorOrganizacion(organizacionId),
        oportunidades.observarPorOrganizacion(organizacionId)
    ) { detalle, listaContactos, listaOportunidades ->
        DetalleOrganizacionUiState(
            cargando = false,
            detalle = detalle,
            contactos = listaContactos,
            oportunidades = listaOportunidades
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DetalleOrganizacionUiState()
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val argumentos = createSavedStateHandle()
                DetalleOrganizacionViewModel(
                    organizacionId = argumentos.get<Long>("organizacionId")
                        ?: error("Falta el identificador de la organización."),
                    organizaciones = contenedor.organizaciones,
                    contactos = contenedor.contactos,
                    oportunidades = contenedor.oportunidades
                )
            }
        }
    }
}
