package cl.duoc.atenasapi.domain;

/** Mismos valores que el enum EstadoOportunidad de la app: viajan en el JSON por su nombre. */
public enum EstadoOportunidad {
    IDENTIFICADA,
    CONTACTO_PENDIENTE,
    CONTACTO_REALIZADO,
    PROPUESTA_ENVIADA,
    EN_ESPERA_RESPUESTA,
    CONFIRMADA,
    DESCARTADA,
    ACTIVACION_REALIZADA,
    CERRADA
}
