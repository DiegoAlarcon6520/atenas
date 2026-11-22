package cl.duoc.atenasapi.exception;

import java.util.Map;

/** 400: los datos enviados no cumplen una regla. Puede indicar el error de cada campo. */
public class SolicitudInvalidaException extends RuntimeException {

    private final Map<String, String> campos;

    public SolicitudInvalidaException(String mensaje) {
        this(mensaje, Map.of());
    }

    public SolicitudInvalidaException(String mensaje, Map<String, String> campos) {
        super(mensaje);
        this.campos = campos;
    }

    public Map<String, String> getCampos() {
        return campos;
    }
}
