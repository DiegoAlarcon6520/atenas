package cl.duoc.atenasapi.domain;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Validaciones que comparan dos o más campos entre sí. Las de un solo campo
 * (obligatorio, largo mínimo, formato de correo) se declaran con anotaciones
 * en los DTO de entrada.
 *
 * Son funciones puras: reciben valores y devuelven los errores encontrados,
 * campo por campo. Por eso se prueban sin base de datos ni servidor.
 */
public final class ReglasNegocio {

    private static final long UN_DIA_MS = 24L * 60 * 60 * 1000;

    private ReglasNegocio() {
    }

    /**
     * Coherencia de las fechas y horas de un evento. Mismas reglas que el
     * formulario de la app: si se guardan mal, la agenda muestra eventos
     * imposibles.
     */
    public static Map<String, String> validarEvento(
        long fechaDeteccion,
        long fechaInicioEvento,
        Long fechaTerminoEvento,
        int horaInicio,
        int horaTermino
    ) {
        Map<String, String> errores = new LinkedHashMap<>();
        if (fechaInicioEvento < fechaDeteccion) {
            errores.put("fechaInicioEvento", "El evento no puede ocurrir antes de haberse detectado");
        }
        if (fechaTerminoEvento != null && fechaTerminoEvento < fechaInicioEvento) {
            errores.put("fechaTerminoEvento", "La fecha de término no puede ser anterior al inicio");
        }
        if (horaTermino <= horaInicio) {
            errores.put("horaTermino", "La hora de término debe ser posterior a la de inicio");
        }
        return errores;
    }

    /**
     * Fechas de una gestión de seguimiento.
     *
     * @param hoy medianoche de hoy: una gestión de hoy es válida a cualquier
     *            hora, una de mañana todavía no ocurrió.
     */
    public static Map<String, String> validarGestion(
        long fechaAccion,
        String proximaAccion,
        Long fechaProximoSeguimiento,
        boolean esCierre,
        long hoy
    ) {
        Map<String, String> errores = new LinkedHashMap<>();
        if (fechaAccion >= hoy + UN_DIA_MS) {
            errores.put("fechaAccion", "La fecha de la gestión no puede ser futura");
        }
        // Una oportunidad que se cierra no deja seguimiento pendiente.
        if (esCierre) {
            return errores;
        }
        boolean hayProximaAccion = proximaAccion != null && !proximaAccion.isBlank();
        if (hayProximaAccion && fechaProximoSeguimiento == null) {
            errores.put("fechaProximoSeguimiento", "Indica cuándo se hará la próxima acción");
        } else if (fechaProximoSeguimiento != null && fechaProximoSeguimiento < fechaAccion) {
            errores.put("fechaProximoSeguimiento", "El próximo seguimiento no puede ser anterior a esta gestión");
        }
        return errores;
    }

    /** Usuario normalizado igual que en el login de la app: sin espacios y en minúsculas. */
    public static String normalizarUsuario(String nombreUsuario) {
        return nombreUsuario == null ? "" : nombreUsuario.trim().toLowerCase();
    }

    /** Texto opcional: si viene vacío se guarda como null, no como "". */
    public static String textoOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
