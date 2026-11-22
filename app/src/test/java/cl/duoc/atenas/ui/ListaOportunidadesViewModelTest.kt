package cl.duoc.atenas.ui

import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.EtapaCartera
import cl.duoc.atenas.domain.model.PotencialComercial
import cl.duoc.atenas.domain.model.Prioridad
import cl.duoc.atenas.domain.model.RangoFecha
import cl.duoc.atenas.domain.model.TipoOportunidad
import cl.duoc.atenas.prueba.Datos
import cl.duoc.atenas.prueba.Entorno
import cl.duoc.atenas.prueba.ReglaCorrutinas
import cl.duoc.atenas.prueba.observar
import cl.duoc.atenas.ui.oportunidades.ListaOportunidadesViewModel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** HU-18: cartera de oportunidades con búsqueda, filtros y pestañas por etapa. */
class ListaOportunidadesViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val e = Entorno()

    private fun TestScope.crear(entorno: Entorno = e): ListaOportunidadesViewModel {
        val vm = ListaOportunidadesViewModel(
            entorno.sesion, entorno.oportunidades, entorno.catalogo, entorno.organizaciones, entorno.usuarios
        )
        observar(vm.estado)
        return vm
    }

    private val busqueda get() = e.oportunidadDao.ultimaBusqueda

    @Test
    fun `las pestanas de etapa cuentan y filtran la cartera en memoria`() = runTest {
        // Arrange
        e.oportunidadDao.agregar(
            Datos.oportunidad(id = 1, estado = EstadoOportunidad.IDENTIFICADA),
            Datos.oportunidad(id = 2, estado = EstadoOportunidad.CONTACTO_PENDIENTE),
            Datos.oportunidad(id = 3, estado = EstadoOportunidad.PROPUESTA_ENVIADA),
            Datos.oportunidad(id = 4, estado = EstadoOportunidad.CONFIRMADA),
            Datos.oportunidad(id = 5, estado = EstadoOportunidad.CERRADA)
        )
        val vm = crear()

        // Act
        vm.alElegirEtapa(EtapaCartera.POR_CONTACTAR)

        // Assert
        val estado = vm.estado.value
        assertEquals(listOf(1L, 2L), estado.oportunidades.map { it.id })
        assertEquals(5, estado.totalSinEtapa)
        assertEquals(2, estado.conteoPorEtapa[EtapaCartera.POR_CONTACTAR])
        assertEquals(1, estado.conteoPorEtapa[EtapaCartera.EN_CURSO])
        assertEquals(1, estado.conteoPorEtapa[EtapaCartera.CONFIRMADAS])
        assertEquals(1, estado.conteoPorEtapa[EtapaCartera.CERRADAS])

        vm.alElegirEtapa(null)
        assertEquals(5, vm.estado.value.oportunidades.size)
    }

    @Test
    fun `el ejecutivo busca solo en su cartera y no ve el filtro de responsable`() = runTest {
        e.oportunidadDao.agregar(
            Datos.oportunidad(id = 1),
            Datos.oportunidad(id = 2, responsableId = Datos.ejecutivo2.id)
        )

        val vm = crear()

        assertEquals(Datos.ejecutiva.id, busqueda["responsableId"])
        assertEquals(listOf(1L), vm.estado.value.oportunidades.map { it.id })
        assertFalse(vm.estado.value.esSupervisor)
        assertTrue(vm.estado.value.ejecutivos.isEmpty())
    }

    @Test
    fun `el supervisor busca en toda la cartera y puede filtrar por ejecutivo`() = runTest {
        // Arrange
        val sup = Entorno(usuario = Datos.supervisor)
        sup.oportunidadDao.agregar(
            Datos.oportunidad(id = 1),
            Datos.oportunidad(id = 2, responsableId = Datos.ejecutivo2.id)
        )
        val vm = crear(sup)
        assertEquals(2, vm.estado.value.oportunidades.size)
        assertEquals(2, vm.estado.value.ejecutivos.size)

        // Act
        vm.alSeleccionarResponsable(Datos.ejecutivo2)

        // Assert
        assertTrue(vm.estado.value.esSupervisor)
        assertNull(sup.oportunidadDao.ultimaBusqueda["responsableId"])
        assertEquals(Datos.ejecutivo2.id, sup.oportunidadDao.ultimaBusqueda["filtroResponsableId"])
        assertEquals(listOf(2L), vm.estado.value.oportunidades.map { it.id })

        vm.alSeleccionarResponsable(null)
        assertNull(vm.estado.value.filtros.responsableId)
    }

    @Test
    fun `cada filtro de chip se activa con un toque y se quita con otro`() = runTest {
        val vm = crear()

        vm.alSeleccionarEstado(EstadoOportunidad.CONFIRMADA)
        vm.alSeleccionarPrioridad(Prioridad.ALTA)
        vm.alSeleccionarTipo(TipoOportunidad.ACTIVIDAD_MUNICIPAL)
        vm.alSeleccionarPotencial(PotencialComercial.ALTO)
        vm.alSeleccionarRangoFecha(RangoFecha.PROXIMOS_7)
        assertEquals(5, vm.estado.value.filtros.cantidadActivos)
        assertEquals("CONFIRMADA", busqueda["estado"])
        assertEquals("ALTA", busqueda["prioridad"])
        assertEquals("ACTIVIDAD_MUNICIPAL", busqueda["tipo"])
        assertEquals("ALTO", busqueda["potencial"])
        assertEquals(Fechas.hoy(), busqueda["desde"])
        assertEquals(Fechas.masDias(Fechas.hoy(), 7), busqueda["hasta"])

        vm.alSeleccionarEstado(EstadoOportunidad.CONFIRMADA)
        vm.alSeleccionarPrioridad(Prioridad.ALTA)
        vm.alSeleccionarTipo(TipoOportunidad.ACTIVIDAD_MUNICIPAL)
        vm.alSeleccionarPotencial(PotencialComercial.ALTO)
        vm.alSeleccionarRangoFecha(RangoFecha.PROXIMOS_7)
        assertEquals(0, vm.estado.value.filtros.cantidadActivos)
        assertNull(busqueda["desde"])
    }

    @Test
    fun `elegir otro valor del mismo filtro lo reemplaza`() = runTest {
        val vm = crear()

        vm.alSeleccionarPrioridad(Prioridad.ALTA)
        vm.alSeleccionarPrioridad(Prioridad.BAJA)

        assertEquals(Prioridad.BAJA, vm.estado.value.filtros.prioridad)
    }

    @Test
    fun `los botones quitar eliminan un filtro puntual`() = runTest {
        val vm = crear()
        vm.alSeleccionarPrioridad(Prioridad.ALTA)
        vm.alSeleccionarTipo(TipoOportunidad.OTRA)
        vm.alSeleccionarPotencial(PotencialComercial.BAJO)
        vm.alSeleccionarRangoFecha(RangoFecha.PASADOS)

        vm.quitarPrioridad()
        vm.quitarTipo()
        vm.quitarPotencial()
        vm.quitarRangoFecha()

        assertEquals(0, vm.estado.value.filtros.cantidadActivos)
    }

    @Test
    fun `cambiar de region limpia la comuna y acota las comunas disponibles`() = runTest {
        // Arrange
        val vm = crear()
        assertEquals(3, vm.estado.value.comunasDisponibles.size)
        vm.alSeleccionarRegion(Datos.rm)
        vm.alSeleccionarComuna(Datos.puenteAlto)
        assertEquals(Datos.puenteAlto.id, vm.estado.value.filtros.comunaId)

        // Act
        vm.alSeleccionarRegion(Datos.valpo)

        // Assert
        assertNull(vm.estado.value.filtros.comunaId)
        assertEquals(listOf("Viña del Mar"), vm.estado.value.comunasDisponibles.map { it.nombre })

        vm.alSeleccionarRegion(null)
        vm.alSeleccionarComuna(null)
        assertNull(vm.estado.value.filtros.regionId)
        assertEquals(3, vm.estado.value.comunasDisponibles.size)
    }

    @Test
    fun `filtrar por organizacion pasa su id a la busqueda`() = runTest {
        val vm = crear()

        vm.alSeleccionarOrganizacion(Datos.organizacion())
        assertEquals(100L, busqueda["organizacionId"])

        vm.alSeleccionarOrganizacion(null)
        assertNull(busqueda["organizacionId"])
        assertEquals(1, vm.estado.value.organizaciones.size)
        assertEquals(2, vm.estado.value.regiones.size)
    }

    @Test
    fun `limpiar filtros conserva el texto buscado`() = runTest {
        val vm = crear()
        vm.alBuscar("feria")
        vm.alSeleccionarEstado(EstadoOportunidad.IDENTIFICADA)
        assertTrue(vm.estado.value.filtros.hayAlguno)

        vm.limpiarFiltros()

        assertEquals("feria", vm.estado.value.filtros.texto)
        assertEquals(0, vm.estado.value.filtros.cantidadActivos)
        assertEquals("feria", busqueda["texto"])
    }

    @Test
    fun `el panel de filtros se abre se alterna y se cierra`() = runTest {
        val vm = crear()

        vm.alternarPanelFiltros()
        assertTrue(vm.estado.value.mostrarFiltros)
        vm.alternarPanelFiltros()
        assertFalse(vm.estado.value.mostrarFiltros)
        vm.alternarPanelFiltros()
        vm.cerrarPanelFiltros()
        assertFalse(vm.estado.value.mostrarFiltros)
    }
}
