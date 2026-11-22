package cl.duoc.atenas.ui.oportunidades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.core.SesionManager
import cl.duoc.atenas.data.local.dao.OportunidadResumen
import cl.duoc.atenas.data.local.entity.Comuna
import cl.duoc.atenas.data.local.entity.Organizacion
import cl.duoc.atenas.data.local.entity.Region
import cl.duoc.atenas.data.local.entity.Usuario
import cl.duoc.atenas.data.repository.CatalogoRepository
import cl.duoc.atenas.data.repository.CriteriosBusqueda
import cl.duoc.atenas.data.repository.OportunidadRepository
import cl.duoc.atenas.data.repository.OrganizacionRepository
import cl.duoc.atenas.data.repository.UsuarioRepository
import cl.duoc.atenas.di.contenedor
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.EtapaCartera
import cl.duoc.atenas.domain.model.PotencialComercial
import cl.duoc.atenas.domain.model.Prioridad
import cl.duoc.atenas.domain.model.RangoFecha
import cl.duoc.atenas.domain.model.TipoOportunidad
import cl.duoc.atenas.domain.rules.Permisos
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/** Filtros elegidos por el usuario en el listado (HU-18). */
data class FiltrosOportunidades(
    val texto: String = "",
    val estado: EstadoOportunidad? = null,
    val prioridad: Prioridad? = null,
    val tipo: TipoOportunidad? = null,
    val potencial: PotencialComercial? = null,
    val rangoFecha: RangoFecha? = null,
    val regionId: Long? = null,
    val comunaId: Long? = null,
    val organizacionId: Long? = null,
    val responsableId: Long? = null
) {
    val cantidadActivos: Int
        get() = listOfNotNull(
            estado, prioridad, tipo, potencial, rangoFecha,
            regionId, comunaId, organizacionId, responsableId
        ).size

    val hayAlguno: Boolean get() = cantidadActivos > 0 || texto.isNotBlank()

    /** Traduce los filtros de pantalla a los criterios que entiende el repositorio. */
    fun aCriterios(hoy: Long): CriteriosBusqueda {
        val (desde, hasta) = rangoFecha?.limites(hoy) ?: (null to null)
        return CriteriosBusqueda(
            texto = texto,
            estado = estado,
            prioridad = prioridad,
            tipo = tipo,
            potencial = potencial,
            desde = desde,
            hasta = hasta,
            regionId = regionId,
            comunaId = comunaId,
            organizacionId = organizacionId,
            responsableId = responsableId
        )
    }
}

/** Opciones de las listas desplegables de filtros. */
data class CatalogosFiltro(
    val regiones: List<Region> = emptyList(),
    val comunas: List<Comuna> = emptyList(),
    val organizaciones: List<Organizacion> = emptyList(),
    val ejecutivos: List<Usuario> = emptyList()
)

