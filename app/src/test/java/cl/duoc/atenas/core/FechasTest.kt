package cl.duoc.atenas.core

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/**
 * Pruebas de la conversión entre el calendario de Material 3 (UTC) y las
 * fechas locales de la aplicación (defecto D1). Se fija la zona horaria de
 * Chile para que el resultado no dependa del computador donde corren.
 */
class FechasTest {

    private lateinit var zonaOriginal: TimeZone

    @Before
    fun fijarZonaChile() {
        zonaOriginal = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("America/Santiago"))
    }

    @After
    fun restaurarZona() {
        TimeZone.setDefault(zonaOriginal)
    }

    private fun medianocheUtc(anio: Int, mes: Int, dia: Int): Long =
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(anio, mes, dia)
        }.timeInMillis

    private fun medianocheLocal(anio: Int, mes: Int, dia: Int): Long =
        Calendar.getInstance().apply {
            clear()
            set(anio, mes, dia)
        }.timeInMillis

    @Test
    fun `la fecha elegida en el calendario conserva el dia`() {
        // El DatePicker entrega el 11/11/2026 como medianoche UTC.
        val elegida = medianocheUtc(2026, Calendar.NOVEMBER, 11)
        assertEquals("11/11/2026", Fechas.aTexto(Fechas.utcALocal(elegida)))
    }

    @Test
    fun `elegir hoy en el calendario equivale a la medianoche local de hoy`() {
        val hoy = medianocheLocal(2026, Calendar.NOVEMBER, 11)
        val elegida = medianocheUtc(2026, Calendar.NOVEMBER, 11)
        assertEquals(hoy, Fechas.utcALocal(elegida))
    }

    @Test
    fun `ida y vuelta entre local y UTC no cambia la fecha`() {
        val local = medianocheLocal(2026, Calendar.MARCH, 5)
        assertEquals(local, Fechas.utcALocal(Fechas.localAUtc(local)))
    }

    @Test
    fun `la fecha local se muestra en el calendario en el mismo dia`() {
        val local = medianocheLocal(2026, Calendar.DECEMBER, 31)
        assertEquals(medianocheUtc(2026, Calendar.DECEMBER, 31), Fechas.localAUtc(local))
    }

    @Test
    fun `los dias entre fechas no se pierden con el cambio de horario`() {
        // En Chile el horario de verano cambia en septiembre: ese "día" dura 23 horas.
        val antes = medianocheLocal(2026, Calendar.SEPTEMBER, 1)
        val despues = medianocheLocal(2026, Calendar.SEPTEMBER, 15)
        assertEquals(14, Fechas.diasEntre(antes, despues))
    }
}
