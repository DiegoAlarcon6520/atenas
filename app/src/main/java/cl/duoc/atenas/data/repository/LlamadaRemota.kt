package cl.duoc.atenas.data.repository

import cl.duoc.atenas.data.remote.RespuestaErrorDto
import com.google.gson.Gson
import java.io.IOException
import retrofit2.HttpException

/**
 * ERRORES DE LA COMUNICACIÓN CON EL SERVIDOR
 *
 * Retrofit avisa los problemas con dos excepciones técnicas:
 *  - HttpException: el servidor respondió, pero con un error (400, 403, 409...).
 *  - IOException: no hubo respuesta (sin red, servidor apagado, tiempo agotado).
 *
 * llamar() las traduce a dos excepciones propias con un mensaje que se puede
 * mostrar al usuario. Así los ViewModel no dependen de Retrofit y muestran
 * el motivo real ("La oportunidad está cerrada...") en vez de "HTTP 409".
 */

/** El servidor rechazó la operación. [codigo] es el código HTTP. */
class ErrorDelServidor(val codigo: Int, mensaje: String) : Exception(mensaje)

/** No se pudo llegar al servidor. */
class SinConexionException :
    Exception("Sin conexión con el servidor. Revisa tu red e inténtalo de nuevo.")

internal suspend fun <T> llamar(peticion: suspend () -> T): T =
    try {
        peticion()
    } catch (e: HttpException) {
        throw ErrorDelServidor(e.code(), mensajeDelServidor(e))
    } catch (e: IOException) {
        throw SinConexionException()
    }

/**
 * El backend responde los errores como JSON con un campo "mensaje". Si el
 * cuerpo no trae ese formato (por ejemplo, un error del propio servidor web),
 * se usa un texto genérico con el código.
 */
internal fun mensajeDelServidor(e: HttpException): String {
    val cuerpo = e.response()?.errorBody()?.string()
    val error = cuerpo?.let { texto ->
        runCatching { Gson().fromJson(texto, RespuestaErrorDto::class.java) }.getOrNull()
    }
    return error?.mensaje ?: "El servidor respondió con un error (${e.code()})"
}
