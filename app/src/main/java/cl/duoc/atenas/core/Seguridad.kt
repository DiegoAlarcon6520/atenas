package cl.duoc.atenas.core

import java.security.MessageDigest

/**
 * Hash de claves para las cuentas locales ficticias.
 * No se almacena la clave en texto plano en la base de datos.
 */
object Seguridad {

    fun hash(clave: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(clave.toByteArray(Charsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte) }
}
