package cl.duoc.atenas.data.repository

import cl.duoc.atenas.data.local.dao.CatalogoDao
import cl.duoc.atenas.data.local.entity.Comuna
import cl.duoc.atenas.data.local.entity.Region
import kotlinx.coroutines.flow.Flow

class CatalogoRepository(private val dao: CatalogoDao) {

    fun observarRegiones(): Flow<List<Region>> = dao.observarRegiones()

    fun observarComunas(): Flow<List<Comuna>> = dao.observarComunas()

    fun observarComunasPorRegion(regionId: Long): Flow<List<Comuna>> =
        dao.observarComunasPorRegion(regionId)
}
