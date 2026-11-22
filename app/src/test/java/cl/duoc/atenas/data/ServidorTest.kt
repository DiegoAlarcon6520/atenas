package cl.duoc.atenas.data

import cl.duoc.atenas.core.Seguridad
import cl.duoc.atenas.data.local.entity.Evidencia
import cl.duoc.atenas.data.remote.ComunaDto
import cl.duoc.atenas.data.remote.RegionDto
import cl.duoc.atenas.data.remote.SincronizacionDto
import cl.duoc.atenas.data.repository.ErrorDelServidor
import cl.duoc.atenas.data.repository.SinConexionException
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.prueba.Datos
import cl.duoc.atenas.prueba.Entorno
import cl.duoc.atenas.prueba.aDto
import cl.duoc.atenas.prueba.errorHttp
import cl.duoc.atenas.prueba.errorHttpCrudo
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * PRUEBAS DE LA COMUNICACIÓN CON EL SERVIDOR
 *
 * El servidor es la fuente de verdad y Room una copia para trabajar sin
 * conexión. Estas pruebas cubren las decisiones de esa relación:
 *  - qué pasa en el login con y sin conexión,
 *  - cómo la sincronización reemplaza la copia local sin perder la clave
 *    guardada para entrar sin red,
 *  - qué ve el usuario cuando el servidor rechaza algo o no responde,
 *  - que una escritura rechazada no deje la copia local distinta del servidor.
 */
class ServidorTest {

    private val e = Entorno(usuario = null)

    // ---------- Login ----------

    @Test
    fun `un login aceptado guarda una copia local del usuario con su clave cifrada`() = runBlocking {
        // Arrange: el teléfono todavía no conoce a nadie.
        e.usuarioDao.usuarios.value = emptyList()

        // Act
        val usuario = e.usuarios.autenticar(" Camila ", Datos.CLAVE)

        // Assert
        assertEquals(Datos.ejecutiva.id, usuario?.id)
        val copia = e.usuarioDao.porId(Datos.ejecutiva.id)
        assertEquals(Seguridad.hash(Datos.CLAVE), copia?.claveHash)
    }

    @Test
    fun `si el servidor dice que la clave es incorrecta no se entra aunque la copia local coincida`() = runBlocking {
        // El servidor ya no acepta esa clave (por ejemplo, porque se cambió).
        e.api.usuariosServidor.replaceAll { it.copy(claveHash = Seguridad.hash("clave-nueva")) }

        assertNull(e.usuarios.autenticar("camila", Datos.CLAVE))
    }

    @Test
    fun `sin conexion se entra con la copia local`() = runBlocking {
        e.api.fallo = IOException("sin red")

        val usuario = e.usuarios.autenticar("camila", Datos.CLAVE)

        assertEquals(Datos.ejecutiva.id, usuario?.id)
    }

    @Test
    fun `sin conexion una clave incorrecta tampoco entra`() = runBlocking {
        e.api.fallo = IOException("sin red")

        assertNull(e.usuarios.autenticar("camila", "otra-clave"))
    }

    @Test
    fun `si el servidor falla por su cuenta se usa la copia local`() = runBlocking {
        e.api.fallo = errorHttp(500, "Error interno")

        assertEquals(Datos.ejecutiva.id, e.usuarios.autenticar("camila", Datos.CLAVE)?.id)
    }

    // ---------- Sincronización ----------

    @Test
    fun `la sincronizacion reemplaza la copia local por la del servidor`() = runBlocking {
        // Arrange: localmente hay una oportunidad que el servidor ya no tiene.
        e.oportunidadDao.agregar(Datos.oportunidad(id = 1, nombre = "Borrada en el servidor"))
        val usuarioNuevo = Datos.ejecutiva.copy(id = 50, nombreUsuario = "nuevo", nombreCompleto = "Nueva Ejecutiva")
        e.api.instantanea = SincronizacionDto(
            generadoEn = 0,
            usuarios = listOf(Datos.ejecutiva.aDto(), usuarioNuevo.aDto()),
            regiones = listOf(RegionDto(1, "Región Metropolitana")),
            comunas = listOf(ComunaDto(10, "Santiago", 1)),
            organizaciones = emptyList(),
            contactos = emptyList(),
            oportunidades = listOf(Datos.oportunidad(id = 7, nombre = "Desde el servidor").aDto()),
            seguimientos = emptyList(),
            evidencias = emptyList()
        )

        // Act
        e.sincronizacion.sincronizar(Datos.ejecutiva.id)

        // Assert
        assertEquals(listOf("Desde el servidor"), e.oportunidadDao.oportunidades.value.map { it.nombre })
        assertEquals(listOf("Santiago"), e.catalogoDao.comunas.value.map { it.nombre })
        val usuarios = e.usuarioDao.usuarios.value.associateBy { it.id }
        // Quien ya había entrado conserva su clave local para entrar sin red...
        assertEquals(Seguridad.hash(Datos.CLAVE), usuarios.getValue(Datos.ejecutiva.id).claveHash)
        // ...y un usuario que nunca entró en este teléfono queda sin clave local.
        assertEquals("", usuarios.getValue(50).claveHash)
    }

