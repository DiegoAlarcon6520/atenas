package cl.duoc.atenasapi.controller;

import cl.duoc.atenasapi.dto.AsignacionRequest;
import cl.duoc.atenasapi.dto.OportunidadRequest;
import cl.duoc.atenasapi.dto.OportunidadResponse;
import cl.duoc.atenasapi.dto.SeguimientoRequest;
import cl.duoc.atenasapi.dto.SeguimientoResponse;
import cl.duoc.atenasapi.service.OportunidadService;
import cl.duoc.atenasapi.service.SeguimientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oportunidades")
@Tag(name = "Oportunidades")
public class OportunidadController {

    private final OportunidadService oportunidades;
    private final SeguimientoService seguimientos;

    public OportunidadController(OportunidadService oportunidades, SeguimientoService seguimientos) {
        this.oportunidades = oportunidades;
        this.seguimientos = seguimientos;
    }

    @GetMapping
    @Operation(summary = "Lista las oportunidades", description = "Con ?responsableId=1 devuelve solo la cartera de ese ejecutivo.")
    public List<OportunidadResponse> listar(@RequestParam(required = false) Long responsableId) {
        return oportunidades.listar(responsableId);
    }

    @GetMapping("/{id}")
    public OportunidadResponse obtener(@PathVariable Long id) {
        return oportunidades.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra una oportunidad", description = "Entra como IDENTIFICADA. Si la crea un ejecutivo, queda a su cargo.")
    public OportunidadResponse crear(@Valid @RequestBody OportunidadRequest solicitud) {
        return oportunidades.crear(solicitud);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edita los datos del evento", description = "No cambia estado ni responsable. 409 si la oportunidad está cerrada.")
    public OportunidadResponse actualizar(@PathVariable Long id, @Valid @RequestBody OportunidadRequest solicitud) {
        return oportunidades.actualizar(id, solicitud);
    }

    @PatchMapping("/{id}/responsable")
    @Operation(summary = "Asigna o quita el responsable", description = "Solo el supervisor (403 en otro caso). responsableId null deja la oportunidad sin asignar.")
    public OportunidadResponse asignarResponsable(@PathVariable Long id, @Valid @RequestBody AsignacionRequest solicitud) {
        return oportunidades.asignarResponsable(id, solicitud);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina una oportunidad", description = "409 si ya tiene gestiones: en ese caso se descarta registrando una gestión.")
    public void eliminar(@PathVariable Long id) {
        oportunidades.eliminar(id);
    }

    @GetMapping("/{id}/seguimientos")
    @Operation(summary = "Historial de gestiones", description = "De la más reciente a la más antigua.")
    public List<SeguimientoResponse> historial(@PathVariable Long id) {
        return seguimientos.historial(id);
    }

    @PostMapping("/{id}/seguimientos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Registra una gestión",
        description = "Guarda la gestión, cambia el estado y actualiza el último contacto en una sola transacción. "
            + "403 si el perfil no puede hacer ese cambio de estado; 409 si el salto no existe o la oportunidad está cerrada."
    )
    public SeguimientoResponse registrarGestion(@PathVariable Long id, @Valid @RequestBody SeguimientoRequest solicitud) {
        return seguimientos.registrar(id, solicitud);
    }
}
