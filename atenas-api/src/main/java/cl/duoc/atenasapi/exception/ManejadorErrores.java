package cl.duoc.atenasapi.exception;

import cl.duoc.atenasapi.domain.Fechas;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Convierte cada excepción en una respuesta HTTP con su código correcto.
 * Sin esto, cualquier error llegaría a la app como un 500 genérico y la
 * pantalla no podría decirle al usuario qué corregir.
 */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<RespuestaError> noEncontrado(RecursoNoEncontradoException e) {
        return responder(HttpStatus.NOT_FOUND, e.getMessage(), Map.of());
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<RespuestaError> solicitudInvalida(SolicitudInvalidaException e) {
        return responder(HttpStatus.BAD_REQUEST, e.getMessage(), e.getCampos());
    }

    @ExceptionHandler(AccionNoPermitidaException.class)
    public ResponseEntity<RespuestaError> noPermitida(AccionNoPermitidaException e) {
        return responder(HttpStatus.FORBIDDEN, e.getMessage(), Map.of());
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<RespuestaError> conflicto(ConflictoException e) {
        return responder(HttpStatus.CONFLICT, e.getMessage(), Map.of());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<RespuestaError> credenciales(CredencialesInvalidasException e) {
        return responder(HttpStatus.UNAUTHORIZED, e.getMessage(), Map.of());
    }

    /** Falló una anotación de validación (@NotBlank, @Email, ...) en el cuerpo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaError> validacion(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return responder(HttpStatus.BAD_REQUEST, "Revisa los campos marcados", campos);
    }

    /** JSON mal formado o un valor que no existe en un enum (por ejemplo, "estado": "GANADA"). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespuestaError> jsonInvalido(HttpMessageNotReadableException e) {
        return responder(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no es válido o tiene un valor no permitido", Map.of());
    }

    /** Un parámetro de la URL con un tipo incorrecto, como /oportunidades/abc. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RespuestaError> tipoInvalido(MethodArgumentTypeMismatchException e) {
        return responder(HttpStatus.BAD_REQUEST, "El parámetro '" + e.getName() + "' no tiene un valor válido", Map.of());
    }

    /** Falta un parámetro obligatorio de la URL, como ?usuarioId= en la sincronización. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<RespuestaError> parametroFaltante(MissingServletRequestParameterException e) {
        return responder(HttpStatus.BAD_REQUEST, "Falta el parámetro '" + e.getParameterName() + "'", Map.of());
    }

    /** Última barrera: la base rechazó la operación por una llave foránea o un valor único. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RespuestaError> integridad(DataIntegrityViolationException e) {
        return responder(HttpStatus.CONFLICT, "La operación viola una restricción de la base de datos: hay registros que dependen de este", Map.of());
    }

    private ResponseEntity<RespuestaError> responder(HttpStatus estado, String mensaje, Map<String, String> campos) {
        RespuestaError cuerpo = new RespuestaError(estado.value(), estado.getReasonPhrase(), mensaje, campos, Fechas.ahora());
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
