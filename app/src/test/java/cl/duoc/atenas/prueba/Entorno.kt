package cl.duoc.atenas.prueba

import cl.duoc.atenas.core.SesionManager
import cl.duoc.atenas.data.local.entity.Usuario
import cl.duoc.atenas.data.repository.CatalogoRepository
import cl.duoc.atenas.data.repository.ContactoRepository
import cl.duoc.atenas.data.repository.EvidenciaRepository
import cl.duoc.atenas.data.repository.OportunidadRepository
import cl.duoc.atenas.data.repository.OrganizacionRepository
import cl.duoc.atenas.data.repository.SeguimientoRepository
import cl.duoc.atenas.data.repository.SincronizacionRepository
import cl.duoc.atenas.data.repository.UsuarioRepository

/**
 * ENTORNO DE PRUEBA
 *
 * Arma, con los DAO falsos y el servidor falso, los mismos repositorios que
 * usa la aplicación real. Cada prueba crea su propio entorno, así ninguna arrastra datos de
 * otra. Si se entrega un usuario, la sesión queda iniciada con él.
 */
class Entorno(usuario: Usuario? = Datos.ejecutiva) {
    val sesion = SesionManager().apply { usuario?.let(::iniciarSesion) }

    val oportunidadDao = OportunidadDaoFalso()
    val seguimientoDao = SeguimientoDaoFalso(oportunidadDao)
    val usuarioDao = UsuarioDaoFalso()
    val catalogoDao = CatalogoDaoFalso()
    val organizacionDao = OrganizacionDaoFalso()
    val contactoDao = ContactoDaoFalso()
    val evidenciaDao = EvidenciaDaoFalso()
    val sincronizacionDao = SincronizacionDaoFalso(
        usuarioDao, catalogoDao, organizacionDao, contactoDao, oportunidadDao, seguimientoDao, evidenciaDao
    )

    /** Servidor falso: las escrituras pasan por aquí antes de llegar a Room. */
    val api = ApiFalsa(oportunidadDao, contactoDao)

    val oportunidades = OportunidadRepository(oportunidadDao, api)
    val seguimientos = SeguimientoRepository(seguimientoDao, api)
    val usuarios = UsuarioRepository(usuarioDao, api)
    val catalogo = CatalogoRepository(catalogoDao)
    val organizaciones = OrganizacionRepository(organizacionDao, api)
    val contactos = ContactoRepository(contactoDao, api)
    val evidencias = EvidenciaRepository(evidenciaDao, api)
    val sincronizacion = SincronizacionRepository(api, sincronizacionDao, usuarioDao)
}
