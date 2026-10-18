package cl.duoc.atenas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.duoc.atenas.data.local.entity.Seguimiento
import kotlinx.coroutines.flow.Flow

/** Gestión pendiente con el nombre de su oportunidad, para alertas y tableros. */
data class GestionPendiente(
    val id: Long,
    val oportunidadId: Long,
    val oportunidadNombre: String,
    val proximaAccion: String?,
    val fechaProximoSeguimiento: Long,
    val responsableNombre: String?
)

@Dao
interface SeguimientoDao {

    /**
     * Última gestión pendiente de cada oportunidad cuya fecha de próxima
     * acción ya venció.
     */
    @Query(
        """
        SELECT s.id AS id,
               s.oportunidadId AS oportunidadId,
               o.nombre AS oportunidadNombre,
               s.proximaAccion AS proximaAccion,
               s.fechaProximoSeguimiento AS fechaProximoSeguimiento,
               u.nombreCompleto AS responsableNombre
        FROM seguimiento s
        INNER JOIN oportunidad o ON o.id = s.oportunidadId
        LEFT JOIN usuario u ON u.id = o.responsableId
        WHERE s.fechaProximoSeguimiento IS NOT NULL
          AND s.fechaProximoSeguimiento < :ahora
          AND o.estado NOT IN ('DESCARTADA', 'CERRADA')
          AND (:responsableId IS NULL OR o.responsableId = :responsableId)
        ORDER BY s.fechaProximoSeguimiento ASC
        """
    )
    fun observarGestionesAtrasadas(ahora: Long, responsableId: Long?): Flow<List<GestionPendiente>>

    @Query("SELECT * FROM seguimiento WHERE oportunidadId = :oportunidadId ORDER BY fechaAccion DESC")
    fun observarHistorial(oportunidadId: Long): Flow<List<Seguimiento>>

    /** Seguimientos cuya próxima acción ya venció. */
    @Query(
        """
        SELECT s.* FROM seguimiento s
        INNER JOIN oportunidad o ON o.id = s.oportunidadId
        WHERE s.fechaProximoSeguimiento IS NOT NULL
          AND s.fechaProximoSeguimiento < :ahora
          AND (:responsableId IS NULL OR o.responsableId = :responsableId)
        ORDER BY s.fechaProximoSeguimiento
        """
    )
    fun observarAtrasados(ahora: Long, responsableId: Long?): Flow<List<Seguimiento>>

    /** Última gestión registrada, para poder reutilizar sus datos. */
    @Query(
        """
        SELECT * FROM seguimiento
        WHERE oportunidadId = :oportunidadId
        ORDER BY fechaAccion DESC, id DESC
        LIMIT 1
        """
    )
    suspend fun ultima(oportunidadId: Long): Seguimiento?

    @Insert
    suspend fun insertar(seguimiento: Seguimiento): Long

    @Insert
    suspend fun insertarTodos(seguimientos: List<Seguimiento>): List<Long>
}
