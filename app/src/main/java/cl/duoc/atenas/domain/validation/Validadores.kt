package cl.duoc.atenas.domain.validation

/**
 * VALIDACIONES DE FORMULARIO
 *
 * Responsabilidad: decidir si un valor es válido y, si no lo es, devolver el
 * mensaje exacto que verá el usuario bajo el campo.
 *
 * Por qué vive acá y no dentro de los Composables:
 *  1. El checklist exige mantener la lógica de validación separada de la UI.
 *  2. Son funciones puras (misma entrada -> misma salida), así que se pueden
 *     probar con tests unitarios sin levantar la aplicación.
 *  3. El mismo validador se reutiliza en varios formularios.
 *
 * Convención: devuelven null cuando el valor es VÁLIDO, y un String con el
 * mensaje de error cuando NO lo es. Así el ViewModel puede hacer
 * `errores.nombre = Validadores.obligatorio(...)` directamente.
 *
 * Para agregar una validación nueva: escribe una función más acá abajo y
 * llámala desde el ViewModel del formulario correspondiente.
 */
object Validadores {

    // Expresiones regulares usadas por los validadores de formato.
    // Correo: algo@algo.dominio (mínimo 2 letras de dominio).
    private val PATRON_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    // Teléfono: opcionalmente un +, luego entre 8 y 15 dígitos o espacios.
    private val PATRON_TELEFONO = Regex("^\\+?[0-9 ]{8,15}$")

    /** El campo no puede quedar vacío ni contener solo espacios. */
    fun obligatorio(valor: String, campo: String): String? =
        if (valor.isBlank()) "$campo es obligatorio" else null

    /** El campo debe tener al menos [minimo] caracteres (sin contar espacios de los extremos). */
    fun longitudMinima(valor: String, minimo: Int, campo: String): String? =
        if (valor.trim().length < minimo) "$campo debe tener al menos $minimo caracteres" else null

    /**
     * Combina obligatorio + longitud mínima, que es el caso más frecuente.
     * El operador ?: devuelve el primer error que encuentre; si el primero
     * es null, evalúa el segundo.
     */
    fun texto(valor: String, campo: String, minimo: Int = 1): String? =
        obligatorio(valor, campo) ?: longitudMinima(valor, minimo, campo)

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

    /**
     * Número entero mayor que cero. Se usa para el público esperado:
     * un evento sin asistentes estimados no es una oportunidad válida.
     */
    fun enteroPositivo(valor: String, campo: String): String? {
        if (valor.isBlank()) return "$campo es obligatorio"
        val numero = valor.trim().toIntOrNull() ?: return "$campo debe ser un número entero"
        return if (numero <= 0) "$campo debe ser mayor que cero" else null
    }

    /**
     * Monto que puede ser cero pero nunca negativo.
     * Regla de negocio del caso: los costos referenciales no pueden ser negativos.
     * El campo vacío se acepta y el ViewModel lo interpreta como 0.
     */
    fun montoNoNegativo(valor: String, campo: String): String? {
        if (valor.isBlank()) return null
        val numero = valor.trim().replace(",", ".").toDoubleOrNull()
            ?: return "$campo debe ser un número"
        return if (numero < 0) "$campo no puede ser negativo" else null
    }

    /** Una fecha obligatoria que el usuario todavía no eligió. */
    fun fechaRequerida(valor: Long?, campo: String): String? =
        if (valor == null) "$campo es obligatoria" else null

    /**
     * Coherencia entre dos fechas: [posterior] no puede ocurrir antes que [anterior].
     * Se usa para evento vs. detección, y para término vs. inicio.
     */
    fun fechaPosteriorA(
        posterior: Long?,
        anterior: Long?,
        mensaje: String
    ): String? = when {
        posterior == null || anterior == null -> null
        posterior < anterior -> mensaje
        else -> null
    }

    /** Una selección de lista desplegable que quedó sin elegir. */
    fun seleccionRequerida(valor: Long?, campo: String): String? =
        if (valor == null || valor <= 0L) "Debes seleccionar $campo" else null

    fun nombreUsuario(valor: String): String? =
        obligatorio(valor, "El usuario") ?: longitudMinima(valor, 3, "El usuario")

    fun clave(valor: String): String? =
        obligatorio(valor, "La contraseña") ?: longitudMinima(valor, 6, "La contraseña")
}
