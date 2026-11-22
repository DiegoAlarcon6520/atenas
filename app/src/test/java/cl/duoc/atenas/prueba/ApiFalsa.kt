package cl.duoc.atenas.prueba

import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.core.Seguridad
import cl.duoc.atenas.data.local.entity.Oportunidad
import cl.duoc.atenas.data.local.entity.Usuario
import cl.duoc.atenas.data.remote.AsignacionSolicitud
import cl.duoc.atenas.data.remote.AtenasApi
import cl.duoc.atenas.data.remote.ContactoDto
import cl.duoc.atenas.data.remote.ContactoSolicitud
import cl.duoc.atenas.data.remote.EvidenciaDto
import cl.duoc.atenas.data.remote.EvidenciaSolicitud
import cl.duoc.atenas.data.remote.FeriadoDto
import cl.duoc.atenas.data.remote.FeriadosApi
import cl.duoc.atenas.data.remote.LoginSolicitud
import cl.duoc.atenas.data.remote.OportunidadDto
import cl.duoc.atenas.data.remote.OportunidadSolicitud
import cl.duoc.atenas.data.remote.OrganizacionDto
import cl.duoc.atenas.data.remote.OrganizacionSolicitud
import cl.duoc.atenas.data.remote.SeguimientoDto
import cl.duoc.atenas.data.remote.SeguimientoSolicitud
import cl.duoc.atenas.data.remote.SincronizacionDto
import cl.duoc.atenas.data.remote.UsuarioDto
import cl.duoc.atenas.domain.model.EstadoOportunidad
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

/**
 * SERVIDOR FALSO
 *
 * Reemplaza a Retrofit en las pruebas: responde como lo haría el backend
 * (asigna ids, deja las oportunidades nuevas en IDENTIFICADA, conserva el
 * estado al editar), sin red ni IntelliJ encendido.
 *
 * Para simular problemas:
 *  - fallo = IOException()      → sin conexión
 *  - fallo = errorHttp(409, ..) → el servidor rechaza la operación
 */
class ApiFalsa(
    private val oportunidades: OportunidadDaoFalso,
    private val contactos: ContactoDaoFalso
) : AtenasApi {

    /** Cuentas que existen en el servidor (independientes de la copia local). */
    val usuariosServidor = mutableListOf(Datos.ejecutiva, Datos.ejecutivo2, Datos.supervisor)

    /** Lo que responde GET /sincronizacion. Null = el servidor no responde. */
    var instantanea: SincronizacionDto? = null

    /** Si no es null, todas las llamadas lanzan esta excepción. */
    var fallo: Exception? = null

    /** Falla solo la sincronización (el login sí responde). */
    var falloSincronizacion: Exception? = null

    val llamadas = mutableListOf<String>()
    var ultimaAsignacion: AsignacionSolicitud? = null
    private var siguienteId = 9000L

    private fun recibir(nombre: String) {
        llamadas += nombre
        fallo?.let { throw it }
    }

    override suspend fun login(solicitud: LoginSolicitud): UsuarioDto {
        recibir("login")
        val usuario = usuariosServidor.firstOrNull {
            it.nombreUsuario == solicitud.nombreUsuario && it.claveHash == Seguridad.hash(solicitud.clave) && it.activo
        } ?: throw errorHttp(401, "Usuario o contraseña incorrectos")
        return usuario.aDto()
    }

    override suspend fun sincronizar(usuarioId: Long): SincronizacionDto {
        recibir("sincronizar")
        falloSincronizacion?.let { throw it }
        return instantanea ?: throw IOException("el servidor no responde")
    }

    override suspend fun crearOrganizacion(solicitud: OrganizacionSolicitud): OrganizacionDto {
        recibir("crearOrganizacion")
        return solicitud.aDto(siguienteId++)
    }

    override suspend fun actualizarOrganizacion(id: Long, solicitud: OrganizacionSolicitud): OrganizacionDto {
        recibir("actualizarOrganizacion")
        return solicitud.aDto(id)
    }

    override suspend fun crearContacto(solicitud: ContactoSolicitud): ContactoDto {
        recibir("crearContacto")
        return solicitud.aDto(siguienteId++, fechaUltimoContacto = null)
    }

    /** El servidor conserva la fecha de último contacto: no viene en la solicitud. */
    override suspend fun actualizarContacto(id: Long, solicitud: ContactoSolicitud): ContactoDto {
        recibir("actualizarContacto")
        return solicitud.aDto(id, fechaUltimoContacto = contactos.porId(id)?.fechaUltimoContacto)
    }

    override suspend fun crearOportunidad(solicitud: OportunidadSolicitud): OportunidadDto {
        recibir("crearOportunidad")
        return solicitud.aDto(siguienteId++, EstadoOportunidad.IDENTIFICADA, solicitud.responsableId, solicitud.creadoPorId)
    }

    /** Al editar, el servidor mantiene estado, responsable y creador. */
    override suspend fun actualizarOportunidad(id: Long, solicitud: OportunidadSolicitud): OportunidadDto {
        recibir("actualizarOportunidad")
        val actual = oportunidades.porId(id) ?: throw errorHttp(404, "La oportunidad $id no existe")
        return solicitud.aDto(id, actual.estado, actual.responsableId, actual.creadoPorId)
    }

    override suspend fun asignarResponsable(id: Long, solicitud: AsignacionSolicitud): OportunidadDto {
        recibir("asignarResponsable")
        ultimaAsignacion = solicitud
        val actual = oportunidades.porId(id) ?: throw errorHttp(404, "La oportunidad $id no existe")
        return actual.copy(responsableId = solicitud.responsableId).aDto()
    }

    override suspend fun registrarGestion(oportunidadId: Long, solicitud: SeguimientoSolicitud): SeguimientoDto {
        recibir("registrarGestion")
        val actual = oportunidades.porId(oportunidadId) ?: throw errorHttp(404, "La oportunidad no existe")
        return SeguimientoDto(
            siguienteId++, oportunidadId, solicitud.usuarioId, solicitud.tipoAccion, solicitud.fechaAccion,
            solicitud.resultado, solicitud.proximaAccion, solicitud.fechaProximoSeguimiento,
            estadoAnterior = actual.estado, estadoNuevo = solicitud.estadoNuevo, observaciones = solicitud.observaciones
        )
    }

    override suspend fun crearEvidencia(solicitud: EvidenciaSolicitud): EvidenciaDto {
        recibir("crearEvidencia")
        return EvidenciaDto(siguienteId++, solicitud.oportunidadId, solicitud.uri, solicitud.descripcion, solicitud.fechaCaptura)
    }

    override suspend fun eliminarEvidencia(id: Long) {
        recibir("eliminarEvidencia")
    }

    // Las reglas de rechazo (409) del backend se simulan con fallo = errorHttp(409, ...).
    override suspend fun eliminarOportunidad(id: Long) {
        recibir("eliminarOportunidad")
    }

    override suspend fun eliminarOrganizacion(id: Long) {
        recibir("eliminarOrganizacion")
    }

    override suspend fun eliminarContacto(id: Long) {
        recibir("eliminarContacto")
    }
}

