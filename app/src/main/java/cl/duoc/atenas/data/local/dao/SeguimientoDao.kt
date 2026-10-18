package cl.duoc.atenas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.duoc.atenas.data.local.entity.Seguimiento
import kotlinx.coroutines.flow.Flow

@Dao
interface SeguimientoDao {

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

    @Insert
    suspend fun insertar(seguimiento: Seguimiento): Long

    @Insert
    suspend fun insertarTodos(seguimientos: List<Seguimiento>): List<Long>
}
