package cl.duoc.atenas.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.duoc.atenas.ui.theme.DegradadoMarca
import cl.duoc.atenas.ui.theme.Espaciado

/**
 * Indicador numérico de los tableros. La tarjeta [destacada] lleva el
 * degradado de marca; las demás, el fondo de tarjeta.
 */
@Composable
fun TarjetaMetrica(
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier,
    destacada: Boolean = false
) {
    val colorTexto = if (destacada) Color.White else MaterialTheme.colorScheme.onSurface
    val colorTitulo = if (destacada) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .then(
                if (destacada) {
                    Modifier.background(DegradadoMarca)
                } else {
                    Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest)
                }
            )
            .padding(horizontal = Espaciado.md, vertical = Espaciado.tarjeta)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineMedium,
                color = colorTexto,
                maxLines = 1
            )
            // Con tres tarjetas lado a lado el título puede no caber en
            // pantallas angostas: dos líneas y, si aún no alcanza, se recorta.
            Text(
                text = titulo,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelMedium,
                color = colorTitulo
            )
        }
    }
}

/** Mensaje para listas vacías, con un ícono suave para que no se vea como un error. */
@Composable
fun EstadoVacio(
    mensaje: String,
    modifier: Modifier = Modifier,
    icono: ImageVector = Icons.Default.Inbox
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Espaciado.lg, horizontal = Espaciado.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Espaciado.sm)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun IndicadorCarga(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

/**
 * Título de sección. Si recibe [accion], muestra a la derecha un enlace
 * ("Ver todo") que lleva al detalle de esa sección.
 */
@Composable
fun TituloSeccion(
    texto: String,
    modifier: Modifier = Modifier,
    accion: String? = null,
    alTocarAccion: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = Espaciado.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        if (accion != null && alTocarAccion != null) {
            TextButton(onClick = alTocarAccion) {
                Text(accion, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/**
 * Bloque de datos "etiqueta / valor" dentro de una tarjeta, para las fichas
 * de detalle. Cada fila separa la etiqueta (gris, arriba) del valor.
 */
@Composable
fun TarjetaDatos(
    titulo: String,
    datos: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(
            modifier = Modifier.padding(Espaciado.tarjeta),
            verticalArrangement = Arrangement.spacedBy(Espaciado.md)
        ) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            datos.forEach { (etiqueta, valor) ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = etiqueta,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(valor, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

/** Tarjeta con las definiciones de los indicadores de un tablero. */
@Composable
fun TarjetaLeyenda(vararg lineas: String) {
    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(
            modifier = Modifier.padding(Espaciado.tarjeta),
            verticalArrangement = Arrangement.spacedBy(Espaciado.sm)
        ) {
            lineas.forEach { linea ->
                Text(
                    text = linea,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
