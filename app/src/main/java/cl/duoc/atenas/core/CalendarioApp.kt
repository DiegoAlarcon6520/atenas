package cl.duoc.atenas.core

import android.content.Intent
import android.provider.CalendarContract

/**
 * INTEGRACIÓN CON EL CALENDARIO DEL DISPOSITIVO
 *
 * Se usa un Intent de inserción en vez de escribir directamente en el
 * proveedor de calendario. La diferencia importa:
 *
 *  - Escribir directo exigiría los permisos READ_CALENDAR y WRITE_CALENDAR,
 *    elegir en qué calendario insertar, y dejaría eventos en el teléfono del
 *    usuario sin que él los vea primero.
 *  - El Intent no necesita ningún permiso: abre la aplicación de calendario
 *    con el evento precargado y es el usuario quien decide guardarlo.
 *
 * Para un MVP que maneja datos ficticios, la segunda opción es la correcta.
 */
object CalendarioApp {

    /**
     * Arma el Intent con el evento precargado.
     *
     * @param inicio instante de inicio en milisegundos.
     * @param termino instante de término en milisegundos.
     */
    fun intentNuevoEvento(
        titulo: String,
        descripcion: String,
        lugar: String,
        inicio: Long,
        termino: Long
    ): Intent = Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, titulo)
        putExtra(CalendarContract.Events.DESCRIPTION, descripcion)
        putExtra(CalendarContract.Events.EVENT_LOCATION, lugar)
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, inicio)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, termino)
    }

    /** Recordatorio de una hora para una gestión pendiente. */
    fun intentRecordatorioGestion(
        tituloOportunidad: String,
        proximaAccion: String,
        fecha: Long
    ): Intent {
        // Los recordatorios se agendan a las 09:00 del día indicado.
        val inicio = Fechas.fechaConHora(fecha, 9 * 60)
        return intentNuevoEvento(
            titulo = "Seguimiento: $tituloOportunidad",
            descripcion = proximaAccion,
            lugar = "",
            inicio = inicio,
            termino = inicio + 60 * 60 * 1000
        )
    }
}