    @Test
    fun `sin conexion la sincronizacion avisa y no toca la copia local`() = runBlocking {
        e.oportunidadDao.agregar(Datos.oportunidad(id = 1))
        e.api.fallo = IOException("sin red")

        val error = runCatching { e.sincronizacion.sincronizar(1) }.exceptionOrNull()

        assertTrue(error is SinConexionException)
        assertEquals(1, e.oportunidadDao.oportunidades.value.size)
    }

    // ---------- Errores del servidor ----------

    @Test
    fun `cuando el servidor rechaza algo se muestra su mensaje y no se guarda nada local`() = runBlocking {
        e.api.fallo = errorHttp(409, "La organización ya existe")

        val error = runCatching { e.organizaciones.crear(Datos.organizacion(id = 0)) }.exceptionOrNull()

        assertTrue(error is ErrorDelServidor)
        assertEquals(409, (error as ErrorDelServidor).codigo)
        assertEquals("La organización ya existe", error.message)
        assertEquals(1, e.organizacionDao.organizaciones.value.size)
    }

    @Test
    fun `un error que no viene en el formato del backend muestra un texto generico`() = runBlocking {
        e.api.fallo = errorHttpCrudo(502, "<html>Bad Gateway</html>")

        val error = runCatching { e.contactos.crear(Datos.contacto(id = 0)) }.exceptionOrNull()

        assertEquals("El servidor respondió con un error (502)", error?.message)
    }

    @Test
    fun `un error en JSON pero sin mensaje tambien muestra el texto generico`() = runBlocking {
        e.api.fallo = errorHttpCrudo(500, """{"estado": 500}""")

        val error = runCatching { e.contactos.crear(Datos.contacto(id = 0)) }.exceptionOrNull()

        assertEquals("El servidor respondió con un error (500)", error?.message)
    }

    @Test
    fun `sin conexion una escritura avisa que no hay red y la copia local no cambia`() = runBlocking {
        e.oportunidadDao.agregar(Datos.oportunidad(id = 1, estado = EstadoOportunidad.CONTACTO_PENDIENTE))
        e.api.fallo = IOException("sin red")

        val error = runCatching {
            e.seguimientos.registrar(Datos.seguimiento(oportunidadId = 1), contactoId = 500)
        }.exceptionOrNull()

        assertTrue(error is SinConexionException)
        assertTrue(e.seguimientoDao.seguimientos.value.isEmpty())
        assertEquals(EstadoOportunidad.CONTACTO_PENDIENTE, e.oportunidadDao.oportunidades.value.single().estado)
    }

    @Test
    fun `al editar un contacto se conserva la fecha de ultimo contacto que lleva el servidor`() = runBlocking {
        e.contactoDao.contactos.value = listOf(Datos.contacto().copy(fechaUltimoContacto = 1234L))

        e.contactos.actualizar(Datos.contacto().copy(cargo = "Gerente", fechaUltimoContacto = null))

        val guardado = e.contactoDao.porId(500)!!
        assertEquals("Gerente", guardado.cargo)
        assertEquals(1234L, guardado.fechaUltimoContacto)
    }

    @Test
    fun `una evidencia que el servidor no pudo borrar sigue en la lista`() = runBlocking {
        val id = e.evidencias.agregar(
            Evidencia(oportunidadId = 1000, uri = "file://f.jpg", descripcion = null, fechaCaptura = 0)
        )
        val guardada = e.evidenciaDao.evidencias.value.single()
        e.api.fallo = IOException("sin red")

        runCatching { e.evidencias.eliminar(guardada) }

        assertEquals(listOf(id), e.evidencias.observarPorOportunidad(1000).first().map { it.id })
    }
}
