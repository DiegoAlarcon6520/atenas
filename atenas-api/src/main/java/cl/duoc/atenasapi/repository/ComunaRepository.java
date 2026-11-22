package cl.duoc.atenasapi.repository;

import cl.duoc.atenasapi.entity.Comuna;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComunaRepository extends JpaRepository<Comuna, Long> {

    List<Comuna> findAllByOrderByNombreAsc();

    List<Comuna> findByRegionIdOrderByNombreAsc(Long regionId);
}
