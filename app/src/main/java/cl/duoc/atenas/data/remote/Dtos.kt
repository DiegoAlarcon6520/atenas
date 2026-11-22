package cl.duoc.atenas.data.remote

import cl.duoc.atenas.data.local.entity.Comuna
import cl.duoc.atenas.data.local.entity.Contacto
import cl.duoc.atenas.data.local.entity.Evidencia
import cl.duoc.atenas.data.local.entity.Oportunidad
import cl.duoc.atenas.data.local.entity.Organizacion
import cl.duoc.atenas.data.local.entity.Region
import cl.duoc.atenas.data.local.entity.Seguimiento
import cl.duoc.atenas.data.local.entity.Usuario
import cl.duoc.atenas.domain.model.AfinidadPublico
import cl.duoc.atenas.domain.model.AreaContacto
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.MedioContacto
import cl.duoc.atenas.domain.model.PotencialComercial
import cl.duoc.atenas.domain.model.Prioridad
import cl.duoc.atenas.domain.model.RolUsuario
import cl.duoc.atenas.domain.model.TipoAccion
import cl.duoc.atenas.domain.model.TipoOportunidad
import cl.duoc.atenas.domain.model.TipoOrganizacion

/*
 * DTO: LA FORMA DEL JSON
 *
 * Igual que en la guía (Cliente / ClienteRequest), se separa lo que se lee
 * del servidor (…Dto) de lo que se le envía (…Solicitud). Los nombres de
 * campo son exactamente los del JSON del backend; Gson los usa para
 * convertir. Los enum viajan por su nombre ("EJECUTIVO", "ALTA"...).
 *
 * Los campos que el servidor puede mandar en null se declaran con "?": si
 * se declararan sin él y llegara null, la app fallaría más adelante.
 *
 * aEntidad() convierte cada respuesta en la entidad de Room que se guarda
 * como copia local; desde(...) arma la solicitud a partir de la entidad.
 */

// ---------- Sesión ----------

data class LoginSolicitud(val nombreUsuario: String, val clave: String)

data class UsuarioDto(
    val id: Long,
    val nombreUsuario: String,
    val nombreCompleto: String,
    val rol: RolUsuario,
    val area: String,
    val zona: String,
    val activo: Boolean,
    val fechaCreacion: Long,
    val fechaActualizacion: Long
) {
    /** El servidor nunca envía la clave; [claveHash] es la copia local para entrar sin conexión. */
    fun aEntidad(claveHash: String) = Usuario(
        id = id, nombreUsuario = nombreUsuario, claveHash = claveHash, nombreCompleto = nombreCompleto,
        rol = rol, area = area, zona = zona, activo = activo,
        fechaCreacion = fechaCreacion, fechaActualizacion = fechaActualizacion
    )
}

// ---------- Catálogo ----------

data class RegionDto(val id: Long, val nombre: String) {
    fun aEntidad() = Region(id = id, nombre = nombre)
}

data class ComunaDto(val id: Long, val nombre: String, val regionId: Long) {
    fun aEntidad() = Comuna(id = id, nombre = nombre, regionId = regionId)
}

// ---------- Organizaciones ----------

data class OrganizacionDto(
    val id: Long,
    val nombre: String,
    val tipo: TipoOrganizacion,
    val rubro: String,
    val comunaId: Long,
    val sitioWeb: String?,
    val publicoAsociado: String?,
    val observaciones: String?,
    val fechaCreacion: Long
) {
    fun aEntidad() = Organizacion(
        id = id, nombre = nombre, tipo = tipo, rubro = rubro, comunaId = comunaId, sitioWeb = sitioWeb,
        publicoAsociado = publicoAsociado, observaciones = observaciones, fechaCreacion = fechaCreacion
    )
}

data class OrganizacionSolicitud(
    val nombre: String,
    val tipo: TipoOrganizacion,
    val rubro: String,
    val comunaId: Long,
    val sitioWeb: String?,
    val publicoAsociado: String?,
    val observaciones: String?
) {
    companion object {
        fun desde(o: Organizacion) = OrganizacionSolicitud(
            o.nombre, o.tipo, o.rubro, o.comunaId, o.sitioWeb, o.publicoAsociado, o.observaciones
        )
    }
}