/**
 * API DE FERIADOS FALSA
 *
 * Reemplaza a date.nager.at. Responde la lista configurada para cualquier año
 * (cambiando el año de cada fecha) y cuenta cuántas veces la llamaron, para
 * probar la memoria del repositorio.
 */
class FeriadosApiFalsa(
    var respuesta: List<FeriadoDto> = emptyList(),
    var fallo: Exception? = null
) : FeriadosApi {
    val aniosPedidos = mutableListOf<Int>()

    override suspend fun feriadosDeChile(anio: Int): List<FeriadoDto> {
        aniosPedidos += anio
        fallo?.let { throw it }
        return respuesta.map { it.copy(date = "$anio" + it.date.substring(4)) }
    }
}

/** Error HTTP con el mismo cuerpo JSON que envía el backend. */
fun errorHttp(codigo: Int, mensaje: String): HttpException =
    errorHttpCrudo(codigo, """{"estado": $codigo, "error": "Error", "mensaje": "$mensaje", "campos": {}}""")

/** Error HTTP con cualquier cuerpo, para probar respuestas que no son del backend. */
fun errorHttpCrudo(codigo: Int, cuerpo: String): HttpException =
    HttpException(Response.error<Any>(codigo, cuerpo.toResponseBody("application/json".toMediaType())))

// ---------- Conversiones para armar respuestas ----------

fun Usuario.aDto() = UsuarioDto(id, nombreUsuario, nombreCompleto, rol, area, zona, activo, fechaCreacion, fechaActualizacion)

fun Oportunidad.aDto() = OportunidadDto(
    id, nombre, tipo, descripcion, fuenteInformacion, fechaDeteccion, fechaInicioEvento, fechaTerminoEvento,
    horaInicio, horaTermino, direccion, comunaId, organizacionId, contactoId, responsableId, creadoPorId,
    publicoObjetivo, publicoEsperado, prioridad, potencial, afinidad, estado, costoReferencial, condiciones,
    requisitos, espaciosDisponibles, observaciones, fechaCreacion, fechaActualizacion
)

private fun OrganizacionSolicitud.aDto(id: Long) =
    OrganizacionDto(id, nombre, tipo, rubro, comunaId, sitioWeb, publicoAsociado, observaciones, Fechas.ahora())

private fun ContactoSolicitud.aDto(id: Long, fechaUltimoContacto: Long?) = ContactoDto(
    id, organizacionId, nombre, cargo, area, correo, telefono, medioPreferente, activo, fechaUltimoContacto, observaciones
)

private fun OportunidadSolicitud.aDto(id: Long, estado: EstadoOportunidad, responsableId: Long?, creadoPorId: Long) =
    OportunidadDto(
        id, nombre, tipo, descripcion, fuenteInformacion, fechaDeteccion, fechaInicioEvento, fechaTerminoEvento,
        horaInicio, horaTermino, direccion, comunaId, organizacionId, contactoId, responsableId, creadoPorId,
        publicoObjetivo, publicoEsperado, prioridad, potencial, afinidad, estado, costoReferencial, condiciones,
        requisitos, espaciosDisponibles, observaciones, Fechas.ahora(), Fechas.ahora()
    )
