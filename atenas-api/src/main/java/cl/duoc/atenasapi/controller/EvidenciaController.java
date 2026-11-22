package cl.duoc.atenasapi.controller;

import cl.duoc.atenasapi.dto.EvidenciaRequest;
import cl.duoc.atenasapi.dto.EvidenciaResponse;
import cl.duoc.atenasapi.service.EvidenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/evidencias")
@Tag(name = "Evidencias")
public class EvidenciaController {

    private final EvidenciaService servicio;

    public EvidenciaController(EvidenciaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Lista las evidencias", description = "Con ?oportunidadId=1 devuelve solo las de esa oportunidad.")
    public List<EvidenciaResponse> listar(@RequestParam(required = false) Long oportunidadId) {
        return servicio.listar(oportunidadId);
    }

    @GetMapping("/{id}")
    public EvidenciaResponse obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EvidenciaResponse crear(@Valid @RequestBody EvidenciaRequest solicitud) {
        return servicio.crear(solicitud);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edita la descripción de una evidencia", description = "La foto y su oportunidad no cambian.")
    public EvidenciaResponse actualizar(@PathVariable Long id, @Valid @RequestBody EvidenciaRequest solicitud) {
        return servicio.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
