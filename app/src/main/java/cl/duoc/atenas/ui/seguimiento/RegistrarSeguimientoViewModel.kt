package cl.duoc.atenas.ui.seguimiento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.core.SesionManager
import cl.duoc.atenas.data.local.entity.Seguimiento
import cl.duoc.atenas.data.repository.OportunidadRepository
import cl.duoc.atenas.data.repository.SeguimientoRepository
import cl.duoc.atenas.di.contenedor
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.TipoAccion
import cl.duoc.atenas.domain.rules.TransicionesEstado
import cl.duoc.atenas.domain.validation.Validadores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ErroresSeguimiento(
    val resultado: String? = null,
    val fechaAccion: String? = null,
    val fechaProximoSeguimiento: String? = null
) {
    val hayErrores: Boolean
        get() = listOfNotNull(resultado, fechaAccion, fechaProximoSeguimiento).isNotEmpty()
}

data class RegistrarSeguimientoUiState(
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val guardadoConExito: Boolean = false,
    val errorGeneral: String? = null,

    val nombreOportunidad: String = "",
    val estadoActual: EstadoOportunidad = EstadoOportunidad.IDENTIFICADA,
    /** Estados a los que este usuario puede mover la oportunidad. */
    val estadosDisponibles: List<EstadoOportunidad> = emptyList(),
    /** Etapas anteriores a las que el supervisor puede devolverla. */
    val estadosAnteriores: List<EstadoOportunidad> = emptyList(),
    /** Si existe una gestión previa cuyos datos se puedan reutilizar. */
    val hayGestionAnterior: Boolean = false,
    /**
     * True cuando el estado resultante cierra la gestión. En ese caso no se
     * pide próxima acción: no hay nada más que hacer con esta oportunidad.
     */
    val esCierre: Boolean = false,
    /** Aviso para el ejecutivo sobre lo que no puede hacer. */
    val mensajeRestriccion: String? = null,

    val tipoAccion: TipoAccion = TipoAccion.SEGUIMIENTO,
    val fechaAccion: Long = Fechas.hoy(),
    val resultado: String = "",
    val proximaAccion: String = "",
    val fechaProximoSeguimiento: Long? = null,
    /** null significa "no cambiar el estado". */
    val nuevoEstado: EstadoOportunidad? = null,
    val observaciones: String = "",

    val errores: ErroresSeguimiento = ErroresSeguimiento()
)

/**
 * VIEWMODEL DE REGISTRO DE GESTIÓN
 *
 * Una gestión es cada acción comercial que se hace sobre una oportunidad:
 * una llamada, el envío de una propuesta, una reunión. Registrarla puede, de
 * paso, mover la oportunidad a otro estado del embudo.
 *
 * Los estados que se ofrecen NO son todos: se le piden a TransicionesEstado,
 * que filtra según el estado actual y el rol. Un ejecutivo nunca va a ver
 * "Participación confirmada" en la lista.
 *
 * Guardar una gestión hace dos escrituras: inserta el seguimiento y, si hubo
 * cambio de estado, actualiza la oportunidad. El seguimiento conserva el
 * estado anterior y el nuevo, así el historial queda trazable.
 */
