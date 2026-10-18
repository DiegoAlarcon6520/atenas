package cl.duoc.atenas.data.repository

import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.data.local.dao.ConteoPorEstado
import cl.duoc.atenas.data.local.dao.OportunidadDao
import cl.duoc.atenas.data.local.dao.OportunidadDetalle
import cl.duoc.atenas.data.local.dao.OportunidadResumen
import cl.duoc.atenas.data.local.entity.Oportunidad
import cl.duoc.atenas.domain.model.EstadoOportunidad
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
 *  - si mañana los datos vinieran de una API, solo cambia esta clase,
 *  - acá se traducen cosas del dominio a cosas de la base (por ejemplo, los
 *    enum de los filtros se convierten a texto con .name),
 *  - acá se calculan parámetros derivados, como el rango de fechas de
 *    "próximos 30 días".
 */
class OportunidadRepository(private val dao: OportunidadDao) {

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
        texto: String,
        estado: EstadoOportunidad?,
        prioridad: Prioridad?,
        tipo: TipoOportunidad?
    ): Flow<List<OportunidadResumen>> = dao.buscar(
        responsableId = responsableId,
        texto = texto.trim(),
        estado = estado?.name,
        prioridad = prioridad?.name,
        tipo = tipo?.name
    )

    fun observarPorOrganizacion(organizacionId: Long): Flow<List<OportunidadResumen>> =
        dao.observarPorOrganizacion(organizacionId)

    fun observarDetalle(id: Long): Flow<OportunidadDetalle?> = dao.observarDetalle(id)

    fun observarPorId(id: Long): Flow<Oportunidad?> = dao.observarPorId(id)

    suspend fun porId(id: Long): Oportunidad? = dao.porId(id)

    suspend fun crear(oportunidad: Oportunidad): Long = dao.insertar(oportunidad)

    suspend fun actualizar(oportunidad: Oportunidad) = dao.actualizar(oportunidad)

    suspend fun cambiarEstado(id: Long, estado: EstadoOportunidad) =
        dao.actualizarEstado(id, estado, Fechas.ahora())

    suspend fun asignarResponsable(id: Long, responsableId: Long?) =
        dao.asignarResponsable(id, responsableId, Fechas.ahora())
}
