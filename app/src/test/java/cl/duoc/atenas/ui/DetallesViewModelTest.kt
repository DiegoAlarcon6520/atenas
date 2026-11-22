package cl.duoc.atenas.ui

import cl.duoc.atenas.data.local.dao.OrganizacionResumen
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.RolUsuario
import cl.duoc.atenas.domain.model.TipoOrganizacion
import cl.duoc.atenas.prueba.Datos
import cl.duoc.atenas.prueba.Entorno
import cl.duoc.atenas.prueba.ReglaCorrutinas
import cl.duoc.atenas.prueba.observar
import cl.duoc.atenas.ui.oportunidades.DetalleOportunidadViewModel
import cl.duoc.atenas.ui.organizaciones.DetalleOrganizacionViewModel
import cl.duoc.atenas.ui.organizaciones.ListaOrganizacionesViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * HU-08 (detalle de oportunidad y asignación), HU-03 y HU-04 (listado y
 * ficha de organizaciones). Lo central es el control de acceso por perfil.
 */
class DetallesViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    // ---------- Detalle de oportunidad ----------

    private fun detalle(e: Entorno, id: Long = 1) =
        DetalleOportunidadViewModel(id, e.sesion, e.oportunidades, e.seguimientos, e.usuarios)

    @Test
    fun `el ejecutivo responsable ve el detalle y su historial pero no puede asignar`() = runTest {
        // Arrange
        val e = Entorno()
        e.oportunidadDao.agregar(Datos.oportunidad(id = 1))
        e.seguimientoDao.insertar(Datos.seguimiento(oportunidadId = 1))

        // Act
        val vm = detalle(e)
        observar(vm.estado)

        // Assert
        val estado = vm.estado.value
        assertNotNull(estado.detalle)
        assertEquals(1, estado.historial.size)
        assertFalse(estado.sinAcceso)
        assertFalse(estado.puedeAsignar)
        assertTrue(estado.ejecutivos.isEmpty())
        assertEquals(RolUsuario.EJECUTIVO, estado.rol)
    }

    @Test
    fun `un ejecutivo no ve la oportunidad de otro`() = runTest {
        val e = Entorno()
        e.oportunidadDao.agregar(Datos.oportunidad(id = 1, responsableId = Datos.ejecutivo2.id))
        e.seguimientoDao.insertar(Datos.seguimiento(oportunidadId = 1))

        val vm = detalle(e)
        observar(vm.estado)

        assertTrue(vm.estado.value.sinAcceso)
        assertNull(vm.estado.value.detalle)
        assertTrue(vm.estado.value.historial.isEmpty())
    }

    @Test
    fun `una oportunidad inexistente se muestra como sin acceso`() = runTest {
        val e = Entorno(usuario = Datos.supervisor)

        val vm = detalle(e, id = 999)
        observar(vm.estado)

        assertTrue(vm.estado.value.sinAcceso)
    }

    @Test
    fun `el supervisor ve cualquier oportunidad y puede asignarla`() = runTest {
        // Arrange
        val e = Entorno(usuario = Datos.supervisor)
        e.oportunidadDao.agregar(Datos.oportunidad(id = 1, responsableId = null))
        val vm = detalle(e)
        observar(vm.estado)

        // Act
        vm.asignarResponsable(Datos.ejecutivo2.id)

        // Assert
        assertTrue(vm.estado.value.puedeAsignar)
        assertEquals(2, vm.estado.value.ejecutivos.size)
        assertEquals(Datos.ejecutivo2.id, e.oportunidadDao.oportunidades.value.single().responsableId)
        assertEquals("Rodrigo Fuentes", vm.estado.value.detalle?.responsableNombre)
    }

    @Test
    fun `el ejecutivo no puede reasignar aunque lo intente`() = runTest {
        val e = Entorno()
        e.oportunidadDao.agregar(Datos.oportunidad(id = 1))
        val vm = detalle(e)

        vm.asignarResponsable(Datos.ejecutivo2.id)

        assertEquals(Datos.ejecutiva.id, e.oportunidadDao.oportunidades.value.single().responsableId)
        assertTrue(e.oportunidadDao.llamadas.isEmpty())
    }

    // ---------- Ficha de organización ----------

    @Test
    fun `la ficha de la organizacion junta contactos oportunidades y gestiones`() = runTest {
        // Arrange
        val e = Entorno()
        e.oportunidadDao.agregar(
            Datos.oportunidad(id = 1),
            Datos.oportunidad(id = 2, estado = EstadoOportunidad.CONFIRMADA)
        )

        // Act
        val vm = DetalleOrganizacionViewModel(100, e.sesion, e.organizaciones, e.contactos, e.oportunidades, e.seguimientos)
        observar(vm.estado)

        // Assert
        val estado = vm.estado.value
        assertFalse(estado.cargando)
        assertEquals("Constructora Vértice", estado.detalle?.organizacion?.nombre)
        assertEquals(1, estado.contactos.size)
        assertEquals(2, estado.oportunidades.size)
        assertTrue(estado.gestiones.isEmpty())
        assertEquals(Datos.ejecutiva.id, e.oportunidadDao.ultimoAlcance)
        assertEquals(Datos.ejecutiva.id, e.seguimientoDao.ultimoAlcance)
    }

    @Test
    fun `el supervisor ve en la ficha las oportunidades de todo el equipo`() = runTest {
        val e = Entorno(usuario = Datos.supervisor)

        val vm = DetalleOrganizacionViewModel(100, e.sesion, e.organizaciones, e.contactos, e.oportunidades, e.seguimientos)
        observar(vm.estado)

        assertNull(e.oportunidadDao.ultimoAlcance)
        assertNull(e.seguimientoDao.ultimoAlcance)
    }

    // ---------- Listado de organizaciones ----------

    private fun resumen(id: Long, nombre: String, tipo: TipoOrganizacion) =
        OrganizacionResumen(id, nombre, tipo, "Rubro", "Santiago", "RM", totalContactos = 1, totalOportunidades = 2)

    @Test
    fun `buscar y filtrar por tipo acotan el listado de organizaciones`() = runTest {
        // Arrange
        val e = Entorno()
        e.organizacionDao.resumenes.value = listOf(
            resumen(1, "Constructora Vértice", TipoOrganizacion.EMPRESA),
            resumen(2, "Municipalidad de Puente Alto", TipoOrganizacion.MUNICIPALIDAD),
            resumen(3, "Municipalidad de Viña", TipoOrganizacion.MUNICIPALIDAD)
        )
        val vm = ListaOrganizacionesViewModel(e.organizaciones)
        observar(vm.estado)
        assertEquals(3, vm.estado.value.organizaciones.size)
        assertFalse(vm.estado.value.filtros.hayAlguno)

        // Act
        vm.alSeleccionarTipo(TipoOrganizacion.MUNICIPALIDAD)
        vm.alBuscar("viña")

        // Assert
        assertEquals(listOf(3L), vm.estado.value.organizaciones.map { it.id })
        assertTrue(vm.estado.value.filtros.hayAlguno)
    }

    @Test
    fun `elegir el mismo tipo dos veces lo quita y limpiar restaura todo`() = runTest {
        val e = Entorno()
        val vm = ListaOrganizacionesViewModel(e.organizaciones)
        observar(vm.estado)

        vm.alSeleccionarTipo(TipoOrganizacion.EMPRESA)
        vm.alSeleccionarTipo(TipoOrganizacion.EMPRESA)
        assertNull(vm.estado.value.filtros.tipo)

        vm.alBuscar("algo")
        vm.alSeleccionarTipo(TipoOrganizacion.ASOCIACION)
        vm.limpiarFiltros()
        assertEquals("", vm.estado.value.filtros.texto)
        assertNull(vm.estado.value.filtros.tipo)
    }
}
