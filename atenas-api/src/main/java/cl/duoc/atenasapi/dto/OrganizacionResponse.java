package cl.duoc.atenasapi.dto;

import cl.duoc.atenasapi.domain.TipoOrganizacion;
import cl.duoc.atenasapi.entity.Organizacion;

public record OrganizacionResponse(
    Long id,
    String nombre,
    TipoOrganizacion tipo,
    String rubro,
    Long comunaId,
    String sitioWeb,
    String publicoAsociado,
    String observaciones,
    long fechaCreacion
) {
    public static OrganizacionResponse desde(Organizacion o) {
        return new OrganizacionResponse(
            o.getId(), o.getNombre(), o.getTipo(), o.getRubro(), o.getComuna().getId(),
            o.getSitioWeb(), o.getPublicoAsociado(), o.getObservaciones(), o.getFechaCreacion()
        );
    }
}
