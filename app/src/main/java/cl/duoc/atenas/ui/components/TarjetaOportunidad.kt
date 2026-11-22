package cl.duoc.atenas.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.data.local.dao.OportunidadResumen
import cl.duoc.atenas.ui.theme.Espaciado
import cl.duoc.atenas.ui.theme.colorDePrioridad

/**
 * TARJETA DE OPORTUNIDAD
 *
 * Componente compartido por los tableros, el listado y la ficha de
 * organización. Sigue la estructura de las tarjetas de la referencia:
 * pastillas arriba, título en negrita, una línea de contexto y los datos
 * clave abajo en gris. La prioridad se marca con un punto de color para no
 * recargar la tarjeta.
 *
 * El parámetro onClick es opcional: cuando viene null la tarjeta es solo
 * informativa, y cuando trae una función la tarjeta se vuelve tocable.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TarjetaOportunidad(
    oportunidad: OportunidadResumen,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val contenido: @Composable () -> Unit = {
        Row(
            modifier = Modifier.padding(Espaciado.tarjeta),
            horizontalArrangement = Arrangement.spacedBy(Espaciado.md)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // FlowRow: si las dos pastillas no caben, la segunda baja de línea.
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EtiquetaNeutra(oportunidad.tipo.etiqueta)
                    EtiquetaEstado(oportunidad.estado)
                }
                Text(
                    text = oportunidad.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${oportunidad.organizacionNombre} · ${oportunidad.comunaNombre}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Espaciado.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DatoConIcono(Icons.Default.CalendarToday, Fechas.aTexto(oportunidad.fechaInicioEvento))
                    DatoConIcono(Icons.Default.People, "${oportunidad.publicoEsperado}")
                }
                Text(
                    text = "Potencial ${oportunidad.potencial.etiqueta.lowercase()} · " +
                        "Afinidad ${oportunidad.afinidad.etiqueta.lowercase()} · " +
                        (oportunidad.responsableNombre ?: "Sin responsable"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            // Punto de prioridad con su etiqueta corta.
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(colorDePrioridad(oportunidad.prioridad))
                )
                Text(
                    text = oportunidad.prioridad.etiqueta,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorDePrioridad(oportunidad.prioridad),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
            contenido()
        }
    } else {
        Card(modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { contenido() }
    }
}

/** Dato breve con un ícono pequeño delante (fecha, asistentes). */
@Composable
fun DatoConIcono(icono: ImageVector, texto: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
