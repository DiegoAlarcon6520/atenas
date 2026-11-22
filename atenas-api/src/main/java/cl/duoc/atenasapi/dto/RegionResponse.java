package cl.duoc.atenasapi.dto;

import cl.duoc.atenasapi.entity.Region;

public record RegionResponse(Long id, String nombre) {

    public static RegionResponse desde(Region r) {
        return new RegionResponse(r.getId(), r.getNombre());
    }
}