data class ListaOportunidadesUiState(
    val cargando: Boolean = true,
    val filtros: FiltrosOportunidades = FiltrosOportunidades(),
    val mostrarFiltros: Boolean = false,
    val esSupervisor: Boolean = false,
    /** Oportunidades de la etapa elegida. */
    val oportunidades: List<OportunidadResumen> = emptyList(),
    /** Etapa elegida en las pestañas; null muestra todas. */
    val etapa: EtapaCartera? = null,
    /** Cuántas oportunidades hay en cada etapa con la búsqueda y filtros actuales. */
    val conteoPorEtapa: Map<EtapaCartera, Int> = emptyMap(),
    val totalSinEtapa: Int = 0,
    val regiones: List<Region> = emptyList(),
    /** Comunas de la región elegida; todas si no hay región. */
    val comunasDisponibles: List<Comuna> = emptyList(),
    val organizaciones: List<Organizacion> = emptyList(),
    /** Solo se llena para el supervisor, que es quien filtra por responsable. */
    val ejecutivos: List<Usuario> = emptyList()
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
    oportunidades: OportunidadRepository,
    catalogo: CatalogoRepository,
    organizaciones: OrganizacionRepository,
    usuarios: UsuarioRepository
) : ViewModel() {

    private val usuario = sesion.requerirUsuario()
    private val esSupervisor = Permisos.puedeVerTodasLasOportunidades(usuario.rol)
    private val alcance: Long? = if (esSupervisor) null else usuario.id

    private val _filtros = MutableStateFlow(FiltrosOportunidades())
    private val _mostrarFiltros = MutableStateFlow(false)
    private val _etapa = MutableStateFlow<EtapaCartera?>(null)

    private val resultados = _filtros.flatMapLatest { filtros ->
        oportunidades.buscar(responsableId = alcance, filtros = filtros.aCriterios(Fechas.hoy()))
    }

    private val catalogos = combine(
        catalogo.observarRegiones(),
        catalogo.observarComunas(),
        organizaciones.observarTodas(),
        if (esSupervisor) usuarios.observarEjecutivos() else flowOf(emptyList())
    ) { regiones, comunas, listaOrganizaciones, ejecutivos ->
        CatalogosFiltro(regiones, comunas, listaOrganizaciones, ejecutivos)
    }

    private val vista = combine(_filtros, _mostrarFiltros, _etapa) { f, m, e -> Triple(f, m, e) }

    val estado: StateFlow<ListaOportunidadesUiState> = combine(
        vista,
        resultados,
        catalogos
    ) { (filtros, mostrarFiltros, etapa), lista, cat ->
        ListaOportunidadesUiState(
            cargando = false,
            filtros = filtros,
            mostrarFiltros = mostrarFiltros,
            esSupervisor = esSupervisor,
            // Las pestañas de etapa filtran en memoria: así cada pestaña
            // puede mostrar su total sin otra consulta a la base.
            oportunidades = if (etapa == null) lista else lista.filter { it.estado in etapa.estados },
            etapa = etapa,
            conteoPorEtapa = EtapaCartera.entries.associateWith { e -> lista.count { it.estado in e.estados } },
            totalSinEtapa = lista.size,
            regiones = cat.regiones,
            comunasDisponibles = filtros.regionId
                ?.let { id -> cat.comunas.filter { it.regionId == id } }
                ?: cat.comunas,
            organizaciones = cat.organizaciones,
            ejecutivos = cat.ejecutivos
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ListaOportunidadesUiState()
    )

    fun alBuscar(texto: String) = _filtros.update { it.copy(texto = texto) }

    fun alternarPanelFiltros() = _mostrarFiltros.update { !it }

    fun cerrarPanelFiltros() = _mostrarFiltros.update { false }

    fun alElegirEtapa(etapa: EtapaCartera?) = _etapa.update { etapa }

    fun quitarRangoFecha() = _filtros.update { it.copy(rangoFecha = null) }
    fun quitarPrioridad() = _filtros.update { it.copy(prioridad = null) }
    fun quitarTipo() = _filtros.update { it.copy(tipo = null) }
    fun quitarPotencial() = _filtros.update { it.copy(potencial = null) }

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

    fun alSeleccionarPotencial(valor: PotencialComercial) = _filtros.update {
        it.copy(potencial = if (it.potencial == valor) null else valor)
    }

    fun alSeleccionarRangoFecha(valor: RangoFecha) = _filtros.update {
        it.copy(rangoFecha = if (it.rangoFecha == valor) null else valor)
    }

    /** Cambiar de región limpia la comuna, que podría no pertenecer a la nueva región. */
    fun alSeleccionarRegion(region: Region?) = _filtros.update {
        it.copy(regionId = region?.id, comunaId = null)
    }

    fun alSeleccionarComuna(comuna: Comuna?) = _filtros.update { it.copy(comunaId = comuna?.id) }

    fun alSeleccionarOrganizacion(organizacion: Organizacion?) = _filtros.update {
        it.copy(organizacionId = organizacion?.id)
    }

    fun alSeleccionarResponsable(ejecutivo: Usuario?) = _filtros.update {
        it.copy(responsableId = ejecutivo?.id)
    }

    /** Quita los filtros pero conserva lo escrito en el buscador. */
    fun limpiarFiltros() = _filtros.update { FiltrosOportunidades(texto = it.texto) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ListaOportunidadesViewModel(
                    sesion = contenedor.sesion,
                    oportunidades = contenedor.oportunidades,
                    catalogo = contenedor.catalogo,
                    organizaciones = contenedor.organizaciones,
                    usuarios = contenedor.usuarios
                )
            }
        }
    }
}
