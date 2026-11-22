package cl.duoc.atenasapi.dto;

import cl.duoc.atenasapi.domain.AfinidadPublico;
import cl.duoc.atenasapi.domain.EstadoOportunidad;
import cl.duoc.atenasapi.domain.PotencialComercial;
import cl.duoc.atenasapi.domain.Prioridad;
import cl.duoc.atenasapi.domain.TipoOportunidad;
import cl.duoc.atenasapi.entity.Oportunidad;

/** Mismos campos y nombres que la entidad Oportunidad de Room, para que la app la guarde directo. */
public record OportunidadResponse(
    Long id,
    String nombre,
    TipoOportunidad tipo,
    String descripcion,
    String fuenteInformacion,
    long fechaDeteccion,
    long fechaInicioEvento,
    Long fechaTerminoEvento,
    int horaInicio,
    int horaTermino,
    String direccion,
    Long comunaId,
    Long organizacionId,
    Long contactoId,
    Long responsableId,
    Long creadoPorId,
    String publicoObjetivo,
    int publicoEsperado,
    Prioridad prioridad,
    PotencialComercial potencial,
    AfinidadPublico afinidad,
    EstadoOportunidad estado,
    double costoReferencial,
    String condiciones,
    String requisitos,
    String espaciosDisponibles,
    String observaciones,
    long fechaCreacion,
    long fechaActualizacion
) {
    public static OportunidadResponse desde(Oportunidad o) {
        return new OportunidadResponse(
            o.getId(), o.getNombre(), o.getTipo(), o.getDescripcion(), o.getFuenteInformacion(),
            o.getFechaDeteccion(), o.getFechaInicioEvento(), o.getFechaTerminoEvento(),
            o.getHoraInicio(), o.getHoraTermino(), o.getDireccion(),
            o.getComuna().getId(), o.getOrganizacion().getId(),
            o.getContacto() == null ? null : o.getContacto().getId(),
            o.getResponsable() == null ? null : o.getResponsable().getId(),
            o.getCreadoPor().getId(),
            o.getPublicoObjetivo(), o.getPublicoEsperado(), o.getPrioridad(), o.getPotencial(), o.getAfinidad(),
            o.getEstado(), o.getCostoReferencial(), o.getCondiciones(), o.getRequisitos(),
            o.getEspaciosDisponibles(), o.getObservaciones(), o.getFechaCreacion(), o.getFechaActualizacion()
        );
    }
}
