package cl.duoc.atenasapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EvidenciaRequest(
    @NotNull(message = "La evidencia debe pertenecer a una oportunidad") Long oportunidadId,
    @NotBlank(message = "La referencia de la foto es obligatoria") String uri,
    String descripcion,
    @NotNull(message = "La fecha de captura es obligatoria") Long fechaCaptura
) {
}
