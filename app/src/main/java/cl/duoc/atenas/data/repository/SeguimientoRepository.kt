package cl.duoc.atenas.data.repository

import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.data.local.dao.GestionDeOrganizacion
import cl.duoc.atenas.data.local.dao.GestionPendiente
import cl.duoc.atenas.data.local.dao.PropuestaEnviada
import cl.duoc.atenas.data.local.dao.SeguimientoDao
import cl.duoc.atenas.data.local.entity.Seguimiento
import cl.duoc.atenas.data.remote.AtenasApi
import cl.duoc.atenas.data.remote.SeguimientoSolicitud
import kotlinx.coroutines.flow.Flow

class SeguimientoRepository(
    private val dao: SeguimientoDao,
    private val api: AtenasApi
) {

    fun observarHistorial(oportunidadId: Long): Flow<List<Seguimiento>> =
        dao.observarHistorial(oportunidadId)

    /** Una acción comprometida para hoy aún está a tiempo: se compara contra la medianoche de hoy. */
    fun observarGestionesAtrasadas(responsableId: Long?): Flow<List<GestionPendiente>> =
        dao.observarGestionesAtrasadas(Fechas.hoy(), responsableId)

    /** Gestiones comprometidas para hoy (todavía a tiempo). */
    fun observarGestionesDeHoy(responsableId: Long?): Flow<List<GestionPendiente>> {
        val hoy = Fechas.hoy()
        return dao.observarGestionesEntre(hoy, Fechas.masDias(hoy, 1), responsableId)
    }

    fun observarPorOrganizacion(organizacionId: Long, responsableId: Long?): Flow<List<GestionDeOrganizacion>> =
        dao.observarPorOrganizacion(organizacionId, responsableId)

    fun observarPropuestas(responsableId: Long?): Flow<List<PropuestaEnviada>> =
        dao.observarPropuestas(responsableId)

    suspend fun ultima(oportunidadId: Long): Seguimiento? = dao.ultima(oportunidadId)

    /**
     * Registra la gestión en el servidor, que vuelve a validar la transición
     * de estado y el perfil. Si la acepta, se replica en Room en una sola
     * transacción: la gestión, el estado de la oportunidad y la fecha de
     * último contacto, para que la copia local quede igual que el servidor.
     */
    suspend fun registrar(seguimiento: Seguimiento, contactoId: Long?): Long {
        val registrada = llamar {
            api.registrarGestion(seguimiento.oportunidadId, SeguimientoSolicitud.desde(seguimiento))
        }
        dao.registrarGestion(
            seguimiento = registrada.aEntidad(),
            contactoId = contactoId,
            actualizaContacto = registrada.tipoAccion.implicaContacto,
            ahora = Fechas.ahora()
        )
        return registrada.id
    }
}
