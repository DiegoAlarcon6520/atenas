package cl.duoc.atenasapi.service;

import cl.duoc.atenasapi.domain.EstadoOportunidad;
import cl.duoc.atenasapi.domain.Fechas;
import cl.duoc.atenasapi.domain.ReglasNegocio;
import cl.duoc.atenasapi.domain.RolUsuario;
import cl.duoc.atenasapi.domain.TransicionesEstado;
import cl.duoc.atenasapi.dto.SeguimientoRequest;
import cl.duoc.atenasapi.dto.SeguimientoResponse;
import cl.duoc.atenasapi.entity.Contacto;
import cl.duoc.atenasapi.entity.Oportunidad;
import cl.duoc.atenasapi.entity.Seguimiento;
import cl.duoc.atenasapi.entity.Usuario;
import cl.duoc.atenasapi.exception.AccionNoPermitidaException;
import cl.duoc.atenasapi.exception.ConflictoException;
import cl.duoc.atenasapi.exception.RecursoNoEncontradoException;
import cl.duoc.atenasapi.exception.SolicitudInvalidaException;
import cl.duoc.atenasapi.repository.OportunidadRepository;
import cl.duoc.atenasapi.repository.SeguimientoRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-09 a HU-11: gestiones de seguimiento.
 *
 * Registrar una gestión es la única forma de cambiar el estado de una
 * oportunidad. Así cada movimiento del embudo queda en el historial, con
 * fecha y autor. Las gestiones no se editan ni se borran.
 */
@Service
@Transactional(readOnly = true)
public class SeguimientoService {

    private final SeguimientoRepository seguimientos;
    private final OportunidadRepository oportunidades;
    private final Referencias referencias;

    public SeguimientoService(
        SeguimientoRepository seguimientos,
        OportunidadRepository oportunidades,
        Referencias referencias
    ) {
        this.seguimientos = seguimientos;
        this.oportunidades = oportunidades;
        this.referencias = referencias;
    }

    /** Historial de una oportunidad, de la gestión más reciente a la más antigua. */
    public List<SeguimientoResponse> historial(Long oportunidadId) {
        buscarOportunidad(oportunidadId);
        return seguimientos.findByOportunidadIdOrderByFechaAccionDescIdDesc(oportunidadId).stream()
            .map(SeguimientoResponse::desde)
            .toList();
    }

    /** Todas las gestiones, o solo las de la cartera de un ejecutivo. */
    public List<SeguimientoResponse> listar(Long responsableId) {
        List<Seguimiento> lista = responsableId == null
            ? seguimientos.findAllByOrderByFechaAccionDescIdDesc()
            : seguimientos.findByOportunidadResponsableIdOrderByFechaAccionDescIdDesc(responsableId);
        return lista.stream().map(SeguimientoResponse::desde).toList();
    }

    public SeguimientoResponse obtener(Long id) {
        return seguimientos.findById(id).map(SeguimientoResponse::desde)
            .orElseThrow(() -> new RecursoNoEncontradoException("La gestión " + id + " no existe"));
    }

    /**
     * Guarda la gestión, cambia el estado de la oportunidad y actualiza el
     * último contacto en una sola transacción: si una de las tres falla, no
     * se guarda ninguna y el historial nunca queda desfasado del embudo.
     */
    @Transactional
    public SeguimientoResponse registrar(Long oportunidadId, SeguimientoRequest solicitud) {
        Oportunidad oportunidad = buscarOportunidad(oportunidadId);
        Usuario usuario = referencias.usuarioActivo(solicitud.usuarioId(), "usuarioId");
        RolUsuario rol = usuario.getRol();

        // Un ejecutivo solo gestiona su propia cartera.
        boolean esSuya = oportunidad.getResponsable() != null
            && Objects.equals(oportunidad.getResponsable().getId(), usuario.getId());
        if (rol == RolUsuario.EJECUTIVO && !esSuya) {
            throw new AccionNoPermitidaException("Solo puedes registrar gestiones en oportunidades a tu cargo");
        }

        EstadoOportunidad actual = oportunidad.getEstado();
        if (!TransicionesEstado.admiteCambios(actual)) {
            throw new ConflictoException(TransicionesEstado.MENSAJE_CERRADA);
        }

        EstadoOportunidad destino = solicitud.estadoNuevo() == null ? actual : solicitud.estadoNuevo();
        if (!TransicionesEstado.esValida(actual, destino, rol)) {
            // Si el supervisor sí podría hacerlo, es un tema de permisos; si no, el salto no existe.
            if (TransicionesEstado.esValida(actual, destino, RolUsuario.SUPERVISOR)) {
                throw new AccionNoPermitidaException(
                    "Confirmar, descartar, cerrar o retroceder una oportunidad es una decisión del supervisor comercial"
                );
            }
            throw new ConflictoException("No se puede pasar de " + actual + " a " + destino);
        }

        boolean esCierre = TransicionesEstado.esEstadoDeCierre(destino);
        Map<String, String> errores = ReglasNegocio.validarGestion(
            solicitud.fechaAccion(),
            solicitud.proximaAccion(),
            solicitud.fechaProximoSeguimiento(),
            esCierre,
            Fechas.hoy()
        );
        if (!errores.isEmpty()) {
            throw new SolicitudInvalidaException("Revisa los campos marcados", errores);
        }

        Seguimiento gestion = new Seguimiento();
        gestion.setOportunidad(oportunidad);
        gestion.setUsuario(usuario);
        gestion.setTipoAccion(solicitud.tipoAccion());
        gestion.setFechaAccion(solicitud.fechaAccion());
        gestion.setResultado(solicitud.resultado().trim());
        // Una oportunidad cerrada no deja pendientes que aparezcan vencidos para siempre.
        gestion.setProximaAccion(esCierre ? null : ReglasNegocio.textoOpcional(solicitud.proximaAccion()));
        gestion.setFechaProximoSeguimiento(esCierre ? null : solicitud.fechaProximoSeguimiento());
        gestion.setEstadoAnterior(actual);
        gestion.setEstadoNuevo(destino);
        gestion.setObservaciones(ReglasNegocio.textoOpcional(solicitud.observaciones()));
        Seguimiento guardada = seguimientos.save(gestion);

        oportunidad.setEstado(destino);
        oportunidad.setFechaActualizacion(Fechas.ahora());

        // Solo una conversación con la organización cuenta como contacto.
        Contacto contacto = oportunidad.getContacto();
        if (contacto != null && solicitud.tipoAccion().implicaContacto()) {
            contacto.setFechaUltimoContacto(solicitud.fechaAccion());
        }
        return SeguimientoResponse.desde(guardada);
    }

    private Oportunidad buscarOportunidad(Long id) {
        return oportunidades.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("La oportunidad " + id + " no existe"));
    }
}
