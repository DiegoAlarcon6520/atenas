package cl.duoc.atenasapi.repository;

import cl.duoc.atenasapi.entity.Seguimiento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeguimientoRepository extends JpaRepository<Seguimiento, Long> {

    List<Seguimiento> findAllByOrderByFechaAccionDescIdDesc();

    List<Seguimiento> findByOportunidadIdOrderByFechaAccionDescIdDesc(Long oportunidadId);

    List<Seguimiento> findByOportunidadResponsableIdOrderByFechaAccionDescIdDesc(Long responsableId);

    boolean existsByOportunidadId(Long oportunidadId);

    boolean existsByUsuarioId(Long usuarioId);
}
