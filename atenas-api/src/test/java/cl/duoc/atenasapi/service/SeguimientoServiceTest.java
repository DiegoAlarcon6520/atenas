package cl.duoc.atenasapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.atenasapi.domain.EstadoOportunidad;
import cl.duoc.atenasapi.domain.Fechas;
import cl.duoc.atenasapi.domain.RolUsuario;
import cl.duoc.atenasapi.domain.TipoAccion;
import cl.duoc.atenasapi.dto.SeguimientoRequest;
import cl.duoc.atenasapi.dto.SeguimientoResponse;
import cl.duoc.atenasapi.entity.Contacto;
import cl.duoc.atenasapi.entity.Oportunidad;
import cl.duoc.atenasapi.entity.Seguimiento;
import cl.duoc.atenasapi.entity.Usuario;
import cl.duoc.atenasapi.exception.AccionNoPermitidaException;
import cl.duoc.atenasapi.exception.ConflictoException;
import cl.duoc.atenasapi.repository.OportunidadRepository;
import cl.duoc.atenasapi.repository.SeguimientoRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Registrar una gestión es la operación con más reglas del sistema: quién
 * puede mover el embudo, hacia dónde y qué se actualiza junto con ella.
 * Los repositorios se reemplazan por simulaciones (Mockito), así se prueba
 * solo la decisión del servicio, sin base de datos.
 */
@ExtendWith(MockitoExtension.class)
class SeguimientoServiceTest {

    @Mock
    private SeguimientoRepository seguimientos;

    @Mock
    private OportunidadRepository oportunidades;

    @Mock
    private Referencias referencias;

    @InjectMocks
    private SeguimientoService servicio;

    private final Usuario camila = usuario(1L, RolUsuario.EJECUTIVO);
    private final Usuario rodrigo = usuario(2L, RolUsuario.EJECUTIVO);
    private final Usuario marcelo = usuario(9L, RolUsuario.SUPERVISOR);
    private final Contacto contacto = contacto();

    @Test
    void elEjecutivoNoPuedeConfirmarUnaOportunidad() {
        Oportunidad oportunidad = oportunidad(EstadoOportunidad.EN_ESPERA_RESPUESTA, camila);
        preparar(oportunidad, camila);

        assertThrows(AccionNoPermitidaException.class,
            () -> servicio.registrar(100L, solicitud(camila, TipoAccion.CONFIRMACION, EstadoOportunidad.CONFIRMADA, null)));

        verify(seguimientos, never()).save(any());
        assertEquals(EstadoOportunidad.EN_ESPERA_RESPUESTA, oportunidad.getEstado());
    }

    @Test
    void unEjecutivoNoGestionaLaCarteraDeOtro() {
        preparar(oportunidad(EstadoOportunidad.CONTACTO_PENDIENTE, rodrigo), camila);

        assertThrows(AccionNoPermitidaException.class,
            () -> servicio.registrar(100L, solicitud(camila, TipoAccion.LLAMADA, null, null)));
    }

    @Test
    void unaOportunidadCerradaNoAdmiteGestiones() {
        preparar(oportunidad(EstadoOportunidad.CERRADA, camila), marcelo);

        assertThrows(ConflictoException.class,
            () -> servicio.registrar(100L, solicitud(marcelo, TipoAccion.SEGUIMIENTO, null, null)));
    }

    @Test
    void niElSupervisorPuedeSaltarseEtapas() {
        preparar(oportunidad(EstadoOportunidad.IDENTIFICADA, camila), marcelo);

        assertThrows(ConflictoException.class,
            () -> servicio.registrar(100L, solicitud(marcelo, TipoAccion.CONFIRMACION, EstadoOportunidad.CONFIRMADA, null)));
    }

    @Test
    void unaGestionValidaCambiaElEstadoYActualizaElUltimoContacto() {
        // Arrange
        Oportunidad oportunidad = oportunidad(EstadoOportunidad.CONTACTO_PENDIENTE, camila);
        preparar(oportunidad, camila);
        when(seguimientos.save(any(Seguimiento.class))).thenAnswer(llamada -> llamada.getArgument(0));
        long hoy = Fechas.hoy();

        // Act
        SeguimientoResponse gestion = servicio.registrar(100L,
            new SeguimientoRequest(1L, TipoAccion.LLAMADA, hoy, "Se conversó con la jefa de bienestar.",
                "Enviar presentación", Fechas.masDias(hoy, 3), EstadoOportunidad.CONTACTO_REALIZADO, null));

        // Assert
        assertEquals(EstadoOportunidad.CONTACTO_PENDIENTE, gestion.estadoAnterior());
        assertEquals(EstadoOportunidad.CONTACTO_REALIZADO, gestion.estadoNuevo());
        assertEquals(EstadoOportunidad.CONTACTO_REALIZADO, oportunidad.getEstado());
        assertEquals(Long.valueOf(hoy), contacto.getFechaUltimoContacto());
    }

    @Test
    void unaInvestigacionInternaNoCuentaComoContacto() {
        preparar(oportunidad(EstadoOportunidad.IDENTIFICADA, camila), camila);
        when(seguimientos.save(any(Seguimiento.class))).thenAnswer(llamada -> llamada.getArgument(0));

        servicio.registrar(100L, solicitud(camila, TipoAccion.INVESTIGACION_INICIAL, null, null));

        assertNull(contacto.getFechaUltimoContacto());
    }

    @Test
    void descartarNoDejaUnaProximaAccionPendiente() {
        preparar(oportunidad(EstadoOportunidad.EN_ESPERA_RESPUESTA, camila), marcelo);
        when(seguimientos.save(any(Seguimiento.class))).thenAnswer(llamada -> llamada.getArgument(0));

        SeguimientoResponse gestion = servicio.registrar(100L,
            solicitud(marcelo, TipoAccion.CIERRE, EstadoOportunidad.DESCARTADA, "Llamar en marzo"));

        assertEquals(EstadoOportunidad.DESCARTADA, gestion.estadoNuevo());
        assertNull(gestion.proximaAccion());
        assertNull(gestion.fechaProximoSeguimiento());
    }

    // ---------- Datos de prueba ----------

    private void preparar(Oportunidad oportunidad, Usuario quienRegistra) {
        when(oportunidades.findById(100L)).thenReturn(Optional.of(oportunidad));
        when(referencias.usuarioActivo(quienRegistra.getId(), "usuarioId")).thenReturn(quienRegistra);
    }

    private SeguimientoRequest solicitud(Usuario usuario, TipoAccion tipo, EstadoOportunidad estadoNuevo, String proximaAccion) {
        return new SeguimientoRequest(usuario.getId(), tipo, Fechas.hoy(), "Resultado de la gestión registrada.",
            proximaAccion, null, estadoNuevo, null);
    }

    private Oportunidad oportunidad(EstadoOportunidad estado, Usuario responsable) {
        Oportunidad o = new Oportunidad();
        o.setId(100L);
        o.setEstado(estado);
        o.setResponsable(responsable);
        o.setContacto(contacto);
        return o;
    }

    private static Usuario usuario(Long id, RolUsuario rol) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setRol(rol);
        u.setActivo(true);
        return u;
    }

    private static Contacto contacto() {
        Contacto c = new Contacto();
        c.setId(500L);
        return c;
    }
}
