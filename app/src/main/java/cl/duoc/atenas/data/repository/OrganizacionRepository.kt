package cl.duoc.atenas.data.repository

import cl.duoc.atenas.data.local.dao.OrganizacionDao
import cl.duoc.atenas.data.local.dao.OrganizacionDetalle
import cl.duoc.atenas.data.local.dao.OrganizacionResumen
import cl.duoc.atenas.domain.model.TipoOrganizacion
import cl.duoc.atenas.data.local.entity.Organizacion
import cl.duoc.atenas.data.remote.AtenasApi
import cl.duoc.atenas.data.remote.OrganizacionSolicitud
import kotlinx.coroutines.flow.Flow

class OrganizacionRepository(
    private val dao: OrganizacionDao,
    private val api: AtenasApi
) {

    fun observarTodas(): Flow<List<Organizacion>> = dao.observarTodas()

    /** Listado con búsqueda libre y filtro opcional por tipo de organización. */
    fun observarResumen(texto: String, tipo: TipoOrganizacion?): Flow<List<OrganizacionResumen>> =
        dao.observarResumen(texto.trim(), tipo?.name)

    fun observarDetalle(id: Long): Flow<OrganizacionDetalle?> = dao.observarDetalle(id)

    suspend fun porId(id: Long): Organizacion? = dao.porId(id)

    suspend fun crear(organizacion: Organizacion): Long {
        val creada = llamar { api.crearOrganizacion(OrganizacionSolicitud.desde(organizacion)) }
        dao.insertar(creada.aEntidad())
        return creada.id
    }

    suspend fun actualizar(organizacion: Organizacion) {
        val actualizada = llamar { api.actualizarOrganizacion(organizacion.id, OrganizacionSolicitud.desde(organizacion)) }
        dao.actualizar(actualizada.aEntidad())
    }
}
