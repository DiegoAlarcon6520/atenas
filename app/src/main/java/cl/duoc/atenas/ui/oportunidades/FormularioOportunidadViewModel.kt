package cl.duoc.atenas.ui.oportunidades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.core.SesionManager
import cl.duoc.atenas.data.local.entity.Comuna
import cl.duoc.atenas.data.local.entity.Contacto
import cl.duoc.atenas.data.local.entity.Oportunidad
import cl.duoc.atenas.data.local.entity.Organizacion
import cl.duoc.atenas.data.local.entity.Region
import cl.duoc.atenas.data.repository.CatalogoRepository
import cl.duoc.atenas.data.repository.ContactoRepository
import cl.duoc.atenas.data.repository.OportunidadRepository
import cl.duoc.atenas.data.repository.OrganizacionRepository
import cl.duoc.atenas.di.contenedor
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.Prioridad
import cl.duoc.atenas.domain.model.RolUsuario
import cl.duoc.atenas.domain.model.TipoOportunidad
import cl.duoc.atenas.domain.validation.Validadores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Mensajes de error de cada campo del formulario.
 *
 * Se modela como una clase con un campo por error (en vez de un Map) para que
 * la pantalla pueda escribir `errores.nombre` y el compilador avise si alguien
 * se equivoca en el nombre del campo.
 */
data class ErroresFormulario(
    val nombre: String? = null,
    val descripcion: String? = null,
    val fuenteInformacion: String? = null,
    val fechaInicioEvento: String? = null,
    val fechaTerminoEvento: String? = null,
    val horario: String? = null,
    val direccion: String? = null,
    val comuna: String? = null,
    val organizacion: String? = null,
    val publicoObjetivo: String? = null,
    val publicoEsperado: String? = null,
    val costoReferencial: String? = null
) {
    /** True si quedó al menos un error: bloquea el guardado. */
    val hayErrores: Boolean
        get() = listOfNotNull(
            nombre, descripcion, fuenteInformacion, fechaInicioEvento, fechaTerminoEvento,
            horario, direccion, comuna, organizacion, publicoObjetivo, publicoEsperado,
            costoReferencial
        ).isNotEmpty()
}

/**
 * Estado completo de la pantalla de formulario.
 *
 * Todos los valores que el usuario escribe se guardan como String aunque el
 * destino sea un número: así, si escribe "12a" en público esperado, el texto
 * no se pierde y podemos mostrarle el error sin borrarle lo que puso. La
 * conversión a Int/Double ocurre recién al guardar.
 */
data class FormularioOportunidadUiState(
    val cargando: Boolean = true,
    val esEdicion: Boolean = false,
    val guardando: Boolean = false,
    val guardadoConExito: Boolean = false,
    val errorGeneral: String? = null,

    // --- Valores del formulario ---
    val nombre: String = "",
    val descripcion: String = "",
    val fuenteInformacion: String = "",
    val tipo: TipoOportunidad = TipoOportunidad.EVENTO_CORPORATIVO,
    val prioridad: Prioridad = Prioridad.MEDIA,
    val fechaDeteccion: Long = Fechas.hoy(),
    val fechaInicioEvento: Long? = null,
    val fechaTerminoEvento: Long? = null,
    val horaInicio: Int = 9 * 60,
    val horaTermino: Int = 18 * 60,
    val direccion: String = "",
    val regionId: Long? = null,
    val comunaId: Long? = null,
    val organizacionId: Long? = null,
    val contactoId: Long? = null,
    val publicoObjetivo: String = "",
    val publicoEsperado: String = "",
    val costoReferencial: String = "",
    val condiciones: String = "",
    val requisitos: String = "",
    val observaciones: String = "",

    // --- Catálogos para las listas desplegables ---
    val regiones: List<Region> = emptyList(),
    val comunasDisponibles: List<Comuna> = emptyList(),
    val organizaciones: List<Organizacion> = emptyList(),
    val contactosDisponibles: List<Contacto> = emptyList(),

    val errores: ErroresFormulario = ErroresFormulario()
)

/**
 * VIEWMODEL DEL FORMULARIO DE OPORTUNIDAD
 *
 * Sirve para crear y para editar: si recibe un id mayor que cero carga la
 * oportunidad existente y la deja precargada; si no, parte en blanco.
 *
 * Responsabilidades:
 *  - mantener el estado de los campos mientras el usuario escribe,
 *  - cargar los catálogos (regiones, comunas, organizaciones, contactos),
 *  - validar antes de guardar usando Validadores,
 *  - construir la entidad Oportunidad y pedirle al repositorio que la persista.
 *
 * Lo que NO hace: dibujar nada. La pantalla solo lee el estado y llama a estos
 * métodos.
 */
