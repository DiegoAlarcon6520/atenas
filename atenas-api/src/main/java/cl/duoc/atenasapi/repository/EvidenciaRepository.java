package cl.duoc.atenasapi.repository;

import cl.duoc.atenasapi.entity.Evidencia;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvidenciaRepository extends JpaRepository<Evidencia, Long> {

    List<Evidencia> findAllByOrderByFechaCapturaDesc();

    List<Evidencia> findByOportunidadIdOrderByFechaCapturaDesc(Long oportunidadId);

    List<Evidencia> findByOportunidadResponsableIdOrderByFechaCapturaDesc(Long responsableId);

    void deleteByOportunidadId(Long oportunidadId);
}
