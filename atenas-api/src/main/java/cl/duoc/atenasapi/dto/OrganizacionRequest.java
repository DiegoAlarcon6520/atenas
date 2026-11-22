package cl.duoc.atenasapi.dto;

import cl.duoc.atenasapi.domain.TipoOrganizacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrganizacionRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres") String nombre,
    @NotNull(message = "El tipo es obligatorio") TipoOrganizacion tipo,
    @NotBlank(message = "El rubro es obligatorio") String rubro,
    @NotNull(message = "Selecciona una comuna") Long comunaId,
    String sitioWeb,
    String publicoAsociado,
    String observaciones
) {
}
