package cl.duoc.atenasapi.dto;

import cl.duoc.atenasapi.domain.AreaContacto;
import cl.duoc.atenasapi.domain.MedioContacto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Alta o edición de un contacto. La fecha de último contacto no se recibe:
 * la calcula el servidor al registrar una gestión que fue una conversación.
 */
public record ContactoRequest(
    @NotNull(message = "El contacto debe pertenecer a una organización") Long organizacionId,
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres") String nombre,
    @NotBlank(message = "El cargo es obligatorio") String cargo,
    @NotNull(message = "El área es obligatoria") AreaContacto area,
    @NotBlank(message = "El correo es obligatorio")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Ingresa un correo con formato válido") String correo,
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\+?[0-9 ]{8,15}$", message = "Usa el formato +56 9 1234 5678") String telefono,
    @NotNull(message = "El medio preferente es obligatorio") MedioContacto medioPreferente,
    Boolean activo,
    String observaciones
) {
}
