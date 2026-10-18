package cl.duoc.atenas.ui.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.data.local.dao.OportunidadResumen
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.Prioridad
import cl.duoc.atenas.ui.theme.Espaciado
import cl.duoc.atenas.ui.theme.colorDeEstado
import cl.duoc.atenas.ui.theme.colorDePrioridad

/**
 * Pantalla temporal de la Fase 0: confirma que Room persiste los datos
 * sembrados y que el flujo ViewModel -> StateFlow -> UI funciona.
 */
@Composable
fun PanelDemoScreen(
    modifier: Modifier = Modifier,
    viewModel: PanelDemoViewModel = viewModel(factory = PanelDemoViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    if (estado.cargando) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(Espaciado.md),
        verticalArrangement = Arrangement.spacedBy(Espaciado.sm)
    ) {
        item {
            Column {
                Text(
                    text = "Atenas",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Verificación de persistencia local",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(Espaciado.md)) {
                    Text("Base de datos sembrada", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${estado.usuarios.size} usuarios · ${estado.oportunidades.size} oportunidades",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = Espaciado.sm))
                    estado.usuarios.forEach { usuario ->
                        Text(
                            text = "${usuario.nombreUsuario} · ${usuario.nombreCompleto} · ${usuario.rol.etiqueta}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        items(estado.oportunidades, key = { it.id }) { oportunidad ->
            TarjetaOportunidad(oportunidad)
        }
    }
}

@Composable
private fun TarjetaOportunidad(oportunidad: OportunidadResumen) {
    Card(modifier = Modifier.fillMaxWidth()) {
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
}

@Composable
private fun EtiquetaEstado(estado: EstadoOportunidad) {
    val color = colorDeEstado(estado)
    Text(
        text = estado.etiqueta,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = Espaciado.sm, vertical = 2.dp)
    )
}

@Composable
private fun EtiquetaPrioridad(prioridad: Prioridad) {
    val color = colorDePrioridad(prioridad)
    Text(
        text = "Prioridad ${prioridad.etiqueta.lowercase()}",
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = Espaciado.sm, vertical = 2.dp)
    )
}
