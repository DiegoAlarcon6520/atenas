package cl.duoc.atenasapi.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

/** Validaciones que cruzan campos: fechas del evento y de la gestión. */
class ReglasNegocioTest {

    private static final long DIA = 24L * 60 * 60 * 1000;
    private final long hoy = Fechas.hoy();

    // ---------- Evento ----------

    @Test
    void unEventoCoherenteNoTieneErrores() {
        assertTrue(ReglasNegocio.validarEvento(hoy, hoy + 10 * DIA, hoy + 11 * DIA, 600, 1080).isEmpty());
    }

    @Test
    void elEventoNoPuedeOcurrirAntesDeDetectarse() {
        Map<String, String> errores = ReglasNegocio.validarEvento(hoy, hoy - DIA, null, 600, 1080);

        assertEquals("El evento no puede ocurrir antes de haberse detectado", errores.get("fechaInicioEvento"));
    }

    @Test
    void elEventoNoPuedeTerminarAntesDeEmpezar() {
        Map<String, String> errores = ReglasNegocio.validarEvento(hoy, hoy + 5 * DIA, hoy + 4 * DIA, 600, 1080);

        assertEquals("La fecha de término no puede ser anterior al inicio", errores.get("fechaTerminoEvento"));
    }

    @Test
    void laHoraDeTerminoDebeSerPosteriorALaDeInicio() {
        Map<String, String> iguales = ReglasNegocio.validarEvento(hoy, hoy, null, 600, 600);

        assertEquals("La hora de término debe ser posterior a la de inicio", iguales.get("horaTermino"));
    }

    @Test
    void sinFechaDeTerminoNoSeComparaContraElInicio() {
        assertNull(ReglasNegocio.validarEvento(hoy, hoy, null, 600, 700).get("fechaTerminoEvento"));
    }

    // ---------- Gestión ----------

    @Test
    void unaGestionDeHoyEsValidaACualquierHora() {
        long hoyEnLaTarde = hoy + 18 * 60 * 60 * 1000L;

        assertTrue(ReglasNegocio.validarGestion(hoyEnLaTarde, null, null, false, hoy).isEmpty());
    }

    @Test
    void unaGestionDeMananaTodaviaNoOcurrio() {
        Map<String, String> errores = ReglasNegocio.validarGestion(hoy + DIA, null, null, false, hoy);

        assertEquals("La fecha de la gestión no puede ser futura", errores.get("fechaAccion"));
    }

    @Test
    void unaProximaAccionSinFechaNuncaApareceriaComoAtrasada() {
        Map<String, String> errores = ReglasNegocio.validarGestion(hoy, "Llamar el lunes", null, false, hoy);

        assertEquals("Indica cuándo se hará la próxima acción", errores.get("fechaProximoSeguimiento"));
    }

    @Test
    void elProximoSeguimientoNoPuedeSerAnteriorALaGestion() {
        Map<String, String> errores = ReglasNegocio.validarGestion(hoy, "Llamar", hoy - DIA, false, hoy);

        assertEquals("El próximo seguimiento no puede ser anterior a esta gestión", errores.get("fechaProximoSeguimiento"));
    }

    @Test
    void unaGestionDeCierreNoExigeProximaAccion() {
        assertTrue(ReglasNegocio.validarGestion(hoy, "Llamar", null, true, hoy).isEmpty());
    }

    @Test
    void unaProximaAccionEnBlancoNoCuentaComoCompromiso() {
        assertTrue(ReglasNegocio.validarGestion(hoy, "   ", null, false, hoy).isEmpty());
    }

    // ---------- Normalización ----------

    @Test
    void elUsuarioSeNormalizaComoEnLaApp() {
        assertEquals("cvaldes", ReglasNegocio.normalizarUsuario("  CValdes "));
        assertEquals("", ReglasNegocio.normalizarUsuario(null));
    }

    @Test
    void unTextoOpcionalVacioSeGuardaComoNulo() {
        assertNull(ReglasNegocio.textoOpcional("   "));
        assertNull(ReglasNegocio.textoOpcional(null));
        assertEquals("Nota", ReglasNegocio.textoOpcional(" Nota "));
    }
}
