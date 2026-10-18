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

    private val localeCL = Locale("es", "CL")

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

    /** Diferencia en días completos entre dos instantes (b - a). */
    fun diasEntre(a: Long, b: Long): Int =
        ((b - a) / (1000L * 60 * 60 * 24)).toInt()
}
