package cl.duoc.atenas.data.repository

import cl.duoc.atenas.data.local.dao.EvidenciaDao
import cl.duoc.atenas.data.local.entity.Evidencia
import cl.duoc.atenas.data.remote.AtenasApi
import cl.duoc.atenas.data.remote.EvidenciaSolicitud
import kotlinx.coroutines.flow.Flow

class EvidenciaRepository(
    private val dao: EvidenciaDao,
    private val api: AtenasApi
) {

    fun observarPorOportunidad(oportunidadId: Long): Flow<List<Evidencia>> =
        dao.observarPorOportunidad(oportunidadId)

    /** El archivo queda en el teléfono; el servidor guarda su referencia. */
    suspend fun agregar(evidencia: Evidencia): Long {
        val creada = llamar { api.crearEvidencia(EvidenciaSolicitud.desde(evidencia)) }
        dao.insertar(creada.aEntidad())
        return creada.id
    }

    suspend fun eliminar(evidencia: Evidencia) {
        llamar { api.eliminarEvidencia(evidencia.id) }
        dao.eliminar(evidencia)
    }
}
