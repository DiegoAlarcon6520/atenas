package cl.duoc.atenasapi.entity;

import cl.duoc.atenasapi.domain.EstadoOportunidad;
import cl.duoc.atenasapi.domain.TipoAccion;
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

/**
 * Gestión registrada sobre una oportunidad. Es el historial del embudo:
 * guarda de qué estado a qué estado pasó y quién lo hizo. No se edita ni se
 * borra, para que la trazabilidad no se pueda reescribir.
 */
@Entity
@Table(name = "seguimiento")
@Getter
@Setter
@NoArgsConstructor
public class Seguimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "oportunidad_id", nullable = false)
    private Oportunidad oportunidad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoAccion tipoAccion;

    @Column(nullable = false)
    private long fechaAccion;

    @Column(nullable = false, length = 1000)
    private String resultado;

    @Column(length = 300)
    private String proximaAccion;

    private Long fechaProximoSeguimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoOportunidad estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoOportunidad estadoNuevo;

    @Column(length = 1000)
    private String observaciones;
}
