package cl.duoc.atenas.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.atenas.core.SesionManager
import cl.duoc.atenas.data.repository.UsuarioRepository
import cl.duoc.atenas.di.contenedor
import cl.duoc.atenas.domain.validation.Validadores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val usuario: String = "",
    val clave: String = "",
    val errorUsuario: String? = null,
    val errorClave: String? = null,
    val errorGeneral: String? = null,
    val cargando: Boolean = false,
    val mostrarClave: Boolean = false,
    val mostrarAyuda: Boolean = false
)

class LoginViewModel(
    private val usuarios: UsuarioRepository,
    private val sesion: SesionManager
) : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    fun alCambiarUsuario(valor: String) = _estado.update {
        it.copy(usuario = valor, errorUsuario = null, errorGeneral = null)
    }

    fun alCambiarClave(valor: String) = _estado.update {
        it.copy(clave = valor, errorClave = null, errorGeneral = null)
    }

    fun alternarVisibilidadClave() = _estado.update { it.copy(mostrarClave = !it.mostrarClave) }

    fun alternarAyuda() = _estado.update { it.copy(mostrarAyuda = !it.mostrarAyuda) }

    /**
     * Valida y autentica. Si algo falla, los valores escritos se conservan:
     * solo se actualizan los mensajes de error.
     */
    fun iniciarSesion() {
        val actual = _estado.value
        val errorUsuario = Validadores.nombreUsuario(actual.usuario)
        val errorClave = Validadores.clave(actual.clave)

        if (errorUsuario != null || errorClave != null) {
            _estado.update { it.copy(errorUsuario = errorUsuario, errorClave = errorClave) }
            return
        }

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, errorGeneral = null) }
            val encontrado = usuarios.autenticar(actual.usuario, actual.clave)
            if (encontrado == null) {
                _estado.update {
                    it.copy(
                        cargando = false,
                        errorGeneral = "Usuario o contraseña incorrectos"
                    )
                }
            } else {
                _estado.update { it.copy(cargando = false) }
                sesion.iniciarSesion(encontrado)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LoginViewModel(
                    usuarios = contenedor.usuarios,
                    sesion = contenedor.sesion
                )
            }
        }
    }
}
