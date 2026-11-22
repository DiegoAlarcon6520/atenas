package cl.duoc.atenasapi.exception;

/** 404: el recurso pedido en la URL no existe. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
