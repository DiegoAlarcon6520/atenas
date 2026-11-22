package cl.duoc.atenas.ui

import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.domain.model.AfinidadPublico
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.PotencialComercial
import cl.duoc.atenas.domain.model.Prioridad
import cl.duoc.atenas.domain.model.TipoOportunidad
import cl.duoc.atenas.domain.rules.TransicionesEstado
import cl.duoc.atenas.prueba.Datos
import cl.duoc.atenas.prueba.Entorno
import cl.duoc.atenas.prueba.ReglaCorrutinas
import cl.duoc.atenas.prueba.errorHttp
import cl.duoc.atenas.ui.oportunidades.FormularioOportunidadViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * HU-05, HU-06 y HU-07: registrar y editar oportunidades con un formulario
 * por pasos, validación por paso, prioridad sugerida y reglas por perfil.
 */
class FormularioOportunidadViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val hoy = Fechas.hoy()

    private fun crear(e: Entorno, id: Long = 0) =
        FormularioOportunidadViewModel(id, e.sesion, e.oportunidades, e.catalogo, e.organizaciones, e.contactos)

    /** Completa todos los campos obligatorios con datos válidos. */
    private fun llenarValido(vm: FormularioOportunidadViewModel) {
        vm.alCambiarNombre("Feria de Beneficios")
        vm.alCambiarDescripcion("Feria anual para trabajadores y sus familias")
        vm.alCambiarFuente("Sitio web")
        vm.alCambiarFechaInicio(Fechas.masDias(hoy, 10))
        vm.alCambiarDireccion("Av. Principal 123")
        vm.alCambiarRegion(Datos.rm.id)
        vm.alCambiarComuna(Datos.santiago.id)
        vm.alCambiarOrganizacion(100)
        vm.alCambiarPublicoObjetivo("Trabajadores")
        vm.alCambiarPublicoEsperado("300")
    }

    // ---------- Carga inicial ----------

    @Test
    fun `al abrir el formulario se cargan los catalogos segun el perfil`() {
        val vm = crear(Entorno())

        val estado = vm.estado.value
        assertFalse(estado.cargando)
        assertFalse(estado.esEdicion)
        assertFalse(estado.puedeCambiarPrioridad)
        assertEquals(2, estado.regiones.size)
        assertEquals(1, estado.organizaciones.size)
    }

    @Test
    fun `el supervisor puede cambiar la prioridad`() {
        val vm = crear(Entorno(usuario = Datos.supervisor))

        assertTrue(vm.estado.value.puedeCambiarPrioridad)
    }

    // ---------- Navegación por pasos ----------

    @Test
    fun `el primer paso vacio marca solo sus propios errores`() {
        // Arrange
        val vm = crear(Entorno())

        // Act
        vm.pasoSiguiente()

        // Assert
        val estado = vm.estado.value
        assertEquals(0, estado.paso)
        assertNotNull(estado.errores.nombre)
        assertNotNull(estado.errores.descripcion)
        assertNotNull(estado.errores.fuenteInformacion)
        assertNull(estado.errores.comuna)
        assertNull(estado.errores.organizacion)
        assertEquals("Revisa los campos marcados", estado.errorGeneral)
    }

    @Test
    fun `con datos validos se recorren los cuatro pasos sin pasarse del ultimo`() {
        val vm = crear(Entorno())
        llenarValido(vm)

        repeat(5) { vm.pasoSiguiente() }

        assertEquals(FormularioOportunidadViewModel.TOTAL_PASOS - 1, vm.estado.value.paso)
        assertNull(vm.estado.value.errorGeneral)
    }

    @Test
    fun `volver desde el primer paso no baja de cero`() {
        val vm = crear(Entorno())
        llenarValido(vm)
        vm.pasoSiguiente()

        vm.pasoAnterior()
        vm.pasoAnterior()

        assertEquals(0, vm.estado.value.paso)
    }

    @Test
    fun `el segundo paso exige fecha comuna direccion y un horario coherente`() {
        // Arrange
        val vm = crear(Entorno())
        vm.alCambiarNombre("Feria de Beneficios")
        vm.alCambiarDescripcion("Feria anual para trabajadores")
        vm.alCambiarFuente("Sitio web")
        vm.pasoSiguiente()
        vm.alCambiarHoraInicio(18 * 60)
        vm.alCambiarHoraTermino(10 * 60)

        // Act
        vm.pasoSiguiente()

        // Assert
        val errores = vm.estado.value.errores
        assertEquals(1, vm.estado.value.paso)
        assertNotNull(errores.fechaInicioEvento)
        assertNotNull(errores.comuna)
        assertNotNull(errores.direccion)
        assertEquals("La hora de término debe ser posterior a la de inicio", errores.horario)
        assertNull(errores.nombre)
    }

    @Test
    fun `el evento no puede ser antes de su deteccion ni terminar antes de empezar`() {
        val vm = crear(Entorno())
        llenarValido(vm)
        vm.pasoSiguiente()
        vm.alCambiarFechaInicio(Fechas.masDias(hoy, -3))
        vm.alCambiarFechaTermino(Fechas.masDias(hoy, -5))

        vm.pasoSiguiente()

        val errores = vm.estado.value.errores
        assertEquals("El evento no puede ocurrir antes de haberse detectado", errores.fechaInicioEvento)
        assertEquals("La fecha de término no puede ser anterior al inicio", errores.fechaTerminoEvento)
    }

    @Test
    fun `el tercer y cuarto paso exigen organizacion y publico`() {
        // Arrange
        val vm = crear(Entorno())
        llenarValido(vm)
        vm.alCambiarOrganizacion(0)
        vm.alCambiarPublicoObjetivo("")
        vm.alCambiarPublicoEsperado("")
        vm.pasoSiguiente()
        vm.pasoSiguiente()

        // Act + Assert: paso 3 (organización)
        vm.pasoSiguiente()
        assertEquals(2, vm.estado.value.paso)
        assertNotNull(vm.estado.value.errores.organizacion)

        // Act + Assert: paso 4 (público)
        vm.alCambiarOrganizacion(100)
        vm.pasoSiguiente()
        vm.pasoSiguiente()
        assertEquals(3, vm.estado.value.paso)
        assertNotNull(vm.estado.value.errores.publicoObjetivo)
        assertNotNull(vm.estado.value.errores.publicoEsperado)
    }

    @Test
    fun `corregir un campo borra su mensaje de error`() {
        val vm = crear(Entorno())
        vm.guardar()
        assertTrue(vm.estado.value.errores.hayErrores)

        llenarValido(vm)
        vm.alCambiarFechaTermino(null)
        vm.alCambiarHoraInicio(9 * 60)
        vm.alCambiarHoraTermino(18 * 60)
        vm.alCambiarCosto("")

        assertFalse(vm.estado.value.errores.hayErrores)
    }

    @Test
    fun `los campos numericos descartan lo que no son digitos`() {
        val vm = crear(Entorno())

        vm.alCambiarPublicoEsperado("1.5a00")
        vm.alCambiarCosto("$ 20.000")

        assertEquals("1500", vm.estado.value.publicoEsperado)
        assertEquals("20000", vm.estado.value.costoReferencial)
    }

    // ---------- Catálogos dependientes ----------

    @Test
    fun `elegir region acota las comunas y limpia la comuna elegida`() {
        val vm = crear(Entorno())
        vm.alCambiarRegion(Datos.rm.id)
        vm.alCambiarComuna(Datos.puenteAlto.id)

        vm.alCambiarRegion(Datos.valpo.id)

        assertNull(vm.estado.value.comunaId)
        assertEquals(listOf(Datos.vina.id), vm.estado.value.comunasDisponibles.map { it.id })
    }

    @Test
    fun `elegir organizacion ofrece solo sus contactos activos`() {
        // Arrange
        val e = Entorno()
        e.contactoDao.contactos.value = listOf(
            Datos.contacto(id = 1),
            Datos.contacto(id = 2, activo = false),
            Datos.contacto(id = 3, organizacionId = 200)
        )
        val vm = crear(e)
        vm.alCambiarOrganizacion(100)
        vm.alCambiarContacto(1)

        // Act
        vm.alCambiarOrganizacion(100)

        // Assert
        assertNull(vm.estado.value.contactoId)
        assertEquals(listOf(1L), vm.estado.value.contactosDisponibles.map { it.id })
    }

    @Test
    fun `una organizacion registrada desde el paso 3 aparece en la lista sin perder lo escrito`() {
        // Arrange: el usuario ya avanzó y escribió datos.
        val e = Entorno()
        val vm = crear(e)
        llenarValido(vm)

        // Act: mientras el formulario está abierto se registra otra organización.
        e.organizacionDao.organizaciones.value += Datos.organizacion(id = 200, nombre = "Municipalidad de Cerro Verde")

        // Assert: está disponible para elegirla, pero no se elige sola.
        assertEquals(listOf(100L, 200L), vm.estado.value.organizaciones.map { it.id })
        assertEquals(100L, vm.estado.value.organizacionId)
        assertEquals("Feria de Beneficios", vm.estado.value.nombre)
    }

    @Test
    fun `un contacto registrado desde el paso 3 aparece para la organizacion elegida sin seleccionarse`() {
        // Arrange
        val e = Entorno()
        val vm = crear(e)
        vm.alCambiarOrganizacion(100)
        assertEquals(listOf(500L), vm.estado.value.contactosDisponibles.map { it.id })

        // Act: se registra un contacto en esa organización y otro en una distinta.
        e.contactoDao.contactos.value += listOf(
            Datos.contacto(id = 501, nombre = "Tomás Riquelme"),
            Datos.contacto(id = 600, organizacionId = 200)
        )

        // Assert
        assertEquals(listOf(500L, 501L), vm.estado.value.contactosDisponibles.map { it.id })
        assertNull(vm.estado.value.contactoId)
    }

    // ---------- Prioridad sugerida (HU-06) ----------

    @Test
    fun `al ejecutivo se le asigna la prioridad sugerida y no puede cambiarla`() {
        val vm = crear(Entorno())

        vm.alCambiarPotencial(PotencialComercial.ALTO)
        vm.alCambiarAfinidad(AfinidadPublico.ALTA)
        vm.alCambiarPrioridad(Prioridad.BAJA)
        vm.aplicarPrioridadSugerida()

        assertEquals(Prioridad.ALTA, vm.estado.value.prioridad)
        assertEquals(Prioridad.ALTA, vm.estado.value.prioridadSugerida)
    }

    @Test
    fun `el supervisor elige la prioridad y puede aplicar la sugerida`() {
        val vm = crear(Entorno(usuario = Datos.supervisor))

        vm.alCambiarPrioridad(Prioridad.BAJA)
        vm.alCambiarPotencial(PotencialComercial.ALTO)
        vm.alCambiarAfinidad(AfinidadPublico.ALTA)
        assertEquals(Prioridad.BAJA, vm.estado.value.prioridad)

        vm.aplicarPrioridadSugerida()
        assertEquals(Prioridad.ALTA, vm.estado.value.prioridad)
    }

    // ---------- Guardar una oportunidad nueva ----------

    @Test
    fun `el ejecutivo crea la oportunidad a su cargo en el inicio del embudo`() {
        // Arrange
        val e = Entorno()
        val vm = crear(e)
        llenarValido(vm)
        vm.alCambiarNombre("  Feria de Beneficios  ")
        vm.alCambiarPotencial(PotencialComercial.BAJO)
        vm.alCambiarAfinidad(AfinidadPublico.BAJA)

        // Act
        vm.guardar()

        // Assert
        assertTrue(vm.estado.value.guardadoConExito)
        assertFalse(vm.estado.value.guardando)
        val creada = e.oportunidadDao.oportunidades.value.single()
        assertEquals("Feria de Beneficios", creada.nombre)
        assertEquals(Datos.ejecutiva.id, creada.responsableId)
        assertEquals(Datos.ejecutiva.id, creada.creadoPorId)
        assertEquals(EstadoOportunidad.IDENTIFICADA, creada.estado)
        assertEquals(Prioridad.BAJA, creada.prioridad)
        assertEquals(0.0, creada.costoReferencial, 0.0)
        assertNull(creada.condiciones)
        assertNull(creada.requisitos)
        assertNull(creada.espaciosDisponibles)
        assertNull(creada.observaciones)
    }

    @Test
    fun `lo que crea el supervisor queda sin responsable y con su prioridad`() {
        // Arrange
        val e = Entorno(usuario = Datos.supervisor)
        val vm = crear(e)
        llenarValido(vm)
        vm.alCambiarTipo(TipoOportunidad.ACTIVIDAD_MUNICIPAL)
        vm.alCambiarPrioridad(Prioridad.ALTA)
        vm.alCambiarContacto(500)
        vm.alCambiarCosto("150000")
        vm.alCambiarCondiciones("Pago al contado")
        vm.alCambiarRequisitos("Seguro vigente")
        vm.alCambiarEspacios("4 stands")
        vm.alCambiarObservaciones("Contactar en marzo")

        // Act
        vm.guardar()

        // Assert
        val creada = e.oportunidadDao.oportunidades.value.single()
        assertNull(creada.responsableId)
        assertEquals(Prioridad.ALTA, creada.prioridad)
        assertEquals(TipoOportunidad.ACTIVIDAD_MUNICIPAL, creada.tipo)
        assertEquals(500L, creada.contactoId)
        assertEquals(150000.0, creada.costoReferencial, 0.0)
        assertEquals("Pago al contado", creada.condiciones)
        assertEquals("Seguro vigente", creada.requisitos)
        assertEquals("4 stands", creada.espaciosDisponibles)
        assertEquals("Contactar en marzo", creada.observaciones)
    }

    @Test
    fun `guardar con errores vuelve al primer paso que los tiene`() {
        // Arrange: todo válido salvo la comuna (paso 2 de 4).
        val vm = crear(Entorno())
        llenarValido(vm)
        vm.alCambiarRegion(Datos.valpo.id)
        repeat(3) { vm.pasoSiguiente() }

        // Act
        vm.guardar()

        // Assert
        assertEquals(1, vm.estado.value.paso)
        assertEquals("Revisa los campos marcados antes de guardar", vm.estado.value.errorGeneral)
        assertFalse(vm.estado.value.guardadoConExito)
    }

    @Test
    fun `si la base rechaza la operacion se informa y no se cierra la pantalla`() {
        val e = Entorno()
        e.oportunidadDao.fallar = true
        val vm = crear(e)
        llenarValido(vm)

        vm.guardar()

        assertFalse(vm.estado.value.guardadoConExito)
        assertFalse(vm.estado.value.guardando)
        assertEquals("No se pudo guardar: la base de datos rechazó la operación", vm.estado.value.errorGeneral)
    }

    @Test
    fun `si el servidor rechaza la oportunidad se muestra su motivo`() {
        val e = Entorno()
        e.api.fallo = errorHttp(400, "La comuna 10 no existe")
        val vm = crear(e)
        llenarValido(vm)

        vm.guardar()

        assertEquals("No se pudo guardar: La comuna 10 no existe", vm.estado.value.errorGeneral)
        assertTrue(e.oportunidadDao.oportunidades.value.isEmpty())
    }

    @Test
    fun `un error sin mensaje se informa como desconocido`() {
        val e = Entorno()
        e.oportunidadDao.fallar = true
        e.oportunidadDao.mensajeFallo = null
        val vm = crear(e)
        llenarValido(vm)

        vm.guardar()

        assertEquals("No se pudo guardar: error desconocido", vm.estado.value.errorGeneral)
    }

    // ---------- Edición ----------

    @Test
    fun `al editar se cargan los datos y el contacto asignado aunque este inactivo`() {
        // Arrange
        val e = Entorno()
        e.contactoDao.contactos.value = listOf(
            Datos.contacto(id = 1),
            Datos.contacto(id = 2, activo = false),
            Datos.contacto(id = 3, activo = false),
            Datos.contacto(id = 4, organizacionId = 200)
        )
        e.oportunidadDao.agregar(
            Datos.oportunidad(id = 7, contactoId = 2, costoReferencial = 50000.0, comunaId = Datos.puenteAlto.id)
                .copy(condiciones = "Pago a 30 días", espaciosDisponibles = "2 módulos")
        )

        // Act
        val vm = crear(e, id = 7)

        // Assert
        val estado = vm.estado.value
        assertTrue(estado.esEdicion)
        assertTrue(estado.admiteCambios)
        assertNull(estado.errorGeneral)
        assertEquals("Feria de Beneficios", estado.nombre)
        assertEquals(Datos.rm.id, estado.regionId)
        assertEquals(listOf(Datos.santiago.id, Datos.puenteAlto.id), estado.comunasDisponibles.map { it.id })
        assertEquals(listOf(1L, 2L), estado.contactosDisponibles.map { it.id })
        assertEquals("50000", estado.costoReferencial)
        assertEquals("300", estado.publicoEsperado)
        assertEquals("Pago a 30 días", estado.condiciones)
        assertEquals("", estado.requisitos)
    }

    @Test
    fun `un costo cero se muestra como campo vacio`() {
        val e = Entorno()
        e.oportunidadDao.agregar(Datos.oportunidad(id = 7, costoReferencial = 0.0))

        val vm = crear(e, id = 7)

        assertEquals("", vm.estado.value.costoReferencial)
    }

    @Test
    fun `si la comuna guardada no esta en el catalogo no se elige region`() {
        val e = Entorno()
        e.oportunidadDao.agregar(Datos.oportunidad(id = 7, comunaId = 999))

        val vm = crear(e, id = 7)

        assertNull(vm.estado.value.regionId)
        assertTrue(vm.estado.value.comunasDisponibles.isEmpty())
    }

    @Test
    fun `al editar el ejecutivo conserva la prioridad original`() {
        // Arrange
        val e = Entorno()
        e.oportunidadDao.agregar(Datos.oportunidad(id = 7, prioridad = Prioridad.BAJA, estado = EstadoOportunidad.CONTACTO_PENDIENTE))
        val vm = crear(e, id = 7)
        vm.alCambiarPotencial(PotencialComercial.ALTO)
        vm.alCambiarAfinidad(AfinidadPublico.ALTA)
        vm.alCambiarNombre("Feria renovada")

        // Act
        vm.guardar()

        // Assert
        val guardada = e.oportunidadDao.oportunidades.value.single()
        assertTrue(vm.estado.value.guardadoConExito)
        assertEquals("Feria renovada", guardada.nombre)
        assertEquals(Prioridad.BAJA, guardada.prioridad)
        assertEquals(PotencialComercial.ALTO, guardada.potencial)
        assertEquals(EstadoOportunidad.CONTACTO_PENDIENTE, guardada.estado)
        assertEquals(Datos.ejecutiva.id, guardada.responsableId)
    }

    @Test
    fun `al editar el supervisor puede cambiar la prioridad y los datos opcionales`() {
        val e = Entorno(usuario = Datos.supervisor)
        e.oportunidadDao.agregar(Datos.oportunidad(id = 7, prioridad = Prioridad.BAJA))
        val vm = crear(e, id = 7)
        vm.alCambiarPrioridad(Prioridad.ALTA)
        vm.alCambiarCosto("9900")
        vm.alCambiarCondiciones("Con factura")
        vm.alCambiarRequisitos("Permiso municipal")
        vm.alCambiarEspacios("1 stand")
        vm.alCambiarObservaciones("Cliente frecuente")

        vm.guardar()

        val guardada = e.oportunidadDao.oportunidades.value.single()
        assertEquals(Prioridad.ALTA, guardada.prioridad)
        assertEquals(9900.0, guardada.costoReferencial, 0.0)
        assertEquals("Con factura", guardada.condiciones)
        assertEquals("Permiso municipal", guardada.requisitos)
        assertEquals("1 stand", guardada.espaciosDisponibles)
        assertEquals("Cliente frecuente", guardada.observaciones)
    }

    @Test
    fun `una oportunidad cerrada se abre en solo lectura y no se puede guardar`() {
        // Arrange
        val e = Entorno()
        e.oportunidadDao.agregar(Datos.oportunidad(id = 7, estado = EstadoOportunidad.CERRADA))
        val vm = crear(e, id = 7)
        assertFalse(vm.estado.value.admiteCambios)
        assertEquals(TransicionesEstado.MENSAJE_CERRADA, vm.estado.value.errorGeneral)

        // Act
        vm.alCambiarNombre("Intento de cambio")
        vm.guardar()

        // Assert
        assertEquals("Feria de Beneficios", e.oportunidadDao.oportunidades.value.single().nombre)
        assertEquals(TransicionesEstado.MENSAJE_CERRADA, vm.estado.value.errorGeneral)
        assertFalse(vm.estado.value.guardadoConExito)
    }

    @Test
    fun `un id que no existe abre el formulario vacio`() {
        val vm = crear(Entorno(), id = 999)

        assertFalse(vm.estado.value.esEdicion)
        assertEquals("", vm.estado.value.nombre)
    }

    @Test
    fun `si la oportunidad se elimino mientras se editaba se informa`() {
        // Arrange
        val e = Entorno()
        e.oportunidadDao.agregar(Datos.oportunidad(id = 7))
        val vm = crear(e, id = 7)
        e.oportunidadDao.quitar(7)

        // Act
        vm.guardar()

        // Assert
        assertEquals("No se pudo guardar: La oportunidad ya no existe.", vm.estado.value.errorGeneral)
    }
}
