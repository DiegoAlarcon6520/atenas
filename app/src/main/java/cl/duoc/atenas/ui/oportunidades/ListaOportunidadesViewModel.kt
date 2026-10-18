package cl.duoc.atenas.ui.oportunidades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.atenas.core.SesionManager
import cl.duoc.atenas.data.local.dao.OportunidadResumen
import cl.duoc.atenas.data.repository.OportunidadRepository
import cl.duoc.atenas.di.contenedor
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.Prioridad
import cl.duoc.atenas.domain.model.TipoOportunidad
import cl.duoc.atenas.domain.rules.Permisos
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class FiltrosOportunidades(
    val texto: String = "",
    val estado: EstadoOportunidad? = null,
    val prioridad: Prioridad? = null,
    val tipo: TipoOportunidad? = null
) {
    val cantidadActivos: Int get() = listOfNotNull(estado, prioridad, tipo).size
    val hayAlguno: Boolean get() = cantidadActivos > 0 || texto.isNotBlank()
}

data class ListaOportunidadesUiState(
    val cargando: Boolean = true,
    val filtros: FiltrosOportunidades = FiltrosOportunidades(),
    val mostrarFiltros: Boolean = false,
    val esSupervisor: Boolean = false,
    val oportunidades: List<OportunidadResumen> = emptyList()
)

/**
 * Listado de oportunidades con búsqueda y filtros.
 *
 * El alcance depende del perfil: el supervisor consulta la cartera completa
 * y el ejecutivo solo las oportunidades asignadas a él.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ListaOportunidadesViewModel(
    sesion: SesionManager,
    oportunidades: OportunidadRepository
) : ViewModel() {

    private val usuario = sesion.requerirUsuario()
    private val alcance: Long? =
        if (Permisos.puedeVerTodasLasOportunidades(usuario.rol)) null else usuario.id

    private val _filtros = MutableStateFlow(FiltrosOportunidades())
    private val _mostrarFiltros = MutableStateFlow(false)

    private val resultados = _filtros.flatMapLatest { filtros ->
        oportunidades.buscar(
            responsableId = alcance,
            texto = filtros.texto,
            estado = filtros.estado,
            prioridad = filtros.prioridad,
            tipo = filtros.tipo
        )
    }

    val estado: StateFlow<ListaOportunidadesUiState> = combine(
        _filtros,
        _mostrarFiltros,
        resultados
    ) { filtros, mostrarFiltros, lista ->
        ListaOportunidadesUiState(
            cargando = false,
            filtros = filtros,
            mostrarFiltros = mostrarFiltros,
            esSupervisor = Permisos.puedeVerTodasLasOportunidades(usuario.rol),
            oportunidades = lista
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ListaOportunidadesUiState()
    )

    fun alBuscar(texto: String) = _filtros.update { it.copy(texto = texto) }

    fun alternarPanelFiltros() = _mostrarFiltros.update { !it }

    /** Tocar el filtro ya seleccionado lo desactiva. */
    fun alSeleccionarEstado(valor: EstadoOportunidad) = _filtros.update {
        it.copy(estado = if (it.estado == valor) null else valor)
    }

    fun alSeleccionarPrioridad(valor: Prioridad) = _filtros.update {
        it.copy(prioridad = if (it.prioridad == valor) null else valor)
    }

    fun alSeleccionarTipo(valor: TipoOportunidad) = _filtros.update {
        it.copy(tipo = if (it.tipo == valor) null else valor)
    }

    fun limpiarFiltros() = _filtros.update { FiltrosOportunidades() }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ListaOportunidadesViewModel(
                    sesion = contenedor.sesion,
                    oportunidades = contenedor.oportunidades
                )
            }
        }
    }
}
