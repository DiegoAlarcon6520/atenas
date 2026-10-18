package cl.duoc.atenas.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.Prioridad
import cl.duoc.atenas.ui.theme.Espaciado
import cl.duoc.atenas.ui.theme.colorDeEstado
import cl.duoc.atenas.ui.theme.colorDePrioridad

/*
 * ETIQUETAS DE ESTADO Y PRIORIDAD
 *
 * Las "pastillas" de colores que se ven en las tarjetas. El color NO está
 * escrito acá: se pide a colorDeEstado() y colorDePrioridad(), que viven en
 * ui/theme/ColoresSemanticos.kt.
 *
 * Para cambiar el color de un estado, edita ese archivo y el cambio se refleja
 * en todas las pantallas a la vez.
 */
@Composable
fun Etiqueta(texto: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        // Una pastilla con el texto partido en dos líneas se ve rota.
        // Si no cabe, se recorta con puntos suspensivos.
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = Espaciado.sm, vertical = 2.dp)
    )
}

@Composable
fun EtiquetaEstado(estado: EstadoOportunidad, modifier: Modifier = Modifier) {
    Etiqueta(texto = estado.etiqueta, color = colorDeEstado(estado), modifier = modifier)
}

@Composable
fun EtiquetaPrioridad(prioridad: Prioridad, modifier: Modifier = Modifier) {
    Etiqueta(
        texto = "Prioridad ${prioridad.etiqueta.lowercase()}",
        color = colorDePrioridad(prioridad),
        modifier = modifier
    )
}
