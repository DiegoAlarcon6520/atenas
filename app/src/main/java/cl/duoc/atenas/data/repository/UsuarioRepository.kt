package cl.duoc.atenas.data.repository

import cl.duoc.atenas.core.Seguridad
import cl.duoc.atenas.data.local.dao.UsuarioDao
import cl.duoc.atenas.data.local.entity.Usuario
import cl.duoc.atenas.domain.model.RolUsuario
import kotlinx.coroutines.flow.Flow

class UsuarioRepository(private val dao: UsuarioDao) {

    suspend fun autenticar(nombreUsuario: String, clave: String): Usuario? =
        dao.autenticar(nombreUsuario.trim().lowercase(), Seguridad.hash(clave))

    suspend fun porId(id: Long): Usuario? = dao.porId(id)

    fun observarTodos(): Flow<List<Usuario>> = dao.observarTodos()

    fun observarEjecutivos(): Flow<List<Usuario>> = dao.observarPorRol(RolUsuario.EJECUTIVO)
}