// ---------- Contactos ----------

data class ContactoDto(
    val id: Long,
    val organizacionId: Long,
    val nombre: String,
    val cargo: String,
    val area: AreaContacto,
    val correo: String,
    val telefono: String,
    val medioPreferente: MedioContacto,
    val activo: Boolean,
    val fechaUltimoContacto: Long?,
    val observaciones: String?
) {
    fun aEntidad() = Contacto(
        id = id, organizacionId = organizacionId, nombre = nombre, cargo = cargo, area = area, correo = correo,
        telefono = telefono, medioPreferente = medioPreferente, activo = activo,
        fechaUltimoContacto = fechaUltimoContacto, observaciones = observaciones
    )
}

data class ContactoSolicitud(
    val organizacionId: Long,
    val nombre: String,
    val cargo: String,
    val area: AreaContacto,
    val correo: String,
    val telefono: String,
    val medioPreferente: MedioContacto,
    val activo: Boolean,
    val observaciones: String?
) {
    companion object {
        fun desde(c: Contacto) = ContactoSolicitud(
            c.organizacionId, c.nombre, c.cargo, c.area, c.correo, c.telefono, c.medioPreferente, c.activo, c.observaciones
        )
    }
}

// ---------- Oportunidades ----------

data class OportunidadDto(
    val id: Long,
    val nombre: String,
    val tipo: TipoOportunidad,
    val descripcion: String,
    val fuenteInformacion: String,
    val fechaDeteccion: Long,
    val fechaInicioEvento: Long,
    val fechaTerminoEvento: Long?,
    val horaInicio: Int,
    val horaTermino: Int,
    val direccion: String,
    val comunaId: Long,
    val organizacionId: Long,
    val contactoId: Long?,
    val responsableId: Long?,
    val creadoPorId: Long,
    val publicoObjetivo: String,
    val publicoEsperado: Int,
    val prioridad: Prioridad,
    val potencial: PotencialComercial,
    val afinidad: AfinidadPublico,
    val estado: EstadoOportunidad,
    val costoReferencial: Double,
    val condiciones: String?,
    val requisitos: String?,
    val espaciosDisponibles: String?,
    val observaciones: String?,
    val fechaCreacion: Long,
    val fechaActualizacion: Long
) {
    fun aEntidad() = Oportunidad(
        id = id, nombre = nombre, tipo = tipo, descripcion = descripcion, fuenteInformacion = fuenteInformacion,
        fechaDeteccion = fechaDeteccion, fechaInicioEvento = fechaInicioEvento, fechaTerminoEvento = fechaTerminoEvento,
        horaInicio = horaInicio, horaTermino = horaTermino, direccion = direccion, comunaId = comunaId,
        organizacionId = organizacionId, contactoId = contactoId, responsableId = responsableId,
        creadoPorId = creadoPorId, publicoObjetivo = publicoObjetivo, publicoEsperado = publicoEsperado,
        prioridad = prioridad, potencial = potencial, afinidad = afinidad, estado = estado,
        costoReferencial = costoReferencial, condiciones = condiciones, requisitos = requisitos,
        espaciosDisponibles = espaciosDisponibles, observaciones = observaciones,
        fechaCreacion = fechaCreacion, fechaActualizacion = fechaActualizacion
    )
}

