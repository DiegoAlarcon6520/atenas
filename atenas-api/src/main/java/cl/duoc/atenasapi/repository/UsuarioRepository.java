package cl.duoc.atenasapi.repository;

import cl.duoc.atenasapi.domain.RolUsuario;
import cl.duoc.atenasapi.entity.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    boolean existsByNombreUsuario(String nombreUsuario);

    List<Usuario> findAllByOrderByNombreCompletoAsc();

    List<Usuario> findByRolOrderByNombreCompletoAsc(RolUsuario rol);
}
