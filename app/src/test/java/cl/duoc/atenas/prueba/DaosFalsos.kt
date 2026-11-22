package cl.duoc.atenas.prueba

import cl.duoc.atenas.data.local.dao.CatalogoDao
import cl.duoc.atenas.data.local.dao.ConteoPorEstado
import cl.duoc.atenas.data.local.dao.ContactoDao
import cl.duoc.atenas.data.local.dao.EvidenciaDao
import cl.duoc.atenas.data.local.dao.GestionDeOrganizacion
import cl.duoc.atenas.data.local.dao.GestionPendiente
import cl.duoc.atenas.data.local.dao.OportunidadDao
import cl.duoc.atenas.data.local.dao.OportunidadDetalle
import cl.duoc.atenas.data.local.dao.OportunidadResumen
import cl.duoc.atenas.data.local.dao.OrganizacionDao
import cl.duoc.atenas.data.local.dao.OrganizacionDetalle
import cl.duoc.atenas.data.local.dao.OrganizacionResumen
import cl.duoc.atenas.data.local.dao.PropuestaEnviada
import cl.duoc.atenas.data.local.dao.SeguimientoDao
import cl.duoc.atenas.data.local.dao.SincronizacionDao
import cl.duoc.atenas.data.local.dao.UsuarioDao
import cl.duoc.atenas.data.local.entity.Comuna
import cl.duoc.atenas.data.local.entity.Contacto
import cl.duoc.atenas.data.local.entity.Evidencia
import cl.duoc.atenas.data.local.entity.Oportunidad
import cl.duoc.atenas.data.local.entity.Organizacion
import cl.duoc.atenas.data.local.entity.Region
import cl.duoc.atenas.data.local.entity.Seguimiento
import cl.duoc.atenas.data.local.entity.Usuario
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.RolUsuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/*
 * DAO FALSOS EN MEMORIA
 *
 * Reemplazan a Room en las pruebas locales: guardan los datos en listas y
 * los exponen como Flow, igual que la base real. Además registran con qué
 * argumentos se los llamó, para comprobar que los repositorios traducen bien
 * los filtros. No buscan reproducir SQL: solo lo necesario para que cada
 * prueba controle exactamente qué datos ve el ViewModel.
 *
 * `fallar = true` hace que las escrituras lancen una excepción, para probar
 * cómo reacciona la pantalla cuando la base rechaza una operación. Con
 * `mensajeFallo = null` la excepción llega sin mensaje, como algunos errores
 * reales de SQLite.
 */

class FalloSimulado(mensaje: String? = "la base de datos rechazó la operación") : RuntimeException(mensaje)

class UsuarioDaoFalso(usuarios: List<Usuario> = listOf(Datos.ejecutiva, Datos.ejecutivo2, Datos.supervisor)) : UsuarioDao {
    val usuarios = MutableStateFlow(usuarios)
    var ultimoRolConsultado: RolUsuario? = null

    override suspend fun autenticar(nombreUsuario: String, claveHash: String): Usuario? =
        usuarios.value.firstOrNull { it.nombreUsuario == nombreUsuario && it.claveHash == claveHash && it.activo }

    override suspend fun porId(id: Long): Usuario? = usuarios.value.firstOrNull { it.id == id }
    override fun observarTodos(): Flow<List<Usuario>> = usuarios
    override fun observarPorRol(rol: RolUsuario): Flow<List<Usuario>> {
        ultimoRolConsultado = rol
        return usuarios.map { lista -> lista.filter { it.rol == rol && it.activo } }
    }
    override suspend fun contar(): Int = usuarios.value.size
    override suspend fun insertar(usuarios: List<Usuario>): List<Long> {
        this.usuarios.update { it + usuarios }
        return usuarios.map { it.id }
    }
    override suspend fun actualizar(usuario: Usuario) {
        usuarios.update { lista -> lista.map { if (it.id == usuario.id) usuario else it } }
    }
    override suspend fun guardar(usuario: Usuario) {
        usuarios.update { lista -> lista.filterNot { it.id == usuario.id } + usuario }
    }
}

class CatalogoDaoFalso : CatalogoDao {
    val regiones = MutableStateFlow(listOf(Datos.rm, Datos.valpo))
    val comunas = MutableStateFlow(listOf(Datos.santiago, Datos.puenteAlto, Datos.vina))

