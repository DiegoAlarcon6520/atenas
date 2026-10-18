package cl.duoc.atenas.di

import android.content.Context
import cl.duoc.atenas.core.SesionManager
import cl.duoc.atenas.data.local.AtenasDatabase
import cl.duoc.atenas.data.local.seed.DatosIniciales
import cl.duoc.atenas.data.repository.CatalogoRepository
import cl.duoc.atenas.data.repository.ContactoRepository
import cl.duoc.atenas.data.repository.EvidenciaRepository
import cl.duoc.atenas.data.repository.OportunidadRepository
import cl.duoc.atenas.data.repository.OrganizacionRepository
import cl.duoc.atenas.data.repository.SeguimientoRepository
import cl.duoc.atenas.data.repository.UsuarioRepository

/**
 * Inyección de dependencias manual: un único punto donde se construye la base
 * de datos y los repositorios que consumen los ViewModel.
 */
class AppContainer(context: Context) {

    private val db = AtenasDatabase.obtener(context)

    val sesion = SesionManager()

    val usuarios = UsuarioRepository(db.usuarioDao())
    val catalogo = CatalogoRepository(db.catalogoDao())
    val organizaciones = OrganizacionRepository(db.organizacionDao())
    val contactos = ContactoRepository(db.contactoDao())
    val oportunidades = OportunidadRepository(db.oportunidadDao())
    val seguimientos = SeguimientoRepository(db.seguimientoDao())
    val evidencias = EvidenciaRepository(db.evidenciaDao())

    suspend fun sembrarDatosIniciales() = DatosIniciales.sembrarSiEstaVacia(db)
}
