package cl.duoc.atenas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.atenas.data.local.entity.Contacto
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactoDao {

    @Query("SELECT * FROM contacto WHERE organizacionId = :organizacionId ORDER BY nombre")
    fun observarPorOrganizacion(organizacionId: Long): Flow<List<Contacto>>

    @Query("SELECT * FROM contacto ORDER BY nombre")
    fun observarTodos(): Flow<List<Contacto>>

    @Query("SELECT * FROM contacto WHERE id = :id")
    fun observarPorId(id: Long): Flow<Contacto?>

    @Query("SELECT * FROM contacto WHERE id = :id")
    suspend fun porId(id: Long): Contacto?

    @Insert
    suspend fun insertar(contacto: Contacto): Long

    @Insert
    suspend fun insertarTodos(contactos: List<Contacto>): List<Long>

    @Update
    suspend fun actualizar(contacto: Contacto)
}
