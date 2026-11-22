package cl.duoc.atenas.data

import cl.duoc.atenas.data.remote.FeriadoDto
import cl.duoc.atenas.data.repository.FeriadosRepository
import cl.duoc.atenas.prueba.FeriadosApiFalsa
import java.io.IOException
import java.util.Calendar
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * API EXTERNA (date.nager.at): feriados de Chile para la agenda.
 *
 * Lo que importa probar: que la fecha de texto quede en medianoche local
 * (para compararla con las fechas de los eventos), que solo se usen los
 * feriados nacionales, que no se repitan descargas y que una falla de red
 * nunca rompa la agenda.
 */
class FeriadosRepositoryTest {

    private val nacional = FeriadoDto(date = "2026-09-18", localName = "Independencia Nacional", global = true)
    private val regional = FeriadoDto(date = "2026-06-07", localName = "Asalto y Toma del Morro de Arica", global = false)
    private val sinDato = FeriadoDto(date = "2026-12-25", localName = "Navidad", global = null)

    @Test
    fun `la fecha de texto se convierte a medianoche local`() {
        val repo = FeriadosRepository(FeriadosApiFalsa())

        val esperado = Calendar.getInstance().apply { clear(); set(2026, Calendar.SEPTEMBER, 18) }.timeInMillis

        assertEquals(esperado, repo.medianocheLocal("2026-09-18"))
    }

    @Test
    fun `solo se usan los feriados nacionales`() = runTest {
        val api = FeriadosApiFalsa(respuesta = listOf(nacional, regional, sinDato))

        val feriados = FeriadosRepository(api).feriados(listOf(2026))

        // global = false es regional; null se trata como nacional.
        assertEquals(listOf("Independencia Nacional", "Navidad"), feriados.map { it.nombre })
    }

    @Test
    fun `cada anio se descarga una sola vez`() = runTest {
        val api = FeriadosApiFalsa(respuesta = listOf(nacional))
        val repo = FeriadosRepository(api)

        repo.feriados(listOf(2026, 2027))
        val segunda = repo.feriados(listOf(2026, 2027))

        assertEquals(listOf(2026, 2027), api.aniosPedidos)
        assertEquals(2, segunda.size)
    }

    @Test
    fun `sin conexion la agenda sigue sin feriados y se reintenta despues`() = runTest {
        val api = FeriadosApiFalsa(respuesta = listOf(nacional), fallo = IOException("sin red"))
        val repo = FeriadosRepository(api)

        assertTrue(repo.feriados(listOf(2026)).isEmpty())

        // La falla no queda guardada: al volver la red se descargan.
        api.fallo = null
        assertEquals(1, repo.feriados(listOf(2026)).size)
        assertEquals(listOf(2026, 2026), api.aniosPedidos)
    }
}
