package cl.duoc.atenas.prueba

import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.core.Seguridad
import cl.duoc.atenas.data.local.dao.GestionPendiente
import cl.duoc.atenas.data.local.dao.OportunidadResumen
import cl.duoc.atenas.data.local.entity.Comuna
import cl.duoc.atenas.data.local.entity.Contacto
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

/**
 * DATOS DE PRUEBA
 *
 * Fábricas de entidades con valores por defecto razonables. Cada prueba
 * cambia solo lo que le importa (por ejemplo, el estado o el responsable),
 * así queda claro qué dato hace la diferencia.
 */
object Datos {

    const val CLAVE = "atenas123"

    val ejecutiva = usuario(id = 1, nombre = "Camila Valdés", rol = RolUsuario.EJECUTIVO)
    val ejecutivo2 = usuario(id = 2, nombre = "Rodrigo Fuentes", rol = RolUsuario.EJECUTIVO)
    val supervisor = usuario(id = 9, nombre = "Marcelo Silva", rol = RolUsuario.SUPERVISOR)

    val rm = Region(id = 1, nombre = "Región Metropolitana")
    val valpo = Region(id = 2, nombre = "Valparaíso")
    val santiago = Comuna(id = 10, nombre = "Santiago", regionId = 1)
    val puenteAlto = Comuna(id = 11, nombre = "Puente Alto", regionId = 1)
    val vina = Comuna(id = 20, nombre = "Viña del Mar", regionId = 2)

    fun usuario(
        id: Long,
        nombre: String,
        rol: RolUsuario,
        nombreUsuario: String = nombre.substringBefore(" ").lowercase()
    ) = Usuario(
        id = id,
        nombreUsuario = nombreUsuario,
        claveHash = Seguridad.hash(CLAVE),
        nombreCompleto = nombre,
        rol = rol,
        area = "Comercial",
        zona = "Santiago",
        activo = true,
        fechaCreacion = 0,
        fechaActualizacion = 0
    )

    fun organizacion(id: Long = 100, nombre: String = "Constructora Vértice", comunaId: Long = santiago.id) =
        Organizacion(
            id = id,
            nombre = nombre,
            tipo = TipoOrganizacion.EMPRESA,
            rubro = "Construcción",
            comunaId = comunaId,
            sitioWeb = null,
            publicoAsociado = null,
            observaciones = null,
            fechaCreacion = 0
        )

    fun contacto(
        id: Long = 500,
        organizacionId: Long = 100,
        nombre: String = "Ignacia Morales",
        activo: Boolean = true
    ) = Contacto(
        id = id,
        organizacionId = organizacionId,
        nombre = nombre,
        cargo = "Jefa de Bienestar",
        area = AreaContacto.BIENESTAR,
        correo = "ignacia@vertice.cl",
        telefono = "+56 9 1234 5678",
        medioPreferente = MedioContacto.CORREO,
        activo = activo,
        fechaUltimoContacto = null,
        observaciones = null
    )

    fun oportunidad(
        id: Long = 1000,
        nombre: String = "Feria de Beneficios",
        estado: EstadoOportunidad = EstadoOportunidad.IDENTIFICADA,
        responsableId: Long? = ejecutiva.id,
        fechaInicioEvento: Long = Fechas.masDias(Fechas.hoy(), 20),
        organizacionId: Long = 100,
        comunaId: Long = santiago.id,
        contactoId: Long? = 500,
        prioridad: Prioridad = Prioridad.MEDIA,
        costoReferencial: Double = 0.0,
        tipo: TipoOportunidad = TipoOportunidad.FERIA_EMPRESARIAL
    ) = Oportunidad(
        id = id,
        nombre = nombre,
        tipo = tipo,
        descripcion = "Feria anual para trabajadores y sus familias.",
        fuenteInformacion = "Sitio web",
        fechaDeteccion = Fechas.hoy(),
        fechaInicioEvento = fechaInicioEvento,
        fechaTerminoEvento = null,
        horaInicio = 10 * 60,
        horaTermino = 14 * 60,
        direccion = "Av. Principal 123",
        comunaId = comunaId,
        organizacionId = organizacionId,
        contactoId = contactoId,
        responsableId = responsableId,
        creadoPorId = ejecutiva.id,
        publicoObjetivo = "Trabajadores",
        publicoEsperado = 300,
        prioridad = prioridad,
        potencial = PotencialComercial.MEDIO,
        afinidad = AfinidadPublico.MEDIA,
        estado = estado,
        costoReferencial = costoReferencial,
        condiciones = null,
        requisitos = null,
        observaciones = null,
        fechaCreacion = 0,
        fechaActualizacion = 0
    )

    fun resumen(
        id: Long = 1000,
        nombre: String = "Feria de Beneficios",
        estado: EstadoOportunidad = EstadoOportunidad.IDENTIFICADA,
        fechaInicioEvento: Long = Fechas.masDias(Fechas.hoy(), 20),
        responsableNombre: String? = ejecutiva.nombreCompleto,
        organizacionNombre: String = "Constructora Vértice"
    ) = OportunidadResumen(
        id = id,
        nombre = nombre,
        tipo = TipoOportunidad.FERIA_EMPRESARIAL,
        estado = estado,
        prioridad = Prioridad.MEDIA,
        fechaInicioEvento = fechaInicioEvento,
        publicoEsperado = 300,
        potencial = PotencialComercial.MEDIO,
        afinidad = AfinidadPublico.MEDIA,
        organizacionNombre = organizacionNombre,
        comunaNombre = "Santiago",
        responsableNombre = responsableNombre
    )

    fun gestionPendiente(
        id: Long,
        oportunidadId: Long,
        fecha: Long,
        proximaAccion: String? = "Llamar a la contraparte",
        responsableNombre: String? = ejecutiva.nombreCompleto,
        nombre: String = "Oportunidad $oportunidadId"
    ) = GestionPendiente(
        id = id,
        oportunidadId = oportunidadId,
        oportunidadNombre = nombre,
        proximaAccion = proximaAccion,
        fechaProximoSeguimiento = fecha,
        responsableNombre = responsableNombre
    )

    fun seguimiento(
        id: Long = 0,
        oportunidadId: Long = 1000,
        tipoAccion: TipoAccion = TipoAccion.LLAMADA,
        fechaAccion: Long = Fechas.hoy(),
        proximaAccion: String? = null,
        fechaProximo: Long? = null,
        anterior: EstadoOportunidad = EstadoOportunidad.CONTACTO_PENDIENTE,
        nuevo: EstadoOportunidad = EstadoOportunidad.CONTACTO_PENDIENTE,
        observaciones: String? = null
    ) = Seguimiento(
        id = id,
        oportunidadId = oportunidadId,
        usuarioId = ejecutiva.id,
        tipoAccion = tipoAccion,
        fechaAccion = fechaAccion,
        resultado = "Se conversó con la contraparte.",
        proximaAccion = proximaAccion,
        fechaProximoSeguimiento = fechaProximo,
        estadoAnterior = anterior,
        estadoNuevo = nuevo,
        observaciones = observaciones
    )
}
