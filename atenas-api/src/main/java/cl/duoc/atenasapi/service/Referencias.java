package cl.duoc.atenasapi.service;

import cl.duoc.atenasapi.entity.Comuna;
import cl.duoc.atenasapi.entity.Contacto;
import cl.duoc.atenasapi.entity.Oportunidad;
import cl.duoc.atenasapi.entity.Organizacion;
import cl.duoc.atenasapi.entity.Usuario;
import cl.duoc.atenasapi.exception.SolicitudInvalidaException;
import cl.duoc.atenasapi.repository.ComunaRepository;
import cl.duoc.atenasapi.repository.ContactoRepository;
import cl.duoc.atenasapi.repository.OportunidadRepository;
import cl.duoc.atenasapi.repository.OrganizacionRepository;
import cl.duoc.atenasapi.repository.UsuarioRepository;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Busca las entidades que un cuerpo JSON menciona por id (comunaId,
 * organizacionId, usuarioId...).
 *
 * Si el id no existe, el error es del dato enviado, así que responde 400 e
 * indica el campo. Es distinto de pedir en la URL un recurso que no existe,
 * que responde 404.
 */
@Component
public class Referencias {

    private final UsuarioRepository usuarios;
    private final ComunaRepository comunas;
    private final OrganizacionRepository organizaciones;
    private final ContactoRepository contactos;
    private final OportunidadRepository oportunidades;

    public Referencias(
        UsuarioRepository usuarios,
        ComunaRepository comunas,
        OrganizacionRepository organizaciones,
        ContactoRepository contactos,
        OportunidadRepository oportunidades
    ) {
        this.usuarios = usuarios;
        this.comunas = comunas;
        this.organizaciones = organizaciones;
        this.contactos = contactos;
        this.oportunidades = oportunidades;
    }

    public Usuario usuario(Long id, String campo) {
        return usuarios.findById(id).orElseThrow(() -> noExiste(campo, "El usuario " + id + " no existe"));
    }

    /** Un usuario desactivado ya no puede actuar en el sistema. */
    public Usuario usuarioActivo(Long id, String campo) {
        Usuario usuario = usuario(id, campo);
        if (!usuario.isActivo()) {
            throw noExiste(campo, "El usuario " + usuario.getNombreCompleto() + " está desactivado");
        }
        return usuario;
    }

    public Comuna comuna(Long id) {
        return comunas.findById(id).orElseThrow(() -> noExiste("comunaId", "La comuna " + id + " no existe"));
    }

    public Organizacion organizacion(Long id) {
        return organizaciones.findById(id)
            .orElseThrow(() -> noExiste("organizacionId", "La organización " + id + " no existe"));
    }

    public Contacto contacto(Long id) {
        return contactos.findById(id).orElseThrow(() -> noExiste("contactoId", "El contacto " + id + " no existe"));
    }

    public Oportunidad oportunidad(Long id) {
        return oportunidades.findById(id)
            .orElseThrow(() -> noExiste("oportunidadId", "La oportunidad " + id + " no existe"));
    }

    private static SolicitudInvalidaException noExiste(String campo, String mensaje) {
        return new SolicitudInvalidaException(mensaje, Map.of(campo, mensaje));
    }
}
