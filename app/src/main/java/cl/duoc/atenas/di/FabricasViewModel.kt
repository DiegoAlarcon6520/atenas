package cl.duoc.atenas.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.atenas.ui.agenda.AgendaViewModel
import cl.duoc.atenas.ui.alertas.AlertasViewModel
import cl.duoc.atenas.ui.dashboard.DashboardEjecutivoViewModel
import cl.duoc.atenas.ui.evidencias.EvidenciasViewModel
import cl.duoc.atenas.ui.login.LoginViewModel
import cl.duoc.atenas.ui.oportunidades.DetalleOportunidadViewModel
import cl.duoc.atenas.ui.oportunidades.FormularioOportunidadViewModel
import cl.duoc.atenas.ui.oportunidades.ListaOportunidadesViewModel
import cl.duoc.atenas.ui.organizaciones.DetalleOrganizacionViewModel
import cl.duoc.atenas.ui.organizaciones.FormularioContactoViewModel
import cl.duoc.atenas.ui.organizaciones.FormularioOrganizacionViewModel
import cl.duoc.atenas.ui.organizaciones.ListaOrganizacionesViewModel
import cl.duoc.atenas.ui.perfil.PerfilViewModel
import cl.duoc.atenas.ui.propuestas.PropuestasViewModel
import cl.duoc.atenas.ui.seguimiento.RegistrarSeguimientoViewModel
import cl.duoc.atenas.ui.supervisor.PanelSupervisorViewModel

/**
 * FÁBRICAS DE VIEWMODEL
 *
 * Cada fábrica arma su ViewModel con las dependencias del contenedor y los
 * argumentos de la ruta. Viven aparte, y no dentro de cada ViewModel, para
 * separar el "cómo se construye" (cableado de Android) del "qué decide"
 * (lógica de negocio). Así el reporte de cobertura mide solo la lógica: este
 * archivo queda fuera de él, igual que el resto del cableado de la app.
 */
object FabricasViewModel {

    val agenda: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            AgendaViewModel(
                sesion = contenedor.sesion,
                oportunidades = contenedor.oportunidades
            )
        }
    }

    val alertas: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            AlertasViewModel(
                sesion = contenedor.sesion,
                oportunidades = contenedor.oportunidades,
                seguimientos = contenedor.seguimientos
            )
        }
    }

    val dashboardEjecutivo: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            DashboardEjecutivoViewModel(
                sesion = contenedor.sesion,
                oportunidades = contenedor.oportunidades,
                seguimientos = contenedor.seguimientos
            )
        }
    }

    val evidencias: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val argumentos = createSavedStateHandle()
            EvidenciasViewModel(
                oportunidadId = argumentos.get<Long>("oportunidadId")
                    ?: error("Falta el identificador de la oportunidad."),
                evidencias = contenedor.evidencias,
                oportunidades = contenedor.oportunidades
            )
        }
    }

    val login: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            LoginViewModel(
                usuarios = contenedor.usuarios,
                sesion = contenedor.sesion
            )
        }
    }

    val detalleOportunidad: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val argumentos = createSavedStateHandle()
            DetalleOportunidadViewModel(
                oportunidadId = argumentos.get<Long>("oportunidadId")
                    ?: error("Falta el identificador de la oportunidad."),
                sesion = contenedor.sesion,
                oportunidades = contenedor.oportunidades,
                seguimientos = contenedor.seguimientos,
                usuarios = contenedor.usuarios
            )
        }
    }

    val formularioOportunidad: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val argumentos = createSavedStateHandle()
            FormularioOportunidadViewModel(
                oportunidadId = argumentos.get<Long>("oportunidadId") ?: -1L,
                sesion = contenedor.sesion,
                oportunidades = contenedor.oportunidades,
                catalogo = contenedor.catalogo,
                organizaciones = contenedor.organizaciones,
                contactos = contenedor.contactos
            )
        }
    }

    val listaOportunidades: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            ListaOportunidadesViewModel(
                sesion = contenedor.sesion,
                oportunidades = contenedor.oportunidades,
                catalogo = contenedor.catalogo,
                organizaciones = contenedor.organizaciones,
                usuarios = contenedor.usuarios
            )
        }
    }

    val detalleOrganizacion: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val argumentos = createSavedStateHandle()
            DetalleOrganizacionViewModel(
                organizacionId = argumentos.get<Long>("organizacionId")
                    ?: error("Falta el identificador de la organización."),
                sesion = contenedor.sesion,
                organizaciones = contenedor.organizaciones,
                contactos = contenedor.contactos,
                oportunidades = contenedor.oportunidades,
                seguimientos = contenedor.seguimientos
            )
        }
    }

    val formularioContacto: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val argumentos = createSavedStateHandle()
            FormularioContactoViewModel(
                organizacionId = argumentos.get<Long>("organizacionId")
                    ?: error("Falta la organización del contacto."),
                contactoId = argumentos.get<Long>("contactoId") ?: -1L,
                contactos = contenedor.contactos,
                organizaciones = contenedor.organizaciones
            )
        }
    }

    val formularioOrganizacion: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val argumentos = createSavedStateHandle()
            FormularioOrganizacionViewModel(
                organizacionId = argumentos.get<Long>("organizacionId") ?: -1L,
                organizaciones = contenedor.organizaciones,
                catalogo = contenedor.catalogo
            )
        }
    }

    val listaOrganizaciones: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            ListaOrganizacionesViewModel(organizaciones = contenedor.organizaciones)
        }
    }

    val perfil: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            PerfilViewModel(
                sesion = contenedor.sesion,
                preferencias = contenedor.preferencias
            )
        }
    }

    val propuestas: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            PropuestasViewModel(
                sesion = contenedor.sesion,
                seguimientos = contenedor.seguimientos
            )
        }
    }

    val registrarSeguimiento: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val argumentos = createSavedStateHandle()
            RegistrarSeguimientoViewModel(
                oportunidadId = argumentos.get<Long>("oportunidadId")
                    ?: error("Falta el identificador de la oportunidad."),
                sesion = contenedor.sesion,
                oportunidades = contenedor.oportunidades,
                seguimientos = contenedor.seguimientos
            )
        }
    }

    val panelSupervisor: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            PanelSupervisorViewModel(
                sesion = contenedor.sesion,
                oportunidades = contenedor.oportunidades,
                seguimientos = contenedor.seguimientos,
                usuarios = contenedor.usuarios
            )
        }
    }
}
