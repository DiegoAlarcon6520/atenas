package cl.duoc.atenas.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * CLIENTE HTTP
 *
 * Un único Retrofit para toda la app (object = singleton). "by lazy" lo
 * construye recién la primera vez que se usa.
 *
 * GsonConverterFactory convierte el JSON del servidor en las clases de
 * Dtos.kt y viceversa, usando los nombres de los campos.
 */
object RetrofitClient {

    /**
     * Dirección del microservicio.
     *  - Emulador: 10.0.2.2 es el PC donde corre IntelliJ.
     *  - Celular físico: reemplazar por la IP del PC en la misma red wifi
     *    (y agregarla en res/xml/network_security_config.xml).
     */
    const val URL_BASE = "http://10.0.2.2:8080/api/"

    val api: AtenasApi by lazy {
        Retrofit.Builder()
            .baseUrl(URL_BASE)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AtenasApi::class.java)
    }
}
