package cl.duoc.atenasapi.service;

import cl.duoc.atenasapi.domain.AfinidadPublico;
import cl.duoc.atenasapi.domain.EstadoOportunidad;
import cl.duoc.atenasapi.domain.Fechas;
import cl.duoc.atenasapi.domain.PotencialComercial;
import cl.duoc.atenasapi.domain.ReglasNegocio;
import cl.duoc.atenasapi.domain.RolUsuario;
import cl.duoc.atenasapi.domain.TransicionesEstado;
import cl.duoc.atenasapi.dto.AsignacionRequest;
import cl.duoc.atenasapi.dto.OportunidadRequest;
import cl.duoc.atenasapi.dto.OportunidadResponse;
import cl.duoc.atenasapi.entity.Contacto;
import cl.duoc.atenasapi.entity.Oportunidad;
import cl.duoc.atenasapi.entity.Organizacion;
import cl.duoc.atenasapi.entity.Usuario;
import cl.duoc.atenasapi.exception.AccionNoPermitidaException;
import cl.duoc.atenasapi.exception.ConflictoException;
import cl.duoc.atenasapi.exception.RecursoNoEncontradoException;
import cl.duoc.atenasapi.exception.SolicitudInvalidaException;
import cl.duoc.atenasapi.repository.EvidenciaRepository;
import cl.duoc.atenasapi.repository.OportunidadRepository;
import cl.duoc.atenasapi.repository.SeguimientoRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU-05 a HU-08: registrar, editar, consultar y asignar oportunidades. */
@Service
@Transactional(readOnly = true)
public class OportunidadService {

    private final OportunidadRepository oportunidades;
    private final SeguimientoRepository seguimientos;
    private final EvidenciaRepository evidencias;
    private final Referencias referencias;

    public OportunidadService(
        OportunidadRepository oportunidades,
        SeguimientoRepository seguimientos,
        EvidenciaRepository evidencias,
        Referencias referencias
    ) {
        this.oportunidades = oportunidades;
        this.seguimientos = seguimientos;
        this.evidencias = evidencias;
        this.referencias = referencias;
    }

    /** Con responsableId, solo la cartera de ese ejecutivo; sin él, todas. */
    public List<OportunidadResponse> listar(Long responsableId) {
        List<Oportunidad> lista = responsableId == null
            ? oportunidades.findAllByOrderByFechaInicioEventoAsc()
            : oportunidades.findByResponsableIdOrderByFechaInicioEventoAsc(responsableId);
        return lista.stream().map(OportunidadResponse::desde).toList();
    }

    public OportunidadResponse obtener(Long id) {
        return OportunidadResponse.desde(buscar(id));
    }

    /**
     * Toda oportunidad nueva entra al inicio del embudo. Lo que crea un
     * ejecutivo queda a su cargo; lo que crea el supervisor puede quedar sin
     * responsable para repartirlo después.
     */
    @Transactional
    public OportunidadResponse crear(OportunidadRequest solicitud) {
        Usuario creador = referencias.usuarioActivo(solicitud.creadoPorId(), "creadoPorId");
        Usuario responsable = creador.getRol() == RolUsuario.EJECUTIVO
            ? creador
            : ejecutivoOpcional(solicitud.responsableId());

        long ahora = Fechas.ahora();
        Oportunidad oportunidad = new Oportunidad();
        oportunidad.setCreadoPor(creador);
        oportunidad.setResponsable(responsable);
        oportunidad.setEstado(EstadoOportunidad.IDENTIFICADA);
        oportunidad.setFechaCreacion(ahora);
        aplicar(oportunidad, solicitud, ahora);
        return OportunidadResponse.desde(oportunidades.save(oportunidad));
    }

    /** Edita los datos del evento. No cambia estado, responsable ni creador. */
    @Transactional
    public OportunidadResponse actualizar(Long id, OportunidadRequest solicitud) {
        Oportunidad oportunidad = buscar(id);
        exigirQueAdmitaCambios(oportunidad);
        aplicar(oportunidad, solicitud, Fechas.ahora());
        return OportunidadResponse.desde(oportunidad);
    }

    /** HU-08: solo el supervisor reparte la cartera. */
    @Transactional
    public OportunidadResponse asignarResponsable(Long id, AsignacionRequest solicitud) {
        Oportunidad oportunidad = buscar(id);
        Usuario quienAsigna = referencias.usuarioActivo(solicitud.usuarioId(), "usuarioId");
        if (quienAsigna.getRol() != RolUsuario.SUPERVISOR) {
            throw new AccionNoPermitidaException("Solo el supervisor comercial puede asignar responsables");
        }
        exigirQueAdmitaCambios(oportunidad);
        oportunidad.setResponsable(ejecutivoOpcional(solicitud.responsableId()));
        oportunidad.setFechaActualizacion(Fechas.ahora());
        return OportunidadResponse.desde(oportunidad);
    }

