package cl.duoc.atenas.data.repository

import cl.duoc.atenas.data.local.dao.ContactoDao
import cl.duoc.atenas.data.local.entity.Contacto
import cl.duoc.atenas.data.remote.AtenasApi
import cl.duoc.atenas.data.remote.ContactoSolicitud
import kotlinx.coroutines.flow.Flow

class ContactoRepository(
    private val dao: ContactoDao,
    private val api: AtenasApi
) {

    fun observarTodos(): Flow<List<Contacto>> = dao.observarTodos()

    fun observarPorOrganizacion(organizacionId: Long): Flow<List<Contacto>> =
        dao.observarPorOrganizacion(organizacionId)

    fun observarPorId(id: Long): Flow<Contacto?> = dao.observarPorId(id)

    suspend fun porId(id: Long): Contacto? = dao.porId(id)

    suspend fun crear(contacto: Contacto): Long {
        val creado = llamar { api.crearContacto(ContactoSolicitud.desde(contacto)) }
        dao.insertar(creado.aEntidad())
        return creado.id
    }

    /** El servidor lo rechaza (409) si está asignado a oportunidades: se desactiva en su lugar. */
    suspend fun eliminar(id: Long) {
        llamar { api.eliminarContacto(id) }
        dao.eliminar(id)
    }

    /** La fecha de último contacto la mantiene el servidor; se guarda la que devuelve. */
    suspend fun actualizar(contacto: Contacto) {
        val actualizado = llamar { api.actualizarContacto(contacto.id, ContactoSolicitud.desde(contacto)) }
        dao.actualizar(actualizado.aEntidad())
    }
}
