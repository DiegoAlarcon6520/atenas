package cl.duoc.atenas.domain.model

import cl.duoc.atenas.core.Fechas

/** Perfiles de usuario definidos para el MVP. */
enum class RolUsuario(val etiqueta: String) {
    EJECUTIVO("Ejecutivo comercial"),
    SUPERVISOR("Supervisor comercial")
}

enum class TipoOrganizacion(val etiqueta: String) {
    EMPRESA("Empresa"),
    MUNICIPALIDAD("Municipalidad"),
    INSTITUCION_PUBLICA("Institución pública"),
    INSTITUCION_PRIVADA("Institución privada"),
    ORGANIZADOR_EVENTOS("Organizador de eventos"),
    CENTRO_COMERCIAL("Centro comercial"),
    ASOCIACION("Asociación"),
    OTRA("Otra entidad")
}

enum class AreaContacto(val etiqueta: String) {
    EVENTOS("Eventos"),
    ACTIVACIONES("Activaciones"),
    MARKETING("Marketing"),
    COMUNICACIONES("Comunicaciones"),
    RECURSOS_HUMANOS("Recursos Humanos"),
    BIENESTAR("Bienestar"),
    BENEFICIOS("Beneficios"),
    ADMINISTRACION("Administración")
}

enum class MedioContacto(val etiqueta: String) {
    CORREO("Correo"),
    TELEFONO("Teléfono"),
    WHATSAPP("WhatsApp"),
    PRESENCIAL("Presencial")
}

enum class TipoOportunidad(val etiqueta: String) {
    EVENTO_CORPORATIVO("Evento corporativo"),
    FERIA_EMPRESARIAL("Feria empresarial"),
    ACTIVIDAD_MUNICIPAL("Actividad municipal"),
    JORNADA_BENEFICIOS("Jornada de beneficios"),
    FERIA_INSTITUCIONAL("Feria institucional"),
    ACTIVIDAD_TRABAJADORES("Actividad de trabajadores"),
    ACTIVACION_COMERCIAL("Activación comercial"),
    OTRA("Otra instancia relevante")
}

enum class Prioridad(val etiqueta: String, val peso: Int) {
    ALTA("Alta", 3),
    MEDIA("Media", 2),
    BAJA("Baja", 1)
}

/**
 * Potencial comercial estimado de una oportunidad: cuánto podría aportar a la
 * agencia en contactos, inscripciones y convenios si se concreta.
 */
enum class PotencialComercial(val etiqueta: String, val peso: Int) {
    ALTO("Alto", 3),
    MEDIO("Medio", 2),
    BAJO("Bajo", 1)
}

/**
 * Afinidad entre el público del evento y el público objetivo de la agencia
 * (trabajadores y familias interesadas en convenios dentales, automotrices
 * y veterinarios).
 */
enum class AfinidadPublico(val etiqueta: String, val peso: Int) {
    ALTA("Alta", 3),
    MEDIA("Media", 2),
    BAJA("Baja", 1)
}

/**
 * Estados de la gestión comercial. El campo [orden] permite ordenar el embudo
 * y comparar avance entre oportunidades.
 */
enum class EstadoOportunidad(val etiqueta: String, val orden: Int) {
    IDENTIFICADA("Evento identificado", 1),
    CONTACTO_PENDIENTE("Contacto pendiente", 2),
    CONTACTO_REALIZADO("Contacto realizado", 3),
    PROPUESTA_ENVIADA("Propuesta enviada", 4),
    EN_ESPERA_RESPUESTA("En espera de respuesta", 5),
    CONFIRMADA("Participación confirmada", 6),
    DESCARTADA("Oportunidad descartada", 7),
    ACTIVACION_REALIZADA("Activación realizada", 8),
    CERRADA("Oportunidad cerrada", 9)
}

/**
 * Tipos de gestión comercial.
 *
 * [implicaContacto] indica si la gestión supone haber hablado o escrito al
 * contacto de la organización. Las dos primeras son trabajo interno de
 * investigación, así que no cuentan como contacto.
 */
enum class TipoAccion(val etiqueta: String, val implicaContacto: Boolean = true) {
    INVESTIGACION_INICIAL("Investigación inicial", implicaContacto = false),
    BUSQUEDA_CONTACTO("Búsqueda de contacto", implicaContacto = false),
    PRIMER_CONTACTO("Primer contacto"),
    ENVIO_PRESENTACION("Envío de presentación"),
    ENVIO_PROPUESTA("Envío de propuesta"),
    SOLICITUD_ANTECEDENTES("Solicitud de antecedentes"),
    REUNION("Reunión"),
    LLAMADA("Llamada"),
    SEGUIMIENTO("Seguimiento"),
    RECEPCION_RESPUESTA("Recepción de respuesta"),
    CONFIRMACION("Confirmación"),
    CIERRE("Cierre")
}

/**
 * Rangos rápidos para filtrar oportunidades por la fecha del evento (HU-18).
 * Se usan rangos predefinidos en vez de dos calendarios porque en terreno es
 * más rápido tocar una opción que elegir dos fechas.
 */
enum class RangoFecha(val etiqueta: String) {
    PROXIMOS_7("Próximos 7 días"),
    PROXIMOS_30("Próximos 30 días"),
    PROXIMOS_90("Próximos 90 días"),
    PASADOS("Ya ocurridos");

    /**
     * Límites del rango a partir de [hoy] (medianoche local). Devuelve el
     * par (desde, hasta), ambos inclusive; null significa "sin límite".
     */
    fun limites(hoy: Long): Pair<Long?, Long?> = when (this) {
        PROXIMOS_7 -> hoy to Fechas.masDias(hoy, 7)
        PROXIMOS_30 -> hoy to Fechas.masDias(hoy, 30)
        PROXIMOS_90 -> hoy to Fechas.masDias(hoy, 90)
        PASADOS -> null to hoy - 1
    }
}

/**
 * Agrupación del embudo en las pestañas de la cartera. Siete estados son
 * demasiados para recorrerlos de un vistazo: el ejecutivo piensa en "a quién
 * me falta contactar", "qué estoy negociando" y "qué ya está cerrado".
 */
enum class EtapaCartera(val etiqueta: String, val estados: Set<EstadoOportunidad>) {
    POR_CONTACTAR(
        "Por contactar",
        setOf(EstadoOportunidad.IDENTIFICADA, EstadoOportunidad.CONTACTO_PENDIENTE)
    ),
    EN_CURSO(
        "En curso",
        setOf(
            EstadoOportunidad.CONTACTO_REALIZADO,
            EstadoOportunidad.PROPUESTA_ENVIADA,
            EstadoOportunidad.EN_ESPERA_RESPUESTA
        )
    ),
    CONFIRMADAS(
        "Confirmadas",
        setOf(EstadoOportunidad.CONFIRMADA, EstadoOportunidad.ACTIVACION_REALIZADA)
    ),
    CERRADAS(
        "Cerradas",
        setOf(EstadoOportunidad.DESCARTADA, EstadoOportunidad.CERRADA)
    );

    companion object {
        fun de(estado: EstadoOportunidad): EtapaCartera = entries.first { estado in it.estados }
    }
}
