package cl.duoc.atenas.data.repository

import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.data.local.dao.SeguimientoDao
import cl.duoc.atenas.data.local.entity.Seguimiento
import kotlinx.coroutines.flow.Flow

class SeguimientoRepository(private val dao: SeguimientoDao) {

    fun observarHistorial(oportunidadId: Long): Flow<List<Seguimiento>> =
        dao.observarHistorial(oportunidadId)

    fun observarAtrasados(responsableId: Long?): Flow<List<Seguimiento>> =
        dao.observarAtrasados(Fechas.ahora(), responsableId)

    suspend fun registrar(seguimiento: Seguimiento): Long = dao.insertar(seguimiento)
}
