package cl.duoc.atenas.domain

import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.EtapaCartera
import cl.duoc.atenas.domain.model.RangoFecha
import cl.duoc.atenas.domain.model.RolUsuario
import cl.duoc.atenas.domain.rules.GuiaGestion
import cl.duoc.atenas.domain.rules.Permisos
import cl.duoc.atenas.domain.rules.TransicionesEstado
import cl.duoc.atenas.domain.validation.Validadores
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Casos de borde de las reglas de dominio que no cubren las otras pruebas:
 * cada rama de los `when` y los valores límite de los validadores.
 */
class ReglasComplementariasTest {

    @Test
    fun `cada etapa del embudo tiene su posicion en el indicador`() {
        val esperado = listOf(0, 1, 2, 3, 3, 4)
        val estados = listOf(
            EstadoOportunidad.IDENTIFICADA,
            EstadoOportunidad.CONTACTO_PENDIENTE,
            EstadoOportunidad.CONTACTO_REALIZADO,
            EstadoOportunidad.PROPUESTA_ENVIADA,
            EstadoOportunidad.EN_ESPERA_RESPUESTA,
            EstadoOportunidad.CONFIRMADA
        )

        assertEquals(esperado, estados.map { GuiaGestion.indiceEtapa(it) })
    }

    @Test
    fun `el seguimiento de una respuesta menciona al contacto si se conoce`() {
        assertEquals(
            "Hacer seguimiento con Ignacia para conocer la respuesta",
            GuiaGestion.siguientePaso(EstadoOportunidad.EN_ESPERA_RESPUESTA, "Ignacia")
        )
        assertEquals(
            "Hacer seguimiento de la respuesta",
            GuiaGestion.siguientePaso(EstadoOportunidad.EN_ESPERA_RESPUESTA, null)
        )
    }

    @Test
    fun `la conversion considera mapas con solo ganadas o solo perdidas`() {
        assertEquals(100, GuiaGestion.tasaConversion(mapOf(EstadoOportunidad.ACTIVACION_REALIZADA to 2)))
        assertEquals(0, GuiaGestion.tasaConversion(mapOf(EstadoOportunidad.DESCARTADA to 3)))
    }

    @Test
    fun `los vencimientos futuros se cuentan en dias`() {
        assertEquals("Vence mañana", GuiaGestion.textoVencimiento(1))
        assertEquals("Vence en 4 días", GuiaGestion.textoVencimiento(4))
    }

    @Test
    fun `los rangos de 30 y 90 dias parten hoy`() {
        val hoy = Fechas.hoy()

        assertEquals(hoy to Fechas.masDias(hoy, 30), RangoFecha.PROXIMOS_30.limites(hoy))
        assertEquals(hoy to Fechas.masDias(hoy, 90), RangoFecha.PROXIMOS_90.limites(hoy))
    }

    @Test
    fun `cada estado se ubica en su pestana de la cartera`() {
        assertEquals(EtapaCartera.POR_CONTACTAR, EtapaCartera.de(EstadoOportunidad.IDENTIFICADA))
        assertEquals(EtapaCartera.CONFIRMADAS, EtapaCartera.de(EstadoOportunidad.ACTIVACION_REALIZADA))
        assertEquals(EtapaCartera.CERRADAS, EtapaCartera.de(EstadoOportunidad.CERRADA))
    }

    @Test
    fun `una oportunidad recien identificada no tiene etapa anterior`() {
        assertTrue(TransicionesEstado.anterioresPosibles(EstadoOportunidad.IDENTIFICADA, RolUsuario.SUPERVISOR).isEmpty())
    }

    @Test
    fun `el ejecutivo nunca retrocede una oportunidad`() {
        assertTrue(TransicionesEstado.anterioresPosibles(EstadoOportunidad.CONFIRMADA, RolUsuario.EJECUTIVO).isEmpty())
    }

    @Test
    fun `solo el supervisor ve el panel de equipo pero ambos crean y registran`() {
        assertTrue(Permisos.puedeVerPanelDeEquipo(RolUsuario.SUPERVISOR))
        assertFalse(Permisos.puedeVerPanelDeEquipo(RolUsuario.EJECUTIVO))
        RolUsuario.entries.forEach { rol ->
            assertTrue(Permisos.puedeCrearOportunidad(rol))
            assertTrue(Permisos.puedeRegistrarSeguimiento(rol))
        }
    }

    @Test
    fun `los validadores rechazan valores vacios o mal escritos`() {
        assertEquals("El correo es obligatorio", Validadores.correo(" "))
        assertEquals("El teléfono es obligatorio", Validadores.telefono(""))
        assertEquals("El costo debe ser un número", Validadores.montoNoNegativo("diez", "El costo"))
        assertEquals("El público es obligatorio", Validadores.enteroPositivo("", "El público"))
        assertNull(Validadores.montoNoNegativo("1500,5", "El costo"))
    }
}
