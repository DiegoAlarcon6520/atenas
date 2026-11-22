package cl.duoc.atenas.domain

import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.EtapaCartera
import cl.duoc.atenas.domain.rules.GuiaGestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de las reglas que guían al usuario en el rediseño (siguiente paso, etapas, conversión). */
class GuiaGestionTest {

    // ---------- Indicador de etapa ----------

    @Test
    fun `una oportunidad recien identificada esta en la primera etapa`() {
        assertEquals(0, GuiaGestion.indiceEtapa(EstadoOportunidad.IDENTIFICADA))
    }

    @Test
    fun `propuesta enviada y en espera comparten la etapa de respuesta`() {
        assertEquals(
            GuiaGestion.indiceEtapa(EstadoOportunidad.PROPUESTA_ENVIADA),
            GuiaGestion.indiceEtapa(EstadoOportunidad.EN_ESPERA_RESPUESTA)
        )
    }

    @Test
    fun `una activacion realizada completa todas las etapas`() {
        assertEquals(GuiaGestion.etapasVisibles.size, GuiaGestion.indiceEtapa(EstadoOportunidad.ACTIVACION_REALIZADA))
    }

    @Test
    fun `descartada y cerrada no tienen posicion en el embudo`() {
        assertNull(GuiaGestion.indiceEtapa(EstadoOportunidad.DESCARTADA))
        assertNull(GuiaGestion.indiceEtapa(EstadoOportunidad.CERRADA))
    }

    // ---------- Siguiente paso ----------

    @Test
    fun `el siguiente paso menciona al contacto cuando se conoce`() {
        val paso = GuiaGestion.siguientePaso(EstadoOportunidad.CONTACTO_PENDIENTE, "Ignacia Morales")
        assertTrue(paso!!.contains("Ignacia Morales"))
    }

    @Test
    fun `sin contacto conocido se sugiere conseguir uno`() {
        val paso = GuiaGestion.siguientePaso(EstadoOportunidad.CONTACTO_PENDIENTE, null)
        assertTrue(paso!!.contains("contacto"))
    }

    @Test
    fun `todo estado abierto tiene un siguiente paso`() {
        EstadoOportunidad.entries
            .filter { it != EstadoOportunidad.CERRADA }
            .forEach { assertNotNull(it.name, GuiaGestion.siguientePaso(it, null)) }
    }

    @Test
    fun `una oportunidad cerrada no tiene siguiente paso`() {
        assertNull(GuiaGestion.siguientePaso(EstadoOportunidad.CERRADA, "Alguien"))
    }

    // ---------- Conversión ----------

    @Test
    fun `la conversion es ganadas sobre ganadas mas descartadas`() {
        val conteos = mapOf(
            EstadoOportunidad.CONFIRMADA to 2,
            EstadoOportunidad.ACTIVACION_REALIZADA to 1,
            EstadoOportunidad.DESCARTADA to 1,
            EstadoOportunidad.CONTACTO_PENDIENTE to 5
        )
        assertEquals(75, GuiaGestion.tasaConversion(conteos))
    }

    @Test
    fun `sin desenlaces no hay conversion`() {
        assertNull(GuiaGestion.tasaConversion(mapOf(EstadoOportunidad.IDENTIFICADA to 3)))
    }

    // ---------- Vencimientos ----------

    @Test
    fun `los vencimientos se expresan en lenguaje natural`() {
        assertEquals("Vence hoy", GuiaGestion.textoVencimiento(0))
        assertEquals("Vence mañana", GuiaGestion.textoVencimiento(1))
        assertEquals("Vence en 5 días", GuiaGestion.textoVencimiento(5))
        assertEquals("Vencida ayer", GuiaGestion.textoVencimiento(-1))
        assertEquals("Vencida hace 3 días", GuiaGestion.textoVencimiento(-3))
    }

    // ---------- Etapas de la cartera ----------

    @Test
    fun `cada estado pertenece a exactamente una etapa de la cartera`() {
        EstadoOportunidad.entries.forEach { estado ->
            assertEquals(estado.name, 1, EtapaCartera.entries.count { estado in it.estados })
        }
    }

    @Test
    fun `una propuesta enviada esta en curso`() {
        assertEquals(EtapaCartera.EN_CURSO, EtapaCartera.de(EstadoOportunidad.PROPUESTA_ENVIADA))
    }
}
