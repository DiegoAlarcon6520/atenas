package cl.duoc.atenasapi.repository;

import cl.duoc.atenasapi.entity.Oportunidad;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OportunidadRepository extends JpaRepository<Oportunidad, Long> {

    List<Oportunidad> findAllByOrderByFechaInicioEventoAsc();

    List<Oportunidad> findByResponsableIdOrderByFechaInicioEventoAsc(Long responsableId);

    boolean existsByOrganizacionId(Long organizacionId);

    boolean existsByContactoId(Long contactoId);

    boolean existsByResponsableIdOrCreadoPorId(Long responsableId, Long creadoPorId);
}
