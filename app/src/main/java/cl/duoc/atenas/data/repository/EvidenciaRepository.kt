package cl.duoc.atenas.data.repository

import cl.duoc.atenas.data.local.dao.EvidenciaDao
import cl.duoc.atenas.data.local.entity.Evidencia
import kotlinx.coroutines.flow.Flow

class EvidenciaRepository(private val dao: EvidenciaDao) {

    fun observarPorOportunidad(oportunidadId: Long): Flow<List<Evidencia>> =
        dao.observarPorOportunidad(oportunidadId)

    suspend fun agregar(evidencia: Evidencia): Long = dao.insertar(evidencia)

    suspend fun eliminar(evidencia: Evidencia) = dao.eliminar(evidencia)
}
