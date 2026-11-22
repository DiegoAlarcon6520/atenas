package cl.duoc.atenasapi.service;

import cl.duoc.atenasapi.dto.ComunaResponse;
import cl.duoc.atenasapi.dto.RegionResponse;
import cl.duoc.atenasapi.exception.RecursoNoEncontradoException;
import cl.duoc.atenasapi.repository.ComunaRepository;
import cl.duoc.atenasapi.repository.RegionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regiones y comunas. Es un catálogo fijo que los usuarios solo consultan,
 * por eso no expone altas, ediciones ni borrados.
 */
@Service
@Transactional(readOnly = true)
public class CatalogoService {

    private final RegionRepository regiones;
    private final ComunaRepository comunas;

    public CatalogoService(RegionRepository regiones, ComunaRepository comunas) {
        this.regiones = regiones;
        this.comunas = comunas;
    }

    public List<RegionResponse> listarRegiones() {
        return regiones.findAllByOrderByNombreAsc().stream().map(RegionResponse::desde).toList();
    }

    public RegionResponse obtenerRegion(Long id) {
        return regiones.findById(id).map(RegionResponse::desde)
            .orElseThrow(() -> new RecursoNoEncontradoException("La región " + id + " no existe"));
    }

    public List<ComunaResponse> listarComunas(Long regionId) {
        var lista = regionId == null
            ? comunas.findAllByOrderByNombreAsc()
            : comunas.findByRegionIdOrderByNombreAsc(regionId);
        return lista.stream().map(ComunaResponse::desde).toList();
    }

    public ComunaResponse obtenerComuna(Long id) {
        return comunas.findById(id).map(ComunaResponse::desde)
            .orElseThrow(() -> new RecursoNoEncontradoException("La comuna " + id + " no existe"));
    }
}
