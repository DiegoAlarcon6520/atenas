package cl.duoc.atenasapi.entity;

import cl.duoc.atenasapi.domain.RolUsuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Cuenta ficticia del equipo comercial. La clave se guarda cifrada con BCrypt. */
@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String nombreUsuario;

    @Column(nullable = false, length = 100)
    private String claveHash;

    @Column(nullable = false, length = 120)
    private String nombreCompleto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RolUsuario rol;

    @Column(nullable = false, length = 80)
    private String area;

    @Column(nullable = false, length = 80)
    private String zona;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(nullable = false)
    private long fechaCreacion;

    @Column(nullable = false)
    private long fechaActualizacion;
}
