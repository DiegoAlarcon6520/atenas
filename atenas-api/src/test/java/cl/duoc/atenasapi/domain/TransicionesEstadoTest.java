package cl.duoc.atenasapi.domain;

import static cl.duoc.atenasapi.domain.EstadoOportunidad.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * La máquina de estados del servidor debe decidir exactamente lo mismo que
 * la de la app: si difieren, la app ofrecería botones que el servidor rechaza.
 */
class TransicionesEstadoTest {

    @Test
    void elEjecutivoSoloAvanzaHastaEsperarRespuesta() {
        assertEquals(List.of(CONTACTO_PENDIENTE), TransicionesEstado.siguientesPosibles(IDENTIFICADA, RolUsuario.EJECUTIVO));
        assertEquals(List.of(), TransicionesEstado.siguientesPosibles(EN_ESPERA_RESPUESTA, RolUsuario.EJECUTIVO));
    }

    @Test
    void confirmarYDescartarSonDecisionesDelSupervisor() {
        assertEquals(List.of(CONFIRMADA, DESCARTADA), TransicionesEstado.siguientesPosibles(EN_ESPERA_RESPUESTA, RolUsuario.SUPERVISOR));
        assertFalse(TransicionesEstado.esValida(EN_ESPERA_RESPUESTA, CONFIRMADA, RolUsuario.EJECUTIVO));
        assertTrue(TransicionesEstado.esValida(EN_ESPERA_RESPUESTA, CONFIRMADA, RolUsuario.SUPERVISOR));
    }

    @Test
    void noSePuedeSaltarEtapasNiSiquieraSiendoSupervisor() {
        assertFalse(TransicionesEstado.esValida(IDENTIFICADA, CONFIRMADA, RolUsuario.SUPERVISOR));
    }

    @Test
    void mantenerElEstadoSiempreEsValido() {
        assertTrue(TransicionesEstado.esValida(CONTACTO_REALIZADO, CONTACTO_REALIZADO, RolUsuario.EJECUTIVO));
    }

    @Test
    void soloElSupervisorRetrocede() {
        assertEquals(List.of(), TransicionesEstado.anterioresPosibles(PROPUESTA_ENVIADA, RolUsuario.EJECUTIVO));
        assertEquals(
            List.of(IDENTIFICADA, CONTACTO_PENDIENTE, CONTACTO_REALIZADO),
            TransicionesEstado.anterioresPosibles(PROPUESTA_ENVIADA, RolUsuario.SUPERVISOR)
        );
    }

    @Test
    void reabrirUnaDescartadaLaDevuelveAlInicioDelEmbudo() {
        assertEquals(
            List.of(IDENTIFICADA, CONTACTO_PENDIENTE, CONTACTO_REALIZADO),
            TransicionesEstado.anterioresPosibles(DESCARTADA, RolUsuario.SUPERVISOR)
        );
        assertEquals(List.of(CONFIRMADA), TransicionesEstado.anterioresPosibles(ACTIVACION_REALIZADA, RolUsuario.SUPERVISOR));
    }

    @Test
    void unaOportunidadRecienIdentificadaNoTieneEtapaAnterior() {
        assertEquals(List.of(), TransicionesEstado.anterioresPosibles(IDENTIFICADA, RolUsuario.SUPERVISOR));
    }

    @Test
    void unaOportunidadCerradaNoSeMueveNiAdmiteCambios() {
        assertEquals(List.of(), TransicionesEstado.siguientesPosibles(CERRADA, RolUsuario.SUPERVISOR));
        assertEquals(List.of(), TransicionesEstado.anterioresPosibles(CERRADA, RolUsuario.SUPERVISOR));
        assertFalse(TransicionesEstado.admiteCambios(CERRADA));
        assertTrue(TransicionesEstado.admiteCambios(DESCARTADA));
    }

    @Test
    void losEstadosDeCierreSonDescartadaActivacionYCerrada() {
        assertTrue(TransicionesEstado.esEstadoDeCierre(DESCARTADA));
        assertTrue(TransicionesEstado.esEstadoDeCierre(ACTIVACION_REALIZADA));
        assertTrue(TransicionesEstado.esEstadoDeCierre(CERRADA));
        assertFalse(TransicionesEstado.esEstadoDeCierre(CONFIRMADA));
    }
}
