package cl.duoc.atenasapi.service;

import cl.duoc.atenasapi.domain.ReglasNegocio;
import cl.duoc.atenasapi.dto.ContactoRequest;
import cl.duoc.atenasapi.dto.ContactoResponse;
import cl.duoc.atenasapi.entity.Contacto;
import cl.duoc.atenasapi.exception.ConflictoException;
import cl.duoc.atenasapi.exception.RecursoNoEncontradoException;
import cl.duoc.atenasapi.repository.ContactoRepository;
import cl.duoc.atenasapi.repository.OportunidadRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Contactos de cada organización. */
@Service
@Transactional(readOnly = true)
public class ContactoService {

    private final ContactoRepository contactos;
    private final OportunidadRepository oportunidades;
    private final Referencias referencias;

    public ContactoService(ContactoRepository contactos, OportunidadRepository oportunidades, Referencias referencias) {
        this.contactos = contactos;
        this.oportunidades = oportunidades;
        this.referencias = referencias;
    }

    public List<ContactoResponse> listar(Long organizacionId) {
        var lista = organizacionId == null
            ? contactos.findAllByOrderByNombreAsc()
            : contactos.findByOrganizacionIdOrderByNombreAsc(organizacionId);
        return lista.stream().map(ContactoResponse::desde).toList();
    }

    public ContactoResponse obtener(Long id) {
        return ContactoResponse.desde(buscar(id));
    }

    @Transactional
    public ContactoResponse crear(ContactoRequest solicitud) {
        Contacto contacto = new Contacto();
        // Aún no se ha hablado con él: la fecha la pone la primera gestión real.
        contacto.setFechaUltimoContacto(null);
        aplicar(contacto, solicitud);
        return ContactoResponse.desde(contactos.save(contacto));
    }

    @Transactional
    public ContactoResponse actualizar(Long id, ContactoRequest solicitud) {
        Contacto contacto = buscar(id);
        aplicar(contacto, solicitud);
        return ContactoResponse.desde(contacto);
    }

    /**
     * Un contacto asignado a oportunidades no se borra, porque dejaría esas
     * oportunidades sin interlocutor en su historial. Se desactiva.
     */
    @Transactional
    public void eliminar(Long id) {
        Contacto contacto = buscar(id);
        if (oportunidades.existsByContactoId(id)) {
            throw new ConflictoException(
                contacto.getNombre() + " está asignado a oportunidades: desactívalo en vez de eliminarlo"
            );
        }
        contactos.delete(contacto);
    }

    private void aplicar(Contacto contacto, ContactoRequest solicitud) {
        contacto.setOrganizacion(referencias.organizacion(solicitud.organizacionId()));
        contacto.setNombre(solicitud.nombre().trim());
        contacto.setCargo(solicitud.cargo().trim());
        contacto.setArea(solicitud.area());
        contacto.setCorreo(solicitud.correo().trim());
        contacto.setTelefono(solicitud.telefono().trim());
        contacto.setMedioPreferente(solicitud.medioPreferente());
        if (solicitud.activo() != null) {
            contacto.setActivo(solicitud.activo());
        }
        contacto.setObservaciones(ReglasNegocio.textoOpcional(solicitud.observaciones()));
    }

    private Contacto buscar(Long id) {
        return contactos.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("El contacto " + id + " no existe"));
    }
}
