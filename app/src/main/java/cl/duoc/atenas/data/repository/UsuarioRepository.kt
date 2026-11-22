package cl.duoc.atenas.data.repository

import cl.duoc.atenas.core.Seguridad
import cl.duoc.atenas.data.local.dao.UsuarioDao
import cl.duoc.atenas.data.local.entity.Usuario
import cl.duoc.atenas.data.remote.AtenasApi
import cl.duoc.atenas.data.remote.LoginSolicitud
import cl.duoc.atenas.domain.model.RolUsuario
import kotlinx.coroutines.flow.Flow

class UsuarioRepository(
    private val dao: UsuarioDao,
    private val api: AtenasApi
) {

    /**
     * Inicio de sesión contra el servidor, con respaldo local.
     *
     *  - Si el servidor acepta, se guarda una copia del usuario con el hash
     *    de la clave, para poder entrar después sin conexión.
     *  - Si el servidor responde 401, la clave es incorrecta: no se intenta
     *    nada más, aunque la copia local tenga una clave antigua.
     *  - Si no hay conexión (o el servidor falla), se valida contra la copia
     *    local: la app sigue sirviendo en terreno con los últimos datos.
     */
    suspend fun autenticar(nombreUsuario: String, clave: String): Usuario? {
        val normalizado = nombreUsuario.trim().lowercase()
        val hash = Seguridad.hash(clave)
        return try {
            val usuario = llamar { api.login(LoginSolicitud(normalizado, clave)) }.aEntidad(claveHash = hash)
            dao.guardar(usuario)
            usuario
        } catch (e: ErrorDelServidor) {
            if (e.codigo == 401) null else dao.autenticar(normalizado, hash)
        } catch (e: SinConexionException) {
            dao.autenticar(normalizado, hash)
        }
    }

    suspend fun porId(id: Long): Usuario? = dao.porId(id)

    fun observarTodos(): Flow<List<Usuario>> = dao.observarTodos()

    fun observarEjecutivos(): Flow<List<Usuario>> = dao.observarPorRol(RolUsuario.EJECUTIVO)
}
