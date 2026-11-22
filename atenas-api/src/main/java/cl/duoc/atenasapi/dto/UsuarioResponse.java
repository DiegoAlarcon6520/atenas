package cl.duoc.atenasapi.dto;

import cl.duoc.atenasapi.domain.RolUsuario;
import cl.duoc.atenasapi.entity.Usuario;

/** Usuario hacia la app. Nunca incluye la clave cifrada. */
public record UsuarioResponse(
    Long id,
    String nombreUsuario,
    String nombreCompleto,
    RolUsuario rol,
    String area,
    String zona,
    boolean activo,
    long fechaCreacion,
    long fechaActualizacion
) {
    public static UsuarioResponse desde(Usuario u) {
        return new UsuarioResponse(
            u.getId(), u.getNombreUsuario(), u.getNombreCompleto(), u.getRol(), u.getArea(), u.getZona(),
            u.isActivo(), u.getFechaCreacion(), u.getFechaActualizacion()
        );
    }
}
