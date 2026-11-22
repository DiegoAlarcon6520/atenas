package cl.duoc.atenas.ui.supervisor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.atenas.data.local.dao.OportunidadResumen
import cl.duoc.atenas.di.FabricasViewModel
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.ui.components.Avatar
import cl.duoc.atenas.ui.components.EstadoVacio
import cl.duoc.atenas.ui.components.IndicadorCarga
import cl.duoc.atenas.ui.components.TarjetaGestionAtrasada
import cl.duoc.atenas.ui.components.TarjetaMetrica
import cl.duoc.atenas.ui.components.TarjetaOportunidad
import cl.duoc.atenas.ui.components.TituloSeccion
import cl.duoc.atenas.ui.theme.DegradadoMarca
import cl.duoc.atenas.ui.theme.Espaciado
import cl.duoc.atenas.ui.theme.colorDeEstado

/**
 * PANEL DEL SUPERVISOR
 *
 * Ordenado por lo que necesita su intervención: primero las decisiones
 * (confirmar o descartar, con los botones en la misma tarjeta), luego lo
 * que no tiene responsable y las gestiones atrasadas, y al final la carga
 * de cada ejecutivo.
 */
@Composable
fun PanelSupervisorScreen(
    alAbrirOportunidad: (Long) -> Unit,
    alVerPropuestas: () -> Unit,
    mostrarMensaje: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PanelSupervisorViewModel = viewModel(factory = FabricasViewModel.panelSupervisor)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.mensajes.collect { mostrarMensaje(it) } }

    if (estado.cargando) {
        IndicadorCarga(modifier)
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = Espaciado.pantalla,
            end = Espaciado.pantalla,
            top = Espaciado.xs,
            bottom = Espaciado.xl
        ),
        verticalArrangement = Arrangement.spacedBy(Espaciado.entreTarjetas)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(Espaciado.entreTarjetas)) {
                TarjetaMetrica("En gestión", estado.enGestion.toString(), Modifier.weight(1f), destacada = true)
                TarjetaMetrica("Confirmadas", estado.confirmadas.toString(), Modifier.weight(1f))
                TarjetaMetrica("Conversión", estado.conversion?.let { "$it%" } ?: "—", Modifier.weight(1f))
            }
        }

        // ---------- Decisiones ----------
        item {
            TituloSeccion(
                texto = "Necesitan tu decisión",
                accion = "Propuestas",
                alTocarAccion = alVerPropuestas
            )
        }
        if (estado.decisiones.isEmpty()) {
            item { EstadoVacio("No hay oportunidades esperando tu decisión.", icono = Icons.Default.Check) }
        } else {
            items(estado.decisiones, key = { "dec-${it.id}" }) { oportunidad ->
                TarjetaDecision(
                    oportunidad = oportunidad,
                    alAbrir = { alAbrirOportunidad(oportunidad.id) },
                    alConfirmar = { viewModel.decidir(oportunidad.id, confirmar = true) },
                    alDescartar = { viewModel.decidir(oportunidad.id, confirmar = false) }
                )
            }
        }

        // ---------- Sin responsable ----------
        if (estado.sinAsignar.isNotEmpty()) {
            item { TituloSeccion("Sin responsable (${estado.sinAsignar.size})") }
            items(estado.sinAsignar, key = { "sin-${it.id}" }) { oportunidad ->
                TarjetaOportunidad(oportunidad, onClick = { alAbrirOportunidad(oportunidad.id) })
            }
        }

        // ---------- Atrasadas ----------
        if (estado.atrasadas.isNotEmpty()) {
            item { TituloSeccion("Gestiones atrasadas (${estado.atrasadas.size})") }
            items(estado.atrasadas, key = { "atr-${it.id}" }) { gestion ->
                TarjetaGestionAtrasada(
                    gestion = gestion,
                    onClick = { alAbrirOportunidad(gestion.oportunidadId) },
                    mostrarResponsable = true
                )
            }
        }

        // ---------- Carga del equipo ----------
        item { TituloSeccion("Carga del equipo") }
        item { TarjetaCarga(estado) }
    }
}

/** Oportunidad que espera respuesta, con Confirmar y Descartar en la misma tarjeta. */
@Composable
private fun TarjetaDecision(
    oportunidad: OportunidadResumen,
    alAbrir: () -> Unit,
    alConfirmar: () -> Unit,
    alDescartar: () -> Unit
) {
    val verde = colorDeEstado(EstadoOportunidad.CONFIRMADA)
    Card(onClick = alAbrir, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Espaciado.md),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Avatar(oportunidad.responsableNombre ?: "?", tamano = 38.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = oportunidad.nombre,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${oportunidad.responsableNombre ?: "Sin responsable"} · ${oportunidad.organizacionNombre}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Espaciado.sm)) {
                Button(
                    onClick = alConfirmar,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = verde.copy(alpha = 0.16f),
                        contentColor = verde
                    )
                ) { Text("Confirmar", style = MaterialTheme.typography.labelLarge) }
                Button(
                    onClick = alDescartar,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) { Text("Descartar", style = MaterialTheme.typography.labelLarge) }
            }
        }
    }
}

/** Barras de carga por ejecutivo, relativas al que más oportunidades tiene. */
@Composable
private fun TarjetaCarga(estado: PanelSupervisorUiState) {
    val maximo = (estado.carga.maxOfOrNull { it.enGestion } ?: 0).coerceAtLeast(1)
    Card(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Espaciado.tarjeta),
            verticalArrangement = Arrangement.spacedBy(Espaciado.md)
        ) {
            estado.carga.forEach { ejecutivo ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Avatar(ejecutivo.nombre, tamano = 36.dp)
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row {
                            Text(
                                text = ejecutivo.nombre,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = when (ejecutivo.atrasadas) {
                                    0 -> "al día"
                                    1 -> "1 atrasada"
                                    else -> "${ejecutivo.atrasadas} atrasadas"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (ejecutivo.atrasadas > 0) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(ejecutivo.enGestion.toFloat() / maximo)
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(DegradadoMarca)
                            )
                        }
                    }
                    Text(ejecutivo.enGestion.toString(), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
