package cl.duoc.atenas.core

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Cómo decide la aplicación si se ve en claro o en oscuro. */
enum class ModoTema(val etiqueta: String) {
    SISTEMA("Según el sistema"),
    CLARO("Claro"),
    OSCURO("Oscuro")
}

/**
 * PREFERENCIAS DE LA APLICACIÓN
 *
 * Guarda las opciones que el usuario elige y que deben sobrevivir al cierre
 * de la aplicación. Hoy solo el modo de tema.
 *
 * Usa SharedPreferences y no Room: son un par de valores sueltos, sin
 * relaciones ni consultas, y no tiene sentido meterlos en la base de datos
 * del negocio. Tampoco usa DataStore para no sumar una dependencia nueva por
 * una sola preferencia.
 *
 * A diferencia de la sesión, que se pierde al cerrar la aplicación a
 * propósito, el tema sí persiste: es una preferencia del usuario, no un
 * estado de trabajo.
 */
class PreferenciasApp(context: Context) {

    private val preferencias =
        context.getSharedPreferences("atenas_preferencias", Context.MODE_PRIVATE)

    private val _modoTema = MutableStateFlow(leerModoTema())
    val modoTema: StateFlow<ModoTema> = _modoTema.asStateFlow()

    private fun leerModoTema(): ModoTema {
        val guardado = preferencias.getString(CLAVE_TEMA, null) ?: return ModoTema.SISTEMA
        // runCatching protege el arranque: si el valor guardado quedara
        // corrupto, la aplicación vuelve al modo por defecto en vez de caerse.
        return runCatching { ModoTema.valueOf(guardado) }.getOrDefault(ModoTema.SISTEMA)
    }

    fun cambiarModoTema(modo: ModoTema) {
        preferencias.edit().putString(CLAVE_TEMA, modo.name).apply()
        _modoTema.value = modo
    }

    private companion object {
        const val CLAVE_TEMA = "modo_tema"
    }
}
