package cl.duoc.atenas.ui.evidencias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.atenas.core.ArchivosImagen
import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.data.local.entity.Evidencia
import cl.duoc.atenas.data.repository.EvidenciaRepository
import cl.duoc.atenas.data.repository.OportunidadRepository
import cl.duoc.atenas.di.contenedor
import kotlinx.coroutines.flow.SharingStarted
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

    /** Registra en la base la foto que la cámara acaba de escribir. */
    fun agregarEvidencia(uri: String, descripcion: String?) {
        viewModelScope.launch {
            evidencias.agregar(
                Evidencia(
                    oportunidadId = oportunidadId,
                    uri = uri,
                    descripcion = descripcion?.ifBlank { null },
                    fechaCaptura = Fechas.ahora()
                )
            )
        }
    }

    /** Elimina la fila y además el archivo físico, para no dejar basura. */
    fun eliminarEvidencia(evidencia: Evidencia) {
        viewModelScope.launch {
            evidencias.eliminar(evidencia)
            ArchivosImagen.eliminarArchivo(evidencia.uri)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val argumentos = createSavedStateHandle()
                EvidenciasViewModel(
                    oportunidadId = argumentos.get<Long>("oportunidadId")
                        ?: error("Falta el identificador de la oportunidad."),
                    evidencias = contenedor.evidencias,
                    oportunidades = contenedor.oportunidades
                )
            }
        }
    }
}
