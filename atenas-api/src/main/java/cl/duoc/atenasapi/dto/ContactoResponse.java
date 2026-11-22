package cl.duoc.atenasapi.dto;

import cl.duoc.atenasapi.domain.AreaContacto;
import cl.duoc.atenasapi.domain.MedioContacto;
import cl.duoc.atenasapi.entity.Contacto;

public record ContactoResponse(
    Long id,
    Long organizacionId,
    String nombre,
    String cargo,
    AreaContacto area,
    String correo,
    String telefono,
    MedioContacto medioPreferente,
    boolean activo,
    Long fechaUltimoContacto,
    String observaciones
) {
    public static ContactoResponse desde(Contacto c) {
        return new ContactoResponse(
            c.getId(), c.getOrganizacion().getId(), c.getNombre(), c.getCargo(), c.getArea(), c.getCorreo(),
            c.getTelefono(), c.getMedioPreferente(), c.isActivo(), c.getFechaUltimoContacto(), c.getObservaciones()
        );
    }
}
