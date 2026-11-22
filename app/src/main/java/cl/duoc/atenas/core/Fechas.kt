package cl.duoc.atenas.core

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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

    /*
     * CONVERSIÓN CON EL CALENDARIO DE MATERIAL 3
     *
     * El DatePicker trabaja con la medianoche en UTC de cada día, mientras que
     * el resto de la aplicación guarda la medianoche en hora local (la que
     * devuelve hoy()). En Chile, UTC-3, la medianoche UTC del 11/11 equivale
     * al 10/11 a las 21:00: si se guardara tal cual, la fecha quedaría
     * corrida un día. Estas dos funciones traducen entre ambos mundos
     * conservando el día del calendario.
     */

    /** Medianoche local de un día -> medianoche UTC del mismo día (para el DatePicker). */
    fun localAUtc(millisLocal: Long): Long {
        val local = Calendar.getInstance().apply { timeInMillis = millisLocal }
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis
    }

    /** Medianoche UTC entregada por el DatePicker -> medianoche local del mismo día. */
    fun utcALocal(millisUtc: Long): Long {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = millisUtc }
        return Calendar.getInstance().apply {
            clear()
            set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis
    }

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

    /**
     * Diferencia en días entre dos fechas (b - a). Se redondea porque un
     * cambio de horario de verano hace que un "día" mida 23 o 25 horas.
     */
    fun diasEntre(a: Long, b: Long): Int =
        Math.round((b - a) / (1000.0 * 60 * 60 * 24)).toInt()
}