    override fun observarRegiones(): Flow<List<Region>> = regiones
    override fun observarComunas(): Flow<List<Comuna>> = comunas
    override fun observarComunasPorRegion(regionId: Long): Flow<List<Comuna>> =
        comunas.map { lista -> lista.filter { it.regionId == regionId } }
    override suspend fun contarRegiones(): Int = regiones.value.size
    override suspend fun insertarRegiones(regiones: List<Region>): List<Long> = regiones.map { it.id }
    override suspend fun insertarComunas(comunas: List<Comuna>): List<Long> = comunas.map { it.id }
}

class OrganizacionDaoFalso : OrganizacionDao {
    val organizaciones = MutableStateFlow(listOf(Datos.organizacion()))
    val resumenes = MutableStateFlow<List<OrganizacionResumen>>(emptyList())
    var fallar = false
    var mensajeFallo: String? = "la base de datos rechazó la operación"
    var ultimoTexto: String? = null
    var ultimoTipo: String? = null
    private var siguienteId = 900L

    override fun observarResumen(texto: String, tipo: String?): Flow<List<OrganizacionResumen>> {
        ultimoTexto = texto
        ultimoTipo = tipo
        return resumenes.map { lista ->
            lista.filter { (texto.isEmpty() || it.nombre.contains(texto, ignoreCase = true)) && (tipo == null || it.tipo.name == tipo) }
        }
    }

    override fun observarDetalle(id: Long): Flow<OrganizacionDetalle?> = organizaciones.map { lista ->
        lista.firstOrNull { it.id == id }?.let { OrganizacionDetalle(it, "Santiago", "Región Metropolitana") }
    }

    override fun observarTodas(): Flow<List<Organizacion>> = organizaciones
    override suspend fun porId(id: Long): Organizacion? = organizaciones.value.firstOrNull { it.id == id }

    override suspend fun insertar(organizacion: Organizacion): Long {
        if (fallar) throw FalloSimulado(mensajeFallo)
        val id = if (organizacion.id != 0L) organizacion.id else siguienteId++
        organizaciones.update { it + organizacion.copy(id = id) }
        return id
    }

    override suspend fun insertarTodas(organizaciones: List<Organizacion>): List<Long> = organizaciones.map { insertar(it) }

    override suspend fun actualizar(organizacion: Organizacion) {
        if (fallar) throw FalloSimulado(mensajeFallo)
        organizaciones.update { lista -> lista.map { if (it.id == organizacion.id) organizacion else it } }
    }
}

class ContactoDaoFalso : ContactoDao {
    val contactos = MutableStateFlow(listOf(Datos.contacto()))
    var fallar = false
    var mensajeFallo: String? = "la base de datos rechazó la operación"
    private var siguienteId = 800L

    override fun observarPorOrganizacion(organizacionId: Long): Flow<List<Contacto>> =
        contactos.map { lista -> lista.filter { it.organizacionId == organizacionId } }
    override fun observarTodos(): Flow<List<Contacto>> = contactos
    override fun observarPorId(id: Long): Flow<Contacto?> = contactos.map { lista -> lista.firstOrNull { it.id == id } }
    override suspend fun porId(id: Long): Contacto? = contactos.value.firstOrNull { it.id == id }

    override suspend fun insertar(contacto: Contacto): Long {
        if (fallar) throw FalloSimulado(mensajeFallo)
        val id = if (contacto.id != 0L) contacto.id else siguienteId++
        contactos.update { it + contacto.copy(id = id) }
        return id
    }

    override suspend fun insertarTodos(contactos: List<Contacto>): List<Long> = contactos.map { insertar(it) }

    override suspend fun actualizar(contacto: Contacto) {
        if (fallar) throw FalloSimulado(mensajeFallo)
        contactos.update { lista -> lista.map { if (it.id == contacto.id) contacto else it } }
    }
}

/**
 * Oportunidades en memoria. Los resúmenes se arman a partir de las entidades,
 * con nombres de organización, comuna y responsable tomados de los mapas.
 */
class OportunidadDaoFalso : OportunidadDao {
    val oportunidades = MutableStateFlow<List<Oportunidad>>(emptyList())
    val nombresUsuario = mutableMapOf(
        Datos.ejecutiva.id to Datos.ejecutiva.nombreCompleto,
        Datos.ejecutivo2.id to Datos.ejecutivo2.nombreCompleto,
        Datos.supervisor.id to Datos.supervisor.nombreCompleto
    )
    var contacto: Contacto? = Datos.contacto()
    var fallar = false
    var mensajeFallo: String? = "la base de datos rechazó la operación"
    private var siguienteId = 5000L

