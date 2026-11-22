package cl.duoc.atenasapi.dto;

import cl.duoc.atenasapi.domain.EstadoOportunidad;
import cl.duoc.atenasapi.domain.TipoAccion;
import cl.duoc.atenasapi.entity.Seguimiento;

public record SeguimientoResponse(
    Long id,
    Long oportunidadId,
    Long usuarioId,
    TipoAccion tipoAccion,
    long fechaAccion,
    String resultado,
    String proximaAccion,
    Long fechaProximoSeguimiento,
    EstadoOportunidad estadoAnterior,
    EstadoOportunidad estadoNuevo,
    String observaciones
) {
    public static SeguimientoResponse desde(Seguimiento s) {
        return new SeguimientoResponse(
            s.getId(), s.getOportunidad().getId(), s.getUsuario().getId(), s.getTipoAccion(), s.getFechaAccion(),
            s.getResultado(), s.getProximaAccion(), s.getFechaProximoSeguimiento(),
            s.getEstadoAnterior(), s.getEstadoNuevo(), s.getObservaciones()
        );
    }
}
