package cl.duoc.atenas.ui

import cl.duoc.atenas.data.local.dao.OrganizacionResumen
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.RolUsuario
import cl.duoc.atenas.domain.model.TipoOrganizacion
import cl.duoc.atenas.prueba.Datos
import cl.duoc.atenas.prueba.Entorno
import cl.duoc.atenas.prueba.ReglaCorrutinas
import cl.duoc.atenas.prueba.errorHttp
import cl.duoc.atenas.prueba.observar
import cl.duoc.atenas.prueba.recolectar
import cl.duoc.atenas.data.repository.SinConexionException
import java.io.IOException
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
        val mensajes = recolectar(vm.mensajes)

        // Act
        vm.asignarResponsable(Datos.ejecutivo2.id)

        // Assert
        assertEquals(listOf("Responsable asignado"), mensajes)
        assertEquals(Datos.supervisor.id, e.api.ultimaAsignacion?.usuarioId)
        assertTrue(vm.estado.value.puedeAsignar)
        assertEquals(2, vm.estado.value.ejecutivos.size)
        assertEquals(Datos.ejecutivo2.id, e.oportunidadDao.oportunidades.value.single().responsableId)
        assertEquals("Rodrigo Fuentes", vm.estado.value.detalle?.responsableNombre)
    }

    @Test
    fun `quitar el responsable deja la oportunidad sin asignar`() = runTest {
        val e = Entorno(usuario = Datos.supervisor)
        e.oportunidadDao.agregar(Datos.oportunidad(id = 1))
        val vm = detalle(e)
        val mensajes = recolectar(vm.mensajes)

        vm.asignarResponsable(null)

        assertEquals(listOf("Responsable quitado"), mensajes)
        assertNull(e.oportunidadDao.oportunidades.value.single().responsableId)
    }

    @Test
    fun `si la asignacion falla la oportunidad no cambia y se avisa el motivo`() = runTest {
        // Arrange: el servidor no responde.
        val e = Entorno(usuario = Datos.supervisor)
        e.oportunidadDao.agregar(Datos.oportunidad(id = 1))
        e.api.fallo = IOException("sin red")
        val vm = detalle(e)
        val mensajes = recolectar(vm.mensajes)

        // Act
        vm.asignarResponsable(Datos.ejecutivo2.id)

        // Assert
        assertEquals(Datos.ejecutiva.id, e.oportunidadDao.oportunidades.value.single().responsableId)
        assertEquals(listOf("No se pudo asignar: ${SinConexionException().message}"), mensajes)
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

    // ---------- Eliminar oportunidad ----------

    @Test
    fun `el supervisor elimina una oportunidad sin gestiones y la pantalla vuelve atras`() = runTest {
        // Arrange
        val e = Entorno(usuario = Datos.supervisor)
        e.oportunidadDao.agregar(Datos.oportunidad(id = 1))
        val vm = detalle(e)
        observar(vm.estado)
        val mensajes = recolectar(vm.mensajes)
        val eliminada = recolectar(vm.eliminada)
        assertTrue(vm.estado.value.puedeEliminar)

        // Act
        vm.eliminar()

        // Assert: primero el servidor, después la copia local.
        assertEquals(listOf("eliminarOportunidad"), e.api.llamadas)
        assertTrue(e.oportunidadDao.oportunidades.value.isEmpty())
        assertEquals(listOf("Oportunidad eliminada"), mensajes)
        assertEquals(1, eliminada.size)
    }

    @Test
    fun `una oportunidad con gestiones no se puede eliminar`() = runTest {
        val e = Entorno(usuario = Datos.supervisor)
        e.oportunidadDao.agregar(Datos.oportunidad(id = 1))
        e.seguimientoDao.insertar(Datos.seguimiento(oportunidadId = 1))
        val vm = detalle(e)
        observar(vm.estado)

        vm.eliminar()

        assertFalse(vm.estado.value.puedeEliminar)
        assertTrue(e.api.llamadas.isEmpty())
        assertEquals(1, e.oportunidadDao.oportunidades.value.size)
    }

    @Test
    fun `el ejecutivo no puede eliminar oportunidades`() = runTest {
        val e = Entorno()
        e.oportunidadDao.agregar(Datos.oportunidad(id = 1))
        val vm = detalle(e)
        observar(vm.estado)

        vm.eliminar()

        assertFalse(vm.estado.value.puedeEliminar)
        assertTrue(e.api.llamadas.isEmpty())
    }

    @Test
    fun `si el servidor rechaza la eliminacion la oportunidad se mantiene y se explica`() = runTest {
        // Arrange: otro usuario registró una gestión que aún no llega a este teléfono.
        val e = Entorno(usuario = Datos.supervisor)
        e.oportunidadDao.agregar(Datos.oportunidad(id = 1))
        e.api.fallo = errorHttp(409, "La oportunidad tiene gestiones registradas")
        val vm = detalle(e)
        observar(vm.estado)
        val mensajes = recolectar(vm.mensajes)
        val eliminada = recolectar(vm.eliminada)

        // Act
        vm.eliminar()

        // Assert
        assertEquals(1, e.oportunidadDao.oportunidades.value.size)
        assertEquals(listOf("No se pudo eliminar: La oportunidad tiene gestiones registradas"), mensajes)
        assertTrue(eliminada.isEmpty())
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

    private fun ficha(e: Entorno) =
        DetalleOrganizacionViewModel(100, e.sesion, e.organizaciones, e.contactos, e.oportunidades, e.seguimientos)

    @Test
    fun `el supervisor elimina una organizacion sin oportunidades`() = runTest {
        val e = Entorno(usuario = Datos.supervisor)
        val vm = ficha(e)
        observar(vm.estado)
        val mensajes = recolectar(vm.mensajes)
        val eliminada = recolectar(vm.eliminada)
        assertTrue(vm.estado.value.puedeEliminar)

        vm.eliminar()

        assertEquals(listOf("eliminarOrganizacion"), e.api.llamadas)
        assertTrue(e.organizacionDao.organizaciones.value.isEmpty())
        assertEquals(listOf("Organización eliminada"), mensajes)
        assertEquals(1, eliminada.size)
    }

    @Test
    fun `una organizacion con oportunidades no se puede eliminar ni por el ejecutivo`() = runTest {
        // Con oportunidades: ni el supervisor.
        val conOportunidades = Entorno(usuario = Datos.supervisor)
        conOportunidades.oportunidadDao.agregar(Datos.oportunidad(id = 1))
        val vm = ficha(conOportunidades)
        observar(vm.estado)
        vm.eliminar()
        assertFalse(vm.estado.value.puedeEliminar)
        assertTrue(conOportunidades.api.llamadas.isEmpty())

        // Sin oportunidades pero ejecutivo: tampoco.
        val ejecutivo = Entorno()
        val vmEjecutivo = ficha(ejecutivo)
        observar(vmEjecutivo.estado)
        vmEjecutivo.eliminar()
        assertFalse(vmEjecutivo.estado.value.puedeEliminar)
        assertTrue(ejecutivo.api.llamadas.isEmpty())
    }

    @Test
    fun `si la organizacion no se puede eliminar se avisa y se mantiene`() = runTest {
        val e = Entorno(usuario = Datos.supervisor)
        e.api.fallo = IOException("sin red")
        val vm = ficha(e)
        observar(vm.estado)
        val mensajes = recolectar(vm.mensajes)

        vm.eliminar()

        assertEquals(1, e.organizacionDao.organizaciones.value.size)
        assertEquals(listOf("No se pudo eliminar: ${SinConexionException().message}"), mensajes)
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
