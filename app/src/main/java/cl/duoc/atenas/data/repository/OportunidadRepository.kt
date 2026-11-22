package cl.duoc.atenas.data.repository

import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.data.local.dao.ConteoPorEstado
import cl.duoc.atenas.data.local.dao.OportunidadDao
import cl.duoc.atenas.data.local.dao.OportunidadDetalle
import cl.duoc.atenas.data.local.dao.OportunidadResumen
import cl.duoc.atenas.data.local.entity.Oportunidad
import cl.duoc.atenas.data.remote.AsignacionSolicitud
import cl.duoc.atenas.data.remote.AtenasApi
import cl.duoc.atenas.data.remote.OportunidadSolicitud
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.PotencialComercial
import cl.duoc.atenas.domain.model.Prioridad
import cl.duoc.atenas.domain.model.TipoOportunidad
import kotlinx.coroutines.flow.Flow

/**
 * REPOSITORIO DE OPORTUNIDADES
 *
 * Capa intermedia entre los ViewModel y la base de datos. Los ViewModel nunca
 * hablan con un DAO directamente.
 *
 * Para qué sirve esta capa:
 *  - decide de dónde viene cada dato: las lecturas salen de Room (la copia
 *    local, que funciona sin conexión) y las escrituras van primero al
 *    servidor, que es la fuente de verdad; lo que el servidor confirma se
 *    guarda después en Room,
 *  - acá se traducen cosas del dominio a cosas de la base (por ejemplo, los
 *    enum de los filtros se convierten a texto con .name),
 *  - acá se calculan parámetros derivados, como el rango de fechas de
 *    "próximos 30 días".
 */
/**
 * Criterios del listado de oportunidades (HU-18). Cada filtro nulo o vacío
 * significa "no filtrar por esto". Las fechas son medianoches locales y
 * acotan la fecha de inicio del evento.
 */
data class CriteriosBusqueda(
    val texto: String = "",
    val estado: EstadoOportunidad? = null,
    val prioridad: Prioridad? = null,
    val tipo: TipoOportunidad? = null,
    val potencial: PotencialComercial? = null,
    val desde: Long? = null,
    val hasta: Long? = null,
    val regionId: Long? = null,
    val comunaId: Long? = null,
    val organizacionId: Long? = null,
    val responsableId: Long? = null
)

class OportunidadRepository(
    private val dao: OportunidadDao,
    private val api: AtenasApi
) {

    /** [responsableId] nulo trae todas: es la vista del supervisor. */
    fun observarResumen(responsableId: Long?): Flow<List<OportunidadResumen>> =
        dao.observarResumen(responsableId)

    fun observarProximas(dias: Int, responsableId: Long?): Flow<List<OportunidadResumen>> {
        val desde = Fechas.hoy()
        return dao.observarProximas(desde, Fechas.masDias(desde, dias), responsableId)
    }

    fun observarConteoPorEstado(responsableId: Long?): Flow<List<ConteoPorEstado>> =
        dao.observarConteoPorEstado(responsableId)

    fun observarSinResponsable(): Flow<List<OportunidadResumen>> = dao.observarSinResponsable()

    fun buscar(
        responsableId: Long?,
        filtros: CriteriosBusqueda
    ): Flow<List<OportunidadResumen>> = dao.buscar(
        responsableId = responsableId,
        texto = filtros.texto.trim(),
        estado = filtros.estado?.name,
        prioridad = filtros.prioridad?.name,
        tipo = filtros.tipo?.name,
        potencial = filtros.potencial?.name,
        desde = filtros.desde,
        hasta = filtros.hasta,
        regionId = filtros.regionId,
        comunaId = filtros.comunaId,
        organizacionId = filtros.organizacionId,
        filtroResponsableId = filtros.responsableId
    )

    /** Alerta: oportunidades activas a las que todavía no se ha contactado. */
    fun observarSinContacto(responsableId: Long?): Flow<List<OportunidadResumen>> =
        dao.observarEnEstados(
            listOf(EstadoOportunidad.IDENTIFICADA.name, EstadoOportunidad.CONTACTO_PENDIENTE.name),
            responsableId
        )

    /** Alerta: propuestas enviadas que siguen esperando la respuesta de la organización. */
    fun observarPropuestasSinRespuesta(responsableId: Long?): Flow<List<OportunidadResumen>> =
        dao.observarEnEstados(
            listOf(EstadoOportunidad.PROPUESTA_ENVIADA.name, EstadoOportunidad.EN_ESPERA_RESPUESTA.name),
            responsableId
        )

    fun observarPorOrganizacion(organizacionId: Long, responsableId: Long?): Flow<List<OportunidadResumen>> =
        dao.observarPorOrganizacion(organizacionId, responsableId)

    fun observarDetalle(id: Long): Flow<OportunidadDetalle?> = dao.observarDetalle(id)

    fun observarPorId(id: Long): Flow<Oportunidad?> = dao.observarPorId(id)

    suspend fun porId(id: Long): Oportunidad? = dao.porId(id)

    /** Crea en el servidor y guarda en Room la versión confirmada, con el id que asignó. */
    suspend fun crear(oportunidad: Oportunidad): Long {
        val creada = llamar { api.crearOportunidad(OportunidadSolicitud.desde(oportunidad)) }
        dao.insertar(creada.aEntidad())
        return creada.id
    }

    suspend fun actualizar(oportunidad: Oportunidad) {
        val actualizada = llamar { api.actualizarOportunidad(oportunidad.id, OportunidadSolicitud.desde(oportunidad)) }
        dao.actualizar(actualizada.aEntidad())
    }

    /**
     * Elimina en el servidor y después en Room. El servidor la rechaza (409)
     * si ya tiene gestiones: su historial no se puede borrar.
     */
    suspend fun eliminar(id: Long) {
        llamar { api.eliminarOportunidad(id) }
        dao.eliminar(id)
    }

    /**
     * [asignadoPor] es quien hace la asignación: el servidor vuelve a
     * verificar que sea supervisor antes de aplicarla.
     */
    suspend fun asignarResponsable(id: Long, responsableId: Long?, asignadoPor: Long) {
        val actualizada = llamar { api.asignarResponsable(id, AsignacionSolicitud(responsableId, asignadoPor)) }
        dao.actualizar(actualizada.aEntidad())
    }
}
