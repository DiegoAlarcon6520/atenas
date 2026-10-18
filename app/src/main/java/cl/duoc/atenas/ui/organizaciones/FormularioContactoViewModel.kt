package cl.duoc.atenas.ui.organizaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.atenas.data.local.entity.Contacto
import cl.duoc.atenas.data.repository.ContactoRepository
import cl.duoc.atenas.data.repository.OrganizacionRepository
import cl.duoc.atenas.di.contenedor
import cl.duoc.atenas.domain.model.AreaContacto
import cl.duoc.atenas.domain.model.MedioContacto
import cl.duoc.atenas.domain.validation.Validadores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ErroresContacto(
    val nombre: String? = null,
    val cargo: String? = null,
    val correo: String? = null,
    val telefono: String? = null
) {
    val hayErrores: Boolean get() = listOfNotNull(nombre, cargo, correo, telefono).isNotEmpty()
}

data class FormularioContactoUiState(
    val cargando: Boolean = true,
    val esEdicion: Boolean = false,
    val guardando: Boolean = false,
    val guardadoConExito: Boolean = false,
    val errorGeneral: String? = null,

    val nombreOrganizacion: String = "",
    val nombre: String = "",
    val cargo: String = "",
    val area: AreaContacto = AreaContacto.EVENTOS,
    val correo: String = "",
    val telefono: String = "",
    val medioPreferente: MedioContacto = MedioContacto.CORREO,
    val activo: Boolean = true,
    val observaciones: String = "",

    val errores: ErroresContacto = ErroresContacto()
)

/**
 * VIEWMODEL DEL FORMULARIO DE CONTACTO
 *
 * El contacto siempre pertenece a una organización, así que el id de esa
 * organización llega por la ruta y no se puede cambiar desde el formulario:
 * se muestra como información, no como campo editable. Eso mantiene la
 * integridad de la clave foránea sin tener que validarla.
 *
 * Correo y teléfono usan validación de formato, no solo de obligatoriedad,
 * porque de eso depende poder contactar efectivamente a la persona.
 */
class FormularioContactoViewModel(
    private val organizacionId: Long,
    private val contactoId: Long,
    private val contactos: ContactoRepository,
    private val organizaciones: OrganizacionRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(FormularioContactoUiState())
    val estado: StateFlow<FormularioContactoUiState> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            val organizacion = organizaciones.porId(organizacionId)
            _estado.update {
                it.copy(cargando = false, nombreOrganizacion = organizacion?.nombre.orEmpty())
            }
            if (contactoId > 0) cargarExistente()
        }
    }

    private suspend fun cargarExistente() {
        val existente = contactos.porId(contactoId) ?: return
        _estado.update {
            it.copy(
                esEdicion = true,
                nombre = existente.nombre,
                cargo = existente.cargo,
                area = existente.area,
                correo = existente.correo,
                telefono = existente.telefono,
                medioPreferente = existente.medioPreferente,
                activo = existente.activo,
                observaciones = existente.observaciones.orEmpty()
            )
        }
    }

    fun alCambiarNombre(valor: String) = _estado.update {
        it.copy(nombre = valor, errores = it.errores.copy(nombre = null))
    }

    fun alCambiarCargo(valor: String) = _estado.update {
        it.copy(cargo = valor, errores = it.errores.copy(cargo = null))
    }

    fun alCambiarArea(valor: AreaContacto) = _estado.update { it.copy(area = valor) }

    fun alCambiarCorreo(valor: String) = _estado.update {
        it.copy(correo = valor, errores = it.errores.copy(correo = null))
    }

    fun alCambiarTelefono(valor: String) = _estado.update {
        it.copy(telefono = valor, errores = it.errores.copy(telefono = null))
    }

    fun alCambiarMedio(valor: MedioContacto) = _estado.update { it.copy(medioPreferente = valor) }

    fun alCambiarActivo(valor: Boolean) = _estado.update { it.copy(activo = valor) }

    fun alCambiarObservaciones(valor: String) = _estado.update { it.copy(observaciones = valor) }

    fun guardar() {
        val actual = _estado.value
        val errores = ErroresContacto(
            nombre = Validadores.texto(actual.nombre, "El nombre", minimo = 3),
            cargo = Validadores.obligatorio(actual.cargo, "El cargo"),
            correo = Validadores.correo(actual.correo),
            telefono = Validadores.telefono(actual.telefono)
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
                    val original = contactos.porId(contactoId) ?: error("El contacto ya no existe.")
                    contactos.actualizar(
                        original.copy(
                            nombre = actual.nombre.trim(),
                            cargo = actual.cargo.trim(),
                            area = actual.area,
                            correo = actual.correo.trim(),
                            telefono = actual.telefono.trim(),
                            medioPreferente = actual.medioPreferente,
                            activo = actual.activo,
                            observaciones = actual.observaciones.ifBlank { null }
                        )
                    )
                } else {
                    contactos.crear(
                        Contacto(
                            organizacionId = organizacionId,
                            nombre = actual.nombre.trim(),
                            cargo = actual.cargo.trim(),
                            area = actual.area,
                            correo = actual.correo.trim(),
                            telefono = actual.telefono.trim(),
                            medioPreferente = actual.medioPreferente,
                            activo = actual.activo,
                            // Un contacto recién creado todavía no ha sido contactado.
                            fechaUltimoContacto = null,
                            observaciones = actual.observaciones.ifBlank { null }
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
                FormularioContactoViewModel(
                    organizacionId = argumentos.get<Long>("organizacionId")
                        ?: error("Falta la organización del contacto."),
                    contactoId = argumentos.get<Long>("contactoId") ?: -1L,
                    contactos = contenedor.contactos,
                    organizaciones = contenedor.organizaciones
                )
            }
        }
    }
}
