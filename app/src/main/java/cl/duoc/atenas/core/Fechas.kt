package cl.duoc.atenas.core

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Utilidades de fecha. Se usa Calendar y SimpleDateFormat en lugar de java.time
 * porque el minSdk del proyecto es 24 y java.time requiere API 26.
 */
object Fechas {

    private val localeCL: Locale = Locale.forLanguageTag("es-CL")

    fun aTexto(millis: Long): String =
        SimpleDateFormat("dd/MM/yyyy", localeCL).format(Date(millis))

    fun aTextoConHora(millis: Long): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", localeCL).format(Date(millis))

    /** Hoy a las 00:00. */
    fun hoy(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun ahora(): Long = System.currentTimeMillis()

    fun masDias(base: Long, dias: Int): Long = Calendar.getInstance().apply {
        timeInMillis = base
        add(Calendar.DAY_OF_YEAR, dias)
    }.timeInMillis

    /*
     * Las horas del día se guardan como minutos desde medianoche (Int).
     * 0 = 00:00, 600 = 10:00, 1080 = 18:00.
     *
     * Por qué así y no como texto "10:00": un entero se ordena, se compara y
     * se valida directamente, y en la Fase 6 se puede sumar a la fecha del
     * evento para crear la cita en el calendario del dispositivo.
     */

    /** Convierte minutos desde medianoche a "HH:mm". */
    fun horaATexto(minutos: Int): String =
        String.format(Locale.US, "%02d:%02d", minutos / 60, minutos % 60)

    /** Arma el texto "10:00 - 18:00" a partir de las dos horas. */
    fun rangoHorario(inicio: Int, termino: Int): String =
        "${horaATexto(inicio)} - ${horaATexto(termino)}"

    /** Combina una fecha (millis) con una hora del día (minutos) en un instante. */
    fun fechaConHora(fecha: Long, minutosDelDia: Int): Long = Calendar.getInstance().apply {
        timeInMillis = fecha
        set(Calendar.HOUR_OF_DAY, minutosDelDia / 60)
        set(Calendar.MINUTE, minutosDelDia % 60)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    /** Diferencia en días completos entre dos instantes (b - a). */
    fun diasEntre(a: Long, b: Long): Int =
        ((b - a) / (1000L * 60 * 60 * 24)).toInt()
}
