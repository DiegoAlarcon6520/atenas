package cl.duoc.atenas.data

import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.core.Seguridad
import cl.duoc.atenas.data.local.entity.Evidencia
import cl.duoc.atenas.data.repository.CriteriosBusqueda
import cl.duoc.atenas.data.repository.EvidenciaRepository
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.PotencialComercial
import cl.duoc.atenas.domain.model.Prioridad
import cl.duoc.atenas.domain.model.RolUsuario
import cl.duoc.atenas.domain.model.TipoAccion
import cl.duoc.atenas.domain.model.TipoOportunidad
import cl.duoc.atenas.domain.model.TipoOrganizacion
import cl.duoc.atenas.prueba.Datos
import cl.duoc.atenas.prueba.Entorno
import cl.duoc.atenas.prueba.EvidenciaDaoFalso
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * PRUEBAS DE LOS REPOSITORIOS
 *
 * Los repositorios traducen el dominio a lo que entiende la base: enum a
 * texto, "próximos 30 días" a un rango de fechas, usuario a minúsculas antes
 * de buscarlo. Estas pruebas verifican esa traducción usando DAO falsos.
 */
class RepositoriosTest {

    private val e = Entorno()

    // ---------- OportunidadRepository ----------

    @Test
    fun `buscar traduce los filtros del dominio a texto y recorta la busqueda`() = runBlocking {
        // Arrange
        val criterios = CriteriosBusqueda(
            texto = "  feria  ",
            estado = EstadoOportunidad.CONFIRMADA,
            prioridad = Prioridad.ALTA,
            tipo = TipoOportunidad.ACTIVIDAD_MUNICIPAL,
            potencial = PotencialComercial.BAJO,
            desde = 10L,
            hasta = 20L,
            regionId = 1L,
            comunaId = 10L,
            organizacionId = 100L,
            responsableId = 2L
        )

        // Act
        e.oportunidades.buscar(responsableId = 1L, filtros = criterios).first()

        // Assert
        val b = e.oportunidadDao.ultimaBusqueda
        assertEquals("feria", b["texto"])
        assertEquals("CONFIRMADA", b["estado"])
        assertEquals("ALTA", b["prioridad"])
        assertEquals("ACTIVIDAD_MUNICIPAL", b["tipo"])
        assertEquals("BAJO", b["potencial"])
        assertEquals(10L, b["desde"])
        assertEquals(20L, b["hasta"])
        assertEquals(1L, b["regionId"])
        assertEquals(10L, b["comunaId"])
        assertEquals(100L, b["organizacionId"])
        assertEquals(2L, b["filtroResponsableId"])
        assertEquals(1L, b["responsableId"])
    }

    @Test
    fun `buscar sin filtros envia nulos para no filtrar`() = runBlocking {
        e.oportunidades.buscar(responsableId = null, filtros = CriteriosBusqueda()).first()

        val b = e.oportunidadDao.ultimaBusqueda
        assertEquals("", b["texto"])
        assertNull(b["estado"])
        assertNull(b["prioridad"])
        assertNull(b["tipo"])
        assertNull(b["potencial"])
        assertNull(b["responsableId"])
    }

    @Test
    fun `proximas calcula el rango desde hoy hasta hoy mas los dias pedidos`() = runBlocking {
        e.oportunidades.observarProximas(dias = 30, responsableId = 1L).first()

        val hoy = Fechas.hoy()
        assertEquals(hoy to Fechas.masDias(hoy, 30), e.oportunidadDao.ultimoRango)
    }

    @Test
    fun `sin contacto consulta solo los estados previos al primer contacto`() = runBlocking {
        e.oportunidades.observarSinContacto(1L).first()

        assertEquals(listOf("IDENTIFICADA", "CONTACTO_PENDIENTE"), e.oportunidadDao.ultimosEstados)
    }

    @Test
    fun `propuestas sin respuesta consulta propuesta enviada y en espera`() = runBlocking {
        e.oportunidades.observarPropuestasSinRespuesta(1L).first()

        assertEquals(listOf("PROPUESTA_ENVIADA", "EN_ESPERA_RESPUESTA"), e.oportunidadDao.ultimosEstados)
    }

