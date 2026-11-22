package cl.duoc.atenas.ui

import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.data.local.dao.ConteoPorEstado
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.prueba.Datos
import cl.duoc.atenas.prueba.Entorno
import cl.duoc.atenas.prueba.ReglaCorrutinas
import cl.duoc.atenas.prueba.observar
import cl.duoc.atenas.ui.dashboard.DashboardEjecutivoViewModel
import cl.duoc.atenas.ui.dashboard.TipoTarea
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** HU-16: inicio "Qué hacer hoy" del ejecutivo. */
class DashboardEjecutivoViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val hoy = Fechas.hoy()

    // ---------- armarTareas (regla pura) ----------

    @Test
    fun `las tareas se ordenan por urgencia atrasadas hoy propuestas y sin contacto`() {
        // Arrange
        val atrasadas = listOf(Datos.gestionPendiente(1, oportunidadId = 10, fecha = Fechas.masDias(hoy, -2)))
        val deHoy = listOf(Datos.gestionPendiente(2, oportunidadId = 20, fecha = hoy))
        val propuestas = listOf(Datos.resumen(id = 30, estado = EstadoOportunidad.PROPUESTA_ENVIADA))
        val sinContacto = listOf(Datos.resumen(id = 40))

        // Act
        val tareas = DashboardEjecutivoViewModel.armarTareas(atrasadas, deHoy, propuestas, sinContacto)

        // Assert
        assertEquals(
            listOf(TipoTarea.ATRASADA, TipoTarea.HOY, TipoTarea.PROPUESTA, TipoTarea.SIN_CONTACTO),
            tareas.map { it.tipo }
        )
        assertEquals("Vencida hace 2 días", tareas[0].meta)
        assertEquals("Vence hoy", tareas[1].meta)
        assertEquals(EstadoOportunidad.PROPUESTA_ENVIADA.etiqueta, tareas[2].meta)
        assertEquals("Sin primer contacto", tareas[3].meta)
    }

    @Test
    fun `una oportunidad aparece una sola vez en su motivo mas urgente`() {
        val atrasadas = listOf(Datos.gestionPendiente(1, oportunidadId = 10, fecha = Fechas.masDias(hoy, -1)))
        val sinContacto = listOf(Datos.resumen(id = 10))

        val tareas = DashboardEjecutivoViewModel.armarTareas(atrasadas, emptyList(), emptyList(), sinContacto)

        assertEquals(1, tareas.size)
        assertEquals(TipoTarea.ATRASADA, tareas.single().tipo)
    }

    @Test
    fun `sin proxima accion comprometida se usa un texto por defecto`() {
        val atrasadas = listOf(Datos.gestionPendiente(1, 10, Fechas.masDias(hoy, -1), proximaAccion = null))
        val deHoy = listOf(Datos.gestionPendiente(2, 20, hoy, proximaAccion = null))

        val tareas = DashboardEjecutivoViewModel.armarTareas(atrasadas, deHoy, emptyList(), emptyList())

        assertEquals("Retomar la gestión", tareas[0].detalle)
        assertEquals("Gestión comprometida para hoy", tareas[1].detalle)
    }

    @Test
    fun `con la proxima accion comprometida se muestra esa accion`() {
        val deHoy = listOf(Datos.gestionPendiente(2, 20, hoy, proximaAccion = "Enviar cotización"))

        val tareas = DashboardEjecutivoViewModel.armarTareas(emptyList(), deHoy, emptyList(), emptyList())

        assertEquals("Enviar cotización", tareas.single().detalle)
    }

    @Test
    fun `sin pendientes la lista de tareas queda vacia`() {
        assertTrue(DashboardEjecutivoViewModel.armarTareas(emptyList(), emptyList(), emptyList(), emptyList()).isEmpty())
    }

    // ---------- armarEmbudo (regla pura) ----------

    @Test
    fun `el embudo suma los estados de cada tramo y pone cero donde no hay`() {
        val conteos = listOf(
            ConteoPorEstado(EstadoOportunidad.CONTACTO_PENDIENTE, 2),
            ConteoPorEstado(EstadoOportunidad.CONTACTO_REALIZADO, 3),
            ConteoPorEstado(EstadoOportunidad.CONFIRMADA, 1),
            ConteoPorEstado(EstadoOportunidad.CERRADA, 9)
        )

        val embudo = DashboardEjecutivoViewModel.armarEmbudo(conteos)

        assertEquals(listOf("Identificadas", "Contacto", "Propuesta", "Confirmadas"), embudo.map { it.etiqueta })
        assertEquals(listOf(0, 5, 0, 1), embudo.map { it.total })
    }

    // ---------- Estado de la pantalla ----------

    @Test
    fun `el estado junta tareas embudo y contadores del ejecutivo en sesion`() = runTest {
        // Arrange
        val e = Entorno()
        e.oportunidadDao.agregar(
            Datos.oportunidad(id = 1, estado = EstadoOportunidad.IDENTIFICADA),
            Datos.oportunidad(id = 2, estado = EstadoOportunidad.EN_ESPERA_RESPUESTA),
            Datos.oportunidad(id = 3, estado = EstadoOportunidad.CONTACTO_REALIZADO, responsableId = 2)
        )
        e.seguimientoDao.atrasadas.value = listOf(Datos.gestionPendiente(9, 3, Fechas.masDias(hoy, -1)))

        // Act
        val vm = DashboardEjecutivoViewModel(e.sesion, e.oportunidades, e.seguimientos)
        observar(vm.estado)

        // Assert
        val estado = vm.estado.value
        assertFalse(estado.cargando)
        assertEquals("Camila", estado.primerNombre)
        assertEquals("Camila Valdés", estado.nombreUsuario)
        assertEquals(1, estado.atrasadas)
        assertEquals(1, estado.propuestasPorInsistir)
        assertEquals(listOf(3L, 2L, 1L), estado.tareas.map { it.oportunidadId })
        assertEquals(2, estado.totalActivas)
    }
}
