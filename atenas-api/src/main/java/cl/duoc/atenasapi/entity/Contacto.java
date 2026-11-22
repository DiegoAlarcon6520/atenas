package cl.duoc.atenasapi.entity;

import cl.duoc.atenasapi.domain.AreaContacto;
import cl.duoc.atenasapi.domain.MedioContacto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Persona de contacto dentro de una organización. */
@Entity
@Table(name = "contacto")
@Getter
@Setter
@NoArgsConstructor
public class Contacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizacion_id", nullable = false)
    private Organizacion organizacion;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String cargo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AreaContacto area;

    @Column(nullable = false, length = 120)
    private String correo;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MedioContacto medioPreferente;

    @Column(nullable = false)
    private boolean activo = true;

    /** Se actualiza solo con gestiones que fueron una conversación real. */
    private Long fechaUltimoContacto;

    @Column(length = 1000)
    private String observaciones;
}
