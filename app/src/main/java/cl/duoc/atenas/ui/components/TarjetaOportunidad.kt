package cl.duoc.atenas.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.data.local.dao.OportunidadResumen
import cl.duoc.atenas.ui.theme.Espaciado

/**
 * TARJETA DE OPORTUNIDAD
 *
 * Componente compartido por el tablero, el listado y el panel del supervisor.
 * Si cambias algo acá, cambia en las tres pantallas.
 *
 * El parámetro onClick es opcional: cuando viene null la tarjeta es solo
 * informativa, y cuando trae una función la tarjeta se vuelve tocable.
 */
@Composable
fun TarjetaOportunidad(
    oportunidad: OportunidadResumen,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val contenido: @Composable () -> Unit = {
        Column(
            modifier = Modifier.padding(Espaciado.md),
            verticalArrangement = Arrangement.spacedBy(Espaciado.xs)
        ) {
            Text(oportunidad.nombre, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${oportunidad.organizacionNombre} · ${oportunidad.comunaNombre}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${Fechas.aTexto(oportunidad.fechaInicioEvento)} · ${oportunidad.publicoEsperado} asistentes estimados",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(Espaciado.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EtiquetaEstado(oportunidad.estado)
                EtiquetaPrioridad(oportunidad.prioridad)
            }
            Text(
                text = "Responsable: ${oportunidad.responsableNombre ?: "sin asignar"}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier.fillMaxWidth()) { contenido() }
    } else {
        Card(modifier = modifier.fillMaxWidth()) { contenido() }
    }
}
