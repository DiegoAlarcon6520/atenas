package cl.duoc.atenasapi.controller;

import cl.duoc.atenasapi.domain.RolUsuario;
import cl.duoc.atenasapi.dto.UsuarioRequest;
import cl.duoc.atenasapi.dto.UsuarioResponse;
import cl.duoc.atenasapi.service.UsuarioService;
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
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios")
public class UsuarioController {

    private final UsuarioService servicio;

    public UsuarioController(UsuarioService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Lista los usuarios", description = "Con ?rol=EJECUTIVO devuelve solo los ejecutivos, para asignar responsables.")
    public List<UsuarioResponse> listar(@RequestParam(required = false) RolUsuario rol) {
        return servicio.listar(rol);
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@Valid @RequestBody UsuarioRequest solicitud) {
        return servicio.crear(solicitud);
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest solicitud) {
        return servicio.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina un usuario", description = "409 si tiene oportunidades o gestiones: en ese caso se desactiva con PUT.")
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
