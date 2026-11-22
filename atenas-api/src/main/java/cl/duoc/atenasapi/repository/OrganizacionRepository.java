package cl.duoc.atenasapi.repository;

import cl.duoc.atenasapi.entity.Organizacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizacionRepository extends JpaRepository<Organizacion, Long> {

    List<Organizacion> findAllByOrderByNombreAsc();
}
