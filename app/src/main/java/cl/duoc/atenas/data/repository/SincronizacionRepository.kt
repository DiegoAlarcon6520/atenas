package cl.duoc.atenas.data.repository

import cl.duoc.atenas.data.local.dao.SincronizacionDao
import cl.duoc.atenas.data.local.dao.UsuarioDao
import cl.duoc.atenas.data.remote.AtenasApi
import kotlinx.coroutines.flow.first

/**
 * SINCRONIZACIÓN CON EL SERVIDOR
 *
 * El servidor es la fuente de verdad y Room es una copia para trabajar sin
 * conexión. Al iniciar sesión se descarga todo lo que el usuario puede ver
 * (el ejecutivo, su cartera; el supervisor, la de todos) y se reemplaza la
 * copia local. Las pantallas siguen leyendo de Room, así que funcionan igual
 * con o sin conexión.
 */
class SincronizacionRepository(
    private val api: AtenasApi,
    private val dao: SincronizacionDao,
    private val usuarios: UsuarioDao
) {

    /**
     * Lanza SinConexionException si no hay red: quien llama decide seguir
     * con la copia local.
     */
    suspend fun sincronizar(usuarioId: Long) {
        val datos = llamar { api.sincronizar(usuarioId) }

        // El servidor nunca envía claves. Se conserva el hash que ya había en
        // el teléfono, para que quien ya entró una vez pueda volver a entrar
        // sin conexión; los usuarios nuevos quedan sin clave local.
        val clavesLocales = usuarios.observarTodos().first().associate { it.id to it.claveHash }

        dao.reemplazarTodo(
            usuarios = datos.usuarios.map { it.aEntidad(claveHash = clavesLocales[it.id].orEmpty()) },
            regiones = datos.regiones.map { it.aEntidad() },
            comunas = datos.comunas.map { it.aEntidad() },
            organizaciones = datos.organizaciones.map { it.aEntidad() },
            contactos = datos.contactos.map { it.aEntidad() },
            oportunidades = datos.oportunidades.map { it.aEntidad() },
            seguimientos = datos.seguimientos.map { it.aEntidad() },
            evidencias = datos.evidencias.map { it.aEntidad() }
        )
    }
}
