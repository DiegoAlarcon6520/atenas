package cl.duoc.atenasapi.controller;

import cl.duoc.atenasapi.dto.LoginRequest;
import cl.duoc.atenasapi.dto.UsuarioResponse;
import cl.duoc.atenasapi.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación")
public class AuthController {

    private final AuthService servicio;

    public AuthController(AuthService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/login")
    @Operation(summary = "Inicia sesión", description = "Devuelve el usuario con su rol, o 401 si el usuario o la clave no coinciden.")
    public UsuarioResponse login(@Valid @RequestBody LoginRequest solicitud) {
        return servicio.iniciarSesion(solicitud);
    }
}
