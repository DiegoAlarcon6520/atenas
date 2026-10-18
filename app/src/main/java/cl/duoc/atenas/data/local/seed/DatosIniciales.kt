package cl.duoc.atenas.data.local.seed

import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.core.Seguridad
import cl.duoc.atenas.data.local.AtenasDatabase
import cl.duoc.atenas.data.local.entity.Comuna
import cl.duoc.atenas.data.local.entity.Contacto
import cl.duoc.atenas.data.local.entity.Oportunidad
import cl.duoc.atenas.data.local.entity.Organizacion
import cl.duoc.atenas.data.local.entity.Region
import cl.duoc.atenas.data.local.entity.Seguimiento
import cl.duoc.atenas.data.local.entity.Usuario
import cl.duoc.atenas.domain.model.AreaContacto
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.MedioContacto
import cl.duoc.atenas.domain.model.Prioridad
import cl.duoc.atenas.domain.model.RolUsuario
import cl.duoc.atenas.domain.model.TipoAccion
import cl.duoc.atenas.domain.model.TipoOportunidad
import cl.duoc.atenas.domain.model.TipoOrganizacion

/**
 * Datos de prueba. Todas las organizaciones, personas, correos y teléfonos
 * son ficticios, según la restricción del caso.
 *
 * Claves de las cuatro cuentas: atenas123
 */
object DatosIniciales {

    const val CLAVE_DEMO = "atenas123"

