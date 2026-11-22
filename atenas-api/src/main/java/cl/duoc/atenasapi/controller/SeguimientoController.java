package cl.duoc.atenasapi.controller;

import cl.duoc.atenasapi.dto.SeguimientoResponse;
import cl.duoc.atenasapi.service.SeguimientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consulta de gestiones. Se crean desde POST /api/oportunidades/{id}/seguimientos
 * y no tienen edición ni borrado: el historial no se reescribe.
 */
@RestController
@RequestMapping("/api/seguimientos")
@Tag(name = "Gestiones de seguimiento")
public class SeguimientoController {

    private final SeguimientoService servicio;

    public SeguimientoController(SeguimientoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Lista las gestiones", description = "Con ?responsableId=1 devuelve solo las de la cartera de ese ejecutivo.")
    public List<SeguimientoResponse> listar(@RequestParam(required = false) Long responsableId) {
        return servicio.listar(responsableId);
    }

    @GetMapping("/{id}")
    public SeguimientoResponse obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }
}
