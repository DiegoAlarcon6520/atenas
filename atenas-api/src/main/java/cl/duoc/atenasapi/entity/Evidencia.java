package cl.duoc.atenasapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * Registro de una fotografía tomada en terreno. El archivo queda en el
 * teléfono; aquí se guarda su referencia y descripción.
 */
@Entity
@Table(name = "evidencia")
@Getter
@Setter
@NoArgsConstructor
public class Evidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "oportunidad_id", nullable = false)
    private Oportunidad oportunidad;

    @Column(nullable = false, length = 500)
    private String uri;

    @Column(length = 300)
    private String descripcion;

    @Column(nullable = false)
    private long fechaCaptura;
}
