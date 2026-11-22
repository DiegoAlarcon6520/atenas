package cl.duoc.atenasapi.service;

import cl.duoc.atenasapi.domain.Fechas;
import cl.duoc.atenasapi.domain.RolUsuario;
import cl.duoc.atenasapi.dto.ComunaResponse;
import cl.duoc.atenasapi.dto.ContactoResponse;
import cl.duoc.atenasapi.dto.EvidenciaResponse;
import cl.duoc.atenasapi.dto.OportunidadResponse;
import cl.duoc.atenasapi.dto.OrganizacionResponse;
import cl.duoc.atenasapi.dto.RegionResponse;
import cl.duoc.atenasapi.dto.SeguimientoResponse;
import cl.duoc.atenasapi.dto.SincronizacionResponse;
import cl.duoc.atenasapi.dto.UsuarioResponse;
import cl.duoc.atenasapi.entity.Usuario;
import cl.duoc.atenasapi.repository.ComunaRepository;
import cl.duoc.atenasapi.repository.ContactoRepository;
import cl.duoc.atenasapi.repository.EvidenciaRepository;
import cl.duoc.atenasapi.repository.OportunidadRepository;
import cl.duoc.atenasapi.repository.OrganizacionRepository;
import cl.duoc.atenasapi.repository.RegionRepository;
import cl.duoc.atenasapi.repository.SeguimientoRepository;
import cl.duoc.atenasapi.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Copia completa de los datos para guardar en Room.
 *
 * La app la pide al iniciar sesión y al volver a tener conexión, y reemplaza
 * su copia local. Las consultas, filtros y alertas siguen calculándose en el
 * teléfono, así la app funciona igual con o sin conexión.
 */
@Service
@Transactional(readOnly = true)
public class SincronizacionService {

    private final Referencias referencias;
    private final UsuarioRepository usuarios;
    private final RegionRepository regiones;
    private final ComunaRepository comunas;
    private final OrganizacionRepository organizaciones;
    private final ContactoRepository contactos;
    private final OportunidadRepository oportunidades;
    private final SeguimientoRepository seguimientos;
    private final EvidenciaRepository evidencias;

    public SincronizacionService(
        Referencias referencias,
        UsuarioRepository usuarios,
        RegionRepository regiones,
        ComunaRepository comunas,
        OrganizacionRepository organizaciones,
        ContactoRepository contactos,
        OportunidadRepository oportunidades,
        SeguimientoRepository seguimientos,
        EvidenciaRepository evidencias
    ) {
        this.referencias = referencias;
        this.usuarios = usuarios;
        this.regiones = regiones;
        this.comunas = comunas;
        this.organizaciones = organizaciones;
        this.contactos = contactos;
        this.oportunidades = oportunidades;
        this.seguimientos = seguimientos;
        this.evidencias = evidencias;
    }

    /**
     * Organizaciones, contactos y catálogos son compartidos por todo el
     * equipo. Las oportunidades, su historial y sus fotos dependen del
     * perfil: el ejecutivo recibe solo su cartera.
     */
    public SincronizacionResponse instantanea(Long usuarioId) {
        Usuario usuario = referencias.usuarioActivo(usuarioId, "usuarioId");
        Long alcance = usuario.getRol() == RolUsuario.SUPERVISOR ? null : usuario.getId();

        var listaOportunidades = alcance == null
            ? oportunidades.findAllByOrderByFechaInicioEventoAsc()
            : oportunidades.findByResponsableIdOrderByFechaInicioEventoAsc(alcance);
        var listaSeguimientos = alcance == null
            ? seguimientos.findAllByOrderByFechaAccionDescIdDesc()
            : seguimientos.findByOportunidadResponsableIdOrderByFechaAccionDescIdDesc(alcance);
        var listaEvidencias = alcance == null
            ? evidencias.findAllByOrderByFechaCapturaDesc()
            : evidencias.findByOportunidadResponsableIdOrderByFechaCapturaDesc(alcance);

        return new SincronizacionResponse(
            Fechas.ahora(),
            usuarios.findAllByOrderByNombreCompletoAsc().stream().map(UsuarioResponse::desde).toList(),
            regiones.findAllByOrderByNombreAsc().stream().map(RegionResponse::desde).toList(),
            comunas.findAllByOrderByNombreAsc().stream().map(ComunaResponse::desde).toList(),
            organizaciones.findAllByOrderByNombreAsc().stream().map(OrganizacionResponse::desde).toList(),
            contactos.findAllByOrderByNombreAsc().stream().map(ContactoResponse::desde).toList(),
            listaOportunidades.stream().map(OportunidadResponse::desde).toList(),
            listaSeguimientos.stream().map(SeguimientoResponse::desde).toList(),
            listaEvidencias.stream().map(EvidenciaResponse::desde).toList()
        );
    }
}
