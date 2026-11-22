package cl.duoc.atenasapi.dto;

import cl.duoc.atenasapi.domain.RolUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Alta o edición de un usuario. La clave es obligatoria al crear; al editar,
 * si no se envía (null) se conserva la anterior.
 */
public record UsuarioRequest(
    @NotBlank(message = "El usuario es obligatorio")
    @Size(min = 3, max = 40, message = "El usuario debe tener entre 3 y 40 caracteres") String nombreUsuario,
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres") String clave,
    @NotBlank(message = "El nombre es obligatorio") String nombreCompleto,
    @NotNull(message = "El rol es obligatorio") RolUsuario rol,
    @NotBlank(message = "El área es obligatoria") String area,
    @NotBlank(message = "La zona es obligatoria") String zona,
    Boolean activo
) {
}