class FormularioOportunidadViewModel(
    private val oportunidadId: Long,
    private val sesion: SesionManager,
    private val oportunidades: OportunidadRepository,
    private val catalogo: CatalogoRepository,
    private val organizaciones: OrganizacionRepository,
    private val contactos: ContactoRepository
) : ViewModel() {

    // Listas completas que se cargan una vez y luego se filtran en memoria.
    // Son pocas filas, así que no vale la pena volver a consultar la base cada
    // vez que el usuario cambia de región o de organización.
    private var todasLasComunas: List<Comuna> = emptyList()
    private var todosLosContactos: List<Contacto> = emptyList()

    // _estado es privado y mutable: solo el ViewModel lo modifica.
    private val _estado = MutableStateFlow(FormularioOportunidadUiState())

    // estado es público y de solo lectura: es lo que observa la pantalla.
    val estado: StateFlow<FormularioOportunidadUiState> = _estado.asStateFlow()

    init {
        cargarDatosIniciales()
    }

    /** Trae los catálogos y, si estamos editando, los datos de la oportunidad. */
    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            // first() toma la primera emisión del Flow y deja de escuchar:
            // los catálogos no cambian mientras el formulario está abierto.
            val regiones = catalogo.observarRegiones().first()
            todasLasComunas = catalogo.observarComunas().first()
            val listaOrganizaciones = organizaciones.observarTodas().first()
            todosLosContactos = contactos.observarTodos().first()

            _estado.update {
                it.copy(
                    cargando = false,
                    regiones = regiones,
                    organizaciones = listaOrganizaciones
                )
            }

            if (oportunidadId > 0) cargarOportunidadExistente()
        }
    }

    /** Precarga el formulario con los datos actuales de la oportunidad a editar. */
    private suspend fun cargarOportunidadExistente() {
        val existente = oportunidades.porId(oportunidadId) ?: return
        val comuna = todasLasComunas.firstOrNull { it.id == existente.comunaId }

        _estado.update {
            it.copy(
                esEdicion = true,
                nombre = existente.nombre,
                descripcion = existente.descripcion,
                fuenteInformacion = existente.fuenteInformacion,
                tipo = existente.tipo,
                prioridad = existente.prioridad,
                fechaDeteccion = existente.fechaDeteccion,
                fechaInicioEvento = existente.fechaInicioEvento,
                fechaTerminoEvento = existente.fechaTerminoEvento,
                horaInicio = existente.horaInicio,
                horaTermino = existente.horaTermino,
                direccion = existente.direccion,
                regionId = comuna?.regionId,
                comunaId = existente.comunaId,
                organizacionId = existente.organizacionId,
                contactoId = existente.contactoId,
                publicoObjetivo = existente.publicoObjetivo,
                publicoEsperado = existente.publicoEsperado.toString(),
                costoReferencial = if (existente.costoReferencial > 0) {
                    existente.costoReferencial.toLong().toString()
                } else {
                    ""
                },
                condiciones = existente.condiciones.orEmpty(),
                requisitos = existente.requisitos.orEmpty(),
                observaciones = existente.observaciones.orEmpty(),
                comunasDisponibles = todasLasComunas.filter { c -> c.regionId == comuna?.regionId },
                contactosDisponibles = todosLosContactos.filter { c ->
                    c.organizacionId == existente.organizacionId
                }
            )
        }
    }

    // ---------------------------------------------------------------------
    // Handlers: un método por campo. Cada uno guarda el valor nuevo y limpia
    // el error de ESE campo, para que el mensaje rojo desaparezca apenas el
    // usuario empieza a corregir.
    // ---------------------------------------------------------------------

    fun alCambiarNombre(valor: String) = _estado.update {
        it.copy(nombre = valor, errores = it.errores.copy(nombre = null))
    }

    fun alCambiarDescripcion(valor: String) = _estado.update {
        it.copy(descripcion = valor, errores = it.errores.copy(descripcion = null))
    }

    fun alCambiarFuente(valor: String) = _estado.update {
        it.copy(fuenteInformacion = valor, errores = it.errores.copy(fuenteInformacion = null))
    }

    fun alCambiarTipo(valor: TipoOportunidad) = _estado.update { it.copy(tipo = valor) }

    fun alCambiarPrioridad(valor: Prioridad) = _estado.update { it.copy(prioridad = valor) }

    fun alCambiarFechaInicio(valor: Long) = _estado.update {
        it.copy(fechaInicioEvento = valor, errores = it.errores.copy(fechaInicioEvento = null))
    }

    fun alCambiarFechaTermino(valor: Long?) = _estado.update {
        it.copy(fechaTerminoEvento = valor, errores = it.errores.copy(fechaTerminoEvento = null))
    }

    fun alCambiarHoraInicio(valor: Int) = _estado.update {
        it.copy(horaInicio = valor, errores = it.errores.copy(horario = null))
    }

    fun alCambiarHoraTermino(valor: Int) = _estado.update {
        it.copy(horaTermino = valor, errores = it.errores.copy(horario = null))
    }

    fun alCambiarDireccion(valor: String) = _estado.update {
        it.copy(direccion = valor, errores = it.errores.copy(direccion = null))
    }

    /** Al cambiar de región se recalculan las comunas y se borra la comuna elegida. */
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

    /** Al cambiar de organización se recalculan sus contactos y se borra el contacto elegido. */
    fun alCambiarOrganizacion(valor: Long) = _estado.update {
        it.copy(
            organizacionId = valor,
            contactoId = null,
            contactosDisponibles = todosLosContactos.filter { c -> c.organizacionId == valor },
            errores = it.errores.copy(organizacion = null)
        )
    }

    fun alCambiarContacto(valor: Long?) = _estado.update { it.copy(contactoId = valor) }

    fun alCambiarPublicoObjetivo(valor: String) = _estado.update {
        it.copy(publicoObjetivo = valor, errores = it.errores.copy(publicoObjetivo = null))
    }

    /** Solo acepta dígitos: evita que el usuario escriba letras en un campo numérico. */
    fun alCambiarPublicoEsperado(valor: String) = _estado.update {
        it.copy(
            publicoEsperado = valor.filter { caracter -> caracter.isDigit() },
            errores = it.errores.copy(publicoEsperado = null)
        )
    }

    fun alCambiarCosto(valor: String) = _estado.update {
        it.copy(
            costoReferencial = valor.filter { caracter -> caracter.isDigit() },
            errores = it.errores.copy(costoReferencial = null)
        )
    }

    fun alCambiarCondiciones(valor: String) = _estado.update { it.copy(condiciones = valor) }

    fun alCambiarRequisitos(valor: String) = _estado.update { it.copy(requisitos = valor) }

    fun alCambiarObservaciones(valor: String) = _estado.update { it.copy(observaciones = valor) }

    /**
     * Valida todos los campos de una vez y devuelve los errores encontrados.
     * No modifica el estado: eso lo decide guardar().
     */
    private fun validar(actual: FormularioOportunidadUiState) = ErroresFormulario(
        nombre = Validadores.texto(actual.nombre, "El nombre del evento", minimo = 5),
        descripcion = Validadores.texto(actual.descripcion, "La descripción", minimo = 10),
        fuenteInformacion = Validadores.obligatorio(actual.fuenteInformacion, "La fuente"),
        // Dos reglas sobre la misma fecha: que exista y que sea coherente con la detección.
        fechaInicioEvento = Validadores.fechaRequerida(actual.fechaInicioEvento, "La fecha del evento")
            ?: Validadores.fechaPosteriorA(
                posterior = actual.fechaInicioEvento,
                anterior = actual.fechaDeteccion,
                mensaje = "El evento no puede ocurrir antes de haberse detectado"
            ),
        fechaTerminoEvento = Validadores.fechaPosteriorA(
            posterior = actual.fechaTerminoEvento,
            anterior = actual.fechaInicioEvento,
            mensaje = "La fecha de término no puede ser anterior al inicio"
        ),
        // Con selectores de reloj ya no puede haber formatos inválidos:
        // lo único que queda por validar es la coherencia entre ambas horas.
        horario = if (actual.horaTermino <= actual.horaInicio) {
            "La hora de término debe ser posterior a la de inicio"
        } else {
            null
        },
        direccion = Validadores.texto(actual.direccion, "La dirección", minimo = 5),
        comuna = Validadores.seleccionRequerida(actual.comunaId, "una comuna"),
        organizacion = Validadores.seleccionRequerida(actual.organizacionId, "una organización"),
        publicoObjetivo = Validadores.obligatorio(actual.publicoObjetivo, "El público objetivo"),
        publicoEsperado = Validadores.enteroPositivo(actual.publicoEsperado, "El público esperado"),
        costoReferencial = Validadores.montoNoNegativo(actual.costoReferencial, "El costo referencial")
    )

    /**
     * Valida y, si todo está correcto, persiste en Room.
     *
     * Si la validación falla NO se borra nada de lo escrito: solo se guardan
     * los mensajes de error en el estado y la pantalla los muestra bajo cada
     * campo.
     */
    fun guardar() {
        val actual = _estado.value
        val errores = validar(actual)

        if (errores.hayErrores) {
            _estado.update {
                it.copy(
                    errores = errores,
                    errorGeneral = "Revisa los campos marcados antes de guardar"
                )
            }
            return
        }

        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, errorGeneral = null) }
            try {
                val usuario = sesion.requerirUsuario()
                val ahora = Fechas.ahora()

                if (actual.esEdicion) {
                    // En edición conservamos lo que el formulario no maneja:
                    // estado, responsable, creador y fecha de creación original.
                    val original = oportunidades.porId(oportunidadId)
                        ?: error("La oportunidad ya no existe.")
                    oportunidades.actualizar(
                        original.copy(
                            nombre = actual.nombre.trim(),
                            tipo = actual.tipo,
                            descripcion = actual.descripcion.trim(),
                            fuenteInformacion = actual.fuenteInformacion.trim(),
                            fechaInicioEvento = actual.fechaInicioEvento!!,
                            fechaTerminoEvento = actual.fechaTerminoEvento,
                            horaInicio = actual.horaInicio,
                            horaTermino = actual.horaTermino,
                            direccion = actual.direccion.trim(),
                            comunaId = actual.comunaId!!,
                            organizacionId = actual.organizacionId!!,
                            contactoId = actual.contactoId,
                            publicoObjetivo = actual.publicoObjetivo.trim(),
                            publicoEsperado = actual.publicoEsperado.toInt(),
                            prioridad = actual.prioridad,
                            costoReferencial = actual.costoReferencial.toDoubleOrNull() ?: 0.0,
                            condiciones = actual.condiciones.ifBlank { null },
                            requisitos = actual.requisitos.ifBlank { null },
                            observaciones = actual.observaciones.ifBlank { null },
                            fechaActualizacion = ahora
                        )
                    )
                } else {
                    oportunidades.crear(
                        Oportunidad(
                            nombre = actual.nombre.trim(),
                            tipo = actual.tipo,
                            descripcion = actual.descripcion.trim(),
                            fuenteInformacion = actual.fuenteInformacion.trim(),
                            fechaDeteccion = actual.fechaDeteccion,
                            fechaInicioEvento = actual.fechaInicioEvento!!,
                            fechaTerminoEvento = actual.fechaTerminoEvento,
                            horaInicio = actual.horaInicio,
                            horaTermino = actual.horaTermino,
                            direccion = actual.direccion.trim(),
                            comunaId = actual.comunaId!!,
                            organizacionId = actual.organizacionId!!,
                            contactoId = actual.contactoId,
                            // Un ejecutivo se queda a cargo de lo que registra;
                            // si la crea el supervisor, queda sin asignar para
                            // que él mismo la reparta después.
                            responsableId = if (usuario.rol == RolUsuario.EJECUTIVO) usuario.id else null,
                            creadoPorId = usuario.id,
                            publicoObjetivo = actual.publicoObjetivo.trim(),
                            publicoEsperado = actual.publicoEsperado.toInt(),
                            prioridad = actual.prioridad,
                            // Toda oportunidad nueva entra al embudo por el principio.
                            estado = EstadoOportunidad.IDENTIFICADA,
                            costoReferencial = actual.costoReferencial.toDoubleOrNull() ?: 0.0,
                            condiciones = actual.condiciones.ifBlank { null },
                            requisitos = actual.requisitos.ifBlank { null },
                            observaciones = actual.observaciones.ifBlank { null },
                            fechaCreacion = ahora,
                            fechaActualizacion = ahora
                        )
                    )
                }

                _estado.update { it.copy(guardando = false, guardadoConExito = true) }
            } catch (excepcion: Exception) {
                // Si la base de datos rechaza la operación el usuario debe
                // enterarse: nunca se cierra la pantalla en silencio.
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
        /**
         * Fábrica del ViewModel. createSavedStateHandle() entrega los
         * argumentos de navegación; de ahí sacamos el id de la oportunidad
         * (-1 significa "crear una nueva").
         */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val argumentos = createSavedStateHandle()
                FormularioOportunidadViewModel(
                    oportunidadId = argumentos.get<Long>("oportunidadId") ?: -1L,
                    sesion = contenedor.sesion,
                    oportunidades = contenedor.oportunidades,
                    catalogo = contenedor.catalogo,
                    organizaciones = contenedor.organizaciones,
                    contactos = contenedor.contactos
                )
            }
        }
    }
}
