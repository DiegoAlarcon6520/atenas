package cl.duoc.atenas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.atenas.data.local.entity.Usuario
import cl.duoc.atenas.domain.model.RolUsuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuario WHERE nombreUsuario = :nombreUsuario AND claveHash = :claveHash AND activo = 1 LIMIT 1")
    suspend fun autenticar(nombreUsuario: String, claveHash: String): Usuario?

    @Query("SELECT * FROM usuario WHERE id = :id")
    suspend fun porId(id: Long): Usuario?

    @Query("SELECT * FROM usuario ORDER BY nombreCompleto")
    fun observarTodos(): Flow<List<Usuario>>

    @Query("SELECT * FROM usuario WHERE rol = :rol AND activo = 1 ORDER BY nombreCompleto")
    fun observarPorRol(rol: RolUsuario): Flow<List<Usuario>>

    @Query("SELECT COUNT(*) FROM usuario")
    suspend fun contar(): Int

    @Insert
    suspend fun insertar(usuarios: List<Usuario>): List<Long>

    @Update
    suspend fun actualizar(usuario: Usuario)
}