class RegistrarSeguimientoViewModel(
    private val oportunidadId: Long,
    private val sesion: SesionManager,
    private val oportunidades: OportunidadRepository,
    private val seguimientos: SeguimientoRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(RegistrarSeguimientoUiState())
    val estado: StateFlow<RegistrarSeguimientoUiState> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            val oportunidad = oportunidades.porId(oportunidadId)
            val rol = sesion.requerirUsuario().rol

            if (oportunidad == null) {
                _estado.update {
                    it.copy(cargando = false, errorGeneral = "La oportunidad ya no existe.")
                }
                return@launch
            }

            // Se consulta si existe una gestión previa para habilitar el
            // botón de reutilizar sus datos.
            val anterior = seguimientos.ultima(oportunidadId)

            _estado.update {
                it.copy(
                    cargando = false,
                    nombreOportunidad = oportunidad.nombre,
                    estadoActual = oportunidad.estado,
                    estadosDisponibles = TransicionesEstado.siguientesPosibles(oportunidad.estado, rol),
                    estadosAnteriores = TransicionesEstado.anterioresPosibles(oportunidad.estado, rol),
                    hayGestionAnterior = anterior != null,
                    esCierre = TransicionesEstado.esEstadoDeCierre(oportunidad.estado),
                    mensajeRestriccion = TransicionesEstado.mensajeRestriccion(rol)
                )
            }
        }
    }

    fun alCambiarTipoAccion(valor: TipoAccion) = _estado.update { it.copy(tipoAccion = valor) }

    fun alCambiarFechaAccion(valor: Long) = _estado.update {
        it.copy(fechaAccion = valor, errores = it.errores.copy(fechaAccion = null))
    }

    fun alCambiarResultado(valor: String) = _estado.update {
        it.copy(resultado = valor, errores = it.errores.copy(resultado = null))
    }

    fun alCambiarProximaAccion(valor: String) = _estado.update { it.copy(proximaAccion = valor) }

    fun alCambiarFechaProximoSeguimiento(valor: Long) = _estado.update {
        it.copy(
            fechaProximoSeguimiento = valor,
            errores = it.errores.copy(fechaProximoSeguimiento = null)
        )
    }

    /**
     * Tocar el estado ya elegido lo deselecciona: vuelve a "no cambiar".
     *
     * Si el estado resultante cierra la gestión, se borra la próxima acción
     * y su fecha: comprometer un seguimiento futuro sobre algo que acaba de
     * cerrarse no tiene sentido, y dejarlo cargado haría que la oportunidad
     * apareciera como atrasada más adelante.
     */
    fun alSeleccionarNuevoEstado(valor: EstadoOportunidad) = _estado.update { actual ->
        val elegido = if (actual.nuevoEstado == valor) null else valor
        val resultante = elegido ?: actual.estadoActual
        val cierra = TransicionesEstado.esEstadoDeCierre(resultante)
        actual.copy(
            nuevoEstado = elegido,
            esCierre = cierra,
            proximaAccion = if (cierra) "" else actual.proximaAccion,
            fechaProximoSeguimiento = if (cierra) null else actual.fechaProximoSeguimiento,
            errores = actual.errores.copy(fechaProximoSeguimiento = null)
        )
    }

    /**
     * Copia los datos de la última gestión al formulario.
     *
     * No guarda nada: solo precarga para que el usuario ajuste lo que cambió.
     * La fecha de la acción queda en hoy, porque esta gestión es nueva, y la
     * próxima acción se copia solo si todavía tiene sentido comprometerla.
     */
    fun autorrellenarConAnterior() {
        viewModelScope.launch {
            val anterior = seguimientos.ultima(oportunidadId) ?: return@launch
            _estado.update { actual ->
                actual.copy(
                    tipoAccion = anterior.tipoAccion,
                    resultado = anterior.resultado,
                    proximaAccion = if (actual.esCierre) "" else anterior.proximaAccion.orEmpty(),
                    observaciones = anterior.observaciones.orEmpty(),
                    errores = ErroresSeguimiento()
                )
            }
        }
    }

    fun alCambiarObservaciones(valor: String) = _estado.update { it.copy(observaciones = valor) }

    fun guardar() {
        val actual = _estado.value

        val errores = ErroresSeguimiento(
            resultado = Validadores.texto(actual.resultado, "El resultado", minimo = 10),
            // Registrar una gestión en el futuro no tiene sentido: se registra
            // lo que ya ocurrió.
            fechaAccion = Validadores.fechaPosteriorA(
                posterior = Fechas.hoy(),
                anterior = actual.fechaAccion,
                mensaje = "La fecha de la gestión no puede ser futura"
            ),
            // Si el usuario define una próxima acción, tiene que decir
            // cuándo. Salvo que la gestión cierre la oportunidad: ahí no se
            // pide próxima acción en absoluto.
            fechaProximoSeguimiento = when {
                actual.esCierre -> null

                actual.proximaAccion.isNotBlank() && actual.fechaProximoSeguimiento == null ->
                    "Indica cuándo harás la próxima acción"

                else -> Validadores.fechaPosteriorA(
                    posterior = actual.fechaProximoSeguimiento,
                    anterior = actual.fechaAccion,
                    mensaje = "El próximo seguimiento no puede ser anterior a esta gestión"
                )
            }
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
                val usuario = sesion.requerirUsuario()
                val estadoDestino = actual.nuevoEstado ?: actual.estadoActual

                // Segunda barrera: aunque la pantalla no ofrezca la opción,
                // acá se vuelve a verificar que la transición sea legítima.
                if (!TransicionesEstado.esValida(actual.estadoActual, estadoDestino, usuario.rol)) {
                    _estado.update {
                        it.copy(
                            guardando = false,
                            errorGeneral = "Tu perfil no puede aplicar ese cambio de estado."
                        )
                    }
                    return@launch
                }

                seguimientos.registrar(
                    Seguimiento(
                        oportunidadId = oportunidadId,
                        usuarioId = usuario.id,
                        tipoAccion = actual.tipoAccion,
                        fechaAccion = actual.fechaAccion,
                        resultado = actual.resultado.trim(),
                        // Una gestión de cierre nunca deja un seguimiento
                        // pendiente colgando.
                        proximaAccion = if (actual.esCierre) null else actual.proximaAccion.ifBlank { null },
                        fechaProximoSeguimiento = if (actual.esCierre) null else actual.fechaProximoSeguimiento,
                        estadoAnterior = actual.estadoActual,
                        estadoNuevo = estadoDestino,
                        observaciones = actual.observaciones.ifBlank { null }
                    )
                )

                // Solo se toca la oportunidad si efectivamente cambió de estado.
                if (estadoDestino != actual.estadoActual) {
                    oportunidades.cambiarEstado(oportunidadId, estadoDestino)
                }

                _estado.update { it.copy(guardando = false, guardadoConExito = true) }
            } catch (excepcion: Exception) {
                _estado.update {
                    it.copy(
                        guardando = false,
                        errorGeneral = "No se pudo registrar: ${excepcion.message ?: "error desconocido"}"
                    )
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val argumentos = createSavedStateHandle()
                RegistrarSeguimientoViewModel(
                    oportunidadId = argumentos.get<Long>("oportunidadId")
                        ?: error("Falta el identificador de la oportunidad."),
                    sesion = contenedor.sesion,
                    oportunidades = contenedor.oportunidades,
                    seguimientos = contenedor.seguimientos
                )
            }
        }
    }
}
