package cl.duoc.atenas.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.atenas.data.local.entity.Oportunidad
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.Prioridad
import cl.duoc.atenas.domain.model.TipoOportunidad
import cl.duoc.atenas.domain.model.TipoOrganizacion
import kotlinx.coroutines.flow.Flow

/**
 * Proyección usada en listados y tableros: trae la oportunidad ya resuelta
 * con el nombre de su organización, comuna y responsable, sin cargar las
 * entidades completas.
 */
data class OportunidadResumen(
    val id: Long,
    val nombre: String,
    val tipo: TipoOportunidad,
    val estado: EstadoOportunidad,
    val prioridad: Prioridad,
    val fechaInicioEvento: Long,
    val publicoEsperado: Int,
    val organizacionNombre: String,
    val comunaNombre: String,
    val responsableNombre: String?
)

/*
 * DAO DE OPORTUNIDADES
 *
 * Es el DAO más grande porque concentra las consultas del listado, los
 * filtros, el detalle y las métricas de los tableros.
 *
 * Las data class que están antes de la interfaz son proyecciones: en vez de
 * traer la entidad completa, cada consulta devuelve solo las columnas que esa
 * pantalla necesita, ya cruzadas con las tablas relacionadas. Room las arma
 * haciendo calzar el nombre de cada columna del SELECT con el nombre del
 * parámetro de la data class; por eso todos los campos llevan AS.
 */
/** Cantidad de oportunidades agrupadas por estado, para los tableros. */
data class ConteoPorEstado(
    val estado: EstadoOportunidad,
    val total: Int
)

/** Oportunidad con todas sus relaciones resueltas, para la pantalla de detalle. */
data class OportunidadDetalle(
    @Embedded val oportunidad: Oportunidad,
    val organizacionNombre: String,
    val organizacionTipo: TipoOrganizacion,
    val organizacionRubro: String,
    val comunaNombre: String,
    val regionNombre: String,
    val contactoNombre: String?,
    val contactoCargo: String?,
    val contactoCorreo: String?,
    val contactoTelefono: String?,
    val responsableNombre: String?,
    val creadorNombre: String
)

@Dao
interface OportunidadDao {

    /**
     * Listado con búsqueda y filtros opcionales.
     *
     * Los filtros de enum se reciben como texto para poder enviarlos nulos
     * cuando el usuario no los aplica; el repositorio hace la conversión.
     */
    @Query(
        """
        SELECT o.id AS id,
               o.nombre AS nombre,
               o.tipo AS tipo,
               o.estado AS estado,
               o.prioridad AS prioridad,
               o.fechaInicioEvento AS fechaInicioEvento,
               o.publicoEsperado AS publicoEsperado,
               org.nombre AS organizacionNombre,
               c.nombre AS comunaNombre,
               u.nombreCompleto AS responsableNombre
        FROM oportunidad o
        INNER JOIN organizacion org ON org.id = o.organizacionId
        INNER JOIN comuna c ON c.id = o.comunaId
        LEFT JOIN usuario u ON u.id = o.responsableId
        WHERE (:responsableId IS NULL OR o.responsableId = :responsableId)
          AND (
                :texto = ''
                OR o.nombre LIKE '%' || :texto || '%'
                OR org.nombre LIKE '%' || :texto || '%'
                OR c.nombre LIKE '%' || :texto || '%'
              )
          AND (:estado IS NULL OR o.estado = :estado)
          AND (:prioridad IS NULL OR o.prioridad = :prioridad)
          AND (:tipo IS NULL OR o.tipo = :tipo)
        ORDER BY o.fechaInicioEvento ASC
        """
    )
    fun buscar(
        responsableId: Long?,
        texto: String,
        estado: String?,
        prioridad: String?,
        tipo: String?
    ): Flow<List<OportunidadResumen>>

    @Query(
        """
        SELECT o.*,
               org.nombre AS organizacionNombre,
               org.tipo AS organizacionTipo,
               org.rubro AS organizacionRubro,
               c.nombre AS comunaNombre,
               r.nombre AS regionNombre,
               ct.nombre AS contactoNombre,
               ct.cargo AS contactoCargo,
               ct.correo AS contactoCorreo,
               ct.telefono AS contactoTelefono,
               u.nombreCompleto AS responsableNombre,
               cr.nombreCompleto AS creadorNombre
        FROM oportunidad o
        INNER JOIN organizacion org ON org.id = o.organizacionId
        INNER JOIN comuna c ON c.id = o.comunaId
        INNER JOIN region r ON r.id = c.regionId
        INNER JOIN usuario cr ON cr.id = o.creadoPorId
        LEFT JOIN contacto ct ON ct.id = o.contactoId
        LEFT JOIN usuario u ON u.id = o.responsableId
        WHERE o.id = :id
        """
    )
    fun observarDetalle(id: Long): Flow<OportunidadDetalle?>

    @Query(
        """
        SELECT o.estado AS estado, COUNT(*) AS total
        FROM oportunidad o
        WHERE (:responsableId IS NULL OR o.responsableId = :responsableId)
        GROUP BY o.estado
        """
    )
    fun observarConteoPorEstado(responsableId: Long?): Flow<List<ConteoPorEstado>>

