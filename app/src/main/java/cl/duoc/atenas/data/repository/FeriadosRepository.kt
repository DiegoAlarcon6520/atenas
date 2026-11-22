package cl.duoc.atenas.data.repository

import cl.duoc.atenas.data.remote.Feriado
import cl.duoc.atenas.data.remote.FeriadosApi
import java.util.Calendar
import kotlin.coroutines.cancellation.CancellationException

/**
 * Feriados de Chile desde la API externa.
 *
 * Los feriados de un año no cambian, así que se piden una sola vez y quedan
 * en memoria mientras la app está abierta. Si no hay conexión o la API falla,
 * la agenda sigue funcionando sin marcar feriados: es información de apoyo,
 * no puede bloquear el trabajo del equipo.
 */
class FeriadosRepository(private val api: FeriadosApi) {

    private val enMemoria = mutableMapOf<Int, List<Feriado>>()

    suspend fun feriados(anios: Collection<Int>): List<Feriado> =
        anios.flatMap { anio -> enMemoria[anio] ?: descargar(anio) }

    private suspend fun descargar(anio: Int): List<Feriado> =
        try {
            val nacionales = llamar { api.feriadosDeChile(anio) }
                // Los feriados regionales (global = false) no afectan a todo el equipo.
                .filter { it.global != false }
                .map { Feriado(fecha = medianocheLocal(it.date), nombre = it.localName) }
            enMemoria[anio] = nacionales
            nacionales
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Sin red, error del servidor o respuesta con otro formato: sin feriados,
            // y no se guarda en memoria para reintentar la próxima vez.
            emptyList()
        }

    /** "2026-09-18" → medianoche de ese día en la hora del teléfono, como las demás fechas de la app. */
    internal fun medianocheLocal(fecha: String): Long {
        val (anio, mes, dia) = fecha.split("-").map { it.toInt() }
        return Calendar.getInstance().apply {
            clear()
            set(anio, mes - 1, dia)
        }.timeInMillis
    }
}
