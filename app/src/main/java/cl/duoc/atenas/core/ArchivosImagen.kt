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
