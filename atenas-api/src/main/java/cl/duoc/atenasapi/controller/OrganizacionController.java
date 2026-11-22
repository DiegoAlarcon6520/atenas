package cl.duoc.atenasapi.controller;

import cl.duoc.atenasapi.dto.OrganizacionRequest;
import cl.duoc.atenasapi.dto.OrganizacionResponse;
import cl.duoc.atenasapi.service.OrganizacionService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizaciones")
@Tag(name = "Organizaciones")
public class OrganizacionController {

    private final OrganizacionService servicio;

    public OrganizacionController(OrganizacionService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<OrganizacionResponse> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public OrganizacionResponse obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizacionResponse crear(@Valid @RequestBody OrganizacionRequest solicitud) {
        return servicio.crear(solicitud);
    }

    @PutMapping("/{id}")
    public OrganizacionResponse actualizar(@PathVariable Long id, @Valid @RequestBody OrganizacionRequest solicitud) {
        return servicio.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina una organización", description = "409 si tiene oportunidades. Si no, se elimina junto con sus contactos.")
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
