package cl.duoc.atenasapi.exception;

/**
 * 409: la acción choca con el estado actual de los datos. Por ejemplo,
 * editar una oportunidad cerrada o eliminar una organización con eventos.
 */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