/** El estado no se envía: en el servidor solo cambia registrando una gestión. */
data class OportunidadSolicitud(
    val nombre: String,
    val tipo: TipoOportunidad,
    val descripcion: String,
    val fuenteInformacion: String,
    val fechaDeteccion: Long,
    val fechaInicioEvento: Long,
    val fechaTerminoEvento: Long?,
    val horaInicio: Int,
    val horaTermino: Int,
    val direccion: String,
    val comunaId: Long,
    val organizacionId: Long,
    val contactoId: Long?,
    val responsableId: Long?,
    val creadoPorId: Long,
    val publicoObjetivo: String,
    val publicoEsperado: Int,
    val prioridad: Prioridad,
    val potencial: PotencialComercial,
    val afinidad: AfinidadPublico,
    val costoReferencial: Double,
    val condiciones: String?,
    val requisitos: String?,
    val espaciosDisponibles: String?,
    val observaciones: String?
) {
    companion object {
        fun desde(o: Oportunidad) = OportunidadSolicitud(
            nombre = o.nombre, tipo = o.tipo, descripcion = o.descripcion, fuenteInformacion = o.fuenteInformacion,
            fechaDeteccion = o.fechaDeteccion, fechaInicioEvento = o.fechaInicioEvento,
            fechaTerminoEvento = o.fechaTerminoEvento, horaInicio = o.horaInicio, horaTermino = o.horaTermino,
            direccion = o.direccion, comunaId = o.comunaId, organizacionId = o.organizacionId,
            contactoId = o.contactoId, responsableId = o.responsableId, creadoPorId = o.creadoPorId,
            publicoObjetivo = o.publicoObjetivo, publicoEsperado = o.publicoEsperado, prioridad = o.prioridad,
            potencial = o.potencial, afinidad = o.afinidad, costoReferencial = o.costoReferencial,
            condiciones = o.condiciones, requisitos = o.requisitos,
            espaciosDisponibles = o.espaciosDisponibles, observaciones = o.observaciones
        )
    }
}

/** [usuarioId] es quien asigna: el servidor verifica que sea supervisor. */
data class AsignacionSolicitud(val responsableId: Long?, val usuarioId: Long)

// ---------- Gestiones ----------

data class SeguimientoDto(
    val id: Long,
    val oportunidadId: Long,
    val usuarioId: Long,
    val tipoAccion: TipoAccion,
    val fechaAccion: Long,
    val resultado: String,
    val proximaAccion: String?,
    val fechaProximoSeguimiento: Long?,
    val estadoAnterior: EstadoOportunidad,
    val estadoNuevo: EstadoOportunidad,
    val observaciones: String?
) {
    fun aEntidad() = Seguimiento(
        id = id, oportunidadId = oportunidadId, usuarioId = usuarioId, tipoAccion = tipoAccion,
        fechaAccion = fechaAccion, resultado = resultado, proximaAccion = proximaAccion,
        fechaProximoSeguimiento = fechaProximoSeguimiento, estadoAnterior = estadoAnterior,
        estadoNuevo = estadoNuevo, observaciones = observaciones
    )
}

data class SeguimientoSolicitud(
    val usuarioId: Long,
    val tipoAccion: TipoAccion,
    val fechaAccion: Long,
    val resultado: String,
    val proximaAccion: String?,
    val fechaProximoSeguimiento: Long?,
    val estadoNuevo: EstadoOportunidad,
    val observaciones: String?
) {
    companion object {
        fun desde(s: Seguimiento) = SeguimientoSolicitud(
            s.usuarioId, s.tipoAccion, s.fechaAccion, s.resultado, s.proximaAccion,
            s.fechaProximoSeguimiento, s.estadoNuevo, s.observaciones
        )
    }
}

// ---------- Evidencias ----------

data class EvidenciaDto(val id: Long, val oportunidadId: Long, val uri: String, val descripcion: String?, val fechaCaptura: Long) {
    fun aEntidad() = Evidencia(id = id, oportunidadId = oportunidadId, uri = uri, descripcion = descripcion, fechaCaptura = fechaCaptura)
}

data class EvidenciaSolicitud(val oportunidadId: Long, val uri: String, val descripcion: String?, val fechaCaptura: Long) {
    companion object {
        fun desde(e: Evidencia) = EvidenciaSolicitud(e.oportunidadId, e.uri, e.descripcion, e.fechaCaptura)
    }
}

// ---------- Sincronización y errores ----------

/** Copia completa para Room. El servidor ya la filtra por perfil. */
data class SincronizacionDto(
    val generadoEn: Long,
    val usuarios: List<UsuarioDto>,
    val regiones: List<RegionDto>,
    val comunas: List<ComunaDto>,
    val organizaciones: List<OrganizacionDto>,
    val contactos: List<ContactoDto>,
    val oportunidades: List<OportunidadDto>,
    val seguimientos: List<SeguimientoDto>,
    val evidencias: List<EvidenciaDto>
)

/** Forma de todos los errores del backend. */
data class RespuestaErrorDto(
    val estado: Int,
    val mensaje: String?,
    val campos: Map<String, String>?
)
