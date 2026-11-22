package cl.duoc.atenasapi.dto;

import cl.duoc.atenasapi.entity.Comuna;

public record ComunaResponse(Long id, String nombre, Long regionId) {

    public static ComunaResponse desde(Comuna c) {
        return new ComunaResponse(c.getId(), c.getNombre(), c.getRegion().getId());
    }
}
