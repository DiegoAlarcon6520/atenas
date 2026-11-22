package cl.duoc.atenas.ui

import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.TipoAccion
import cl.duoc.atenas.prueba.Datos
import cl.duoc.atenas.prueba.Entorno
import cl.duoc.atenas.prueba.ReglaCorrutinas
import cl.duoc.atenas.prueba.observar
import cl.duoc.atenas.prueba.recolectar
import cl.duoc.atenas.ui.supervisor.PanelSupervisorViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** HU-17 y HU-14: panel del supervisor, métricas del equipo y decisiones. */
class PanelSupervisorViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val e = Entorno(usuario = Datos.supervisor)

    private fun crear() = PanelSupervisorViewModel(e.sesion, e.oportunidades, e.seguimientos, e.usuarios)

    @Test
    fun `el panel calcula metricas decisiones y carga del equipo`() = runTest {
        // Arrange
        e.oportunidadDao.agregar(
            Datos.oportunidad(id = 1, estado = EstadoOportunidad.EN_ESPERA_RESPUESTA, responsableId = 1),
            Datos.oportunidad(id = 2, estado = EstadoOportunidad.CONFIRMADA, responsableId = 1),
            Datos.oportunidad(id = 3, estado = EstadoOportunidad.ACTIVACION_REALIZADA, responsableId = 2),
            Datos.oportunidad(id = 4, estado = EstadoOportunidad.DESCARTADA, responsableId = 2),
            Datos.oportunidad(id = 5, estado = EstadoOportunidad.CERRADA, responsableId = 2),
            Datos.oportunidad(id = 6, estado = EstadoOportunidad.IDENTIFICADA, responsableId = null)
        )
        e.seguimientoDao.atrasadas.value = listOf(
            Datos.gestionPendiente(1, 3, Fechas.masDias(Fechas.hoy(), -1), responsableNombre = Datos.ejecutivo2.nombreCompleto)
        )

        // Act
        val vm = crear()
        observar(vm.estado)

        // Assert
        val estado = vm.estado.value
        assertEquals("Marcelo Silva", estado.nombreUsuario)
        assertEquals(4, estado.enGestion) // 1, 2, 3 y 6: descartada y cerrada no cuentan
        assertEquals(2, estado.confirmadas) // confirmada + activación realizada
        assertEquals(67, estado.conversion) // 2 ganadas de 3 con desenlace
        assertEquals(listOf(1L), estado.decisiones.map { it.id })
        assertEquals(listOf(6L), estado.sinAsignar.map { it.id })
        assertEquals(3, estado.totalAlertas)
        // Camila tiene 2 activas, Rodrigo 1 activa y 1 atrasada; se ordena por carga.
        assertEquals(listOf("Camila Valdés", "Rodrigo Fuentes"), estado.carga.map { it.nombre })
        assertEquals(listOf(2, 1), estado.carga.map { it.enGestion })
        assertEquals(listOf(0, 1), estado.carga.map { it.atrasadas })
    }

    @Test
    fun `sin oportunidades con desenlace la conversion no se calcula`() = runTest {
        e.oportunidadDao.agregar(Datos.oportunidad(id = 1, estado = EstadoOportunidad.IDENTIFICADA))

        val vm = crear()
        observar(vm.estado)

        assertNull(vm.estado.value.conversion)
        assertEquals(0, vm.estado.value.confirmadas)
    }

    @Test
    fun `confirmar desde el panel registra la gestion y cambia el estado`() = runTest {
        // Arrange
        e.oportunidadDao.agregar(Datos.oportunidad(id = 1, estado = EstadoOportunidad.EN_ESPERA_RESPUESTA))
        val vm = crear()
        val mensajes = recolectar(vm.mensajes)

        // Act
        vm.decidir(1, confirmar = true)

        // Assert
        assertEquals(EstadoOportunidad.CONFIRMADA, e.oportunidadDao.oportunidades.value.single().estado)
        val gestion = e.seguimientoDao.seguimientos.value.single()
        assertEquals(TipoAccion.CONFIRMACION, gestion.tipoAccion)
        assertEquals(Datos.supervisor.id, gestion.usuarioId)
        assertEquals(EstadoOportunidad.EN_ESPERA_RESPUESTA, gestion.estadoAnterior)
        assertEquals(listOf("Participación confirmada"), mensajes)
    }

    @Test
    fun `descartar desde el panel registra un cierre`() = runTest {
        e.oportunidadDao.agregar(Datos.oportunidad(id = 1, estado = EstadoOportunidad.EN_ESPERA_RESPUESTA))
        val vm = crear()
        val mensajes = recolectar(vm.mensajes)

        vm.decidir(1, confirmar = false)

        assertEquals(EstadoOportunidad.DESCARTADA, e.oportunidadDao.oportunidades.value.single().estado)
        assertEquals(TipoAccion.CIERRE, e.seguimientoDao.seguimientos.value.single().tipoAccion)
        assertEquals(listOf("Oportunidad descartada"), mensajes)
    }

    @Test
    fun `una transicion invalida no se aplica y avisa`() = runTest {
        // Una oportunidad cerrada no se puede confirmar.
        e.oportunidadDao.agregar(Datos.oportunidad(id = 1, estado = EstadoOportunidad.CERRADA))
        val vm = crear()
        val mensajes = recolectar(vm.mensajes)

        vm.decidir(1, confirmar = true)

        assertEquals(EstadoOportunidad.CERRADA, e.oportunidadDao.oportunidades.value.single().estado)
        assertTrue(e.seguimientoDao.seguimientos.value.isEmpty())
        assertEquals(listOf("No se puede aplicar ese cambio desde el estado actual."), mensajes)
    }

    @Test
    fun `decidir sobre una oportunidad inexistente no hace nada`() = runTest {
        val vm = crear()
        val mensajes = recolectar(vm.mensajes)

        vm.decidir(999, confirmar = true)

        assertTrue(e.seguimientoDao.seguimientos.value.isEmpty())
        assertTrue(mensajes.isEmpty())
    }
}
