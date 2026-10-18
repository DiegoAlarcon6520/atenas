package cl.duoc.atenas.data.repository

import cl.duoc.atenas.data.local.dao.ContactoDao
import cl.duoc.atenas.data.local.entity.Contacto
import kotlinx.coroutines.flow.Flow

class ContactoRepository(private val dao: ContactoDao) {

    fun observarTodos(): Flow<List<Contacto>> = dao.observarTodos()

    fun observarPorOrganizacion(organizacionId: Long): Flow<List<Contacto>> =
        dao.observarPorOrganizacion(organizacionId)

    fun observarPorId(id: Long): Flow<Contacto?> = dao.observarPorId(id)

    suspend fun porId(id: Long): Contacto? = dao.porId(id)

    suspend fun crear(contacto: Contacto): Long = dao.insertar(contacto)

    suspend fun actualizar(contacto: Contacto) = dao.actualizar(contacto)
}
