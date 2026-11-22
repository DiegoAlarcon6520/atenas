package cl.duoc.atenasapi.domain;

import static cl.duoc.atenasapi.domain.EstadoOportunidad.*;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Máquina de estados del embudo comercial.
 *
 * Es la misma regla que aplica la app (domain/rules/TransicionesEstado.kt).
 * Se repite aquí porque el servidor es la fuente de verdad: si alguien llama
 * a la API sin pasar por la app, la regla se sigue cumpliendo.
 */
public final class TransicionesEstado {

    public static final String MENSAJE_CERRADA =
        "La oportunidad está cerrada: su historial quedó sellado y no admite cambios.";

    private static final Map<EstadoOportunidad, List<EstadoOportunidad>> SIGUIENTES = new EnumMap<>(EstadoOportunidad.class);

    static {
        SIGUIENTES.put(IDENTIFICADA, List.of(CONTACTO_PENDIENTE, DESCARTADA));
        SIGUIENTES.put(CONTACTO_PENDIENTE, List.of(CONTACTO_REALIZADO, DESCARTADA));
        SIGUIENTES.put(CONTACTO_REALIZADO, List.of(PROPUESTA_ENVIADA, DESCARTADA));
        SIGUIENTES.put(PROPUESTA_ENVIADA, List.of(EN_ESPERA_RESPUESTA, DESCARTADA));
        SIGUIENTES.put(EN_ESPERA_RESPUESTA, List.of(CONFIRMADA, DESCARTADA));
        SIGUIENTES.put(CONFIRMADA, List.of(ACTIVACION_REALIZADA, DESCARTADA));
        SIGUIENTES.put(ACTIVACION_REALIZADA, List.of(CERRADA));
        SIGUIENTES.put(DESCARTADA, List.of(CERRADA));
        SIGUIENTES.put(CERRADA, List.of());
    }

    /** Decisiones que comprometen a la agencia: solo el supervisor las toma. */
    private static final Set<EstadoOportunidad> RESERVADOS_AL_SUPERVISOR = Set.of(CONFIRMADA, DESCARTADA, CERRADA);

    private static final List<EstadoOportunidad> ETAPAS_DE_AVANCE = List.of(
        IDENTIFICADA, CONTACTO_PENDIENTE, CONTACTO_REALIZADO, PROPUESTA_ENVIADA, EN_ESPERA_RESPUESTA, CONFIRMADA
    );

    private TransicionesEstado() {
    }

    public static List<EstadoOportunidad> siguientesPosibles(EstadoOportunidad actual, RolUsuario rol) {
        return SIGUIENTES.get(actual).stream()
            .filter(destino -> !RESERVADOS_AL_SUPERVISOR.contains(destino) || rol == RolUsuario.SUPERVISOR)
            .toList();
    }

    /** Solo el supervisor puede devolver una oportunidad a una etapa anterior. */
    public static List<EstadoOportunidad> anterioresPosibles(EstadoOportunidad actual, RolUsuario rol) {
        if (rol != RolUsuario.SUPERVISOR) {
            return List.of();
        }
        return switch (actual) {
            case DESCARTADA -> List.of(IDENTIFICADA, CONTACTO_PENDIENTE, CONTACTO_REALIZADO);
            case ACTIVACION_REALIZADA -> List.of(CONFIRMADA);
            case CERRADA -> List.of();
            default -> {
                int posicion = ETAPAS_DE_AVANCE.indexOf(actual);
                yield posicion <= 0 ? List.of() : ETAPAS_DE_AVANCE.subList(0, posicion);
            }
        };
    }

    /** Mantener el estado siempre es válido: una gestión no obliga a avanzar. */
    public static boolean esValida(EstadoOportunidad desde, EstadoOportunidad hacia, RolUsuario rol) {
        return hacia == desde
            || siguientesPosibles(desde, rol).contains(hacia)
            || anterioresPosibles(desde, rol).contains(hacia);
    }

    /** En un estado de cierre no queda gestión comercial pendiente. */
    public static boolean esEstadoDeCierre(EstadoOportunidad estado) {
        return estado == DESCARTADA || estado == ACTIVACION_REALIZADA || estado == CERRADA;
    }

    public static boolean admiteCambios(EstadoOportunidad estado) {
        return estado != CERRADA;
    }
}
