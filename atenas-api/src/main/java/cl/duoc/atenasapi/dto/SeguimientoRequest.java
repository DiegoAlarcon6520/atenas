package cl.duoc.atenasapi.dto;

import cl.duoc.atenasapi.domain.EstadoOportunidad;
import cl.duoc.atenasapi.domain.TipoAccion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Registrar una gestión. Si estadoNuevo viene vacío, la oportunidad se
 * mantiene en su estado actual.
 */
public record SeguimientoRequest(
    @NotNull(message = "Indica quién registra la gestión") Long usuarioId,
    @NotNull(message = "El tipo de acción es obligatorio") TipoAccion tipoAccion,
    @NotNull(message = "La fecha de la gestión es obligatoria") Long fechaAccion,
    @NotBlank(message = "El resultado es obligatorio")
    @Size(min = 10, max = 1000, message = "El resultado debe tener entre 10 y 1000 caracteres") String resultado,
    String proximaAccion,
    Long fechaProximoSeguimiento,
    EstadoOportunidad estadoNuevo,
    String observaciones
) {
}
