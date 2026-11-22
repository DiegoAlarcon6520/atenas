package cl.duoc.atenas.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * ENDPOINTS DEL MICROSERVICIO
 *
 * Retrofit genera la implementación de esta interfaz: cada función es una
 * petición HTTP. La anotación dice el método y la ruta, @Path reemplaza una
 * parte de la ruta, @Query agrega un parámetro (?usuarioId=1) y @Body se
 * envía como JSON. Son suspend para llamarlas desde una corrutina sin
 * bloquear la pantalla.
 *
 * Si el servidor responde un error (400, 403, 409...), Retrofit lanza
 * HttpException; si no hay red, IOException. Los repositorios traducen
 * ambas a mensajes para el usuario (ver data/repository/LlamadaRemota.kt).
 *
 * Solo están los endpoints que usa la app. La API completa (con todos los
 * CRUD) está documentada en Swagger: http://localhost:8080/swagger-ui.html
 */
interface AtenasApi {

    @POST("auth/login")
    suspend fun login(@Body solicitud: LoginSolicitud): UsuarioDto

    @GET("sincronizacion")
    suspend fun sincronizar(@Query("usuarioId") usuarioId: Long): SincronizacionDto

    @POST("organizaciones")
    suspend fun crearOrganizacion(@Body solicitud: OrganizacionSolicitud): OrganizacionDto

    @PUT("organizaciones/{id}")
    suspend fun actualizarOrganizacion(@Path("id") id: Long, @Body solicitud: OrganizacionSolicitud): OrganizacionDto

    /** 409 si tiene oportunidades; si no, el servidor la elimina junto con sus contactos. */
    @DELETE("organizaciones/{id}")
    suspend fun eliminarOrganizacion(@Path("id") id: Long)

    @POST("contactos")
    suspend fun crearContacto(@Body solicitud: ContactoSolicitud): ContactoDto

    @PUT("contactos/{id}")
    suspend fun actualizarContacto(@Path("id") id: Long, @Body solicitud: ContactoSolicitud): ContactoDto

    /** 409 si está asignado a oportunidades: en ese caso se desactiva. */
    @DELETE("contactos/{id}")
    suspend fun eliminarContacto(@Path("id") id: Long)

    @POST("oportunidades")
    suspend fun crearOportunidad(@Body solicitud: OportunidadSolicitud): OportunidadDto

    @PUT("oportunidades/{id}")
    suspend fun actualizarOportunidad(@Path("id") id: Long, @Body solicitud: OportunidadSolicitud): OportunidadDto

    /** 409 si ya tiene gestiones: en ese caso se descarta registrando una gestión. */
    @DELETE("oportunidades/{id}")
    suspend fun eliminarOportunidad(@Path("id") id: Long)

    @PATCH("oportunidades/{id}/responsable")
    suspend fun asignarResponsable(@Path("id") id: Long, @Body solicitud: AsignacionSolicitud): OportunidadDto

    @POST("oportunidades/{id}/seguimientos")
    suspend fun registrarGestion(@Path("id") oportunidadId: Long, @Body solicitud: SeguimientoSolicitud): SeguimientoDto

    @POST("evidencias")
    suspend fun crearEvidencia(@Body solicitud: EvidenciaSolicitud): EvidenciaDto

    @DELETE("evidencias/{id}")
    suspend fun eliminarEvidencia(@Path("id") id: Long)
}
