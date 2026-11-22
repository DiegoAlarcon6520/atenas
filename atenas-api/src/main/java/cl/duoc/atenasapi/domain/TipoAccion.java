package cl.duoc.atenasapi.domain;

/**
 * Tipos de gestión. Mismos valores que en la app.
 *
 * implicaContacto indica si la gestión fue una conversación con la
 * organización: solo esas actualizan la fecha de último contacto. Investigar
 * o buscar un contacto es trabajo interno y no cuenta.
 */
public enum TipoAccion {
    INVESTIGACION_INICIAL(false),
    BUSQUEDA_CONTACTO(false),
    PRIMER_CONTACTO(true),
    ENVIO_PRESENTACION(true),
    ENVIO_PROPUESTA(true),
    SOLICITUD_ANTECEDENTES(true),
    REUNION(true),
    LLAMADA(true),
    SEGUIMIENTO(true),
    RECEPCION_RESPUESTA(true),
    CONFIRMACION(true),
    CIERRE(true);

    private final boolean implicaContacto;

    TipoAccion(boolean implicaContacto) {
        this.implicaContacto = implicaContacto;
    }

    public boolean implicaContacto() {
        return implicaContacto;
    }
}
