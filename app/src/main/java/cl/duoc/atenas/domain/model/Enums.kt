package cl.duoc.atenas.domain.model

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

enum class TipoAccion(val etiqueta: String) {
    INVESTIGACION_INICIAL("Investigación inicial"),
    BUSQUEDA_CONTACTO("Búsqueda de contacto"),
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