    @Test
    fun `cambiar estado y asignar responsable escriben en la base`() = runBlocking {
        // Arrange
        e.oportunidadDao.agregar(Datos.oportunidad(id = 1))

        // Act
        e.oportunidades.cambiarEstado(1, EstadoOportunidad.CONTACTO_PENDIENTE)
        e.oportunidades.asignarResponsable(1, 2L)

        // Assert
        val guardada = e.oportunidades.porId(1)!!
        assertEquals(EstadoOportunidad.CONTACTO_PENDIENTE, guardada.estado)
        assertEquals(2L, guardada.responsableId)
    }

    @Test
    fun `crear actualizar y consultar oportunidades delega en el dao`() = runBlocking {
        val id = e.oportunidades.crear(Datos.oportunidad(id = 0))
        e.oportunidades.actualizar(e.oportunidades.porId(id)!!.copy(nombre = "Otra"))

        assertEquals("Otra", e.oportunidades.observarPorId(id).first()?.nombre)
        assertEquals("Otra", e.oportunidades.observarDetalle(id).first()?.oportunidad?.nombre)
        assertEquals(1, e.oportunidades.observarResumen(null).first().size)
        assertEquals(1, e.oportunidades.observarConteoPorEstado(null).first().single().total)
        assertEquals(1, e.oportunidades.observarPorOrganizacion(100, null).first().size)
        assertTrue(e.oportunidades.observarSinResponsable().first().isEmpty())
    }

    // ---------- SeguimientoRepository ----------

    @Test
    fun `registrar una llamada guarda la gestion cambia el estado y actualiza el contacto`() = runBlocking {
        // Arrange
        e.oportunidadDao.agregar(Datos.oportunidad(id = 1000, estado = EstadoOportunidad.CONTACTO_PENDIENTE))
        val gestion = Datos.seguimiento(
            tipoAccion = TipoAccion.LLAMADA,
            nuevo = EstadoOportunidad.CONTACTO_REALIZADO
        )

        // Act
        val id = e.seguimientos.registrar(gestion, contactoId = 500)

        // Assert
        assertTrue(id > 0)
        assertEquals(1, e.seguimientoDao.seguimientos.value.size)
        assertEquals(EstadoOportunidad.CONTACTO_REALIZADO, e.oportunidadDao.oportunidades.value.single().estado)
        assertEquals(gestion.fechaAccion, e.seguimientoDao.ultimosContactos[500])
    }

    @Test
    fun `una investigacion interna no cuenta como contacto con la organizacion`() = runBlocking {
        e.seguimientos.registrar(Datos.seguimiento(tipoAccion = TipoAccion.INVESTIGACION_INICIAL), contactoId = 500)

        assertTrue(e.seguimientoDao.ultimosContactos.isEmpty())
    }

    @Test
    fun `sin contacto asignado no se actualiza ningun ultimo contacto`() = runBlocking {
        e.seguimientos.registrar(Datos.seguimiento(tipoAccion = TipoAccion.LLAMADA), contactoId = null)

        assertTrue(e.seguimientoDao.ultimosContactos.isEmpty())
    }

    @Test
    fun `atrasadas se compara contra la medianoche de hoy`() = runBlocking {
        e.seguimientos.observarGestionesAtrasadas(1L).first()

        assertEquals(Fechas.hoy(), e.seguimientoDao.ultimoHoy)
        assertEquals(1L, e.seguimientoDao.ultimoAlcance)
    }

    @Test
    fun `gestiones de hoy usan el rango desde hoy hasta manana`() = runBlocking {
        e.seguimientos.observarGestionesDeHoy(null).first()

        val hoy = Fechas.hoy()
        assertEquals(hoy to Fechas.masDias(hoy, 1), e.seguimientoDao.ultimoRango)
    }

    @Test
    fun `historial ultima propuestas y gestiones por organizacion delegan en el dao`() = runBlocking {
        e.seguimientoDao.insertar(Datos.seguimiento(fechaAccion = 1))
        e.seguimientoDao.insertar(Datos.seguimiento(fechaAccion = 5))

        assertEquals(5L, e.seguimientos.ultima(1000)?.fechaAccion)
        assertEquals(2, e.seguimientos.observarHistorial(1000).first().size)
        assertTrue(e.seguimientos.observarPropuestas(null).first().isEmpty())
        assertTrue(e.seguimientos.observarPorOrganizacion(100, 1L).first().isEmpty())
        assertEquals(1L, e.seguimientoDao.ultimoAlcance)
    }

    // ---------- UsuarioRepository ----------

