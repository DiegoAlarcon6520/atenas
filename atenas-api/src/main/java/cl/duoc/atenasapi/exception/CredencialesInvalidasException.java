package cl.duoc.atenasapi.exception;

/** 401: usuario o clave incorrectos. El mensaje no dice cuál de los dos falló. */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Usuario o contraseña incorrectos");
    }
}
