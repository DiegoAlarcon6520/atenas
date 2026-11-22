package cl.duoc.atenas.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cl.duoc.atenas.ui.theme.DegradadoBoton

/**
 * BARRA INFERIOR CON ACCIÓN CENTRAL
 *
 * Cuatro pestañas y, al medio, un botón "+" elevado para registrar una
 * oportunidad en segundos, incluso en terreno. Es el patrón de captura
 * rápida: la acción más frecuente queda siempre a un toque del pulgar.
 *
 * La pestaña activa se marca con el color de marca y un punto debajo.
 */
@Composable
fun BarraInferior(
    items: List<ItemNavegacion>,
    pestanaActual: String,
    alSeleccionar: (ItemNavegacion) -> Unit,
    alCrear: () -> Unit
) {
    val mitad = items.size / 2
    Surface(color = MaterialTheme.colorScheme.surfaceContainerHighest) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.take(mitad).forEach { item ->
                    Pestana(item, item.ruta == pestanaActual) { alSeleccionar(item) }
                }
                BotonCrear(alCrear)
                items.drop(mitad).forEach { item ->
                    Pestana(item, item.ruta == pestanaActual) { alSeleccionar(item) }
                }
            }
        }
    }
}

@Composable
private fun Pestana(item: ItemNavegacion, activa: Boolean, onClick: () -> Unit) {
    val color = if (activa) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = Modifier
            .size(width = 68.dp, height = 60.dp)
            .clip(MaterialTheme.shapes.medium)
            .selectable(selected = activa, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(item.icono, contentDescription = null, tint = color)
        Text(
            text = item.etiqueta,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(top = 2.dp)
        )
        Box(
            modifier = Modifier
                .padding(top = 3.dp)
                .size(4.dp)
                .clip(CircleShape)
                .background(if (activa) color else Color.Transparent)
        )
    }
}

@Composable
private fun BotonCrear(onClick: () -> Unit) {
    // El botón sobresale de la barra y lleva un anillo del color de fondo,
    // como si estuviera recortado sobre ella.
    Box(
        modifier = Modifier
            .offset(y = (-14).dp)
            .size(60.dp)
            .shadow(12.dp, CircleShape, ambientColor = Color.Black, spotColor = Color.Black)
            .clip(CircleShape)
            .background(DegradadoBoton)
            .border(4.dp, MaterialTheme.colorScheme.background, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(60.dp)) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Nueva oportunidad",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
