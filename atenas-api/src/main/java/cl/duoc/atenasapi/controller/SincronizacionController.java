package cl.duoc.atenasapi.controller;

import cl.duoc.atenasapi.dto.SincronizacionResponse;
import cl.duoc.atenasapi.service.SincronizacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sincronizacion")
@Tag(name = "Sincronización")
public class SincronizacionController {

    private final SincronizacionService servicio;

    public SincronizacionController(SincronizacionService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(
        summary = "Copia completa para Room",
        description = "Todo lo que la app guarda para trabajar sin conexión. El ejecutivo recibe solo su cartera; el supervisor, la de todos."
    )
    public SincronizacionResponse instantanea(@RequestParam Long usuarioId) {
        return servicio.instantanea(usuarioId);
    }
}
