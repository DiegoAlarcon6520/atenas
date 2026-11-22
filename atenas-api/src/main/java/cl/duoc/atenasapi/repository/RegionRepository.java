package cl.duoc.atenasapi.repository;

import cl.duoc.atenasapi.entity.Region;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {

    List<Region> findAllByOrderByNombreAsc();
}