    @Query(
        """
        SELECT o.id AS id,
               o.nombre AS nombre,
               o.tipo AS tipo,
               o.estado AS estado,
               o.prioridad AS prioridad,
               o.fechaInicioEvento AS fechaInicioEvento,
               o.publicoEsperado AS publicoEsperado,
               org.nombre AS organizacionNombre,
               c.nombre AS comunaNombre,
               u.nombreCompleto AS responsableNombre
        FROM oportunidad o
        INNER JOIN organizacion org ON org.id = o.organizacionId
        INNER JOIN comuna c ON c.id = o.comunaId
        LEFT JOIN usuario u ON u.id = o.responsableId
        WHERE o.responsableId IS NULL
          AND o.estado NOT IN ('DESCARTADA', 'CERRADA')
        ORDER BY o.fechaInicioEvento ASC
        """
    )
    fun observarSinResponsable(): Flow<List<OportunidadResumen>>

    @Query(
        """
        SELECT o.id AS id,
               o.nombre AS nombre,
               o.tipo AS tipo,
               o.estado AS estado,
               o.prioridad AS prioridad,
               o.fechaInicioEvento AS fechaInicioEvento,
               o.publicoEsperado AS publicoEsperado,
               org.nombre AS organizacionNombre,
               c.nombre AS comunaNombre,
               u.nombreCompleto AS responsableNombre
        FROM oportunidad o
        INNER JOIN organizacion org ON org.id = o.organizacionId
        INNER JOIN comuna c ON c.id = o.comunaId
        LEFT JOIN usuario u ON u.id = o.responsableId
        WHERE (:responsableId IS NULL OR o.responsableId = :responsableId)
        ORDER BY o.fechaInicioEvento ASC
        """
    )
    fun observarResumen(responsableId: Long?): Flow<List<OportunidadResumen>>

    /** Eventos que ocurren dentro de la ventana indicada. */
    @Query(
        """
        SELECT o.id AS id,
               o.nombre AS nombre,
               o.tipo AS tipo,
               o.estado AS estado,
               o.prioridad AS prioridad,
               o.fechaInicioEvento AS fechaInicioEvento,
               o.publicoEsperado AS publicoEsperado,
               org.nombre AS organizacionNombre,
               c.nombre AS comunaNombre,
               u.nombreCompleto AS responsableNombre
        FROM oportunidad o
        INNER JOIN organizacion org ON org.id = o.organizacionId
        INNER JOIN comuna c ON c.id = o.comunaId
        LEFT JOIN usuario u ON u.id = o.responsableId
        WHERE o.fechaInicioEvento BETWEEN :desde AND :hasta
          AND o.estado NOT IN ('DESCARTADA', 'CERRADA')
          AND (:responsableId IS NULL OR o.responsableId = :responsableId)
        ORDER BY o.fechaInicioEvento ASC
        """
    )
    fun observarProximas(desde: Long, hasta: Long, responsableId: Long?): Flow<List<OportunidadResumen>>

    /** Oportunidades asociadas a una organización, para su ficha. */
    @Query(
        """
        SELECT o.id AS id,
               o.nombre AS nombre,
               o.tipo AS tipo,
               o.estado AS estado,
               o.prioridad AS prioridad,
               o.fechaInicioEvento AS fechaInicioEvento,
               o.publicoEsperado AS publicoEsperado,
               org.nombre AS organizacionNombre,
               c.nombre AS comunaNombre,
               u.nombreCompleto AS responsableNombre
        FROM oportunidad o
        INNER JOIN organizacion org ON org.id = o.organizacionId
        INNER JOIN comuna c ON c.id = o.comunaId
        LEFT JOIN usuario u ON u.id = o.responsableId
        WHERE o.organizacionId = :organizacionId
        ORDER BY o.fechaInicioEvento DESC
        """
    )
    fun observarPorOrganizacion(organizacionId: Long): Flow<List<OportunidadResumen>>

    @Query("SELECT * FROM oportunidad WHERE id = :id")
    fun observarPorId(id: Long): Flow<Oportunidad?>

    @Query("SELECT * FROM oportunidad WHERE id = :id")
    suspend fun porId(id: Long): Oportunidad?

    @Query("SELECT COUNT(*) FROM oportunidad")
    suspend fun contar(): Int

    @Query("UPDATE oportunidad SET estado = :estado, fechaActualizacion = :fecha WHERE id = :id")
    suspend fun actualizarEstado(id: Long, estado: EstadoOportunidad, fecha: Long)

    @Query("UPDATE oportunidad SET responsableId = :responsableId, fechaActualizacion = :fecha WHERE id = :id")
    suspend fun asignarResponsable(id: Long, responsableId: Long?, fecha: Long)

    @Insert
    suspend fun insertar(oportunidad: Oportunidad): Long

    @Insert
    suspend fun insertarTodas(oportunidades: List<Oportunidad>): List<Long>

    @Update
    suspend fun actualizar(oportunidad: Oportunidad)
}
