package cl.duoc.atenas.domain.validation

/**
 * Validaciones de formulario como funciones puras: devuelven el mensaje de
 * error o null si el valor es válido. Se mantienen fuera de los Composables
 * para poder probarlas con tests unitarios.
 */
object Validadores {

    private val PATRON_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val PATRON_TELEFONO = Regex("^\\+?[0-9 ]{8,15}$")

    fun obligatorio(valor: String, campo: String): String? =
        if (valor.isBlank()) "$campo es obligatorio" else null

    fun longitudMinima(valor: String, minimo: Int, campo: String): String? =
        if (valor.trim().length < minimo) "$campo debe tener al menos $minimo caracteres" else null

    fun correo(valor: String): String? = when {
        valor.isBlank() -> "El correo es obligatorio"
        !PATRON_CORREO.matches(valor.trim()) -> "Ingresa un correo con formato válido"
        else -> null
    }

    fun telefono(valor: String): String? = when {
        valor.isBlank() -> "El teléfono es obligatorio"
        !PATRON_TELEFONO.matches(valor.trim()) -> "Usa el formato +56 9 1234 5678"
        else -> null
    }

    fun nombreUsuario(valor: String): String? =
        obligatorio(valor, "El usuario") ?: longitudMinima(valor, 3, "El usuario")

    fun clave(valor: String): String? =
        obligatorio(valor, "La contraseña") ?: longitudMinima(valor, 6, "La contraseña")
}
