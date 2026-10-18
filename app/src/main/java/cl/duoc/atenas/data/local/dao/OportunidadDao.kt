package cl.duoc.atenas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.atenas.data.local.entity.Oportunidad
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.Prioridad
import cl.duoc.atenas.domain.model.TipoOportunidad
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

@Dao
interface OportunidadDao {

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
