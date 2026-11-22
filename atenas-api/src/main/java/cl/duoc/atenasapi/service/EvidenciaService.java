package cl.duoc.atenasapi.service;

import cl.duoc.atenasapi.domain.ReglasNegocio;
import cl.duoc.atenasapi.dto.EvidenciaRequest;
import cl.duoc.atenasapi.dto.EvidenciaResponse;
import cl.duoc.atenasapi.entity.Evidencia;
import cl.duoc.atenasapi.exception.RecursoNoEncontradoException;
import cl.duoc.atenasapi.repository.EvidenciaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registro de las fotos tomadas en terreno. */
@Service
@Transactional(readOnly = true)
public class EvidenciaService {

    private final EvidenciaRepository evidencias;
    private final Referencias referencias;

    public EvidenciaService(EvidenciaRepository evidencias, Referencias referencias) {
        this.evidencias = evidencias;
        this.referencias = referencias;
    }

    public List<EvidenciaResponse> listar(Long oportunidadId) {
        var lista = oportunidadId == null
            ? evidencias.findAllByOrderByFechaCapturaDesc()
            : evidencias.findByOportunidadIdOrderByFechaCapturaDesc(oportunidadId);
        return lista.stream().map(EvidenciaResponse::desde).toList();
    }

    public EvidenciaResponse obtener(Long id) {
        return EvidenciaResponse.desde(buscar(id));
    }

    @Transactional
    public EvidenciaResponse crear(EvidenciaRequest solicitud) {
        Evidencia evidencia = new Evidencia();
        evidencia.setOportunidad(referencias.oportunidad(solicitud.oportunidadId()));
        evidencia.setUri(solicitud.uri().trim());
        evidencia.setDescripcion(ReglasNegocio.textoOpcional(solicitud.descripcion()));
        evidencia.setFechaCaptura(solicitud.fechaCaptura());
        return EvidenciaResponse.desde(evidencias.save(evidencia));
    }

    @Transactional
    public EvidenciaResponse actualizar(Long id, EvidenciaRequest solicitud) {
        Evidencia evidencia = buscar(id);
        evidencia.setDescripcion(ReglasNegocio.textoOpcional(solicitud.descripcion()));
        return EvidenciaResponse.desde(evidencia);
    }

    @Transactional
    public void eliminar(Long id) {
        evidencias.delete(buscar(id));
    }

    private Evidencia buscar(Long id) {
        return evidencias.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("La evidencia " + id + " no existe"));
    }
}
