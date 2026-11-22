package cl.duoc.atenasapi.entity;

import cl.duoc.atenasapi.domain.TipoOrganizacion;
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

/** Empresa, municipalidad o institución que organiza eventos. */
@Entity
@Table(name = "organizacion")
@Getter
@Setter
@NoArgsConstructor
public class Organizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoOrganizacion tipo;

    @Column(nullable = false, length = 80)
    private String rubro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comuna_id", nullable = false)
    private Comuna comuna;

    @Column(length = 150)
    private String sitioWeb;

    @Column(length = 200)
    private String publicoAsociado;

    @Column(length = 1000)
    private String observaciones;

    @Column(nullable = false)
    private long fechaCreacion;
}
