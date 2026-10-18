package cl.duoc.atenas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.atenas.data.local.entity.Organizacion
import kotlinx.coroutines.flow.Flow

@Dao
interface OrganizacionDao {

    @Query("SELECT * FROM organizacion ORDER BY nombre")
    fun observarTodas(): Flow<List<Organizacion>>

    @Query("SELECT * FROM organizacion WHERE nombre LIKE '%' || :texto || '%' ORDER BY nombre")
    fun buscar(texto: String): Flow<List<Organizacion>>

    @Query("SELECT * FROM organizacion WHERE id = :id")
    fun observarPorId(id: Long): Flow<Organizacion?>

    @Insert
    suspend fun insertar(organizacion: Organizacion): Long

    @Insert
    suspend fun insertarTodas(organizaciones: List<Organizacion>): List<Long>

    @Update
    suspend fun actualizar(organizacion: Organizacion)
}
