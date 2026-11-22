package cl.duoc.atenas.di

import android.content.Context
import cl.duoc.atenas.core.PreferenciasApp
import cl.duoc.atenas.core.SesionManager
import cl.duoc.atenas.data.local.AtenasDatabase
import cl.duoc.atenas.data.local.seed.DatosIniciales
import cl.duoc.atenas.data.remote.RetrofitClient
import cl.duoc.atenas.data.repository.CatalogoRepository
import cl.duoc.atenas.data.repository.ContactoRepository
import cl.duoc.atenas.data.repository.EvidenciaRepository
import cl.duoc.atenas.data.repository.FeriadosRepository
import cl.duoc.atenas.data.repository.OportunidadRepository
import cl.duoc.atenas.data.repository.OrganizacionRepository
import cl.duoc.atenas.data.repository.SeguimientoRepository
import cl.duoc.atenas.data.repository.SincronizacionRepository
import cl.duoc.atenas.data.repository.UsuarioRepository

/**
 * Inyección de dependencias manual: un único punto donde se construye la base
 * de datos, el cliente del servidor y los repositorios que consumen los
 * ViewModel. Cada repositorio recibe las dos fuentes: Room para leer y la
 * API para escribir.
 */
class AppContainer(context: Context) {

    private val db = AtenasDatabase.obtener(context)

    val sesion = SesionManager()

    val preferencias = PreferenciasApp(context)

    private val api = RetrofitClient.api

    val usuarios = UsuarioRepository(db.usuarioDao(), api)
    val catalogo = CatalogoRepository(db.catalogoDao())
    val organizaciones = OrganizacionRepository(db.organizacionDao(), api)
    val contactos = ContactoRepository(db.contactoDao(), api)
    val oportunidades = OportunidadRepository(db.oportunidadDao(), api)
    val seguimientos = SeguimientoRepository(db.seguimientoDao(), api)
    val evidencias = EvidenciaRepository(db.evidenciaDao(), api)
    val feriados = FeriadosRepository(RetrofitClient.feriados)
    val sincronizacion = SincronizacionRepository(api, db.sincronizacionDao(), db.usuarioDao())

    suspend fun sembrarDatosIniciales() = DatosIniciales.sembrarSiEstaVacia(db)
}
