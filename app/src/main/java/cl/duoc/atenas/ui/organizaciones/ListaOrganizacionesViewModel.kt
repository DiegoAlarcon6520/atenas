package cl.duoc.atenas.ui.organizaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.atenas.data.local.dao.OrganizacionResumen
import cl.duoc.atenas.data.repository.OrganizacionRepository
import cl.duoc.atenas.di.contenedor
import cl.duoc.atenas.domain.model.TipoOrganizacion
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class FiltrosOrganizaciones(
    val texto: String = "",
    val tipo: TipoOrganizacion? = null
) {
    val hayAlguno: Boolean get() = texto.isNotBlank() || tipo != null
}

data class ListaOrganizacionesUiState(
    val cargando: Boolean = true,
    val filtros: FiltrosOrganizaciones = FiltrosOrganizaciones(),
    val organizaciones: List<OrganizacionResumen> = emptyList()
)

/**
 * VIEWMODEL DEL LISTADO DE ORGANIZACIONES
 *
 * A diferencia de las oportunidades, las organizaciones no se filtran por
 * responsable: son un catálogo compartido por todo el equipo comercial, así
 * que ambos roles ven la misma lista completa.
 *
 * flatMapLatest: cada vez que cambian los filtros se cancela la consulta
 * anterior y se lanza una nueva. Evita que una búsqueda vieja y lenta
 * sobrescriba el resultado de la búsqueda actual.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ListaOrganizacionesViewModel(
    organizaciones: OrganizacionRepository
) : ViewModel() {

    private val _filtros = MutableStateFlow(FiltrosOrganizaciones())

    private val resultados = _filtros.flatMapLatest { filtros ->
        organizaciones.observarResumen(filtros.texto, filtros.tipo)
    }

    val estado: StateFlow<ListaOrganizacionesUiState> = combine(
        _filtros,
        resultados
    ) { filtros, lista ->
        ListaOrganizacionesUiState(
            cargando = false,
            filtros = filtros,
            organizaciones = lista
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ListaOrganizacionesUiState()
    )

    fun alBuscar(texto: String) = _filtros.update { it.copy(texto = texto) }

    /** Tocar el tipo ya seleccionado lo desactiva. */
    fun alSeleccionarTipo(valor: TipoOrganizacion) = _filtros.update {
        it.copy(tipo = if (it.tipo == valor) null else valor)
    }

    fun limpiarFiltros() = _filtros.update { FiltrosOrganizaciones() }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ListaOrganizacionesViewModel(organizaciones = contenedor.organizaciones)
            }
        }
    }
}
