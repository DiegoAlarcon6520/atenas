package cl.duoc.atenas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.duoc.atenas.data.local.entity.Comuna
import cl.duoc.atenas.data.local.entity.Region
import kotlinx.coroutines.flow.Flow

/** Catálogos de ubicación. Separar región de comuna evita la dependencia transitiva (3FN). */
@Dao
interface CatalogoDao {

    @Query("SELECT * FROM region ORDER BY nombre")
    fun observarRegiones(): Flow<List<Region>>

    @Query(
        """
        SELECT c.* FROM comuna c
        INNER JOIN region r ON r.id = c.regionId
        ORDER BY r.nombre, c.nombre
        """
    )
    fun observarComunas(): Flow<List<Comuna>>

    @Query("SELECT * FROM comuna WHERE regionId = :regionId ORDER BY nombre")
    fun observarComunasPorRegion(regionId: Long): Flow<List<Comuna>>

    @Query("SELECT COUNT(*) FROM region")
    suspend fun contarRegiones(): Int

    @Insert
    suspend fun insertarRegiones(regiones: List<Region>): List<Long>

    @Insert
    suspend fun insertarComunas(comunas: List<Comuna>): List<Long>
}