    // Registro de llamadas, para verificar la traducción de los repositorios.
    val llamadas = mutableListOf<String>()
    var ultimaBusqueda: Map<String, Any?> = emptyMap()
    var ultimosEstados: List<String> = emptyList()
    var ultimoAlcance: Long? = -1
    var ultimoRango: Pair<Long, Long>? = null

    private val terminadas = setOf(EstadoOportunidad.DESCARTADA, EstadoOportunidad.CERRADA)

    fun agregar(vararg lista: Oportunidad) = oportunidades.update { it + lista }

    private fun resumenDe(o: Oportunidad) = OportunidadResumen(
        id = o.id,
        nombre = o.nombre,
        tipo = o.tipo,
        estado = o.estado,
        prioridad = o.prioridad,
        fechaInicioEvento = o.fechaInicioEvento,
        publicoEsperado = o.publicoEsperado,
        potencial = o.potencial,
        afinidad = o.afinidad,
        organizacionNombre = "Organización ${o.organizacionId}",
        comunaNombre = "Comuna ${o.comunaId}",
        responsableNombre = o.responsableId?.let { nombresUsuario[it] }
    )

    private fun delAlcance(responsableId: Long?) = oportunidades.map { lista ->
        lista.filter { responsableId == null || it.responsableId == responsableId }
    }

    override fun buscar(
        responsableId: Long?,
        texto: String,
        estado: String?,
        prioridad: String?,
        tipo: String?,
        potencial: String?,
        desde: Long?,
        hasta: Long?,
        regionId: Long?,
        comunaId: Long?,
        organizacionId: Long?,
        filtroResponsableId: Long?
    ): Flow<List<OportunidadResumen>> {
        ultimaBusqueda = mapOf(
            "responsableId" to responsableId, "texto" to texto, "estado" to estado, "prioridad" to prioridad,
            "tipo" to tipo, "potencial" to potencial, "desde" to desde, "hasta" to hasta, "regionId" to regionId,
            "comunaId" to comunaId, "organizacionId" to organizacionId, "filtroResponsableId" to filtroResponsableId
        )
        return delAlcance(responsableId).map { lista ->
            lista.filter { o ->
                (texto.isEmpty() || o.nombre.contains(texto, ignoreCase = true)) &&
                    (prioridad == null || o.prioridad.name == prioridad) &&
                    (tipo == null || o.tipo.name == tipo) &&
                    (potencial == null || o.potencial.name == potencial) &&
                    (desde == null || o.fechaInicioEvento >= desde) &&
                    (hasta == null || o.fechaInicioEvento <= hasta) &&
                    (organizacionId == null || o.organizacionId == organizacionId) &&
                    (filtroResponsableId == null || o.responsableId == filtroResponsableId)
            }.map(::resumenDe)
        }
    }

    override fun observarEnEstados(estados: List<String>, responsableId: Long?): Flow<List<OportunidadResumen>> {
        ultimosEstados = estados
        return delAlcance(responsableId).map { lista -> lista.filter { it.estado.name in estados }.map(::resumenDe) }
    }

    override fun observarDetalle(id: Long): Flow<OportunidadDetalle?> = oportunidades.map { lista ->
        lista.firstOrNull { it.id == id }?.let { o ->
            val c = contacto?.takeIf { it.id == o.contactoId }
            OportunidadDetalle(
                oportunidad = o,
                organizacionNombre = "Organización ${o.organizacionId}",
                organizacionTipo = cl.duoc.atenas.domain.model.TipoOrganizacion.EMPRESA,
                organizacionRubro = "Construcción",
                comunaNombre = "Comuna ${o.comunaId}",
                regionNombre = "Región",
                contactoNombre = c?.nombre,
                contactoCargo = c?.cargo,
                contactoCorreo = c?.correo,
                contactoTelefono = c?.telefono,
                responsableNombre = o.responsableId?.let { nombresUsuario[it] },
                creadorNombre = nombresUsuario[o.creadoPorId] ?: "?"
            )
        }
    }

    override fun observarConteoPorEstado(responsableId: Long?): Flow<List<ConteoPorEstado>> =
        delAlcance(responsableId).map { lista ->
            lista.groupBy { it.estado }.map { (estado, grupo) -> ConteoPorEstado(estado, grupo.size) }
        }

    override fun observarSinResponsable(): Flow<List<OportunidadResumen>> = oportunidades.map { lista ->
        lista.filter { it.responsableId == null && it.estado !in terminadas }.map(::resumenDe)
    }

