package cl.duoc.atenasapi.service;

import cl.duoc.atenasapi.domain.Fechas;
import cl.duoc.atenasapi.domain.ReglasNegocio;
import cl.duoc.atenasapi.domain.RolUsuario;
import cl.duoc.atenasapi.dto.UsuarioRequest;
import cl.duoc.atenasapi.dto.UsuarioResponse;
import cl.duoc.atenasapi.entity.Usuario;
import cl.duoc.atenasapi.exception.ConflictoException;
import cl.duoc.atenasapi.exception.RecursoNoEncontradoException;
import cl.duoc.atenasapi.exception.SolicitudInvalidaException;
import cl.duoc.atenasapi.repository.OportunidadRepository;
import cl.duoc.atenasapi.repository.SeguimientoRepository;
import cl.duoc.atenasapi.repository.UsuarioRepository;
import java.util.List;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final OportunidadRepository oportunidades;
    private final SeguimientoRepository seguimientos;
    private final PasswordEncoder cifrador;

    public UsuarioService(
        UsuarioRepository usuarios,
        OportunidadRepository oportunidades,
        SeguimientoRepository seguimientos,
        PasswordEncoder cifrador
    ) {
        this.usuarios = usuarios;
        this.oportunidades = oportunidades;
        this.seguimientos = seguimientos;
        this.cifrador = cifrador;
    }

    public List<UsuarioResponse> listar(RolUsuario rol) {
        List<Usuario> lista = rol == null
            ? usuarios.findAllByOrderByNombreCompletoAsc()
            : usuarios.findByRolOrderByNombreCompletoAsc(rol);
        return lista.stream().map(UsuarioResponse::desde).toList();
    }

    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.desde(buscar(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest solicitud) {
        if (solicitud.clave() == null) {
            String mensaje = "La contraseña es obligatoria";
            throw new SolicitudInvalidaException(mensaje, Map.of("clave", mensaje));
        }
        String nombreUsuario = ReglasNegocio.normalizarUsuario(solicitud.nombreUsuario());
        if (usuarios.existsByNombreUsuario(nombreUsuario)) {
            throw new ConflictoException("Ya existe el usuario " + nombreUsuario);
        }
        long ahora = Fechas.ahora();
        Usuario usuario = new Usuario();
        usuario.setFechaCreacion(ahora);
        aplicar(usuario, solicitud, nombreUsuario, ahora);
        return UsuarioResponse.desde(usuarios.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest solicitud) {
        Usuario usuario = buscar(id);
        String nombreUsuario = ReglasNegocio.normalizarUsuario(solicitud.nombreUsuario());
        boolean cambiaNombre = !nombreUsuario.equals(usuario.getNombreUsuario());
        if (cambiaNombre && usuarios.existsByNombreUsuario(nombreUsuario)) {
            throw new ConflictoException("Ya existe el usuario " + nombreUsuario);
        }
        aplicar(usuario, solicitud, nombreUsuario, Fechas.ahora());
        return UsuarioResponse.desde(usuario);
    }

    /**
     * Un usuario con oportunidades o gestiones a su nombre no se borra: se
     * perdería quién hizo qué. Para que deje de operar se desactiva.
     */
    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = buscar(id);
        if (oportunidades.existsByResponsableIdOrCreadoPorId(id, id) || seguimientos.existsByUsuarioId(id)) {
            throw new ConflictoException(
                usuario.getNombreCompleto() + " tiene oportunidades o gestiones registradas: desactívalo en vez de eliminarlo"
            );
        }
        usuarios.delete(usuario);
    }

    private void aplicar(Usuario usuario, UsuarioRequest solicitud, String nombreUsuario, long ahora) {
        usuario.setNombreUsuario(nombreUsuario);
        if (solicitud.clave() != null) {
            usuario.setClaveHash(cifrador.encode(solicitud.clave()));
        }
        usuario.setNombreCompleto(solicitud.nombreCompleto().trim());
        usuario.setRol(solicitud.rol());
        usuario.setArea(solicitud.area().trim());
        usuario.setZona(solicitud.zona().trim());
        if (solicitud.activo() != null) {
            usuario.setActivo(solicitud.activo());
        }
        usuario.setFechaActualizacion(ahora);
    }

    private Usuario buscar(Long id) {
        return usuarios.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("El usuario " + id + " no existe"));
    }
}
