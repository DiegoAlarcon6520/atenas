package cl.duoc.atenas.ui

import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.data.local.entity.Usuario
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.TipoAccion
import cl.duoc.atenas.domain.rules.TransicionesEstado
import cl.duoc.atenas.prueba.Datos
import cl.duoc.atenas.prueba.Entorno
import cl.duoc.atenas.prueba.ReglaCorrutinas
import cl.duoc.atenas.ui.seguimiento.RegistrarSeguimientoViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * HU-09, HU-10 y HU-11: registrar gestiones, mover la oportunidad por el
 * embudo respetando las transiciones del perfil y comprometer la próxima
 * acción.
 */
class RegistrarSeguimientoViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val hoy = Fechas.hoy()
    private val resultadoValido = "Se conversó con la jefa de bienestar."

    private fun entorno(
        estado: EstadoOportunidad,
        usuario: Usuario = Datos.ejecutiva
    ) = Entorno(usuario).apply { oportunidadDao.agregar(Datos.oportunidad(id = 1000, estado = estado)) }

    private fun crear(e: Entorno, id: Long = 1000) =
        RegistrarSeguimientoViewModel(id, e.sesion, e.oportunidades, e.seguimientos)

    // ---------- Carga ----------

    @Test
    fun `una oportunidad inexistente muestra un error`() {
        val vm = crear(Entorno(), id = 999)

        assertFalse(vm.estado.value.cargando)
        assertEquals("La oportunidad ya no existe.", vm.estado.value.errorGeneral)
    }

    @Test
    fun `el ejecutivo solo puede avanzar y ve la restriccion del supervisor`() {
        val vm = crear(entorno(EstadoOportunidad.CONTACTO_PENDIENTE))

        val estado = vm.estado.value
        assertEquals(Datos.oportunidad().nombre, estado.nombreOportunidad)
        assertEquals(listOf(EstadoOportunidad.CONTACTO_REALIZADO), estado.estadosDisponibles)
        assertTrue(estado.estadosAnteriores.isEmpty())
        assertNotNull(estado.mensajeRestriccion)
        assertFalse(estado.hayGestionAnterior)
        assertFalse(estado.esCierre)
        assertTrue(estado.admiteCambios)
    }

    @Test
    fun `el supervisor puede confirmar descartar y retroceder`() {
        val e = entorno(EstadoOportunidad.EN_ESPERA_RESPUESTA, Datos.supervisor)
        e.seguimientoDao.seguimientos.value = listOf(Datos.seguimiento(id = 1))

        val vm = crear(e)

        val estado = vm.estado.value
        assertEquals(listOf(EstadoOportunidad.CONFIRMADA, EstadoOportunidad.DESCARTADA), estado.estadosDisponibles)
        assertEquals(4, estado.estadosAnteriores.size)
        assertNull(estado.mensajeRestriccion)
        assertTrue(estado.hayGestionAnterior)
    }

    @Test
    fun `una oportunidad cerrada no admite nuevas gestiones`() {
        // Arrange
        val e = entorno(EstadoOportunidad.CERRADA, Datos.supervisor)
        val vm = crear(e)
        assertFalse(vm.estado.value.admiteCambios)
        assertTrue(vm.estado.value.esCierre)
        assertEquals(TransicionesEstado.MENSAJE_CERRADA, vm.estado.value.errorGeneral)

        // Act
        vm.alCambiarResultado(resultadoValido)
        vm.guardar()

        // Assert
        assertTrue(e.seguimientoDao.seguimientos.value.isEmpty())
        assertEquals(TransicionesEstado.MENSAJE_CERRADA, vm.estado.value.errorGeneral)
    }

    // ---------- Tipo de acción y estado ----------

    @Test
    fun `enviar una propuesta preselecciona el estado propuesta enviada`() {
        val vm = crear(entorno(EstadoOportunidad.CONTACTO_REALIZADO))

        vm.alCambiarTipoAccion(TipoAccion.ENVIO_PROPUESTA)

        assertEquals(TipoAccion.ENVIO_PROPUESTA, vm.estado.value.tipoAccion)
        assertEquals(EstadoOportunidad.PROPUESTA_ENVIADA, vm.estado.value.nuevoEstado)
    }

    @Test
    fun `la preseleccion no pisa un estado ya elegido`() {
        val vm = crear(entorno(EstadoOportunidad.CONTACTO_REALIZADO, Datos.supervisor))
        vm.alSeleccionarNuevoEstado(EstadoOportunidad.DESCARTADA)

        vm.alCambiarTipoAccion(TipoAccion.ENVIO_PROPUESTA)

        assertEquals(EstadoOportunidad.DESCARTADA, vm.estado.value.nuevoEstado)
    }

    @Test
    fun `no se preselecciona si el estado no esta disponible o la accion es otra`() {
        val vm = crear(entorno(EstadoOportunidad.IDENTIFICADA))

        vm.alCambiarTipoAccion(TipoAccion.ENVIO_PROPUESTA)
        assertNull(vm.estado.value.nuevoEstado)

        vm.alCambiarTipoAccion(TipoAccion.LLAMADA)
        assertNull(vm.estado.value.nuevoEstado)
    }

    @Test
    fun `elegir un estado de cierre borra la proxima accion y tocarlo de nuevo lo quita`() {
        // Arrange
        val vm = crear(entorno(EstadoOportunidad.CONTACTO_REALIZADO, Datos.supervisor))
        vm.alCambiarProximaAccion("Llamar el lunes")
        vm.alElegirPlazo(3)

        // Act
        vm.alSeleccionarNuevoEstado(EstadoOportunidad.DESCARTADA)

        // Assert
        assertTrue(vm.estado.value.esCierre)
        assertEquals("", vm.estado.value.proximaAccion)
        assertNull(vm.estado.value.fechaProximoSeguimiento)

        vm.alSeleccionarNuevoEstado(EstadoOportunidad.DESCARTADA)
        assertNull(vm.estado.value.nuevoEstado)
        assertFalse(vm.estado.value.esCierre)
    }

    @Test
    fun `elegir un estado de avance conserva la proxima accion`() {
        val vm = crear(entorno(EstadoOportunidad.CONTACTO_REALIZADO))
        vm.alCambiarProximaAccion("Enviar propuesta")
        vm.alElegirPlazo(2)

        vm.alSeleccionarNuevoEstado(EstadoOportunidad.PROPUESTA_ENVIADA)

        assertEquals("Enviar propuesta", vm.estado.value.proximaAccion)
        assertEquals(Fechas.masDias(hoy, 2), vm.estado.value.fechaProximoSeguimiento)
    }

    // ---------- Reutilizar la gestión anterior ----------

    @Test
    fun `autorrellenar copia los datos de la ultima gestion`() {
        val e = entorno(EstadoOportunidad.CONTACTO_PENDIENTE)
        e.seguimientoDao.seguimientos.value = listOf(
            Datos.seguimiento(id = 1, tipoAccion = TipoAccion.REUNION, proximaAccion = "Enviar ficha", observaciones = "Prefiere correo")
        )
        val vm = crear(e)

        vm.autorrellenarConAnterior()

        assertEquals(TipoAccion.REUNION, vm.estado.value.tipoAccion)
        assertEquals("Se conversó con la contraparte.", vm.estado.value.resultado)
        assertEquals("Enviar ficha", vm.estado.value.proximaAccion)
        assertEquals("Prefiere correo", vm.estado.value.observaciones)
    }

    @Test
    fun `autorrellenar con campos vacios y en una gestion de cierre no deja proxima accion`() {
        val e = entorno(EstadoOportunidad.CONTACTO_PENDIENTE, Datos.supervisor)
        e.seguimientoDao.seguimientos.value = listOf(Datos.seguimiento(id = 1, proximaAccion = "Llamar"))
        val vm = crear(e)
        vm.alSeleccionarNuevoEstado(EstadoOportunidad.DESCARTADA)

        vm.autorrellenarConAnterior()
        assertEquals("", vm.estado.value.proximaAccion)

        e.seguimientoDao.seguimientos.value = listOf(Datos.seguimiento(id = 2, proximaAccion = null, observaciones = null))
        vm.alSeleccionarNuevoEstado(EstadoOportunidad.DESCARTADA)
        vm.autorrellenarConAnterior()
        assertEquals("", vm.estado.value.proximaAccion)
        assertEquals("", vm.estado.value.observaciones)
    }

    @Test
    fun `sin gestion anterior autorrellenar no cambia nada`() {
        val vm = crear(entorno(EstadoOportunidad.CONTACTO_PENDIENTE))
        vm.alCambiarResultado("Texto propio")

        vm.autorrellenarConAnterior()

        assertEquals("Texto propio", vm.estado.value.resultado)
    }

    // ---------- Validaciones ----------

    @Test
    fun `un resultado corto y una fecha futura no se aceptan`() {
        val vm = crear(entorno(EstadoOportunidad.CONTACTO_PENDIENTE))
        vm.alCambiarResultado("Ok")
        vm.alCambiarFechaAccion(Fechas.masDias(hoy, 2))

        vm.guardar()

        val errores = vm.estado.value.errores
        assertNotNull(errores.resultado)
        assertEquals("La fecha de la gestión no puede ser futura", errores.fechaAccion)
        assertEquals("Revisa los campos marcados", vm.estado.value.errorGeneral)

        vm.alCambiarResultado(resultadoValido)
        vm.alCambiarFechaAccion(hoy)
        assertFalse(vm.estado.value.errores.hayErrores)
    }

    @Test
    fun `una proxima accion sin fecha pide indicar cuando`() {
        val vm = crear(entorno(EstadoOportunidad.CONTACTO_PENDIENTE))
        vm.alCambiarResultado(resultadoValido)
        vm.alCambiarProximaAccion("Volver a llamar")

        vm.guardar()

        assertEquals("Indica cuándo harás la próxima acción", vm.estado.value.errores.fechaProximoSeguimiento)
    }

    @Test
    fun `el proximo seguimiento no puede ser anterior a la gestion`() {
        val vm = crear(entorno(EstadoOportunidad.CONTACTO_PENDIENTE))
        vm.alCambiarResultado(resultadoValido)
        vm.alCambiarFechaProximoSeguimiento(Fechas.masDias(hoy, -1))

        vm.guardar()

        assertEquals(
            "El próximo seguimiento no puede ser anterior a esta gestión",
            vm.estado.value.errores.fechaProximoSeguimiento
        )
    }

    // ---------- Guardar ----------

    @Test
    fun `una gestion valida se guarda con el cambio de estado y actualiza el contacto`() {
        // Arrange
        val e = entorno(EstadoOportunidad.CONTACTO_PENDIENTE)
        val vm = crear(e)
        vm.alCambiarTipoAccion(TipoAccion.LLAMADA)
        vm.alCambiarResultado("  $resultadoValido  ")
        vm.alSeleccionarNuevoEstado(EstadoOportunidad.CONTACTO_REALIZADO)
        vm.alCambiarProximaAccion("Enviar presentación")
        vm.alElegirPlazo(3)

        // Act
        vm.guardar()

        // Assert
        assertTrue(vm.estado.value.guardadoConExito)
        val gestion = e.seguimientoDao.seguimientos.value.single()
        assertEquals(resultadoValido, gestion.resultado)
        assertEquals(EstadoOportunidad.CONTACTO_PENDIENTE, gestion.estadoAnterior)
        assertEquals(EstadoOportunidad.CONTACTO_REALIZADO, gestion.estadoNuevo)
        assertEquals("Enviar presentación", gestion.proximaAccion)
        assertEquals(Fechas.masDias(hoy, 3), gestion.fechaProximoSeguimiento)
        assertNull(gestion.observaciones)
        assertEquals(EstadoOportunidad.CONTACTO_REALIZADO, e.oportunidadDao.oportunidades.value.single().estado)
        assertEquals(hoy, e.seguimientoDao.ultimosContactos[500])
    }

    @Test
    fun `sin elegir estado la oportunidad se mantiene donde estaba`() {
        val e = entorno(EstadoOportunidad.CONTACTO_PENDIENTE)
        val vm = crear(e)
        vm.alCambiarResultado(resultadoValido)
        vm.alCambiarObservaciones("Pidió llamar después de las 15:00")

        vm.guardar()

        val gestion = e.seguimientoDao.seguimientos.value.single()
        assertEquals(EstadoOportunidad.CONTACTO_PENDIENTE, gestion.estadoNuevo)
        assertNull(gestion.proximaAccion)
        assertEquals("Pidió llamar después de las 15:00", gestion.observaciones)
    }

    @Test
    fun `una gestion de cierre no deja seguimiento pendiente`() {
        val e = entorno(EstadoOportunidad.EN_ESPERA_RESPUESTA, Datos.supervisor)
        val vm = crear(e)
        vm.alCambiarResultado(resultadoValido)
        vm.alSeleccionarNuevoEstado(EstadoOportunidad.DESCARTADA)

        vm.guardar()

        val gestion = e.seguimientoDao.seguimientos.value.single()
        assertEquals(EstadoOportunidad.DESCARTADA, gestion.estadoNuevo)
        assertNull(gestion.proximaAccion)
        assertNull(gestion.fechaProximoSeguimiento)
    }

    @Test
    fun `el ejecutivo no puede confirmar aunque fuerce el estado`() {
        val e = entorno(EstadoOportunidad.EN_ESPERA_RESPUESTA)
        val vm = crear(e)
        vm.alCambiarResultado(resultadoValido)
        vm.alSeleccionarNuevoEstado(EstadoOportunidad.CONFIRMADA)

        vm.guardar()

        assertEquals("Tu perfil no puede aplicar ese cambio de estado.", vm.estado.value.errorGeneral)
        assertFalse(vm.estado.value.guardando)
        assertTrue(e.seguimientoDao.seguimientos.value.isEmpty())
    }

    @Test
    fun `si la base rechaza la gestion se informa el error`() {
        val e = entorno(EstadoOportunidad.CONTACTO_PENDIENTE)
        e.seguimientoDao.fallar = true
        val vm = crear(e)
        vm.alCambiarResultado(resultadoValido)

        vm.guardar()

        assertEquals("No se pudo registrar: la base de datos rechazó la operación", vm.estado.value.errorGeneral)
        assertFalse(vm.estado.value.guardadoConExito)
    }

    @Test
    fun `un error sin mensaje se informa como desconocido`() {
        val e = entorno(EstadoOportunidad.CONTACTO_PENDIENTE)
        e.seguimientoDao.fallar = true
        e.seguimientoDao.mensajeFallo = null
        val vm = crear(e)
        vm.alCambiarResultado(resultadoValido)

        vm.guardar()

        assertEquals("No se pudo registrar: error desconocido", vm.estado.value.errorGeneral)
    }
}
