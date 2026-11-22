package cl.duoc.atenas.ui.propuestas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.data.local.dao.PropuestaEnviada
import cl.duoc.atenas.di.FabricasViewModel
import cl.duoc.atenas.ui.components.EstadoVacio
import cl.duoc.atenas.ui.components.EtiquetaEstado
import cl.duoc.atenas.ui.components.IndicadorCarga
import cl.duoc.atenas.ui.theme.Espaciado

/**
 * PANTALLA DE PROPUESTAS ENVIADAS (HU-16)
 *
 * Lista las propuestas de la más reciente a la más antigua, con el estado
 * actual de su oportunidad y los días transcurridos desde el envío. Tocar
 * una propuesta abre su oportunidad.
 */
@Composable
fun PropuestasScreen(
    alAbrirOportunidad: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PropuestasViewModel = viewModel(factory = FabricasViewModel.propuestas)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    if (estado.cargando) {
        IndicadorCarga(modifier)
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(Espaciado.md),
        verticalArrangement = Arrangement.spacedBy(Espaciado.sm)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(Espaciado.xs)) {
                Text(
                    text = if (estado.esSupervisor) "Propuestas del equipo" else "Mis propuestas",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "${estado.totalSinRespuesta} esperando respuesta",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FilterChip(
                    selected = estado.soloSinRespuesta,
                    onClick = viewModel::alternarSoloSinRespuesta,
                    label = { Text("Solo sin respuesta") }
                )
            }
        }

        if (estado.propuestas.isEmpty()) {
            item {
                EstadoVacio(
                    if (estado.soloSinRespuesta) {
                        "No hay propuestas esperando respuesta."
                    } else {
                        "Todavía no se registran propuestas. Se registran como una gestión de tipo \"Envío de propuesta\"."
                    }
                )
            }
        } else {
            items(estado.propuestas, key = { it.id }) { propuesta ->
                TarjetaPropuesta(
                    propuesta = propuesta,
                    mostrarResponsable = estado.esSupervisor,
                    onClick = { alAbrirOportunidad(propuesta.oportunidadId) }
                )
            }
        }
    }
}

@Composable
private fun TarjetaPropuesta(
    propuesta: PropuestaEnviada,
    mostrarResponsable: Boolean,
    onClick: () -> Unit
) {
    val dias = Fechas.diasEntre(propuesta.fechaEnvio, Fechas.hoy()).coerceAtLeast(0)
    val esperando = PropuestasViewModel.esperaRespuesta(propuesta)

    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Espaciado.md),
            verticalArrangement = Arrangement.spacedBy(Espaciado.xs)
        ) {
            Text(propuesta.oportunidadNombre, style = MaterialTheme.typography.titleMedium)
            Text(
                text = propuesta.organizacionNombre,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(propuesta.resultado, style = MaterialTheme.typography.bodyMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Espaciado.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EtiquetaEstado(propuesta.estadoActual)
                Text(
                    text = buildString {
                        append("Enviada el ${Fechas.aTexto(propuesta.fechaEnvio)}")
                        if (esperando) append(" · hace $dias ${if (dias == 1) "día" else "días"}")
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (esperando) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (mostrarResponsable) {
                Text(
                    text = "Responsable: ${propuesta.responsableNombre ?: "sin asignar"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
