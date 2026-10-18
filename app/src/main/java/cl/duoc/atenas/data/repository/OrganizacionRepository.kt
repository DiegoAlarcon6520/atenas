package cl.duoc.atenas.data.repository

import cl.duoc.atenas.data.local.dao.OrganizacionDao
import cl.duoc.atenas.data.local.entity.Organizacion
import kotlinx.coroutines.flow.Flow

class OrganizacionRepository(private val dao: OrganizacionDao) {

    fun observarTodas(): Flow<List<Organizacion>> = dao.observarTodas()

    fun buscar(texto: String): Flow<List<Organizacion>> = dao.buscar(texto)

    fun observarPorId(id: Long): Flow<Organizacion?> = dao.observarPorId(id)

    suspend fun crear(organizacion: Organizacion): Long = dao.insertar(organizacion)

    suspend fun actualizar(organizacion: Organizacion) = dao.actualizar(organizacion)
}
