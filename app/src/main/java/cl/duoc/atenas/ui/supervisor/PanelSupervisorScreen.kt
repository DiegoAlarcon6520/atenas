package cl.duoc.atenas.ui.supervisor

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.atenas.core.Fechas
import cl.duoc.atenas.ui.components.EstadoVacio
import cl.duoc.atenas.ui.components.Etiqueta
import cl.duoc.atenas.ui.components.IndicadorCarga
import cl.duoc.atenas.ui.components.TarjetaMetrica
import cl.duoc.atenas.ui.components.TarjetaOportunidad
import cl.duoc.atenas.ui.components.TituloSeccion
import cl.duoc.atenas.ui.theme.Espaciado
import cl.duoc.atenas.ui.theme.colorDeEstado

@Composable
fun PanelSupervisorScreen(
    modifier: Modifier = Modifier,
    viewModel: PanelSupervisorViewModel = viewModel(factory = PanelSupervisorViewModel.Factory)
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
                Text("Panel del equipo", style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = "Consulta global de la cartera comercial",
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
                    titulo = "Sin asignar",
                    valor = estado.sinAsignar.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                TarjetaMetrica(
                    titulo = "Confirmadas",
                    valor = estado.confirmadas.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { TituloSeccion("Carga por ejecutivo") }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(Espaciado.md)) {
                    estado.cargaPorEjecutivo.entries.forEachIndexed { indice, (nombre, cantidad) ->
                        if (indice > 0) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = Espaciado.sm))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(nombre, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "$cantidad en gestión",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        item { TituloSeccion("Distribución por estado") }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(Espaciado.md),
                    verticalArrangement = Arrangement.spacedBy(Espaciado.sm)
                ) {
                    estado.conteos.forEach { conteo ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Etiqueta(
                                texto = conteo.estado.etiqueta,
                                color = colorDeEstado(conteo.estado)
                            )
                            Text(
                                text = conteo.total.toString(),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }

        item { TituloSeccion("Oportunidades sin responsable") }
        if (estado.sinAsignar.isEmpty()) {
            item { EstadoVacio("Todas las oportunidades tienen responsable asignado.") }
        } else {
            items(estado.sinAsignar, key = { "sin-${it.id}" }) { oportunidad ->
                TarjetaOportunidad(oportunidad)
            }
        }

        item { TituloSeccion("Gestiones atrasadas del equipo") }
        if (estado.atrasadas.isEmpty()) {
            item { EstadoVacio("El equipo está al día con sus seguimientos.") }
        } else {
            items(estado.atrasadas, key = { "atraso-${it.id}" }) { gestion ->
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
                            text = gestion.responsableNombre ?: "Sin responsable",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Vencía el ${Fechas.aTexto(gestion.fechaProximoSeguimiento)}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}
