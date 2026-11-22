package cl.duoc.atenasapi.service;

import cl.duoc.atenasapi.domain.Fechas;
import cl.duoc.atenasapi.domain.ReglasNegocio;
import cl.duoc.atenasapi.dto.OrganizacionRequest;
import cl.duoc.atenasapi.dto.OrganizacionResponse;
import cl.duoc.atenasapi.entity.Organizacion;
import cl.duoc.atenasapi.exception.ConflictoException;
import cl.duoc.atenasapi.exception.RecursoNoEncontradoException;
import cl.duoc.atenasapi.repository.ContactoRepository;
import cl.duoc.atenasapi.repository.OportunidadRepository;
import cl.duoc.atenasapi.repository.OrganizacionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Organizaciones que organizan eventos. */
@Service
@Transactional(readOnly = true)
public class OrganizacionService {

    private final OrganizacionRepository organizaciones;
    private final ContactoRepository contactos;
    private final OportunidadRepository oportunidades;
    private final Referencias referencias;

    public OrganizacionService(
        OrganizacionRepository organizaciones,
        ContactoRepository contactos,
        OportunidadRepository oportunidades,
        Referencias referencias
    ) {
        this.organizaciones = organizaciones;
        this.contactos = contactos;
        this.oportunidades = oportunidades;
        this.referencias = referencias;
    }

    public List<OrganizacionResponse> listar() {
        return organizaciones.findAllByOrderByNombreAsc().stream().map(OrganizacionResponse::desde).toList();
    }

    public OrganizacionResponse obtener(Long id) {
        return OrganizacionResponse.desde(buscar(id));
    }

    @Transactional
    public OrganizacionResponse crear(OrganizacionRequest solicitud) {
        Organizacion organizacion = new Organizacion();
        organizacion.setFechaCreacion(Fechas.ahora());
        aplicar(organizacion, solicitud);
        return OrganizacionResponse.desde(organizaciones.save(organizacion));
    }

    @Transactional
    public OrganizacionResponse actualizar(Long id, OrganizacionRequest solicitud) {
        Organizacion organizacion = buscar(id);
        aplicar(organizacion, solicitud);
        return OrganizacionResponse.desde(organizacion);
    }

    /**
     * Una organización con oportunidades no se borra: se perdería el historial
     * comercial. Si no tiene ninguna, se elimina junto con sus contactos.
     */
    @Transactional
    public void eliminar(Long id) {
        Organizacion organizacion = buscar(id);
        if (oportunidades.existsByOrganizacionId(id)) {
            throw new ConflictoException(organizacion.getNombre() + " tiene oportunidades registradas y no se puede eliminar");
        }
        contactos.deleteByOrganizacionId(id);
        organizaciones.delete(organizacion);
    }

    private void aplicar(Organizacion organizacion, OrganizacionRequest solicitud) {
        organizacion.setNombre(solicitud.nombre().trim());
        organizacion.setTipo(solicitud.tipo());
        organizacion.setRubro(solicitud.rubro().trim());
        organizacion.setComuna(referencias.comuna(solicitud.comunaId()));
        organizacion.setSitioWeb(ReglasNegocio.textoOpcional(solicitud.sitioWeb()));
        organizacion.setPublicoAsociado(ReglasNegocio.textoOpcional(solicitud.publicoAsociado()));
        organizacion.setObservaciones(ReglasNegocio.textoOpcional(solicitud.observaciones()));
    }

    private Organizacion buscar(Long id) {
        return organizaciones.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("La organización " + id + " no existe"));
    }
}