    suspend fun sembrarSiEstaVacia(db: AtenasDatabase) {
        if (db.usuarioDao().contar() > 0) return

        val hoy = Fechas.hoy()
        val ahora = Fechas.ahora()
        val clave = Seguridad.hash(CLAVE_DEMO)

        // ---------- Usuarios ----------
        val usuarios = db.usuarioDao().insertar(
            listOf(
                Usuario(
                    nombreUsuario = "cvaldes", claveHash = clave,
                    nombreCompleto = "Camila Valdés", rol = RolUsuario.EJECUTIVO,
                    area = "Convenios corporativos", zona = "Región Metropolitana",
                    fechaCreacion = ahora, fechaActualizacion = ahora
                ),
                Usuario(
                    nombreUsuario = "rfuentes", claveHash = clave,
                    nombreCompleto = "Rodrigo Fuentes", rol = RolUsuario.EJECUTIVO,
                    area = "Convenios corporativos", zona = "Valparaíso",
                    fechaCreacion = ahora, fechaActualizacion = ahora
                ),
                Usuario(
                    nombreUsuario = "psoto", claveHash = clave,
                    nombreCompleto = "Paulina Soto", rol = RolUsuario.EJECUTIVO,
                    area = "Convenios corporativos", zona = "Biobío",
                    fechaCreacion = ahora, fechaActualizacion = ahora
                ),
                Usuario(
                    nombreUsuario = "msilva", claveHash = clave,
                    nombreCompleto = "Marcelo Silva", rol = RolUsuario.SUPERVISOR,
                    area = "Jefatura comercial", zona = "Nacional",
                    fechaCreacion = ahora, fechaActualizacion = ahora
                )
            )
        )
        val camila = usuarios[0]
        val rodrigo = usuarios[1]
        val paulina = usuarios[2]
        val marcelo = usuarios[3]

        // ---------- Catálogo de ubicación ----------
        val regiones = db.catalogoDao().insertarRegiones(
            listOf(
                Region(nombre = "Región Metropolitana"),
                Region(nombre = "Valparaíso"),
                Region(nombre = "Biobío")
            )
        )
        val rm = regiones[0]
        val valpo = regiones[1]
        val biobio = regiones[2]

        val comunas = db.catalogoDao().insertarComunas(
            listOf(
                Comuna(nombre = "Santiago", regionId = rm),
                Comuna(nombre = "Puente Alto", regionId = rm),
                Comuna(nombre = "Providencia", regionId = rm),
                Comuna(nombre = "Maipú", regionId = rm),
                Comuna(nombre = "Viña del Mar", regionId = valpo),
                Comuna(nombre = "Valparaíso", regionId = valpo),
                Comuna(nombre = "Concepción", regionId = biobio),
                Comuna(nombre = "Talcahuano", regionId = biobio)
            )
        )
        val santiago = comunas[0]
        val puenteAlto = comunas[1]
        val providencia = comunas[2]
        val maipu = comunas[3]
        val vinaDelMar = comunas[4]
        val concepcion = comunas[6]
        val talcahuano = comunas[7]

        // ---------- Organizaciones ----------
        val organizaciones = db.organizacionDao().insertarTodas(
            listOf(
                Organizacion(
                    nombre = "Constructora Vértice Austral", tipo = TipoOrganizacion.EMPRESA,
                    rubro = "Construcción", comunaId = santiago,
                    sitioWeb = "www.vertice-austral.cl", publicoAsociado = "Trabajadores de obra y oficina central",
                    observaciones = "Alta rotación de personal, interés en convenios dentales.",
                    fechaCreacion = ahora
                ),
                Organizacion(
                    nombre = "Municipalidad de Cerro Verde", tipo = TipoOrganizacion.MUNICIPALIDAD,
                    rubro = "Gobierno local", comunaId = puenteAlto,
                    sitioWeb = "www.cerroverde.cl", publicoAsociado = "Vecinos y funcionarios municipales",
                    observaciones = "Realiza ferias de beneficios trimestrales.",
                    fechaCreacion = ahora
                ),
                Organizacion(
                    nombre = "Instituto Previsional Araucaria", tipo = TipoOrganizacion.INSTITUCION_PRIVADA,
                    rubro = "Previsión y bienestar", comunaId = providencia,
                    sitioWeb = "www.araucaria-prev.cl", publicoAsociado = "Afiliados y sus familias",
                    observaciones = null, fechaCreacion = ahora
                ),
                Organizacion(
                    nombre = "Expo Ferias del Sur SpA", tipo = TipoOrganizacion.ORGANIZADOR_EVENTOS,
                    rubro = "Producción de eventos", comunaId = concepcion,
                    sitioWeb = "www.expoferiasdelsur.cl", publicoAsociado = "Expositores y público general",
                    observaciones = "Arrienda stands por metro cuadrado.",
                    fechaCreacion = ahora
                ),
                Organizacion(
                    nombre = "Centro Comercial Las Riberas", tipo = TipoOrganizacion.CENTRO_COMERCIAL,
                    rubro = "Retail", comunaId = vinaDelMar,
                    sitioWeb = "www.lasriberas.cl", publicoAsociado = "Visitantes de fin de semana",
                    observaciones = null, fechaCreacion = ahora
                ),
                Organizacion(
                    nombre = "Asociación Gremial Metalúrgica Andes", tipo = TipoOrganizacion.ASOCIACION,
                    rubro = "Gremial industrial", comunaId = maipu,
                    sitioWeb = "www.agmandes.cl", publicoAsociado = "Empresas socias y sus trabajadores",
                    observaciones = "Jornada anual de bienestar en noviembre.",
                    fechaCreacion = ahora
                )
            )
        )
        val vertice = organizaciones[0]
        val cerroVerde = organizaciones[1]
        val araucaria = organizaciones[2]
        val expoSur = organizaciones[3]
        val riberas = organizaciones[4]
        val agmAndes = organizaciones[5]

        // ---------- Contactos ----------
        val contactos = db.contactoDao().insertarTodos(
            listOf(
                Contacto(
                    organizacionId = vertice, nombre = "Ignacia Morales", cargo = "Jefa de Bienestar",
                    area = AreaContacto.BIENESTAR, correo = "imorales@vertice-austral.cl",
                    telefono = "+56 9 7000 1122", medioPreferente = MedioContacto.CORREO,
                    fechaUltimoContacto = Fechas.masDias(hoy, -12), observaciones = null
                ),
                Contacto(
                    organizacionId = vertice, nombre = "Tomás Riquelme", cargo = "Encargado de RR.HH.",
                    area = AreaContacto.RECURSOS_HUMANOS, correo = "triquelme@vertice-austral.cl",
                    telefono = "+56 9 7000 1123", medioPreferente = MedioContacto.TELEFONO,
                    fechaUltimoContacto = null, observaciones = "Contacto alternativo."
                ),
                Contacto(
                    organizacionId = cerroVerde, nombre = "Valeria Cáceres", cargo = "Coordinadora de Eventos",
                    area = AreaContacto.EVENTOS, correo = "vcaceres@cerroverde.cl",
                    telefono = "+56 9 7100 2233", medioPreferente = MedioContacto.CORREO,
                    fechaUltimoContacto = Fechas.masDias(hoy, -4), observaciones = null
                ),
                Contacto(
                    organizacionId = araucaria, nombre = "Héctor Pizarro", cargo = "Jefe de Beneficios",
                    area = AreaContacto.BENEFICIOS, correo = "hpizarro@araucaria-prev.cl",
                    telefono = "+56 9 7200 3344", medioPreferente = MedioContacto.CORREO,
                    fechaUltimoContacto = Fechas.masDias(hoy, -20), observaciones = null
                ),
                Contacto(
                    organizacionId = expoSur, nombre = "Daniela Núñez", cargo = "Ejecutiva de Expositores",
                    area = AreaContacto.ACTIVACIONES, correo = "dnunez@expoferiasdelsur.cl",
                    telefono = "+56 9 7300 4455", medioPreferente = MedioContacto.WHATSAPP,
                    fechaUltimoContacto = Fechas.masDias(hoy, -2), observaciones = null
                ),
                Contacto(
                    organizacionId = riberas, nombre = "Sebastián Lagos", cargo = "Jefe de Marketing",
                    area = AreaContacto.MARKETING, correo = "slagos@lasriberas.cl",
                    telefono = "+56 9 7400 5566", medioPreferente = MedioContacto.CORREO,
                    fechaUltimoContacto = null, observaciones = "Responde solo en horario de oficina."
                ),
                Contacto(
                    organizacionId = agmAndes, nombre = "Marcela Tapia", cargo = "Secretaria Ejecutiva",
                    area = AreaContacto.ADMINISTRACION, correo = "mtapia@agmandes.cl",
                    telefono = "+56 9 7500 6677", medioPreferente = MedioContacto.CORREO,
                    fechaUltimoContacto = Fechas.masDias(hoy, -35), observaciones = null
                ),
                Contacto(
                    organizacionId = cerroVerde, nombre = "Jorge Sandoval", cargo = "Director de Comunicaciones",
                    area = AreaContacto.COMUNICACIONES, correo = "jsandoval@cerroverde.cl",
                    telefono = "+56 9 7100 2234", medioPreferente = MedioContacto.PRESENCIAL,
                    fechaUltimoContacto = null, observaciones = null
                )
            )
        )

        // ---------- Oportunidades ----------
        val oportunidades = db.oportunidadDao().insertarTodas(
            listOf(
                Oportunidad(
                    nombre = "Feria de Beneficios Municipal Primavera", tipo = TipoOportunidad.ACTIVIDAD_MUNICIPAL,
                    descripcion = "Feria abierta a vecinos con stands de salud, educación y convenios.",
                    fuenteInformacion = "Sitio web municipal", fechaDeteccion = Fechas.masDias(hoy, -18),
                    fechaInicioEvento = Fechas.masDias(hoy, 21), fechaTerminoEvento = Fechas.masDias(hoy, 21),
                    horario = "10:00 - 18:00", direccion = "Plaza Cívica s/n", comunaId = puenteAlto,
                    organizacionId = cerroVerde, contactoId = contactos[2], responsableId = camila,
                    creadoPorId = camila, publicoObjetivo = "Vecinos y familias", publicoEsperado = 2500,
                    prioridad = Prioridad.ALTA, estado = EstadoOportunidad.PROPUESTA_ENVIADA,
                    costoReferencial = 180000.0, condiciones = "Stand de 3x3 con mesa y dos sillas.",
                    requisitos = "Inscripción con 10 días de anticipación.", observaciones = null,
                    fechaCreacion = ahora, fechaActualizacion = ahora
                ),
                Oportunidad(
                    nombre = "Jornada de Bienestar Vértice Austral", tipo = TipoOportunidad.ACTIVIDAD_TRABAJADORES,
                    descripcion = "Jornada interna para trabajadores de obra y oficina central.",
                    fuenteInformacion = "Contacto directo", fechaDeteccion = Fechas.masDias(hoy, -9),
                    fechaInicioEvento = Fechas.masDias(hoy, 12), fechaTerminoEvento = null,
                    horario = "09:00 - 14:00", direccion = "Av. Industrial 1450", comunaId = santiago,
                    organizacionId = vertice, contactoId = contactos[0], responsableId = camila,
                    creadoPorId = camila, publicoObjetivo = "Trabajadores de la constructora", publicoEsperado = 400,
                    prioridad = Prioridad.ALTA, estado = EstadoOportunidad.EN_ESPERA_RESPUESTA,
                    costoReferencial = 0.0, condiciones = "Sin costo, espacio facilitado por la empresa.",
                    requisitos = "Credencial de acceso para dos promotores.", observaciones = null,
                    fechaCreacion = ahora, fechaActualizacion = ahora
                ),
                Oportunidad(
                    nombre = "Expo Industrial Biobío 2026", tipo = TipoOportunidad.FERIA_EMPRESARIAL,
                    descripcion = "Feria industrial con expositores regionales y público general.",
                    fuenteInformacion = "Redes sociales del organizador", fechaDeteccion = Fechas.masDias(hoy, -30),
                    fechaInicioEvento = Fechas.masDias(hoy, 45), fechaTerminoEvento = Fechas.masDias(hoy, 47),
                    horario = "11:00 - 20:00", direccion = "Centro de Eventos Ribera Norte", comunaId = concepcion,
                    organizacionId = expoSur, contactoId = contactos[4], responsableId = paulina,
                    creadoPorId = paulina, publicoObjetivo = "Trabajadores del sector industrial", publicoEsperado = 8000,
                    prioridad = Prioridad.ALTA, estado = EstadoOportunidad.CONTACTO_REALIZADO,
                    costoReferencial = 950000.0, condiciones = "Arriendo por metro cuadrado, mínimo 9 m².",
                    requisitos = "Contrato firmado y pago del 50% por adelantado.", observaciones = null,
                    fechaCreacion = ahora, fechaActualizacion = ahora
                ),
                Oportunidad(
                    nombre = "Activación Fin de Semana Las Riberas", tipo = TipoOportunidad.ACTIVACION_COMERCIAL,
                    descripcion = "Módulo de captación en pasillo central del centro comercial.",
                    fuenteInformacion = "Prospección en terreno", fechaDeteccion = Fechas.masDias(hoy, -6),
                    fechaInicioEvento = Fechas.masDias(hoy, 30), fechaTerminoEvento = Fechas.masDias(hoy, 31),
                    horario = "12:00 - 21:00", direccion = "Av. Costanera 2300, nivel 1", comunaId = vinaDelMar,
                    organizacionId = riberas, contactoId = contactos[5], responsableId = rodrigo,
                    creadoPorId = rodrigo, publicoObjetivo = "Visitantes del centro comercial", publicoEsperado = 15000,
                    prioridad = Prioridad.MEDIA, estado = EstadoOportunidad.CONTACTO_PENDIENTE,
                    costoReferencial = 420000.0, condiciones = null,
                    requisitos = "Seguro de responsabilidad civil.", observaciones = null,
                    fechaCreacion = ahora, fechaActualizacion = ahora
                ),
                Oportunidad(
                    nombre = "Feria de Afiliados Araucaria", tipo = TipoOportunidad.FERIA_INSTITUCIONAL,
                    descripcion = "Feria de beneficios para afiliados y sus familias.",
                    fuenteInformacion = "Newsletter institucional", fechaDeteccion = Fechas.masDias(hoy, -25),
                    fechaInicioEvento = Fechas.masDias(hoy, 8), fechaTerminoEvento = null,
                    horario = "09:30 - 17:00", direccion = "Av. Nueva Providencia 1200", comunaId = providencia,
                    organizacionId = araucaria, contactoId = contactos[3], responsableId = camila,
                    creadoPorId = marcelo, publicoObjetivo = "Afiliados y familias", publicoEsperado = 1200,
                    prioridad = Prioridad.ALTA, estado = EstadoOportunidad.CONFIRMADA,
                    costoReferencial = 250000.0, condiciones = "Stand asignado por la institución.",
                    requisitos = "Nómina de personal con 5 días de anticipación.", observaciones = null,
                    fechaCreacion = ahora, fechaActualizacion = ahora
                ),
                Oportunidad(
                    nombre = "Jornada Anual de Bienestar AGM Andes", tipo = TipoOportunidad.JORNADA_BENEFICIOS,
                    descripcion = "Jornada para trabajadores de las empresas socias del gremio.",
                    fuenteInformacion = "Referencia de un cliente", fechaDeteccion = Fechas.masDias(hoy, -40),
                    fechaInicioEvento = Fechas.masDias(hoy, 60), fechaTerminoEvento = null,
                    horario = "08:30 - 16:00", direccion = "Camino a Melipilla 7800", comunaId = maipu,
                    organizacionId = agmAndes, contactoId = contactos[6], responsableId = null,
                    creadoPorId = marcelo, publicoObjetivo = "Trabajadores de empresas socias", publicoEsperado = 900,
                    prioridad = Prioridad.MEDIA, estado = EstadoOportunidad.IDENTIFICADA,
                    costoReferencial = 150000.0, condiciones = null, requisitos = null,
                    observaciones = "Falta asignar responsable.",
                    fechaCreacion = ahora, fechaActualizacion = ahora
                ),
                Oportunidad(
                    nombre = "Feria Laboral Municipal Invierno", tipo = TipoOportunidad.ACTIVIDAD_MUNICIPAL,
                    descripcion = "Feria de empleo y servicios con alta afluencia de público.",
                    fuenteInformacion = "Sitio web municipal", fechaDeteccion = Fechas.masDias(hoy, -70),
                    fechaInicioEvento = Fechas.masDias(hoy, -15), fechaTerminoEvento = Fechas.masDias(hoy, -15),
                    horario = "10:00 - 17:00", direccion = "Gimnasio Municipal", comunaId = puenteAlto,
                    organizacionId = cerroVerde, contactoId = contactos[7], responsableId = camila,
                    creadoPorId = camila, publicoObjetivo = "Vecinos en búsqueda de empleo", publicoEsperado = 1800,
                    prioridad = Prioridad.MEDIA, estado = EstadoOportunidad.ACTIVACION_REALIZADA,
                    costoReferencial = 120000.0, condiciones = null, requisitos = null,
                    observaciones = "Activación ejecutada, pendiente registrar resultados.",
                    fechaCreacion = ahora, fechaActualizacion = ahora
                ),
                Oportunidad(
                    nombre = "Encuentro de Proveedores Vértice", tipo = TipoOportunidad.EVENTO_CORPORATIVO,
                    descripcion = "Encuentro cerrado con proveedores y contratistas.",
                    fuenteInformacion = "Contacto directo", fechaDeteccion = Fechas.masDias(hoy, -50),
                    fechaInicioEvento = Fechas.masDias(hoy, -5), fechaTerminoEvento = null,
                    horario = "18:00 - 21:00", direccion = "Hotel Centro Santiago", comunaId = santiago,
                    organizacionId = vertice, contactoId = contactos[1], responsableId = rodrigo,
                    creadoPorId = rodrigo, publicoObjetivo = "Proveedores y contratistas", publicoEsperado = 150,
                    prioridad = Prioridad.BAJA, estado = EstadoOportunidad.DESCARTADA,
                    costoReferencial = 0.0, condiciones = null, requisitos = null,
                    observaciones = "Público no coincide con el objetivo de la agencia.",
                    fechaCreacion = ahora, fechaActualizacion = ahora
                ),
                Oportunidad(
                    nombre = "Feria Costumbrista Talcahuano", tipo = TipoOportunidad.ACTIVIDAD_MUNICIPAL,
                    descripcion = "Feria comunal con alta concentración de público familiar.",
                    fuenteInformacion = "Redes sociales municipales", fechaDeteccion = Fechas.masDias(hoy, -3),
                    fechaInicioEvento = Fechas.masDias(hoy, 38), fechaTerminoEvento = Fechas.masDias(hoy, 40),
                    horario = "11:00 - 22:00", direccion = "Costanera Talcahuano", comunaId = talcahuano,
                    organizacionId = expoSur, contactoId = null, responsableId = paulina,
                    creadoPorId = paulina, publicoObjetivo = "Público familiar", publicoEsperado = 6000,
                    prioridad = Prioridad.BAJA, estado = EstadoOportunidad.IDENTIFICADA,
                    costoReferencial = 90000.0, condiciones = null,
                    requisitos = null, observaciones = "Falta identificar el contacto responsable.",
                    fechaCreacion = ahora, fechaActualizacion = ahora
                ),
                Oportunidad(
                    nombre = "Semana del Trabajador Araucaria", tipo = TipoOportunidad.ACTIVIDAD_TRABAJADORES,
                    descripcion = "Ciclo de actividades para funcionarios de la institución.",
                    fuenteInformacion = "Contacto directo", fechaDeteccion = Fechas.masDias(hoy, -14),
                    fechaInicioEvento = Fechas.masDias(hoy, 25), fechaTerminoEvento = Fechas.masDias(hoy, 29),
                    horario = "09:00 - 18:00", direccion = "Av. Nueva Providencia 1200", comunaId = providencia,
                    organizacionId = araucaria, contactoId = contactos[3], responsableId = rodrigo,
                    creadoPorId = marcelo, publicoObjetivo = "Funcionarios de la institución", publicoEsperado = 600,
                    prioridad = Prioridad.MEDIA, estado = EstadoOportunidad.CONTACTO_REALIZADO,
                    costoReferencial = 75000.0, condiciones = null, requisitos = null, observaciones = null,
                    fechaCreacion = ahora, fechaActualizacion = ahora
                )
            )
        )

        // ---------- Seguimientos ----------
        db.seguimientoDao().insertarTodos(
            listOf(
                Seguimiento(
                    oportunidadId = oportunidades[0], usuarioId = camila,
                    tipoAccion = TipoAccion.PRIMER_CONTACTO, fechaAccion = Fechas.masDias(hoy, -15),
                    resultado = "Se contactó a la coordinadora de eventos, pidió enviar propuesta formal.",
                    proximaAccion = "Enviar propuesta con valores", fechaProximoSeguimiento = Fechas.masDias(hoy, -10),
                    estadoAnterior = EstadoOportunidad.CONTACTO_PENDIENTE,
                    estadoNuevo = EstadoOportunidad.CONTACTO_REALIZADO, observaciones = null
                ),
                Seguimiento(
                    oportunidadId = oportunidades[0], usuarioId = camila,
                    tipoAccion = TipoAccion.ENVIO_PROPUESTA, fechaAccion = Fechas.masDias(hoy, -10),
                    resultado = "Propuesta enviada por correo con dos alternativas de stand.",
                    proximaAccion = "Confirmar recepción y agendar reunión",
                    fechaProximoSeguimiento = Fechas.masDias(hoy, -2),
                    estadoAnterior = EstadoOportunidad.CONTACTO_REALIZADO,
                    estadoNuevo = EstadoOportunidad.PROPUESTA_ENVIADA, observaciones = null
                ),
                Seguimiento(
                    oportunidadId = oportunidades[1], usuarioId = camila,
                    tipoAccion = TipoAccion.ENVIO_PRESENTACION, fechaAccion = Fechas.masDias(hoy, -7),
                    resultado = "Se envió presentación institucional a Bienestar.",
                    proximaAccion = "Llamar para confirmar interés", fechaProximoSeguimiento = Fechas.masDias(hoy, 2),
                    estadoAnterior = EstadoOportunidad.CONTACTO_REALIZADO,
                    estadoNuevo = EstadoOportunidad.EN_ESPERA_RESPUESTA, observaciones = null
                ),
                Seguimiento(
                    oportunidadId = oportunidades[2], usuarioId = paulina,
                    tipoAccion = TipoAccion.SOLICITUD_ANTECEDENTES, fechaAccion = Fechas.masDias(hoy, -5),
                    resultado = "Solicitamos plano de stands y valores por metro cuadrado.",
                    proximaAccion = "Evaluar costo contra presupuesto",
                    fechaProximoSeguimiento = Fechas.masDias(hoy, 5),
                    estadoAnterior = EstadoOportunidad.CONTACTO_REALIZADO,
                    estadoNuevo = EstadoOportunidad.CONTACTO_REALIZADO, observaciones = null
                ),
                Seguimiento(
                    oportunidadId = oportunidades[4], usuarioId = marcelo,
                    tipoAccion = TipoAccion.CONFIRMACION, fechaAccion = Fechas.masDias(hoy, -3),
                    resultado = "La institución confirmó la participación de la agencia.",
                    proximaAccion = "Enviar nómina de personal", fechaProximoSeguimiento = Fechas.masDias(hoy, 3),
                    estadoAnterior = EstadoOportunidad.EN_ESPERA_RESPUESTA,
                    estadoNuevo = EstadoOportunidad.CONFIRMADA, observaciones = null
                ),
                Seguimiento(
                    oportunidadId = oportunidades[6], usuarioId = camila,
                    tipoAccion = TipoAccion.CIERRE, fechaAccion = Fechas.masDias(hoy, -14),
                    resultado = "Activación ejecutada, se generaron 62 contactos.",
                    proximaAccion = "Registrar resultado final", fechaProximoSeguimiento = Fechas.masDias(hoy, -7),
                    estadoAnterior = EstadoOportunidad.CONFIRMADA,
                    estadoNuevo = EstadoOportunidad.ACTIVACION_REALIZADA, observaciones = null
                ),
                Seguimiento(
                    oportunidadId = oportunidades[7], usuarioId = rodrigo,
                    tipoAccion = TipoAccion.INVESTIGACION_INICIAL, fechaAccion = Fechas.masDias(hoy, -45),
                    resultado = "Se revisó el perfil del evento, público no coincide con el objetivo.",
                    proximaAccion = null, fechaProximoSeguimiento = null,
                    estadoAnterior = EstadoOportunidad.IDENTIFICADA,
                    estadoNuevo = EstadoOportunidad.DESCARTADA, observaciones = "Descartada por el supervisor."
                ),
                Seguimiento(
                    oportunidadId = oportunidades[9], usuarioId = rodrigo,
                    tipoAccion = TipoAccion.LLAMADA, fechaAccion = Fechas.masDias(hoy, -8),
                    resultado = "Conversación con el jefe de beneficios, pidió propuesta en octubre.",
                    proximaAccion = "Preparar propuesta", fechaProximoSeguimiento = Fechas.masDias(hoy, -1),
                    estadoAnterior = EstadoOportunidad.CONTACTO_REALIZADO,
                    estadoNuevo = EstadoOportunidad.CONTACTO_REALIZADO, observaciones = null
                )
            )
        )
    }
}
