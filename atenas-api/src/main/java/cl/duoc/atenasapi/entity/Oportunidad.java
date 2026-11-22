package cl.duoc.atenasapi.entity;

import cl.duoc.atenasapi.domain.AfinidadPublico;
import cl.duoc.atenasapi.domain.EstadoOportunidad;
import cl.duoc.atenasapi.domain.PotencialComercial;
import cl.duoc.atenasapi.domain.Prioridad;
import cl.duoc.atenasapi.domain.TipoOportunidad;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Oportunidad de activación: el evento y su gestión comercial. La relación
 * evento-oportunidad es 1 a 1 en el MVP, por eso viven en la misma tabla.
 *
 * Las fechas son milisegundos epoch (ver domain/Fechas); las horas, minutos
 * desde la medianoche (600 = 10:00), igual que en la app.
 */
@Entity
@Table(name = "oportunidad", indexes = @Index(columnList = "estado"))
@Getter
@Setter
@NoArgsConstructor
public class Oportunidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoOportunidad tipo;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Column(nullable = false, length = 150)
    private String fuenteInformacion;

    @Column(nullable = false)
    private long fechaDeteccion;

    @Column(nullable = false)
    private long fechaInicioEvento;

    private Long fechaTerminoEvento;

    @Column(nullable = false)
    private int horaInicio;

    @Column(nullable = false)
    private int horaTermino;

    @Column(nullable = false, length = 200)
    private String direccion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comuna_id", nullable = false)
    private Comuna comuna;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizacion_id", nullable = false)
    private Organizacion organizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contacto_id")
    private Contacto contacto;

    /** Sin responsable mientras el supervisor no la asigne. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creado_por_id", nullable = false)
    private Usuario creadoPor;

    @Column(nullable = false, length = 200)
    private String publicoObjetivo;

    @Column(nullable = false)
    private int publicoEsperado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Prioridad prioridad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PotencialComercial potencial = PotencialComercial.MEDIO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AfinidadPublico afinidad = AfinidadPublico.MEDIA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoOportunidad estado;

    @Column(nullable = false)
    private double costoReferencial;

    @Column(length = 500)
    private String condiciones;

    @Column(length = 500)
    private String requisitos;

    @Column(length = 300)
    private String espaciosDisponibles;

    @Column(length = 1000)
    private String observaciones;

    @Column(nullable = false)
    private long fechaCreacion;

    @Column(nullable = false)
    private long fechaActualizacion;
}
