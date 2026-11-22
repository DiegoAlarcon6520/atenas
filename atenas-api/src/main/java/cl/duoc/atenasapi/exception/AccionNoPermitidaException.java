package cl.duoc.atenasapi.exception;

/** 403: el perfil del usuario no puede hacer esa acción (por ejemplo, un ejecutivo que confirma). */
public class AccionNoPermitidaException extends RuntimeException {

    public AccionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}
