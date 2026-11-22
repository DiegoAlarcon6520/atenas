package cl.duoc.atenas.ui

import cl.duoc.atenas.domain.model.AreaContacto
import cl.duoc.atenas.domain.model.MedioContacto
import cl.duoc.atenas.domain.model.TipoOrganizacion
import cl.duoc.atenas.prueba.Datos
import cl.duoc.atenas.prueba.Entorno
import cl.duoc.atenas.prueba.ReglaCorrutinas
import cl.duoc.atenas.ui.organizaciones.FormularioContactoViewModel
import cl.duoc.atenas.ui.organizaciones.FormularioOrganizacionViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** HU-02 y HU-04: registrar y editar organizaciones y sus contactos. */
class FormulariosOrganizacionViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val e = Entorno()

    // ---------- Organización ----------

    private fun organizacion(id: Long = 0) = FormularioOrganizacionViewModel(id, e.organizaciones, e.catalogo)

    private fun FormularioOrganizacionViewModel.llenar() {
        alCambiarNombre("  Municipalidad de Puente Alto ")
        alCambiarTipo(TipoOrganizacion.MUNICIPALIDAD)
        alCambiarRubro(" Sector público ")
        alCambiarRegion(Datos.rm.id)
        alCambiarComuna(Datos.puenteAlto.id)
    }

    @Test
    fun `la organizacion exige nombre rubro y comuna`() {
        val vm = organizacion()
        assertFalse(vm.estado.value.cargando)
        assertEquals(2, vm.estado.value.regiones.size)

        vm.guardar()

        val errores = vm.estado.value.errores
        assertNotNull(errores.nombre)
        assertNotNull(errores.rubro)
        assertNotNull(errores.comuna)
        assertEquals("Revisa los campos marcados", vm.estado.value.errorGeneral)

        vm.llenar()
        assertFalse(vm.estado.value.errores.hayErrores)
    }

    @Test
    fun `cambiar de region acota las comunas y limpia la elegida`() {
        val vm = organizacion()
        vm.alCambiarRegion(Datos.rm.id)
        vm.alCambiarComuna(Datos.santiago.id)

        vm.alCambiarRegion(Datos.valpo.id)

        assertNull(vm.estado.value.comunaId)
        assertEquals(listOf(Datos.vina.id), vm.estado.value.comunasDisponibles.map { it.id })
    }

    @Test
    fun `una organizacion nueva se guarda recortada y sin opcionales vacios`() {
        val vm = organizacion()
        vm.llenar()

        vm.guardar()

        assertTrue(vm.estado.value.guardadoConExito)
        val creada = e.organizacionDao.organizaciones.value.last()
        assertEquals("Municipalidad de Puente Alto", creada.nombre)
        assertEquals("Sector público", creada.rubro)
        assertEquals(TipoOrganizacion.MUNICIPALIDAD, creada.tipo)
        assertEquals(Datos.puenteAlto.id, creada.comunaId)
        assertNull(creada.sitioWeb)
        assertNull(creada.publicoAsociado)
        assertNull(creada.observaciones)
    }

    @Test
    fun `los datos opcionales se guardan cuando se completan`() {
        val vm = organizacion()
        vm.llenar()
        vm.alCambiarSitioWeb("www.mpuentealto.cl")
        vm.alCambiarPublicoAsociado("Vecinos")
        vm.alCambiarObservaciones("Organiza la feria de primavera")

        vm.guardar()

        val creada = e.organizacionDao.organizaciones.value.last()
        assertEquals("www.mpuentealto.cl", creada.sitioWeb)
        assertEquals("Vecinos", creada.publicoAsociado)
        assertEquals("Organiza la feria de primavera", creada.observaciones)
    }

    @Test
    fun `al editar se cargan los datos y se actualiza la misma organizacion`() {
        // Arrange
        e.organizacionDao.organizaciones.value = listOf(
            Datos.organizacion().copy(sitioWeb = "www.vertice.cl", publicoAsociado = "Trabajadores", observaciones = "Cliente")
        )
        val vm = organizacion(id = 100)
        val cargado = vm.estado.value
        assertTrue(cargado.esEdicion)
        assertEquals(Datos.rm.id, cargado.regionId)
        assertEquals(2, cargado.comunasDisponibles.size)
        assertEquals("www.vertice.cl", cargado.sitioWeb)

        // Act
        vm.alCambiarNombre("Constructora Vértice SpA")
        vm.alCambiarSitioWeb("")
        vm.alCambiarPublicoAsociado("")
        vm.alCambiarObservaciones("")
        vm.guardar()

        // Assert
        val guardada = e.organizacionDao.organizaciones.value.single()
        assertEquals("Constructora Vértice SpA", guardada.nombre)
        assertNull(guardada.sitioWeb)
        assertNull(guardada.publicoAsociado)
        assertNull(guardada.observaciones)
    }

    @Test
    fun `al editar una organizacion sin opcionales y con comuna desconocida los campos quedan vacios`() {
        e.organizacionDao.organizaciones.value = listOf(Datos.organizacion(comunaId = 999))

        val vm = organizacion(id = 100)
        assertEquals("", vm.estado.value.sitioWeb)
        assertEquals("", vm.estado.value.publicoAsociado)
        assertNull(vm.estado.value.regionId)
        assertTrue(vm.estado.value.comunasDisponibles.isEmpty())

        vm.alCambiarSitioWeb("www.nuevo.cl")
        vm.alCambiarPublicoAsociado("Familias")
        vm.alCambiarObservaciones("Nota")
        vm.guardar()
        assertEquals("www.nuevo.cl", e.organizacionDao.organizaciones.value.single().sitioWeb)
    }

    @Test
    fun `un id de organizacion inexistente abre el formulario vacio`() {
        val vm = organizacion(id = 999)

        assertFalse(vm.estado.value.esEdicion)
    }

    @Test
    fun `si la organizacion se elimino mientras se editaba se informa`() {
        val vm = organizacion(id = 100)
        e.organizacionDao.organizaciones.value = emptyList()

        vm.guardar()

        assertEquals("No se pudo guardar: La organización ya no existe.", vm.estado.value.errorGeneral)
    }

    @Test
    fun `si la base rechaza la organizacion se informa`() {
        e.organizacionDao.fallar = true
        val vm = organizacion()
        vm.llenar()

        vm.guardar()

        assertFalse(vm.estado.value.guardando)
        assertEquals("No se pudo guardar: la base de datos rechazó la operación", vm.estado.value.errorGeneral)

        e.organizacionDao.mensajeFallo = null
        vm.guardar()
        assertEquals("No se pudo guardar: error desconocido", vm.estado.value.errorGeneral)
    }

    // ---------- Contacto ----------

    private fun contacto(organizacionId: Long = 100, id: Long = 0) =
        FormularioContactoViewModel(organizacionId, id, e.contactos, e.organizaciones)

    private fun FormularioContactoViewModel.llenar() {
        alCambiarNombre(" Pedro Rojas ")
        alCambiarCargo(" Encargado de eventos ")
        alCambiarArea(AreaContacto.EVENTOS)
        alCambiarCorreo(" pedro@vertice.cl ")
        alCambiarTelefono("+56 9 8765 4321")
        alCambiarMedio(MedioContacto.WHATSAPP)
    }

    @Test
    fun `el formulario de contacto muestra a que organizacion pertenece`() {
        assertEquals("Constructora Vértice", contacto().estado.value.nombreOrganizacion)
        assertEquals("", contacto(organizacionId = 999).estado.value.nombreOrganizacion)
    }

    @Test
    fun `el contacto exige nombre cargo correo y telefono validos`() {
        val vm = contacto()
        vm.alCambiarCorreo("sin-arroba")
        vm.alCambiarTelefono("123")

        vm.guardar()

        val errores = vm.estado.value.errores
        assertNotNull(errores.nombre)
        assertNotNull(errores.cargo)
        assertEquals("Ingresa un correo con formato válido", errores.correo)
        assertEquals("Usa el formato +56 9 1234 5678", errores.telefono)

        vm.llenar()
        assertFalse(vm.estado.value.errores.hayErrores)
    }

    @Test
    fun `un contacto nuevo se guarda recortado y sin fecha de ultimo contacto`() {
        val vm = contacto()
        vm.llenar()

        vm.guardar()

        assertTrue(vm.estado.value.guardadoConExito)
        val creado = e.contactoDao.contactos.value.last()
        assertEquals("Pedro Rojas", creado.nombre)
        assertEquals("Encargado de eventos", creado.cargo)
        assertEquals("pedro@vertice.cl", creado.correo)
        assertEquals(MedioContacto.WHATSAPP, creado.medioPreferente)
        assertEquals(100L, creado.organizacionId)
        assertNull(creado.fechaUltimoContacto)
        assertNull(creado.observaciones)
    }

    @Test
    fun `al editar un contacto se puede desactivar y agregar observaciones`() {
        // Arrange
        val vm = contacto(id = 500)
        assertTrue(vm.estado.value.esEdicion)
        assertEquals("Ignacia Morales", vm.estado.value.nombre)
        assertEquals("", vm.estado.value.observaciones)

        // Act
        vm.alCambiarActivo(false)
        vm.alCambiarObservaciones("Dejó la empresa")
        vm.guardar()

        // Assert
        val guardado = e.contactoDao.contactos.value.single()
        assertFalse(guardado.activo)
        assertEquals("Dejó la empresa", guardado.observaciones)
    }

    @Test
    fun `al editar un contacto con observaciones se cargan y se pueden borrar`() {
        e.contactoDao.contactos.value = listOf(Datos.contacto().copy(observaciones = "Prefiere mañanas"))
        val vm = contacto(id = 500)
        assertEquals("Prefiere mañanas", vm.estado.value.observaciones)

        vm.alCambiarObservaciones("")
        vm.guardar()

        assertNull(e.contactoDao.contactos.value.single().observaciones)
    }

    @Test
    fun `un id de contacto inexistente abre el formulario vacio`() {
        assertFalse(contacto(id = 999).estado.value.esEdicion)
    }

    @Test
    fun `si el contacto se elimino mientras se editaba se informa`() {
        val vm = contacto(id = 500)
        e.contactoDao.contactos.value = emptyList()

        vm.guardar()

        assertEquals("No se pudo guardar: El contacto ya no existe.", vm.estado.value.errorGeneral)
    }

    @Test
    fun `si la base rechaza el contacto se informa`() {
        e.contactoDao.fallar = true
        val vm = contacto()
        vm.llenar()
        vm.alCambiarObservaciones("Nota")

        vm.guardar()
        assertEquals("No se pudo guardar: la base de datos rechazó la operación", vm.estado.value.errorGeneral)

        e.contactoDao.mensajeFallo = null
        vm.guardar()
        assertEquals("No se pudo guardar: error desconocido", vm.estado.value.errorGeneral)
        assertFalse(vm.estado.value.guardando)
    }
}