    override fun observarResumen(responsableId: Long?): Flow<List<OportunidadResumen>> {
        ultimoAlcance = responsableId
        return delAlcance(responsableId).map { lista -> lista.sortedBy { it.fechaInicioEvento }.map(::resumenDe) }
    }

    override fun observarProximas(desde: Long, hasta: Long, responsableId: Long?): Flow<List<OportunidadResumen>> {
        ultimoRango = desde to hasta
        return delAlcance(responsableId).map { lista ->
            lista.filter { it.fechaInicioEvento in desde..hasta && it.estado !in terminadas }.map(::resumenDe)
        }
    }

    override fun observarPorOrganizacion(organizacionId: Long, responsableId: Long?): Flow<List<OportunidadResumen>> {
        ultimoAlcance = responsableId
        return delAlcance(responsableId).map { lista -> lista.filter { it.organizacionId == organizacionId }.map(::resumenDe) }
    }

    override fun observarPorId(id: Long): Flow<Oportunidad?> = oportunidades.map { lista -> lista.firstOrNull { it.id == id } }
    override suspend fun porId(id: Long): Oportunidad? = oportunidades.value.firstOrNull { it.id == id }
    override suspend fun contar(): Int = oportunidades.value.size

    override suspend fun actualizarEstado(id: Long, estado: EstadoOportunidad, fecha: Long) {
        llamadas += "actualizarEstado($id,$estado)"
        oportunidades.update { lista -> lista.map { if (it.id == id) it.copy(estado = estado, fechaActualizacion = fecha) else it } }
    }

    override suspend fun asignarResponsable(id: Long, responsableId: Long?, fecha: Long) {
        llamadas += "asignarResponsable($id,$responsableId)"
        oportunidades.update { lista -> lista.map { if (it.id == id) it.copy(responsableId = responsableId) else it } }
    }

    override suspend fun insertar(oportunidad: Oportunidad): Long {
        if (fallar) throw FalloSimulado(mensajeFallo)
        val id = if (oportunidad.id != 0L) oportunidad.id else siguienteId++
        oportunidades.update { it + oportunidad.copy(id = id) }
        return id
    }

    override suspend fun insertarTodas(oportunidades: List<Oportunidad>): List<Long> = oportunidades.map { insertar(it) }

    override suspend fun actualizar(oportunidad: Oportunidad) {
        if (fallar) throw FalloSimulado(mensajeFallo)
        oportunidades.update { lista -> lista.map { if (it.id == oportunidad.id) oportunidad else it } }
    }

    fun eliminar(id: Long) = oportunidades.update { lista -> lista.filterNot { it.id == id } }
}

/**
 * Seguimientos en memoria. Hereda de la clase real, así registrarGestion()
 * (la transacción que guarda gestión, estado y último contacto) se prueba
 * con su código verdadero.
 */
class SeguimientoDaoFalso(private val oportunidades: OportunidadDaoFalso? = null) : SeguimientoDao() {
    val seguimientos = MutableStateFlow<List<Seguimiento>>(emptyList())
    val atrasadas = MutableStateFlow<List<GestionPendiente>>(emptyList())
    val entre = MutableStateFlow<List<GestionPendiente>>(emptyList())
    val propuestas = MutableStateFlow<List<PropuestaEnviada>>(emptyList())
    val deOrganizacion = MutableStateFlow<List<GestionDeOrganizacion>>(emptyList())
    val ultimosContactos = mutableMapOf<Long, Long>()
    var fallar = false
    var mensajeFallo: String? = "la base de datos rechazó la operación"
    var ultimoHoy: Long? = null
    var ultimoRango: Pair<Long, Long>? = null
    var ultimoAlcance: Long? = -1
    private var siguienteId = 7000L

    override fun observarGestionesAtrasadas(hoy: Long, responsableId: Long?): Flow<List<GestionPendiente>> {
        ultimoHoy = hoy
        ultimoAlcance = responsableId
        return atrasadas
    }

    override fun observarGestionesEntre(desde: Long, hasta: Long, responsableId: Long?): Flow<List<GestionPendiente>> {
        ultimoRango = desde to hasta
        return entre
    }

    override fun observarHistorial(oportunidadId: Long): Flow<List<Seguimiento>> = seguimientos.map { lista ->
        lista.filter { it.oportunidadId == oportunidadId }.sortedWith(compareByDescending<Seguimiento> { it.fechaAccion }.thenByDescending { it.id })
    }

