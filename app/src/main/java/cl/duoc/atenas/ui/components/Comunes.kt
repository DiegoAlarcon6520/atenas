package cl.duoc.atenas.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import cl.duoc.atenas.ui.theme.Espaciado

/** Indicador numérico usado en los tableros. */
@Composable
fun TarjetaMetrica(
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier,
    destacada: Boolean = false
) {
    Card(
        modifier = modifier,
        colors = if (destacada) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Espaciado.md),
            verticalArrangement = Arrangement.spacedBy(Espaciado.xs)
        ) {
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1
            )
            // Con tres tarjetas lado a lado el título puede no caber en
            // pantallas angostas: dos líneas y, si aún no alcanza, se recorta.
            Text(
                text = titulo,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelMedium,
                color = if (destacada) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
fun EstadoVacio(mensaje: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(Espaciado.lg),
        contentAlignment = Alignment.Center
    ) {
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
        CircularProgressIndicator()
    }
}

@Composable
fun TituloSeccion(texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(top = Espaciado.sm)
    )
}
