package cl.duoc.atenasapi.controller;

import cl.duoc.atenasapi.dto.ComunaResponse;
import cl.duoc.atenasapi.dto.RegionResponse;
import cl.duoc.atenasapi.service.CatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Catálogo de solo lectura: regiones y comunas no las administra el usuario. */
@RestController
@RequestMapping("/api")
@Tag(name = "Catálogo de ubicación")
public class CatalogoController {

    private final CatalogoService servicio;

    public CatalogoController(CatalogoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/regiones")
    public List<RegionResponse> regiones() {
        return servicio.listarRegiones();
    }

    @GetMapping("/regiones/{id}")
    public RegionResponse region(@PathVariable Long id) {
        return servicio.obtenerRegion(id);
    }

    @GetMapping("/comunas")
    @Operation(summary = "Lista las comunas", description = "Con ?regionId=1 devuelve solo las de esa región.")
    public List<ComunaResponse> comunas(@RequestParam(required = false) Long regionId) {
        return servicio.listarComunas(regionId);
    }

    @GetMapping("/comunas/{id}")
    public ComunaResponse comuna(@PathVariable Long id) {
        return servicio.obtenerComuna(id);
    }
}