    override fun observarPorOrganizacion(organizacionId: Long, responsableId: Long?): Flow<List<GestionDeOrganizacion>> {
        ultimoAlcance = responsableId
        return deOrganizacion
    }

    override fun observarPropuestas(responsableId: Long?): Flow<List<PropuestaEnviada>> {
        ultimoAlcance = responsableId
        return propuestas
    }

    override suspend fun ultima(oportunidadId: Long): Seguimiento? =
        seguimientos.value.filter { it.oportunidadId == oportunidadId }
            .maxWithOrNull(compareBy<Seguimiento> { it.fechaAccion }.thenBy { it.id })

    override suspend fun insertar(seguimiento: Seguimiento): Long {
        if (fallar) throw FalloSimulado(mensajeFallo)
        val id = if (seguimiento.id != 0L) seguimiento.id else siguienteId++
        seguimientos.update { it + seguimiento.copy(id = id) }
        return id
    }

    override suspend fun insertarTodos(seguimientos: List<Seguimiento>): List<Long> = seguimientos.map { insertar(it) }

    override suspend fun actualizarOportunidad(oportunidadId: Long, estado: EstadoOportunidad, fecha: Long) {
        oportunidades?.actualizarEstado(oportunidadId, estado, fecha)
    }

    override suspend fun actualizarUltimoContacto(contactoId: Long, fecha: Long) {
        ultimosContactos[contactoId] = fecha
    }
}

class EvidenciaDaoFalso : EvidenciaDao {
    val evidencias = MutableStateFlow<List<Evidencia>>(emptyList())
    override fun observarPorOportunidad(oportunidadId: Long): Flow<List<Evidencia>> =
        evidencias.map { lista -> lista.filter { it.oportunidadId == oportunidadId } }
    override suspend fun insertar(evidencia: Evidencia): Long {
        evidencias.update { it + evidencia }
        return evidencia.id
    }
    override suspend fun eliminar(evidencia: Evidencia) {
        evidencias.update { it - evidencia }
    }
}

/**
 * La sincronización real (reemplazarTodo) corre con su código verdadero:
 * este falso solo dirige cada borrado e inserción a los DAO falsos.
 */
class SincronizacionDaoFalso(
    private val usuarios: UsuarioDaoFalso,
    private val catalogo: CatalogoDaoFalso,
    private val organizaciones: OrganizacionDaoFalso,
    private val contactos: ContactoDaoFalso,
    private val oportunidades: OportunidadDaoFalso,
    private val seguimientos: SeguimientoDaoFalso,
    private val evidencias: EvidenciaDaoFalso
) : SincronizacionDao() {
    override suspend fun borrarEvidencias() { evidencias.evidencias.value = emptyList() }
    override suspend fun borrarSeguimientos() { seguimientos.seguimientos.value = emptyList() }
    override suspend fun borrarOportunidades() { oportunidades.oportunidades.value = emptyList() }
    override suspend fun borrarContactos() { contactos.contactos.value = emptyList() }
    override suspend fun borrarOrganizaciones() { organizaciones.organizaciones.value = emptyList() }
    override suspend fun borrarComunas() { catalogo.comunas.value = emptyList() }
    override suspend fun borrarRegiones() { catalogo.regiones.value = emptyList() }
    override suspend fun borrarUsuarios() { usuarios.usuarios.value = emptyList() }
    override suspend fun insertarUsuarios(usuarios: List<Usuario>) { this.usuarios.usuarios.update { it + usuarios } }
    override suspend fun insertarRegiones(regiones: List<Region>) { catalogo.regiones.update { it + regiones } }
    override suspend fun insertarComunas(comunas: List<Comuna>) { catalogo.comunas.update { it + comunas } }
    override suspend fun insertarOrganizaciones(organizaciones: List<Organizacion>) { this.organizaciones.organizaciones.update { it + organizaciones } }
    override suspend fun insertarContactos(contactos: List<Contacto>) { this.contactos.contactos.update { it + contactos } }
    override suspend fun insertarOportunidades(oportunidades: List<Oportunidad>) { this.oportunidades.oportunidades.update { it + oportunidades } }
    override suspend fun insertarSeguimientos(seguimientos: List<Seguimiento>) { this.seguimientos.seguimientos.update { it + seguimientos } }
    override suspend fun insertarEvidencias(evidencias: List<Evidencia>) { this.evidencias.evidencias.update { it + evidencias } }
}
