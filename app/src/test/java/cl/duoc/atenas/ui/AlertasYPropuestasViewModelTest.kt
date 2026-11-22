package cl.duoc.atenas.ui

import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.data.local.dao.PropuestaEnviada
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.prueba.Datos
import cl.duoc.atenas.prueba.Entorno
import cl.duoc.atenas.prueba.ReglaCorrutinas
import cl.duoc.atenas.prueba.observar
import cl.duoc.atenas.ui.alertas.AlertasViewModel
import cl.duoc.atenas.ui.alertas.MotivoAlerta
import cl.duoc.atenas.ui.propuestas.PropuestasViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** HU-13 (alertas de la campana) y HU-12 (seguimiento de propuestas enviadas). */
class AlertasYPropuestasViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val hoy = Fechas.hoy()

    // ---------- Alertas ----------

    @Test
    fun `el supervisor ve decisiones pendientes oportunidades sin responsable y atrasadas`() = runTest {
        // Arrange
        val e = Entorno(usuario = Datos.supervisor)
        e.oportunidadDao.agregar(
            Datos.oportunidad(id = 1, estado = EstadoOportunidad.EN_ESPERA_RESPUESTA),
            Datos.oportunidad(id = 2, estado = EstadoOportunidad.EN_ESPERA_RESPUESTA, responsableId = null),
            Datos.oportunidad(id = 3, estado = EstadoOportunidad.IDENTIFICADA, responsableId = null)
        )
        e.seguimientoDao.atrasadas.value = listOf(
            Datos.gestionPendiente(7, 4, Fechas.masDias(hoy, -1)),
            Datos.gestionPendiente(8, 5, Fechas.masDias(hoy, -2), proximaAccion = null, responsableNombre = null)
        )

        // Act
        val vm = AlertasViewModel(e.sesion, e.oportunidades, e.seguimientos)
        observar(vm.alertas)

        // Assert
        val alertas = vm.alertas.value
        assertEquals(
            listOf(
                MotivoAlerta.DECISION, MotivoAlerta.DECISION,
                MotivoAlerta.SIN_RESPONSABLE, MotivoAlerta.SIN_RESPONSABLE,
                MotivoAlerta.ATRASADA, MotivoAlerta.ATRASADA
            ),
            alertas.map { it.motivo }
        )
        assertEquals("Camila Valdés · Organización 100", alertas[0].detalle)
        assertEquals("Sin responsable · Organización 100", alertas[1].detalle)
        assertEquals("Camila Valdés · Llamar a la contraparte", alertas[4].detalle)
        assertEquals("Sin responsable · Retomar la gestión", alertas[5].detalle)
    }

    @Test
    fun `el ejecutivo ve las mismas tareas que su inicio con cada motivo`() = runTest {
        // Arrange
        val e = Entorno()
        e.oportunidadDao.agregar(
            Datos.oportunidad(id = 3, estado = EstadoOportunidad.PROPUESTA_ENVIADA),
            Datos.oportunidad(id = 4, estado = EstadoOportunidad.CONTACTO_PENDIENTE)
        )
        e.seguimientoDao.atrasadas.value = listOf(Datos.gestionPendiente(1, 1, Fechas.masDias(hoy, -1)))
        e.seguimientoDao.entre.value = listOf(Datos.gestionPendiente(2, 2, hoy))

        // Act
        val vm = AlertasViewModel(e.sesion, e.oportunidades, e.seguimientos)
        observar(vm.alertas)

        // Assert
        assertEquals(
            listOf(MotivoAlerta.ATRASADA, MotivoAlerta.HOY, MotivoAlerta.PROPUESTA, MotivoAlerta.SIN_CONTACTO),
            vm.alertas.value.map { it.motivo }
        )
        assertEquals("Llamar a la contraparte · Vencida ayer", vm.alertas.value[0].detalle)
    }

    @Test
    fun `sin pendientes la campana no muestra alertas`() = runTest {
        val e = Entorno()

        val vm = AlertasViewModel(e.sesion, e.oportunidades, e.seguimientos)
        observar(vm.alertas)

        assertTrue(vm.alertas.value.isEmpty())
    }

    // ---------- Propuestas ----------

    private fun propuesta(id: Long, estado: EstadoOportunidad) = PropuestaEnviada(
        id = id,
        oportunidadId = id,
        oportunidadNombre = "Oportunidad $id",
        organizacionNombre = "Constructora Vértice",
        fechaEnvio = hoy,
        resultado = "Se envió la propuesta.",
        estadoActual = estado,
        responsableNombre = Datos.ejecutiva.nombreCompleto
    )

    @Test
    fun `una propuesta espera respuesta mientras esta enviada o en espera`() {
        assertTrue(PropuestasViewModel.esperaRespuesta(propuesta(1, EstadoOportunidad.PROPUESTA_ENVIADA)))
        assertTrue(PropuestasViewModel.esperaRespuesta(propuesta(2, EstadoOportunidad.EN_ESPERA_RESPUESTA)))
        assertFalse(PropuestasViewModel.esperaRespuesta(propuesta(3, EstadoOportunidad.CONFIRMADA)))
    }

    @Test
    fun `el filtro de solo sin respuesta oculta las propuestas ya respondidas`() = runTest {
        // Arrange
        val e = Entorno()
        e.seguimientoDao.propuestas.value = listOf(
            propuesta(1, EstadoOportunidad.PROPUESTA_ENVIADA),
            propuesta(2, EstadoOportunidad.EN_ESPERA_RESPUESTA),
            propuesta(3, EstadoOportunidad.DESCARTADA)
        )
        val vm = PropuestasViewModel(e.sesion, e.seguimientos)
        observar(vm.estado)
        assertEquals(3, vm.estado.value.propuestas.size)
        assertEquals(2, vm.estado.value.totalSinRespuesta)

        // Act
        vm.alternarSoloSinRespuesta()

        // Assert
        assertTrue(vm.estado.value.soloSinRespuesta)
        assertEquals(listOf(1L, 2L), vm.estado.value.propuestas.map { it.id })
        assertFalse(vm.estado.value.esSupervisor)
        assertEquals(Datos.ejecutiva.id, e.seguimientoDao.ultimoAlcance)
    }

    @Test
    fun `el supervisor ve las propuestas de todo el equipo`() = runTest {
        val e = Entorno(usuario = Datos.supervisor)

        val vm = PropuestasViewModel(e.sesion, e.seguimientos)
        observar(vm.estado)

        assertTrue(vm.estado.value.esSupervisor)
        assertNull(e.seguimientoDao.ultimoAlcance)
    }
}
