package cl.duoc.atenas.ui

import cl.duoc.atenas.prueba.Datos
import cl.duoc.atenas.prueba.Entorno
import cl.duoc.atenas.prueba.ReglaCorrutinas
import cl.duoc.atenas.ui.login.LoginViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** HU-01: inicio de sesión con validación de campos y credenciales. */
class LoginViewModelTest {

    @get:Rule
    val corrutinas = ReglaCorrutinas()

    private val e = Entorno(usuario = null)
    private val vm = LoginViewModel(e.usuarios, e.sesion)

    @Test
    fun `campos vacios muestran ambos errores y no consultan la base`() {
        // Act
        vm.iniciarSesion()

        // Assert
        val estado = vm.estado.value
        assertNotNull(estado.errorUsuario)
        assertNotNull(estado.errorClave)
        assertNull(e.sesion.usuarioActual.value)
    }

    @Test
    fun `solo la clave corta muestra solo el error de clave`() {
        vm.alCambiarUsuario("camila")
        vm.alCambiarClave("123")

        vm.iniciarSesion()

        assertNull(vm.estado.value.errorUsuario)
        assertNotNull(vm.estado.value.errorClave)
    }

    @Test
    fun `solo el usuario corto muestra solo el error de usuario`() {
        vm.alCambiarUsuario("ca")
        vm.alCambiarClave(Datos.CLAVE)

        vm.iniciarSesion()

        assertNotNull(vm.estado.value.errorUsuario)
        assertNull(vm.estado.value.errorClave)
    }

    @Test
    fun `credenciales correctas inician la sesion`() {
        // Arrange
        vm.alCambiarUsuario("Camila")
        vm.alCambiarClave(Datos.CLAVE)

        // Act
        vm.iniciarSesion()

        // Assert
        assertEquals(Datos.ejecutiva.id, e.sesion.usuarioActual.value?.id)
        assertFalse(vm.estado.value.cargando)
        assertNull(vm.estado.value.errorGeneral)
    }

    @Test
    fun `credenciales incorrectas muestran un error general y conservan lo escrito`() {
        vm.alCambiarUsuario("camila")
        vm.alCambiarClave("incorrecta")

        vm.iniciarSesion()

        assertEquals("Usuario o contraseña incorrectos", vm.estado.value.errorGeneral)
        assertEquals("camila", vm.estado.value.usuario)
        assertNull(e.sesion.usuarioActual.value)
    }

    @Test
    fun `escribir de nuevo limpia los errores del campo`() {
        vm.iniciarSesion()

        vm.alCambiarUsuario("c")
        vm.alCambiarClave("x")

        assertNull(vm.estado.value.errorUsuario)
        assertNull(vm.estado.value.errorClave)
    }

    @Test
    fun `mostrar clave y ayuda se alternan`() {
        vm.alternarVisibilidadClave()
        vm.alternarAyuda()
        assertTrue(vm.estado.value.mostrarClave)
        assertTrue(vm.estado.value.mostrarAyuda)

        vm.alternarVisibilidadClave()
        vm.alternarAyuda()
        assertFalse(vm.estado.value.mostrarClave)
        assertFalse(vm.estado.value.mostrarAyuda)
    }
}