    @Test
    fun `autenticar normaliza el usuario y compara la clave cifrada`() = runBlocking {
        val usuario = e.usuarios.autenticar("  CAMILA ", Datos.CLAVE)

        assertEquals(Datos.ejecutiva.id, usuario?.id)
    }

    @Test
    fun `autenticar con clave incorrecta no devuelve usuario`() = runBlocking {
        assertNull(e.usuarios.autenticar("camila", "otra-clave"))
    }

    @Test
    fun `la clave nunca se compara en texto plano`() {
        assertTrue(Seguridad.hash(Datos.CLAVE) != Datos.CLAVE)
    }

    @Test
    fun `observar ejecutivos pide solo el rol ejecutivo`() = runBlocking {
        val ejecutivos = e.usuarios.observarEjecutivos().first()

        assertEquals(RolUsuario.EJECUTIVO, e.usuarioDao.ultimoRolConsultado)
        assertEquals(2, ejecutivos.size)
        assertEquals(3, e.usuarios.observarTodos().first().size)
        assertEquals(Datos.supervisor.nombreCompleto, e.usuarios.porId(Datos.supervisor.id)?.nombreCompleto)
    }

    // ---------- Organizaciones, contactos, catálogo y evidencias ----------

    @Test
    fun `el listado de organizaciones recorta el texto y traduce el tipo`() = runBlocking {
        e.organizaciones.observarResumen("  vértice ", TipoOrganizacion.MUNICIPALIDAD).first()

        assertEquals("vértice", e.organizacionDao.ultimoTexto)
        assertEquals("MUNICIPALIDAD", e.organizacionDao.ultimoTipo)
    }

    @Test
    fun `el listado de organizaciones sin tipo no filtra por tipo`() = runBlocking {
        e.organizaciones.observarResumen("", null).first()

        assertNull(e.organizacionDao.ultimoTipo)
    }

    @Test
    fun `crear y actualizar organizaciones delega en el dao`() = runBlocking {
        val id = e.organizaciones.crear(Datos.organizacion(id = 0, nombre = "Nueva"))
        e.organizaciones.actualizar(e.organizaciones.porId(id)!!.copy(nombre = "Renombrada"))

        assertEquals("Renombrada", e.organizaciones.observarDetalle(id).first()?.organizacion?.nombre)
        assertEquals(2, e.organizaciones.observarTodas().first().size)
    }

    @Test
    fun `crear y actualizar contactos delega en el dao`() = runBlocking {
        val id = e.contactos.crear(Datos.contacto(id = 0, nombre = "Pedro"))
        e.contactos.actualizar(e.contactos.porId(id)!!.copy(cargo = "Gerente"))

        assertEquals("Gerente", e.contactos.observarPorId(id).first()?.cargo)
        assertEquals(2, e.contactos.observarPorOrganizacion(100).first().size)
        assertEquals(2, e.contactos.observarTodos().first().size)
    }

    @Test
    fun `el catalogo filtra comunas por region`() = runBlocking {
        assertEquals(2, e.catalogo.observarRegiones().first().size)
        assertEquals(3, e.catalogo.observarComunas().first().size)
        assertEquals(listOf("Viña del Mar"), e.catalogo.observarComunasPorRegion(2).first().map { it.nombre })
    }

    @Test
    fun `las evidencias se agregan y eliminan por oportunidad`() = runBlocking {
        // Arrange
        val repo = EvidenciaRepository(EvidenciaDaoFalso())
        val foto = Evidencia(id = 1, oportunidadId = 1000, uri = "file://foto.jpg", descripcion = null, fechaCaptura = 0)

        // Act
        repo.agregar(foto)
        val conFoto = repo.observarPorOportunidad(1000).first()
        repo.eliminar(foto)

        // Assert
        assertEquals(1, conFoto.size)
        assertTrue(repo.observarPorOportunidad(1000).first().isEmpty())
    }

    // ---------- Sesión ----------

    @Test
    fun `requerir usuario sin sesion iniciada es un error`() {
        val sinSesion = Entorno(usuario = null).sesion

        val error = runCatching { sinSesion.requerirUsuario() }.exceptionOrNull()

        assertNotNull(error)
        assertTrue(error is IllegalStateException)
    }

    @Test
    fun `cerrar sesion deja al usuario actual vacio`() {
        e.sesion.cerrarSesion()

        assertNull(e.sesion.usuarioActual.value)
    }
}
