package cl.duoc.atenasapi.dto;

import cl.duoc.atenasapi.entity.Evidencia;

public record EvidenciaResponse(Long id, Long oportunidadId, String uri, String descripcion, long fechaCaptura) {

    public static EvidenciaResponse desde(Evidencia e) {
        return new EvidenciaResponse(e.getId(), e.getOportunidad().getId(), e.getUri(), e.getDescripcion(), e.getFechaCaptura());
    }
}
