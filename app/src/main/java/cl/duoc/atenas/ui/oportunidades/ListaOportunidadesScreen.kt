package cl.duoc.atenas.ui.oportunidades

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.atenas.domain.model.EstadoOportunidad
import cl.duoc.atenas.domain.model.Prioridad
import cl.duoc.atenas.domain.model.TipoOportunidad
import cl.duoc.atenas.ui.components.EstadoVacio
import cl.duoc.atenas.ui.components.IndicadorCarga
import cl.duoc.atenas.ui.components.TarjetaOportunidad
import cl.duoc.atenas.ui.theme.Espaciado

@Composable
fun ListaOportunidadesScreen(
    alAbrirDetalle: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ListaOportunidadesViewModel = viewModel(factory = ListaOportunidadesViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    if (estado.cargando) {
        IndicadorCarga(modifier)
        return
    }

    Column(modifier = modifier.fillMaxSize()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Espaciado.md, vertical = Espaciado.sm),
            horizontalArrangement = Arrangement.spacedBy(Espaciado.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = estado.filtros.texto,
                onValueChange = viewModel::alBuscar,
                placeholder = { Text("Buscar por evento, organización o comuna") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (estado.filtros.texto.isNotEmpty()) {
                        IconButton(onClick = { viewModel.alBuscar("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            BadgedBox(
                badge = {
                    if (estado.filtros.cantidadActivos > 0) {
                        Badge { Text(estado.filtros.cantidadActivos.toString()) }
                    }
                }
            ) {
                IconButton(onClick = viewModel::alternarPanelFiltros) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filtros")
                }
            }
        }

        AnimatedVisibility(
            visible = estado.mostrarFiltros,
            enter = fadeIn(tween(200)) + expandVertically(tween(200)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Espaciado.md)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = Espaciado.sm),
                    verticalArrangement = Arrangement.spacedBy(Espaciado.xs)
                ) {
                    GrupoFiltros(
                        titulo = "Estado",
                        opciones = EstadoOportunidad.entries,
                        etiqueta = { it.etiqueta },
                        seleccionada = estado.filtros.estado,
                        alSeleccionar = viewModel::alSeleccionarEstado
                    )
                    GrupoFiltros(
                        titulo = "Prioridad",
                        opciones = Prioridad.entries,
                        etiqueta = { it.etiqueta },
                        seleccionada = estado.filtros.prioridad,
                        alSeleccionar = viewModel::alSeleccionarPrioridad
                    )
                    GrupoFiltros(
                        titulo = "Tipo de evento",
                        opciones = TipoOportunidad.entries,
                        etiqueta = { it.etiqueta },
                        seleccionada = estado.filtros.tipo,
                        alSeleccionar = viewModel::alSeleccionarTipo
                    )
                    if (estado.filtros.hayAlguno) {
                        TextButton(
                            onClick = viewModel::limpiarFiltros,
                            modifier = Modifier.padding(start = Espaciado.sm)
                        ) {
                            Text("Limpiar filtros")
                        }
                    }
                }
            }
        }

        Text(
            text = buildString {
                append(estado.oportunidades.size)
                append(if (estado.oportunidades.size == 1) " oportunidad" else " oportunidades")
                if (!estado.esSupervisor) append(" asignadas a ti")
            },
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Espaciado.md, vertical = Espaciado.sm)
        )

        if (estado.oportunidades.isEmpty()) {
            EstadoVacio(
                mensaje = if (estado.filtros.hayAlguno) {
                    "Ninguna oportunidad coincide con la búsqueda. Prueba ajustando los filtros."
                } else {
                    "Todavía no hay oportunidades registradas."
                }
            )
        } else {
            LazyColumn(
                // El espacio inferior extra deja que la última tarjeta se
                // pueda leer completa por encima del botón flotante.
                contentPadding = PaddingValues(
                    start = Espaciado.md,
                    end = Espaciado.md,
                    bottom = Espaciado.xl * 3
                ),
                verticalArrangement = Arrangement.spacedBy(Espaciado.sm)
            ) {
                items(estado.oportunidades, key = { it.id }) { oportunidad ->
                    TarjetaOportunidad(
                        oportunidad = oportunidad,
                        onClick = { alAbrirDetalle(oportunidad.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> GrupoFiltros(
    titulo: String,
    opciones: List<T>,
    etiqueta: (T) -> String,
    seleccionada: T?,
    alSeleccionar: (T) -> Unit
) {
    Text(
        text = titulo,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = Espaciado.md, top = Espaciado.xs)
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = Espaciado.md),
        horizontalArrangement = Arrangement.spacedBy(Espaciado.sm)
    ) {
        items(opciones) { opcion ->
            FilterChip(
                selected = seleccionada == opcion,
                onClick = { alSeleccionar(opcion) },
                label = { Text(etiqueta(opcion)) }
            )
        }
    }
}
