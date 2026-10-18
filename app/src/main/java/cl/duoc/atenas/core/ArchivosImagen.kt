package cl.duoc.atenas.core

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File

/**
 * ARCHIVOS DE EVIDENCIA FOTOGRÁFICA
 *
 * Las fotos se guardan en el almacenamiento privado externo de la
 * aplicación: getExternalFilesDir(Pictures)/evidencias.
 *
 * Por qué ahí y no en la galería del teléfono:
 *  - no requiere permisos de almacenamiento en ninguna versión de Android,
 *  - las fotos no se mezclan con las personales del usuario,
 *  - al desinstalar la aplicación se eliminan solas, que es lo correcto
 *    para evidencia de un MVP académico.
 */
object ArchivosImagen {

    private const val CARPETA = "evidencias"

    /** Carpeta de evidencias, creándola si todavía no existe. */
    private fun carpetaEvidencias(context: Context): File =
        File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), CARPETA)
            .apply { if (!exists()) mkdirs() }

    /**
     * Crea el archivo vacío donde la cámara va a escribir la foto.
     * El nombre lleva la marca de tiempo para que nunca se pisen entre sí.
     */
    fun crearArchivoTemporal(context: Context): File =
        File(carpetaEvidencias(context), "evidencia_${System.currentTimeMillis()}.jpg")

    /**
     * Convierte el archivo en una URI content:// que la aplicación de cámara
     * puede escribir. La autoridad tiene que coincidir con la declarada en
     * el AndroidManifest.
     */
    fun uriPara(context: Context, archivo: File): Uri =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )

    /**
     * Copia a la carpeta de la aplicación una imagen elegida en la galería.
     *
     * Por qué se copia en vez de guardar la URI que entrega el selector:
     *  - esa URI es prestada, con permiso temporal; al reiniciar la
     *    aplicación deja de poder leerse,
     *  - si el usuario borra la foto de su galería, la evidencia
     *    desaparecería del registro comercial.
     *
     * Una evidencia tiene que ser una copia bajo nuestro control, no un
     * enlace a un archivo ajeno.
     *
     * @return el archivo copiado, o null si la imagen no se pudo leer.
     */
    fun copiarDesdeGaleria(context: Context, origen: Uri): File? = runCatching {
        val destino = File(carpetaEvidencias(context), "evidencia_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(origen)?.use { entrada ->
            destino.outputStream().use { salida -> entrada.copyTo(salida) }
        } ?: return null
        destino
    }.getOrNull()

    /**
     * Borra el archivo físico de una evidencia.
     * Se usa al cancelar una captura y al eliminar una evidencia guardada,
     * para que no queden fotos huérfanas ocupando espacio.
     */
    fun eliminarArchivo(uri: String) {
        runCatching {
            val ruta = Uri.parse(uri).lastPathSegment ?: return
            File(ruta).takeIf { it.exists() }?.delete()
        }
    }
}
