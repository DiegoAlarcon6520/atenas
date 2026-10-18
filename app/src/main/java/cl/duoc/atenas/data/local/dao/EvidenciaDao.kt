package cl.duoc.atenas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import cl.duoc.atenas.data.local.entity.Evidencia
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenciaDao {

    @Query("SELECT * FROM evidencia WHERE oportunidadId = :oportunidadId ORDER BY fechaCaptura DESC")
    fun observarPorOportunidad(oportunidadId: Long): Flow<List<Evidencia>>

    @Insert
    suspend fun insertar(evidencia: Evidencia): Long

    @Delete
    suspend fun eliminar(evidencia: Evidencia)
}
