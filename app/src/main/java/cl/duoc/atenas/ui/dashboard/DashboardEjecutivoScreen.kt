package cl.duoc.atenas.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.data.local.dao.ConteoPorEstado
import cl.duoc.atenas.data.local.dao.GestionPendiente
import cl.duoc.atenas.ui.components.EstadoVacio
import cl.duoc.atenas.ui.components.IndicadorCarga
import cl.duoc.atenas.ui.components.TarjetaMetrica
import cl.duoc.atenas.ui.components.TarjetaOportunidad
import cl.duoc.atenas.ui.components.TituloSeccion
import cl.duoc.atenas.ui.theme.Espaciado
import cl.duoc.atenas.ui.theme.colorDeEstado

@Composable
fun DashboardEjecutivoScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardEjecutivoViewModel = viewModel(factory = DashboardEjecutivoViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    if (estado.cargando) {
        IndicadorCarga(modifier)
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(Espaciado.md),
        verticalArrangement = Arrangement.spacedBy(Espaciado.sm)
    ) {
        item {
            Column {
                Text(
                    text = "Hola, ${estado.nombreUsuario}",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Zona ${estado.zona}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(Espaciado.sm)) {
                TarjetaMetrica(
                    titulo = "En gestión",
                    valor = estado.enGestion.toString(),
                    destacada = true,
                    modifier = Modifier.weight(1f)
                )
                TarjetaMetrica(
                    titulo = "Próximos 30 días",
                    valor = estado.proximas.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                TarjetaMetrica(
                    titulo = "Atrasadas",
                    valor = estado.atrasadas.size.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (estado.atrasadas.isNotEmpty()) {
            item { TituloSeccion("Gestiones atrasadas") }
            items(estado.atrasadas, key = { "atraso-${it.id}" }) { gestion ->
                TarjetaGestionAtrasada(gestion)
            }
        }

        item { TituloSeccion("Avance por estado") }
        item {
            if (estado.conteos.isEmpty()) {
                EstadoVacio("Todavía no tienes oportunidades asignadas.")
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(Espaciado.md),
                        verticalArrangement = Arrangement.spacedBy(Espaciado.sm)
                    ) {
                        val maximo = estado.conteos.maxOf { it.total }.coerceAtLeast(1)
                        estado.conteos.forEach { conteo ->
                            BarraEstado(conteo = conteo, maximo = maximo)
                        }
                    }
                }
            }
        }

        item { TituloSeccion("Eventos próximos") }
        if (estado.proximas.isEmpty()) {
            item { EstadoVacio("No hay eventos en los próximos 30 días.") }
        } else {
            items(estado.proximas, key = { it.id }) { oportunidad ->
                TarjetaOportunidad(oportunidad)
            }
        }
    }
}

@Composable
private fun BarraEstado(conteo: ConteoPorEstado, maximo: Int) {
    val color = colorDeEstado(conteo.estado)
    val progreso by animateFloatAsState(
        targetValue = conteo.total.toFloat() / maximo,
        animationSpec = tween(durationMillis = 600),
        label = "barra-${conteo.estado.name}"
    )
    Column(verticalArrangement = Arrangement.spacedBy(Espaciado.xs)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(conteo.estado.etiqueta, style = MaterialTheme.typography.bodySmall)
            Text(conteo.total.toString(), style = MaterialTheme.typography.labelLarge)
        }
        LinearProgressIndicator(
            progress = { progreso },
            color = color,
            trackColor = color.copy(alpha = 0.16f),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TarjetaGestionAtrasada(gestion: GestionPendiente) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Espaciado.md),
            verticalArrangement = Arrangement.spacedBy(Espaciado.xs)
        ) {
            Text(gestion.oportunidadNombre, style = MaterialTheme.typography.titleSmall)
            Text(
                text = gestion.proximaAccion ?: "Sin próxima acción definida",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Vencía el ${Fechas.aTexto(gestion.fechaProximoSeguimiento)}",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
