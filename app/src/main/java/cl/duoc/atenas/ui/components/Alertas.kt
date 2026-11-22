package cl.duoc.atenas.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.data.local.dao.GestionPendiente
import cl.duoc.atenas.ui.theme.Espaciado

/*
 * COMPONENTES DE ALERTAS (HU-17)
 *
 * Compartidos por el tablero del ejecutivo y el panel del supervisor, para
 * que una alerta se vea igual en ambos.
 */

/**
 * Tarjeta de una gestión atrasada. Tocarla abre la oportunidad para
 * registrar la gestión pendiente. El supervisor ve además el responsable.
 * El rojo se usa solo en el ícono y la fecha, para alertar sin gritar.
 */
@Composable
fun TarjetaGestionAtrasada(
    gestion: GestionPendiente,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    mostrarResponsable: Boolean = false
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Row(
            modifier = Modifier.padding(Espaciado.tarjeta),
            horizontalArrangement = Arrangement.spacedBy(Espaciado.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = gestion.oportunidadNombre,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = gestion.proximaAccion ?: "Sin próxima acción definida",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (mostrarResponsable) {
                    Text(
                        text = gestion.responsableNombre ?: "Sin responsable",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Vencía el ${Fechas.aTexto(gestion.fechaProximoSeguimiento)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
