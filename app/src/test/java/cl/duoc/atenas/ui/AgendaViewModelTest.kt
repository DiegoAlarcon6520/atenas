package cl.duoc.atenas.ui

import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.data.remote.FeriadoDto
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.prueba.Datos
import cl.duoc.atenas.prueba.Entorno
import cl.duoc.atenas.prueba.ReglaCorrutinas
import cl.duoc.atenas.prueba.observar
import cl.duoc.atenas.ui.agenda.AgendaViewModel
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** HU-15: agenda de eventos próximos agrupados por cercanía. */
class AgendaViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val hoy = Fechas.hoy()

    private fun en(dias: Int) = Fechas.masDias(hoy, dias)

    @Test
    fun `la agenda descarta eventos pasados y terminados y agrupa por cercania`() = runTest {
        // Arrange
        val e = Entorno()
        e.oportunidadDao.agregar(
            Datos.oportunidad(id = 1, fechaInicioEvento = hoy),
            Datos.oportunidad(id = 2, fechaInicioEvento = en(20)),
            Datos.oportunidad(id = 3, fechaInicioEvento = en(45)),
            Datos.oportunidad(id = 4, fechaInicioEvento = en(-1)),
            Datos.oportunidad(id = 5, fechaInicioEvento = en(3), estado = EstadoOportunidad.DESCARTADA),
            Datos.oportunidad(id = 6, fechaInicioEvento = en(4), estado = EstadoOportunidad.CERRADA)
        )

        // Act
        val vm = AgendaViewModel(e.sesion, e.oportunidades, e.feriados)
        observar(vm.estado)

        // Assert
        val estado = vm.estado.value
        assertFalse(estado.cargando)
        assertEquals(7, estado.semana.size)
        assertEquals(1, estado.semana.count { it.esHoy })
        assertTrue(estado.semana.single { it.esHoy }.tieneEventos)
        assertEquals(listOf("Esta semana", "Próximas semanas", "Más adelante"), estado.grupos.map { it.titulo })
        assertEquals(listOf(listOf(1L), listOf(2L), listOf(3L)), estado.grupos.map { g -> g.eventos.map { it.oportunidad.id } })
        assertEquals(0, estado.grupos[0].eventos.single().diasRestantes)
        assertEquals(20, estado.grupos[1].eventos.single().diasRestantes)
    }

    @Test
    fun `cuenta los eventos cercanos que aun no tienen participacion confirmada`() = runTest {
        val e = Entorno()
        e.oportunidadDao.agregar(
            Datos.oportunidad(id = 1, fechaInicioEvento = en(5), estado = EstadoOportunidad.CONTACTO_REALIZADO),
            Datos.oportunidad(id = 2, fechaInicioEvento = en(6), estado = EstadoOportunidad.CONFIRMADA),
            Datos.oportunidad(id = 3, fechaInicioEvento = en(7), estado = EstadoOportunidad.ACTIVACION_REALIZADA),
            Datos.oportunidad(id = 4, fechaInicioEvento = en(30), estado = EstadoOportunidad.IDENTIFICADA)
        )

        val vm = AgendaViewModel(e.sesion, e.oportunidades, e.feriados)
        observar(vm.estado)

        assertEquals(1, vm.estado.value.sinConfirmarPronto)
    }

    @Test
    fun `elegir un dia filtra la lista y elegirlo de nuevo la restaura`() = runTest {
        // Arrange
        val e = Entorno()
        e.oportunidadDao.agregar(
            Datos.oportunidad(id = 1, fechaInicioEvento = hoy),
            Datos.oportunidad(id = 2, fechaInicioEvento = en(40))
        )
        val vm = AgendaViewModel(e.sesion, e.oportunidades, e.feriados)
        observar(vm.estado)

        // Act
        vm.alElegirDia(hoy)

        // Assert
        assertEquals(hoy, vm.estado.value.diaElegido)
        assertEquals(listOf(1L), vm.estado.value.grupos.flatMap { g -> g.eventos.map { it.oportunidad.id } })

        vm.alElegirDia(hoy)
        assertNull(vm.estado.value.diaElegido)
        assertEquals(2, vm.estado.value.grupos.sumOf { it.eventos.size })
    }

    @Test
    fun `sin eventos no se muestran grupos vacios`() = runTest {
        val e = Entorno()

        val vm = AgendaViewModel(e.sesion, e.oportunidades, e.feriados)
        observar(vm.estado)

        assertTrue(vm.estado.value.grupos.isEmpty())
        assertTrue(vm.estado.value.semana.none { it.tieneEventos })
    }

    @Test
    fun `el ejecutivo ve solo su cartera y el supervisor la de todos`() = runTest {
        val ejecutivo = Entorno()
        observar(AgendaViewModel(ejecutivo.sesion, ejecutivo.oportunidades, ejecutivo.feriados).estado)
        assertEquals(Datos.ejecutiva.id, ejecutivo.oportunidadDao.ultimoAlcance)

        val supervisor = Entorno(usuario = Datos.supervisor)
        observar(AgendaViewModel(supervisor.sesion, supervisor.oportunidades, supervisor.feriados).estado)
        assertNull(supervisor.oportunidadDao.ultimoAlcance)
    }

    // ---------- Feriados (API externa) ----------

    private fun texto(fecha: Long) = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(fecha))

    @Test
    fun `un evento que cae en feriado queda marcado y se anuncia el proximo feriado`() = runTest {
        // Arrange: feriado nacional en 3 días, con un evento ese mismo día.
        val e = Entorno()
        e.feriadosApi.respuesta = listOf(FeriadoDto(date = texto(en(3)), localName = "Feriado de prueba", global = true))
        e.oportunidadDao.agregar(
            Datos.oportunidad(id = 1, fechaInicioEvento = en(3)),
            Datos.oportunidad(id = 2, fechaInicioEvento = en(5))
        )

        // Act
        val vm = AgendaViewModel(e.sesion, e.oportunidades, e.feriados)
        observar(vm.estado)

        // Assert
        val estado = vm.estado.value
        val eventos = estado.grupos.flatMap { it.eventos }
        assertEquals("Feriado de prueba", eventos.single { it.oportunidad.id == 1L }.feriado)
        assertNull(eventos.single { it.oportunidad.id == 2L }.feriado)
        assertEquals("Feriado de prueba", estado.proximoFeriado?.nombre)
        assertEquals(3, estado.proximoFeriado?.diasRestantes)
    }

    @Test
    fun `sin feriados disponibles la agenda funciona igual`() = runTest {
        val e = Entorno()
        e.feriadosApi.fallo = IOException("sin red")
        e.oportunidadDao.agregar(Datos.oportunidad(id = 1, fechaInicioEvento = en(2)))

        val vm = AgendaViewModel(e.sesion, e.oportunidades, e.feriados)
        observar(vm.estado)

        val estado = vm.estado.value
        assertNull(estado.proximoFeriado)
        assertTrue(estado.semana.all { it.feriado == null })
        assertEquals(1, estado.grupos.sumOf { it.eventos.size })
    }
}
