package cl.duoc.atenasapi.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "El usuario es obligatorio") String nombreUsuario,
    @NotBlank(message = "La contraseña es obligatoria") String clave
) {
}
