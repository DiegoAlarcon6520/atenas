package cl.duoc.atenasapi.exception;

import java.util.Map;

/**
 * Forma única de todos los errores de la API, para que la app los lea siempre
 * igual: el código HTTP, un mensaje para mostrar y, si corresponde, el error
 * de cada campo del formulario.
 */
public record RespuestaError(int estado, String error, String mensaje, Map<String, String> campos, long fecha) {
}
