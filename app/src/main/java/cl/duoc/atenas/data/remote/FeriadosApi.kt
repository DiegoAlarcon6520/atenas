package cl.duoc.atenas.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * API EXTERNA: FERIADOS DE CHILE (Nager.Date)
 *
 * Servicio público y gratuito, sin clave: https://date.nager.at
 * GET https://date.nager.at/api/v3/PublicHolidays/2026/CL
 *
 * Para el negocio, un evento en feriado cambia la afluencia esperada y los
 * permisos del recinto, y un feriado irrenunciable deja al equipo sin
 * personal disponible. La agenda los marca para planificar con tiempo.
 */
interface FeriadosApi {

    @GET("PublicHolidays/{anio}/CL")
    suspend fun feriadosDeChile(@Path("anio") anio: Int): List<FeriadoDto>
}

/**
 * Un feriado tal como lo entrega la API. Solo se leen los campos que se
 * usan: la fecha ("2026-09-18"), el nombre en español y si es nacional
 * (global = false indica un feriado regional, como el de Arica).
 */
data class FeriadoDto(
    val date: String,
    val localName: String,
    val global: Boolean?
)

/** Feriado ya convertido para la app: fecha a medianoche local y nombre. */
data class Feriado(val fecha: Long, val nombre: String)
