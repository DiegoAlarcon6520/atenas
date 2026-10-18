package cl.duoc.atenas.ui.evidencias

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import cl.duoc.atenas.core.ArchivosImagen
import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.data.local.entity.Evidencia
import cl.duoc.atenas.ui.components.EstadoVacio
import cl.duoc.atenas.ui.components.IndicadorCarga
import cl.duoc.atenas.ui.theme.Espaciado
import java.io.File

/**
 * PANTALLA DE EVIDENCIAS FOTOGRÁFICAS
 *
 * Flujo completo de la captura:
 *  1. se verifica si el permiso de cámara ya está concedido,
 *  2. si no lo está, se pide con el diálogo del sistema,
 *  3. se crea un archivo vacío en la carpeta privada de la aplicación,
 *  4. se obtiene su URI a través del FileProvider,
 *  5. se abre la cámara pasándole esa URI para que escriba ahí,
 *  6. si el usuario confirma la foto se guarda la evidencia en la base;
 *     si cancela, se borra el archivo vacío.
 *
 * El paso 6 es el que suele olvidarse: sin él, cada cancelación dejaría un
 * archivo de cero bytes tirado en el dispositivo.
 */
@Composable
fun EvidenciasScreen(
    mostrarMensaje: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EvidenciasViewModel = viewModel(factory = EvidenciasViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    // Archivo donde la cámara está escribiendo en este momento. Se guarda
    // para poder registrarlo si la foto sale bien, o borrarlo si se cancela.
    var archivoPendiente by remember { mutableStateOf<File?>(null) }
    var evidenciaAEliminar by remember { mutableStateOf<Evidencia?>(null) }

    // Lanzador de la cámara. Devuelve true solo si el usuario aceptó la foto.
    val lanzadorCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->
        val archivo = archivoPendiente
        when {
            exito && archivo != null -> {
                viewModel.agregarEvidencia(
                    uri = ArchivosImagen.uriPara(contexto, archivo).toString(),
                    descripcion = null
                )
                mostrarMensaje("Evidencia guardada")
            }
            // Cancelación: la cámara dejó un archivo vacío que hay que limpiar.
            else -> archivo?.delete()
        }
        archivoPendiente = null
    }

    /** Crea el archivo destino y abre la cámara apuntando a él. */
    fun abrirCamara() {
        val archivo = ArchivosImagen.crearArchivoTemporal(contexto)
        archivoPendiente = archivo
        lanzadorCamara.launch(ArchivosImagen.uriPara(contexto, archivo))
    }

    // Lanzador del diálogo de permiso del sistema.
    val lanzadorPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            abrirCamara()
        } else {
            mostrarMensaje("Sin permiso de cámara no se pueden capturar evidencias")
        }
    }

    /*
     * Lanzador del selector de fotos del sistema.
     *
     * PickVisualMedia abre el selector propio de Android: el usuario elige
     * una imagen y la aplicación recibe solo esa. No requiere ningún permiso
     * de almacenamiento, porque nunca se accede a la galería completa.
     *
     * La imagen elegida se copia a la carpeta de la aplicación antes de
     * registrarla, por las razones explicadas en ArchivosImagen.
     */
    val lanzadorGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uriElegida ->
        if (uriElegida == null) return@rememberLauncherForActivityResult
        val copia = ArchivosImagen.copiarDesdeGaleria(contexto, uriElegida)
        if (copia == null) {
            mostrarMensaje("No se pudo leer la imagen seleccionada")
        } else {
            viewModel.agregarEvidencia(
                uri = ArchivosImagen.uriPara(contexto, copia).toString(),
                descripcion = null
            )
            mostrarMensaje("Evidencia agregada desde la galería")
        }
    }

    if (estado.cargando) {
        IndicadorCarga(modifier)
        return
    }

    // GridCells.Adaptive en vez de Fixed(2): la cantidad de columnas se
    // calcula según el ancho disponible, así que en horizontal o en una
    // tablet caben más miniaturas en lugar de estirarse.
    // El encabezado va dentro de la grilla ocupando toda la línea, para que
    // se desplace con el contenido y no bloquee la vista en horizontal.
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(Espaciado.md),
        horizontalArrangement = Arrangement.spacedBy(Espaciado.sm),
        verticalArrangement = Arrangement.spacedBy(Espaciado.sm)
    ) {

        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Text("Evidencias", style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = estado.nombreOportunidad,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            // Dos vías para registrar evidencia: capturar en el momento o
            // subir una foto que ya se tomó durante el evento. La segunda es
            // la realista: en terreno se fotografía con la cámara del
            // teléfono y se sube después.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Espaciado.sm)
            ) {
                Button(
                    onClick = {
                        // Si el permiso ya está concedido se abre directo; si
                        // no, se pide primero. Volver a pedirlo cuando ya se
                        // tiene molesta al usuario sin necesidad.
                        val concedido = ContextCompat.checkSelfPermission(
                            contexto,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (concedido) {
                            abrirCamara()
                        } else {
                            lanzadorPermiso.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Text("Cámara", modifier = Modifier.padding(start = Espaciado.xs))
                }

                OutlinedButton(
                    onClick = {
                        lanzadorGaleria.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Text("Galería", modifier = Modifier.padding(start = Espaciado.xs))
                }
            }
        }

        if (estado.evidencias.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EstadoVacio("Todavía no hay fotografías de esta oportunidad.")
            }
        } else {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "${estado.evidencias.size} fotografías",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(estado.evidencias, key = { it.id }) { evidencia ->
                TarjetaEvidencia(
                    evidencia = evidencia,
                    alEliminar = { evidenciaAEliminar = evidencia }
                )
            }
        }
    }

    // Confirmación antes de borrar: eliminar una evidencia es irreversible.
    evidenciaAEliminar?.let { evidencia ->
        AlertDialog(
            onDismissRequest = { evidenciaAEliminar = null },
            title = { Text("Eliminar evidencia") },
            text = { Text("La fotografía se borrará del dispositivo y no se puede recuperar.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarEvidencia(evidencia)
                        evidenciaAEliminar = null
                        mostrarMensaje("Evidencia eliminada")
                    }
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { evidenciaAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

/** Miniatura con la fecha de captura y el botón de eliminar superpuesto. */
@Composable
private fun TarjetaEvidencia(evidencia: Evidencia, alEliminar: () -> Unit) {
    // La miniatura aparece con una animación de entrada, para que al tomar
    // una foto se note cuál es la nueva.
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(250)) + scaleIn(tween(250), initialScale = 0.92f),
        exit = fadeOut()
    ) {
        Card {
            Box {
                AsyncImage(
                    model = evidencia.uri,
                    contentDescription = evidencia.descripcion ?: "Evidencia fotográfica",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )

                FilledTonalIconButton(
                    onClick = alEliminar,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Espaciado.xs)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar evidencia")
                }

                Text(
                    text = Fechas.aTextoConHora(evidencia.fechaCaptura),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(Espaciado.xs)
                )
            }
        }
    }
}
