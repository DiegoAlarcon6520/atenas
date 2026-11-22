package cl.duoc.atenasapi.service;

import cl.duoc.atenasapi.domain.ReglasNegocio;
import cl.duoc.atenasapi.dto.LoginRequest;
import cl.duoc.atenasapi.dto.UsuarioResponse;
import cl.duoc.atenasapi.entity.Usuario;
import cl.duoc.atenasapi.exception.CredencialesInvalidasException;
import cl.duoc.atenasapi.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU-01: inicio de sesión contra el servidor. */
@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder cifrador;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder cifrador) {
        this.usuarios = usuarios;
        this.cifrador = cifrador;
    }

    /**
     * Devuelve el usuario si las credenciales son correctas y la cuenta está
     * activa. Ante cualquier falla responde lo mismo, para no revelar si el
     * usuario existe.
     */
    public UsuarioResponse iniciarSesion(LoginRequest solicitud) {
        String nombreUsuario = ReglasNegocio.normalizarUsuario(solicitud.nombreUsuario());
        Usuario usuario = usuarios.findByNombreUsuario(nombreUsuario)
            .filter(Usuario::isActivo)
            .filter(u -> cifrador.matches(solicitud.clave(), u.getClaveHash()))
            .orElseThrow(CredencialesInvalidasException::new);
        return UsuarioResponse.desde(usuario);
    }
}
