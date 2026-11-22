package cl.duoc.atenas.ui.evidencias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.atenas.core.ArchivosImagen
import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.data.local.entity.Evidencia
import cl.duoc.atenas.data.repository.EvidenciaRepository
import cl.duoc.atenas.data.repository.OportunidadRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EvidenciasUiState(
    val cargando: Boolean = true,
    val nombreOportunidad: String = "",
    val evidencias: List<Evidencia> = emptyList()
)

/**
 * VIEWMODEL DE EVIDENCIAS FOTOGRÁFICAS
 *
 * La foto en sí vive como archivo en el almacenamiento privado de la
 * aplicación; en la base de datos solo se guarda su URI junto al id de la
 * oportunidad. Guardar imágenes dentro de SQLite haría crecer la base sin
 * control y volvería lentas todas las consultas.
 *
 * Al eliminar una evidencia se borran las dos cosas: la fila y el archivo.
 */
class EvidenciasViewModel(
    private val oportunidadId: Long,
    private val evidencias: EvidenciaRepository,
    oportunidades: OportunidadRepository
) : ViewModel() {

    val estado: StateFlow<EvidenciasUiState> = combine(
        evidencias.observarPorOportunidad(oportunidadId),
        oportunidades.observarDetalle(oportunidadId)
    ) { lista, detalle ->
        EvidenciasUiState(
            cargando = false,
            nombreOportunidad = detalle?.oportunidad?.nombre.orEmpty(),
            evidencias = lista
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EvidenciasUiState()
    )

    /** Resultado de cada operación, para el snackbar. */
    private val _mensajes = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val mensajes: SharedFlow<String> = _mensajes.asSharedFlow()

    /**
     * Registra la foto que la cámara acaba de escribir. Si el servidor no la
     * acepta (por ejemplo, sin conexión), se borra el archivo para no dejar
     * una foto huérfana que no aparece en ninguna lista.
     */
    fun agregarEvidencia(uri: String, descripcion: String?, mensajeExito: String = "Evidencia guardada") {
        viewModelScope.launch {
            try {
                evidencias.agregar(
                    Evidencia(
                        oportunidadId = oportunidadId,
                        uri = uri,
                        descripcion = descripcion?.ifBlank { null },
                        fechaCaptura = Fechas.ahora()
                    )
                )
                _mensajes.tryEmit(mensajeExito)
            } catch (excepcion: Exception) {
                ArchivosImagen.eliminarArchivo(uri)
                _mensajes.tryEmit("No se pudo guardar la evidencia: ${excepcion.message}")
            }
        }
    }

    /** Elimina el registro y después el archivo físico, para no dejar basura. */
    fun eliminarEvidencia(evidencia: Evidencia) {
        viewModelScope.launch {
            try {
                evidencias.eliminar(evidencia)
                ArchivosImagen.eliminarArchivo(evidencia.uri)
                _mensajes.tryEmit("Evidencia eliminada")
            } catch (excepcion: Exception) {
                _mensajes.tryEmit("No se pudo eliminar la evidencia: ${excepcion.message}")
            }
        }
    }
}
