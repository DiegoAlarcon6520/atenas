package cl.duoc.atenas.data.repository

import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.data.local.dao.GestionDeOrganizacion
import cl.duoc.atenas.data.local.dao.GestionPendiente
import cl.duoc.atenas.data.local.dao.PropuestaEnviada
import cl.duoc.atenas.data.local.dao.SeguimientoDao
import cl.duoc.atenas.data.local.entity.Seguimiento
import kotlinx.coroutines.flow.Flow

class SeguimientoRepository(private val dao: SeguimientoDao) {

    fun observarHistorial(oportunidadId: Long): Flow<List<Seguimiento>> =
        dao.observarHistorial(oportunidadId)

    /** Una acción comprometida para hoy aún está a tiempo: se compara contra la medianoche de hoy. */
    fun observarGestionesAtrasadas(responsableId: Long?): Flow<List<GestionPendiente>> =
        dao.observarGestionesAtrasadas(Fechas.hoy(), responsableId)

    fun observarPorOrganizacion(organizacionId: Long, responsableId: Long?): Flow<List<GestionDeOrganizacion>> =
        dao.observarPorOrganizacion(organizacionId, responsableId)

    fun observarPropuestas(responsableId: Long?): Flow<List<PropuestaEnviada>> =
        dao.observarPropuestas(responsableId)

    suspend fun ultima(oportunidadId: Long): Seguimiento? = dao.ultima(oportunidadId)

    /**
     * Guarda la gestión, el estado de la oportunidad y la fecha de último
     * contacto en una sola transacción.
     */
    suspend fun registrar(seguimiento: Seguimiento, contactoId: Long?): Long =
        dao.registrarGestion(
            seguimiento = seguimiento,
            contactoId = contactoId,
            actualizaContacto = seguimiento.tipoAccion.implicaContacto,
            ahora = Fechas.ahora()
        )
}
