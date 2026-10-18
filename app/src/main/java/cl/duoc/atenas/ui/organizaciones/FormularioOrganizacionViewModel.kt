package cl.duoc.atenas.ui.organizaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.data.local.entity.Comuna
import cl.duoc.atenas.data.local.entity.Organizacion
import cl.duoc.atenas.data.local.entity.Region
import cl.duoc.atenas.data.repository.CatalogoRepository
import cl.duoc.atenas.data.repository.OrganizacionRepository
import cl.duoc.atenas.di.contenedor
import cl.duoc.atenas.domain.model.TipoOrganizacion
import cl.duoc.atenas.domain.validation.Validadores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ErroresOrganizacion(
    val nombre: String? = null,
    val rubro: String? = null,
    val comuna: String? = null
) {
    val hayErrores: Boolean get() = listOfNotNull(nombre, rubro, comuna).isNotEmpty()
}

data class FormularioOrganizacionUiState(
    val cargando: Boolean = true,
    val esEdicion: Boolean = false,
    val guardando: Boolean = false,
    val guardadoConExito: Boolean = false,
    val errorGeneral: String? = null,

    val nombre: String = "",
    val tipo: TipoOrganizacion = TipoOrganizacion.EMPRESA,
    val rubro: String = "",
    val regionId: Long? = null,
    val comunaId: Long? = null,
    val sitioWeb: String = "",
    val publicoAsociado: String = "",
    val observaciones: String = "",

    val regiones: List<Region> = emptyList(),
    val comunasDisponibles: List<Comuna> = emptyList(),

    val errores: ErroresOrganizacion = ErroresOrganizacion()
)

/**
 * VIEWMODEL DEL FORMULARIO DE ORGANIZACIÓN
 *
 * Mismo patrón que el formulario de oportunidad, pero más corto: una
 * organización tiene menos campos y no participa en el embudo de estados.
 *
 * Sitio web, público asociado y observaciones son opcionales: si quedan
 * vacíos se guardan como null en vez de cadena vacía, para que la base no
 * tenga filas con texto en blanco que después haya que interpretar.
 */
class FormularioOrganizacionViewModel(
    private val organizacionId: Long,
    private val organizaciones: OrganizacionRepository,
    private val catalogo: CatalogoRepository
) : ViewModel() {

    private var todasLasComunas: List<Comuna> = emptyList()

    private val _estado = MutableStateFlow(FormularioOrganizacionUiState())
    val estado: StateFlow<FormularioOrganizacionUiState> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            val regiones = catalogo.observarRegiones().first()
            todasLasComunas = catalogo.observarComunas().first()
            _estado.update { it.copy(cargando = false, regiones = regiones) }

            if (organizacionId > 0) cargarExistente()
        }
    }

    private suspend fun cargarExistente() {
        val existente = organizaciones.porId(organizacionId) ?: return
        val comuna = todasLasComunas.firstOrNull { it.id == existente.comunaId }
        _estado.update {
            it.copy(
                esEdicion = true,
                nombre = existente.nombre,
                tipo = existente.tipo,
                rubro = existente.rubro,
                regionId = comuna?.regionId,
                comunaId = existente.comunaId,
                sitioWeb = existente.sitioWeb.orEmpty(),
                publicoAsociado = existente.publicoAsociado.orEmpty(),
                observaciones = existente.observaciones.orEmpty(),
                comunasDisponibles = todasLasComunas.filter { c -> c.regionId == comuna?.regionId }
            )
        }
    }

    fun alCambiarNombre(valor: String) = _estado.update {
        it.copy(nombre = valor, errores = it.errores.copy(nombre = null))
    }

    fun alCambiarTipo(valor: TipoOrganizacion) = _estado.update { it.copy(tipo = valor) }

    fun alCambiarRubro(valor: String) = _estado.update {
        it.copy(rubro = valor, errores = it.errores.copy(rubro = null))
    }

    fun alCambiarRegion(valor: Long) = _estado.update {
        it.copy(
            regionId = valor,
            comunaId = null,
            comunasDisponibles = todasLasComunas.filter { comuna -> comuna.regionId == valor },
            errores = it.errores.copy(comuna = null)
        )
    }

    fun alCambiarComuna(valor: Long) = _estado.update {
        it.copy(comunaId = valor, errores = it.errores.copy(comuna = null))
    }

    fun alCambiarSitioWeb(valor: String) = _estado.update { it.copy(sitioWeb = valor) }

    fun alCambiarPublicoAsociado(valor: String) = _estado.update { it.copy(publicoAsociado = valor) }

    fun alCambiarObservaciones(valor: String) = _estado.update { it.copy(observaciones = valor) }

    fun guardar() {
        val actual = _estado.value
        val errores = ErroresOrganizacion(
            nombre = Validadores.texto(actual.nombre, "El nombre", minimo = 3),
            rubro = Validadores.obligatorio(actual.rubro, "El rubro"),
            comuna = Validadores.seleccionRequerida(actual.comunaId, "una comuna")
        )

        if (errores.hayErrores) {
            _estado.update {
                it.copy(errores = errores, errorGeneral = "Revisa los campos marcados")
            }
            return
        }

        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, errorGeneral = null) }
            try {
                if (actual.esEdicion) {
                    val original = organizaciones.porId(organizacionId)
                        ?: error("La organización ya no existe.")
                    organizaciones.actualizar(
                        original.copy(
                            nombre = actual.nombre.trim(),
                            tipo = actual.tipo,
                            rubro = actual.rubro.trim(),
                            comunaId = actual.comunaId!!,
                            sitioWeb = actual.sitioWeb.ifBlank { null },
                            publicoAsociado = actual.publicoAsociado.ifBlank { null },
                            observaciones = actual.observaciones.ifBlank { null }
                        )
                    )
                } else {
                    organizaciones.crear(
                        Organizacion(
                            nombre = actual.nombre.trim(),
                            tipo = actual.tipo,
                            rubro = actual.rubro.trim(),
                            comunaId = actual.comunaId!!,
                            sitioWeb = actual.sitioWeb.ifBlank { null },
                            publicoAsociado = actual.publicoAsociado.ifBlank { null },
                            observaciones = actual.observaciones.ifBlank { null },
                            fechaCreacion = Fechas.ahora()
                        )
                    )
                }
                _estado.update { it.copy(guardando = false, guardadoConExito = true) }
            } catch (excepcion: Exception) {
                _estado.update {
                    it.copy(
                        guardando = false,
                        errorGeneral = "No se pudo guardar: ${excepcion.message ?: "error desconocido"}"
                    )
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val argumentos = createSavedStateHandle()
                FormularioOrganizacionViewModel(
                    organizacionId = argumentos.get<Long>("organizacionId") ?: -1L,
                    organizaciones = contenedor.organizaciones,
                    catalogo = contenedor.catalogo
                )
            }
        }
    }
}
