package cl.duoc.atenasapi.repository;

import cl.duoc.atenasapi.entity.Contacto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactoRepository extends JpaRepository<Contacto, Long> {

    List<Contacto> findAllByOrderByNombreAsc();

    List<Contacto> findByOrganizacionIdOrderByNombreAsc(Long organizacionId);

    void deleteByOrganizacionId(Long organizacionId);
}