    /**
     * Solo se puede eliminar una oportunidad sin gestiones: si ya tiene
     * historial, se descarta. Sus fotos se eliminan con ella.
     */
    @Transactional
    public void eliminar(Long id) {
        Oportunidad oportunidad = buscar(id);
        if (seguimientos.existsByOportunidadId(id)) {
            throw new ConflictoException("La oportunidad tiene gestiones registradas: descártala en vez de eliminarla");
        }
        evidencias.deleteByOportunidadId(id);
        oportunidades.delete(oportunidad);
    }

    private void aplicar(Oportunidad oportunidad, OportunidadRequest solicitud, long ahora) {
        Map<String, String> errores = ReglasNegocio.validarEvento(
            solicitud.fechaDeteccion(),
            solicitud.fechaInicioEvento(),
            solicitud.fechaTerminoEvento(),
            solicitud.horaInicio(),
            solicitud.horaTermino()
        );
        if (!errores.isEmpty()) {
            throw new SolicitudInvalidaException("Revisa las fechas y horarios del evento", errores);
        }

        Organizacion organizacion = referencias.organizacion(solicitud.organizacionId());
        Contacto contacto = solicitud.contactoId() == null ? null : referencias.contacto(solicitud.contactoId());
        if (contacto != null && !contacto.getOrganizacion().getId().equals(organizacion.getId())) {
            String mensaje = "El contacto no pertenece a la organización elegida";
            throw new SolicitudInvalidaException(mensaje, Map.of("contactoId", mensaje));
        }

        oportunidad.setNombre(solicitud.nombre().trim());
        oportunidad.setTipo(solicitud.tipo());
        oportunidad.setDescripcion(solicitud.descripcion().trim());
        oportunidad.setFuenteInformacion(solicitud.fuenteInformacion().trim());
        oportunidad.setFechaDeteccion(solicitud.fechaDeteccion());
        oportunidad.setFechaInicioEvento(solicitud.fechaInicioEvento());
        oportunidad.setFechaTerminoEvento(solicitud.fechaTerminoEvento());
        oportunidad.setHoraInicio(solicitud.horaInicio());
        oportunidad.setHoraTermino(solicitud.horaTermino());
        oportunidad.setDireccion(solicitud.direccion().trim());
        oportunidad.setComuna(referencias.comuna(solicitud.comunaId()));
        oportunidad.setOrganizacion(organizacion);
        oportunidad.setContacto(contacto);
        oportunidad.setPublicoObjetivo(solicitud.publicoObjetivo().trim());
        oportunidad.setPublicoEsperado(solicitud.publicoEsperado());
        oportunidad.setPrioridad(solicitud.prioridad());
        oportunidad.setPotencial(solicitud.potencial() == null ? PotencialComercial.MEDIO : solicitud.potencial());
        oportunidad.setAfinidad(solicitud.afinidad() == null ? AfinidadPublico.MEDIA : solicitud.afinidad());
        // Algunas participaciones no tienen costo: vacío se guarda como 0.
        oportunidad.setCostoReferencial(solicitud.costoReferencial() == null ? 0.0 : solicitud.costoReferencial());
        oportunidad.setCondiciones(ReglasNegocio.textoOpcional(solicitud.condiciones()));
        oportunidad.setRequisitos(ReglasNegocio.textoOpcional(solicitud.requisitos()));
        oportunidad.setEspaciosDisponibles(ReglasNegocio.textoOpcional(solicitud.espaciosDisponibles()));
        oportunidad.setObservaciones(ReglasNegocio.textoOpcional(solicitud.observaciones()));
        oportunidad.setFechaActualizacion(ahora);
    }

    /** La cartera se reparte entre ejecutivos activos; null deja la oportunidad sin responsable. */
    private Usuario ejecutivoOpcional(Long responsableId) {
        if (responsableId == null) {
            return null;
        }
        Usuario responsable = referencias.usuarioActivo(responsableId, "responsableId");
        if (responsable.getRol() != RolUsuario.EJECUTIVO) {
            String mensaje = "El responsable debe ser un ejecutivo comercial";
            throw new SolicitudInvalidaException(mensaje, Map.of("responsableId", mensaje));
        }
        return responsable;
    }

    private void exigirQueAdmitaCambios(Oportunidad oportunidad) {
        if (!TransicionesEstado.admiteCambios(oportunidad.getEstado())) {
            throw new ConflictoException(TransicionesEstado.MENSAJE_CERRADA);
        }
    }

    private Oportunidad buscar(Long id) {
        return oportunidades.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("La oportunidad " + id + " no existe"));
    }
}
