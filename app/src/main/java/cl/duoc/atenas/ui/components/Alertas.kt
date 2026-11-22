package cl.duoc.atenas.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

/** Una fila del panel de alertas. */
data class Alerta(
    val clave: String,
    val oportunidadId: Long,
    val titulo: String,
    val detalle: String,
    val icono: ImageVector,
    val color: Color
)

/** Grupo de alertas bajo un título ("Atrasadas", "Sin responsable"…). */
data class GrupoAlertas(val titulo: String, val alertas: List<Alerta>)

/**
 * PANEL DE ALERTAS
 *
 * Hoja inferior que abre la campana del encabezado. Siempre muestra algo,
 * así el toque nunca parece no haber hecho nada: los pendientes agrupados
 * o, si no hay ninguno, un mensaje de que todo está al día. Tocar una alerta
 * cierra la hoja y abre su oportunidad.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaAlertas(
    grupos: List<GrupoAlertas>,
    alCerrar: () -> Unit,
    alAbrirOportunidad: (Long) -> Unit
) {
    val estadoHoja = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val total = grupos.sumOf { it.alertas.size }
    ModalBottomSheet(
        onDismissRequest = alCerrar,
        sheetState = estadoHoja,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                start = Espaciado.pantalla,
                end = Espaciado.pantalla,
                bottom = Espaciado.xl
            ),
            verticalArrangement = Arrangement.spacedBy(Espaciado.sm)
        ) {
            item {
                Text(
                    text = if (total == 0) "Alertas" else "Alertas · $total",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = Espaciado.sm)
                )
            }
            if (total == 0) {
                item {
                    EstadoVacio(
                        mensaje = "Estás al día: no hay pendientes que requieran tu atención.",
                        icono = Icons.Default.NotificationsNone
                    )
                }
            }
            grupos.filter { it.alertas.isNotEmpty() }.forEach { grupo ->
                item(key = "g-${grupo.titulo}") {
                    Text(
                        text = "${grupo.titulo.uppercase()} · ${grupo.alertas.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Espaciado.sm)
                    )
                }
                items(grupo.alertas, key = { it.clave }) { alerta ->
                    FilaAlerta(alerta) {
                        alCerrar()
                        alAbrirOportunidad(alerta.oportunidadId)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaAlerta(alerta: Alerta, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(alerta.color.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(alerta.icono, contentDescription = null, tint = alerta.color, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(alerta.titulo, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = alerta.detalle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
