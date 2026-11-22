package cl.duoc.atenasapi.controller;

import cl.duoc.atenasapi.dto.ContactoRequest;
import cl.duoc.atenasapi.dto.ContactoResponse;
import cl.duoc.atenasapi.service.ContactoService;
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
@RequestMapping("/api/contactos")
@Tag(name = "Contactos")
public class ContactoController {

    private final ContactoService servicio;

    public ContactoController(ContactoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Lista los contactos", description = "Con ?organizacionId=1 devuelve solo los de esa organización.")
    public List<ContactoResponse> listar(@RequestParam(required = false) Long organizacionId) {
        return servicio.listar(organizacionId);
    }

    @GetMapping("/{id}")
    public ContactoResponse obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContactoResponse crear(@Valid @RequestBody ContactoRequest solicitud) {
        return servicio.crear(solicitud);
    }

    @PutMapping("/{id}")
    public ContactoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ContactoRequest solicitud) {
        return servicio.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina un contacto", description = "409 si está asignado a oportunidades: en ese caso se desactiva con PUT.")
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
